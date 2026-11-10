package sportsseer.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One-time tool (not part of the running app) that turns the Kaggle
 * "NBA Stats" dataset (built from Basketball-Reference) into the two
 * CSV files Sports Seer reads: data/team_seasons.csv and
 * data/player_seasons.csv.
 *
 * Run it again whenever a newer copy of the dataset is downloaded:
 * java -cp bin sportsseer.data.DatasetConverter kaggleFolder data
 *
 * Rules:
 * - Only NBA and BAA (the NBA's first 3 seasons) rows are kept.
 * - "League Average" rows are skipped.
 * - The season still being played is skipped.
 * - A traded player keeps his full-season line (TOT / 2TM / 3TM row),
 *   listed under the last team he played for.
 * - Stats not tracked in early years are written as NaN.
 */
public final class DatasetConverter {

    // Kaggle file names
    private static final String SUMMARY_FILE = "Team Summaries.csv";
    private static final String TEAM_FILE = "Team Stats Per Game.csv";
    private static final String OPP_FILE = "Opponent Stats Per Game.csv";
    private static final String PLAYER_FILE = "Player Per Game.csv";

    // missing-value marker in the Kaggle files
    private static final String MISSING = "NA";

    /** Not meant to be created. */
    private DatasetConverter() {
    }

    /**
     * Converts the dataset.
     * @param args [0] = Kaggle folder, [1] = output data folder
     * @throws IOException if a file cannot be read or written
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: DatasetConverter kaggleFolder "
                    + "outputFolder");
            return;
        }
        Path in = Path.of(args[0]);
        Path out = Path.of(args[1]);
        Files.createDirectories(out);

        List<Map<String, String>> summaries =
                readCsv(in.resolve(SUMMARY_FILE));
        int currentSeason = findInProgressSeason(summaries);

        int teams = writeTeams(in, out, summaries, currentSeason);
        int players = writePlayers(in, out, summaries, currentSeason);
        System.out.println("Wrote " + teams + " team-seasons and "
                + players + " player-seasons.");
    }

    // ---- teams ----

    /**
     * Writes team_seasons.csv.
     * @param in Kaggle folder
     * @param out output folder
     * @param summaries rows of Team Summaries.csv
     * @param skipSeason season year still in progress
     * @return number of rows written
     * @throws IOException if a file cannot be read or written
     */
    private static int writeTeams(Path in, Path out,
            List<Map<String, String>> summaries, int skipSeason)
            throws IOException {
        Map<String, String> ppg = indexColumn(
                readCsv(in.resolve(TEAM_FILE)), "pts_per_game");
        Map<String, String> oppg = indexColumn(
                readCsv(in.resolve(OPP_FILE)), "opp_pts_per_game");

        List<String> lines = new ArrayList<>();
        lines.add("team,season,wins,losses,pointsPerGame,"
                + "pointsAllowedPerGame");
        lines.add("# Source: Basketball-Reference.com via Kaggle "
                + "NBA Stats dataset (regular season)");
        for (Map<String, String> row : summaries) {
            int year = Integer.parseInt(row.get("season"));
            if (!keep(row, year, skipSeason)) {
                continue;
            }
            String key = row.get("season") + "|" + row.get("lg") + "|"
                    + row.get("team");
            String pts = ppg.get(key);
            String opp = oppg.get(key);
            if (isMissing(pts) || isMissing(opp)) {
                continue;
            }
            lines.add(String.join(",", row.get("team"),
                    seasonLabel(year), row.get("w"), row.get("l"),
                    pts, opp));
        }
        Files.write(out.resolve(StatsRepository.TEAM_FILE), lines,
                StandardCharsets.UTF_8);
        return lines.size() - 2;
    }

    // ---- players ----

    /**
     * Writes player_seasons.csv.
     * @param in Kaggle folder
     * @param out output folder
     * @param summaries rows of Team Summaries.csv
     * @param skipSeason season year still in progress
     * @return number of rows written
     * @throws IOException if a file cannot be read or written
     */
    private static int writePlayers(Path in, Path out,
            List<Map<String, String>> summaries, int skipSeason)
            throws IOException {
        // season|abbreviation -> full team name
        Map<String, String> teamNames = new HashMap<>();
        for (Map<String, String> row : summaries) {
            if (!"ABA".equals(row.get("lg"))) {
                teamNames.put(row.get("season") + "|"
                        + row.get("abbreviation"), row.get("team"));
            }
        }

        List<Map<String, String>> rows = readCsv(in.resolve(PLAYER_FILE));
        List<String> lines = new ArrayList<>();
        lines.add("player,team,season,pointsPerGame,reboundsPerGame,"
                + "assistsPerGame,threePointPct");
        lines.add("# Source: Basketball-Reference.com via Kaggle "
                + "NBA Stats dataset (regular season)");

        for (int i = 0; i < rows.size(); i++) {
            Map<String, String> row = rows.get(i);
            int year = Integer.parseInt(row.get("season"));
            if (!keep(row, year, skipSeason)) {
                continue;
            }
            String abbrev = row.get("team");
            if (isCombinedRow(abbrev)) {
                // use the last stint's team for the full-season line
                int j = i + 1;
                while (j < rows.size() && isStint(row, rows.get(j))) {
                    j++;
                }
                abbrev = rows.get(j - 1).get("team");
                i = j - 1; // skip the individual stints
            }
            String team = teamNames.get(row.get("season") + "|" + abbrev);
            if (team == null) {
                continue;
            }
            lines.add(String.join(",", row.get("player"), team,
                    seasonLabel(year), number(row.get("pts_per_game")),
                    number(row.get("trb_per_game")),
                    number(row.get("ast_per_game")),
                    percent(row.get("x3p_percent"))));
        }
        Files.write(out.resolve(StatsRepository.PLAYER_FILE), lines,
                StandardCharsets.UTF_8);
        return lines.size() - 2;
    }

    // ---- helpers ----

    /**
     * @param row a Kaggle row with season, lg and team columns
     * @param year the row's season year
     * @param skipSeason season still in progress
     * @return true if the row is a finished NBA/BAA season, not an
     *         average row
     */
    private static boolean keep(Map<String, String> row, int year,
            int skipSeason) {
        String league = row.get("lg");
        boolean nba = "NBA".equals(league) || "BAA".equals(league);
        return nba && year != skipSeason
                && !"League Average".equals(row.get("team"))
                && !"League Average".equals(row.get("player"));
    }

    /**
     * The newest season is skipped if its teams have not all played
     * the same full schedule as the season before.
     * @param summaries rows of Team Summaries.csv
     * @return newest season year if unfinished, otherwise -1
     */
    private static int findInProgressSeason(
            List<Map<String, String>> summaries) {
        int newest = 0;
        for (Map<String, String> row : summaries) {
            newest = Math.max(newest, Integer.parseInt(row.get("season")));
        }
        for (Map<String, String> row : summaries) {
            if (Integer.parseInt(row.get("season")) == newest
                    && !isMissing(row.get("w"))) {
                int games = Integer.parseInt(row.get("w"))
                        + Integer.parseInt(row.get("l"));
                if (games < 82) {
                    return newest;
                }
            }
        }
        return -1;
    }

    /**
     * @param abbrev a team abbreviation from the player file
     * @return true for full-season rows of traded players
     */
    private static boolean isCombinedRow(String abbrev) {
        return "TOT".equals(abbrev) || abbrev.matches("\\dTM");
    }

    /**
     * @param combined the traded player's full-season row
     * @param next the row after it
     * @return true if next is one of that player's team stints
     */
    private static boolean isStint(Map<String, String> combined,
            Map<String, String> next) {
        return next.get("player_id").equals(combined.get("player_id"))
                && next.get("season").equals(combined.get("season"))
                && !isCombinedRow(next.get("team"));
    }

    /**
     * @param rows parsed CSV rows
     * @param column column to keep
     * @return "season|league|team" -> value of that column
     */
    private static Map<String, String> indexColumn(
            List<Map<String, String>> rows, String column) {
        Map<String, String> index = new HashMap<>();
        for (Map<String, String> row : rows) {
            index.put(row.get("season") + "|" + row.get("lg") + "|"
                    + row.get("team"), row.get(column));
        }
        return index;
    }

    /**
     * @param year season end year, e.g. 2017
     * @return season label, e.g. "2016-17"
     */
    static String seasonLabel(int year) {
        return (year - 1) + "-" + String.format("%02d", year % 100);
    }

    /**
     * @param value raw cell
     * @return the number, or NaN if missing
     */
    private static String number(String value) {
        return isMissing(value) ? "NaN" : value;
    }

    /**
     * @param value raw cell like ".411"
     * @return percent like "41.1", or NaN if missing
     */
    private static String percent(String value) {
        if (isMissing(value)) {
            return "NaN";
        }
        double pct = Double.parseDouble(value) * 100.0;
        return String.format("%.1f", pct);
    }

    /**
     * @param value raw cell
     * @return true if the cell is empty or NA
     */
    private static boolean isMissing(String value) {
        return value == null || value.isEmpty() || MISSING.equals(value);
    }

    /**
     * Reads a Kaggle CSV (no quoted fields) into rows keyed by header.
     * @param file the CSV file
     * @return one map per data row
     * @throws IOException if the file cannot be read
     */
    private static List<Map<String, String>> readCsv(Path file)
            throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file,
                StandardCharsets.UTF_8)) {
            String[] header = reader.readLine().split(",", -1);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = line.split(",", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < header.length && i < cols.length; i++) {
                    row.put(header[i], cols[i]);
                }
                rows.add(row);
            }
        }
        return rows;
    }
}
