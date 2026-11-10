package sportsseer.model;

/**
 * One NBA player's per-game averages for one season.
 */
public class PlayerSeason {

    // identity of the player-season
    private final String f_playerName;
    private final String f_teamName;
    private final String f_season;

    // per-game averages
    private final double f_pointsPerGame;
    private final double f_reboundsPerGame;
    private final double f_assistsPerGame;
    private final double f_threePointPercentage;

    /**
     * Creates a player-season record.
     * @param playerName player's full name
     * @param teamName team the player played for that season
     * @param season season label, e.g. "2016-17"
     * @param pointsPerGame points per game
     * @param reboundsPerGame rebounds per game
     * @param assistsPerGame assists per game
     * @param threePointPercentage three-point percentage (0-100)
     */
    public PlayerSeason(String playerName, String teamName, String season,
            double pointsPerGame, double reboundsPerGame,
            double assistsPerGame, double threePointPercentage) {
        f_playerName = playerName;
        f_teamName = teamName;
        f_season = season;
        f_pointsPerGame = pointsPerGame;
        f_reboundsPerGame = reboundsPerGame;
        f_assistsPerGame = assistsPerGame;
        f_threePointPercentage = threePointPercentage;
    }

    /** @return the player's name */
    public String getPlayerName() {
        return f_playerName;
    }

    /** @return the player's team for this season */
    public String getTeamName() {
        return f_teamName;
    }

    /** @return the season label */
    public String getSeason() {
        return f_season;
    }

    /** @return points per game */
    public double getPointsPerGame() {
        return f_pointsPerGame;
    }

    /** @return rebounds per game */
    public double getReboundsPerGame() {
        return f_reboundsPerGame;
    }

    /** @return assists per game */
    public double getAssistsPerGame() {
        return f_assistsPerGame;
    }

    /** @return three-point percentage (0-100) */
    public double getThreePointPercentage() {
        return f_threePointPercentage;
    }

    /** @return label shown in the UI */
    @Override
    public String toString() {
        return f_playerName + " (" + f_season + ")";
    }
}
