package sportsseer.model;

/**
 * A projected stat line for one player against one opponent.
 */
public class PlayerProjection {

    // who is projected, and against whom
    private final PlayerSeason f_player;
    private final TeamSeason f_opponent;

    // projected per-game numbers for this matchup
    private final double f_projectedPoints;
    private final double f_projectedRebounds;
    private final double f_projectedAssists;

    /**
     * Creates a player projection.
     * @param player the player-season being projected
     * @param opponent the opposing team-season
     * @param projectedPoints projected points
     * @param projectedRebounds projected rebounds
     * @param projectedAssists projected assists
     */
    public PlayerProjection(PlayerSeason player, TeamSeason opponent,
            double projectedPoints, double projectedRebounds,
            double projectedAssists) {
        f_player = player;
        f_opponent = opponent;
        f_projectedPoints = projectedPoints;
        f_projectedRebounds = projectedRebounds;
        f_projectedAssists = projectedAssists;
    }

    /** @return the projected player-season */
    public PlayerSeason getPlayer() {
        return f_player;
    }

    /** @return the opposing team-season */
    public TeamSeason getOpponent() {
        return f_opponent;
    }

    /** @return projected points */
    public double getProjectedPoints() {
        return f_projectedPoints;
    }

    /** @return projected rebounds */
    public double getProjectedRebounds() {
        return f_projectedRebounds;
    }

    /** @return projected assists */
    public double getProjectedAssists() {
        return f_projectedAssists;
    }
}
