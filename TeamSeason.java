package sportsseer.model;

/**
 * One NBA team's statistics for one season (for example, the 2016-17
 * Golden State Warriors). Pairing a team with a season is what lets
 * Sports Seer compare teams from different years.
 */
public class TeamSeason {

    // identity of the team-season
    private final String f_teamName;
    private final String f_season;

    // season record and scoring averages (per game)
    private final int f_wins;
    private final int f_losses;
    private final double f_pointsPerGame;
    private final double f_pointsAllowedPerGame;

    /**
     * Creates a team-season record.
     * @param teamName full team name, e.g. "Golden State Warriors"
     * @param season season label, e.g. "2016-17"
     * @param wins regular-season wins
     * @param losses regular-season losses
     * @param pointsPerGame points the team scored per game
     * @param pointsAllowedPerGame points the team gave up per game
     */
    public TeamSeason(String teamName, String season, int wins,
            int losses, double pointsPerGame,
            double pointsAllowedPerGame) {
        f_teamName = teamName;
        f_season = season;
        f_wins = wins;
        f_losses = losses;
        f_pointsPerGame = pointsPerGame;
        f_pointsAllowedPerGame = pointsAllowedPerGame;
    }

    /** @return the team name */
    public String getTeamName() {
        return f_teamName;
    }

    /** @return the season label */
    public String getSeason() {
        return f_season;
    }

    /** @return regular-season wins */
    public int getWins() {
        return f_wins;
    }

    /** @return regular-season losses */
    public int getLosses() {
        return f_losses;
    }

    /** @return points scored per game */
    public double getPointsPerGame() {
        return f_pointsPerGame;
    }

    /** @return points allowed per game */
    public double getPointsAllowedPerGame() {
        return f_pointsAllowedPerGame;
    }

    /**
     * A unique key for this team-season, used by the repository and
     * the prediction cache.
     * @return key in the form "Team Name|season"
     */
    public String getKey() {
        return f_teamName + "|" + f_season;
    }

    /** @return label shown in the UI, e.g. "2016-17 Golden State..." */
    @Override
    public String toString() {
        return f_season + " " + f_teamName;
    }
}
