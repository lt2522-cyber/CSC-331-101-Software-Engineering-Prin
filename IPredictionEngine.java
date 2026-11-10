package sportsseer.engine;

import sportsseer.model.Matchup;
import sportsseer.model.PlayerProjection;
import sportsseer.model.PlayerSeason;
import sportsseer.model.Prediction;
import sportsseer.model.TeamSeason;

/**
 * What any Sports Seer prediction engine must be able to do. Keeping
 * this as an interface means the team can swap in a better algorithm
 * later without touching the controller or the UI.
 */
public interface IPredictionEngine {

    /**
     * Predicts a team-vs-team matchup.
     * @param matchup a valid matchup (two different team-seasons)
     * @return winner, win probability, projected scores and spread
     * @throws IllegalArgumentException if the matchup is not valid
     */
    Prediction predictMatchup(Matchup matchup);

    /**
     * Projects a player's stat line against an opponent.
     * @param player the player-season to project
     * @param opponent the opposing team-season
     * @return projected points, rebounds and assists
     */
    PlayerProjection predictPlayer(PlayerSeason player,
            TeamSeason opponent);
}
