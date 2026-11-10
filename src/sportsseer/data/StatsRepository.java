package sportsseer.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import sportsseer.model.PlayerSeason;
import sportsseer.model.TeamSeason;

/**
 * Loads historical NBA stats from local CSV files and keeps them in
 * memory. This replaces the SQL database: the stats are downloaded
 * once, saved as CSV, and shipped with the app, so the app never has
 * to call an NBA API while it runs.
 *
 * Singleton pattern: every part of the app shares one repository, so
 * the files are only read once.
 */
public final class StatsRepository {

    // file names inside the data folder
    public static final String TEAM_FILE = "team_seasons.csv";
    public static final String PLAYER_FILE = "player_seasons.csv";

    // the one shared instance
    private static StatsRepository f_instance;

    // loaded data, keyed for fast lookup
    private final Map<String, TeamSeason> f_teams;
    private final List<PlayerSeason> f_players;

    /** Private so only getInstance() can create it. */
    private StatsRepository() {
        f_teams = new LinkedHashMap<>();
        f_players = new ArrayList<>();
    }

    /**
     * Returns the shared repository, creating it the first time.
     * @return the single StatsRepository instance
     */
    public static synchronized StatsRepository getInstance() {
        if (f_instance == null) {
            f_instance = new StatsRepository();
        }
        return f_instance;
    }

    /**
     * Reads both CSV files from a folder. Any data loaded before is
     * cleared first.
     * @param dataFolder folder that holds the CSV files
     * @throws IOException if a file is missing or cannot be read
     */
    public void load(Path dataFolder) throws IOException {
        f_teams.clear();
        f_players.clear();
        loadTeams(dataFolder.resolve(TEAM_FILE));
        loadPlayers(dataFolder.resolve(PLAYER_FILE));
    }

    /**
     * Adds one team-season directly (used by unit tests).
     * @param team the team-season to add
     */
    public void addTeam(TeamSeason team) {
        f_teams.put(team.getKey(), team);
    }

    /** @return every loaded team-season, in file order */
    public List<TeamSeason> getAllTeams() {
        return new ArrayList<>(f_teams.values());
    }

    /** @return every loaded player-season, in file order */
    public List<PlayerSeason> getAllPlayers() {
        return new ArrayList<>(f_players);
    }

    /**
     * Every season that has team data, newest first.
     * @return season labels such as "2025-26", "2024-25", ...
     */
    public List<String> getSeasons() {
        TreeSet<String> seasons = new TreeSet<>(Comparator.reverseOrder());
        for (TeamSeason team : f_teams.values()) {
            seasons.add(team.getSeason());
        }
        return new ArrayList<>(seasons);
    }

    /**
     * Teams that played in one season, sorted by name.
     * @param season season label
     * @return the team-seasons for that season
     */
    public List<TeamSeason> getTeams(String season) {
        List<TeamSeason> result = new ArrayList<>();
        for (TeamSeason team : f_teams.values()) {
            if (team.getSeason().equals(season)) {
                result.add(team);
            }
        }
        result.sort(Comparator.comparing(TeamSeason::getTeamName));
        return result;
    }

    /**
     * Players on one team in one season, highest scorer first.
     * @param season season label
     * @param teamName full team name
     * @return matching player-seasons
     */
    public List<PlayerSeason> getPlayers(String season, String teamName) {
        List<PlayerSeason> result = new ArrayList<>();
        for (PlayerSeason player : f_players) {
            if (player.getSeason().equals(season)
                    && player.getTeamName().equals(teamName)) {
                result.add(player);
            }
        }
        result.sort(Comparator.comparingDouble(
                PlayerSeason::getPointsPerGame).reversed());
        return result;
    }

    /**
     * Finds one team-season.
     * @param teamName full team name
     * @param season season label
     * @return the team-season, or null if it is not in the data
     */
    public TeamSeason findTeam(String teamName, String season) {
        return f_teams.get(teamName + "|" + season);
    }

    /**
     * Average points allowed per game for one season, across the
     * loaded teams from that season. Scoring levels change a lot by
     * era, so a defense is judged against its own season. If no team
     * from that season is loaded, all teams are used.
     * @param season season label, e.g. "2016-17"
     * @return average points allowed, or 0 if nothing is loaded
     */
    public double getLeagueAveragePointsAllowed(String season) {
        double total = 0.0;
        int count = 0;
        for (TeamSeason team : f_teams.values()) {
            if (team.getSeason().equals(season)) {
                total += team.getPointsAllowedPerGame();
                count++;
            }
        }
        if (count == 0) {
            for (TeamSeason team : f_teams.values()) {
                total += team.getPointsAllowedPerGame();
                count++;
            }
        }
        return count == 0 ? 0.0 : total / count;
    }

    // ---- private CSV helpers ----

    /**
     * Reads team rows: team,season,wins,losses,ppg,oppg
     * @param file the team CSV file
     * @throws IOException if the file cannot be read
     */
    private void loadTeams(Path file) throws IOException {
        for (String[] row : readRows(file)) {
            TeamSeason team = new TeamSeason(row[0], row[1],
                    Integer.parseInt(row[2]), Integer.parseInt(row[3]),
                    Double.parseDouble(row[4]),
                    Double.parseDouble(row[5]));
            addTeam(team);
        }
    }

    /**
     * Reads player rows: player,team,season,ppg,rpg,apg,threePct
     * @param file the player CSV file
     * @throws IOException if the file cannot be read
     */
    private void loadPlayers(Path file) throws IOException {
        for (String[] row : readRows(file)) {
            f_players.add(new PlayerSeason(row[0], row[1], row[2],
                    Double.parseDouble(row[3]),
                    Double.parseDouble(row[4]),
                    Double.parseDouble(row[5]),
                    Double.parseDouble(row[6])));
        }
    }

    /**
     * Reads a simple CSV file, skipping the header line, blank lines
     * and lines that start with '#'.
     * @param file the CSV file
     * @return each data row split into columns
     * @throws IOException if the file cannot be read
     */
    private List<String[]> readRows(Path file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                String[] cols = line.split(",");
                for (int i = 0; i < cols.length; i++) {
                    cols[i] = cols[i].trim();
                }
                rows.add(cols);
            }
        }
        return rows;
    }
}
