package sportsseer.controller;

import java.util.List;

import sportsseer.data.StatsRepository;
import sportsseer.engine.IPredictionEngine;
import sportsseer.engine.PredictionCache;
import sportsseer.model.Matchup;
import sportsseer.model.PlayerSeason;
import sportsseer.model.Prediction;
import sportsseer.model.ResultsModel;
import sportsseer.model.TeamSeason;

/**
 * Handles requests from the View: validates the selections, checks
 * the cache, runs the prediction engine and puts the result in the
 * ResultsModel (which then updates the View).
 */
public class PredictionController {

    // collaborators
    private final StatsRepository f_repository;
    private final IPredictionEngine f_engine;
    private final PredictionCache f_cache;
    private final ResultsModel f_results;

    /**
     * Creates the controller.
     * @param repository loaded stats
     * @param engine the prediction engine to use
     * @param cache cache for finished predictions
     * @param results model the View is observing
     */
    public PredictionController(StatsRepository repository,
            IPredictionEngine engine, PredictionCache cache,
            ResultsModel results) {
        f_repository = repository;
        f_engine = engine;
        f_cache = cache;
        f_results = results;
    }

    /** @return every season with data, newest first */
    public List<String> getSeasons() {
        return f_repository.getSeasons();
    }

    /**
     * @param season season label
     * @return teams from that season, for a drop-down
     */
    public List<TeamSeason> getTeams(String season) {
        return f_repository.getTeams(season);
    }

    /**
     * @param team a team-season
     * @return that team's players that season, top scorer first
     */
    public List<PlayerSeason> getPlayers(TeamSeason team) {
        return f_repository.getPlayers(team.getSeason(),
                team.getTeamName());
    }

    /**
     * Use case: Generate NBA Matchup Prediction.
     * @param teamA first selected team-season
     * @param teamB second selected team-season
     */
    public void requestMatchupPrediction(TeamSeason teamA,
            TeamSeason teamB) {
        Matchup matchup = new Matchup(teamA, teamB);
        if (!matchup.isValid()) {
            f_results.reportError("Pick two different team-seasons.");
            return;
        }
        Prediction prediction = f_cache.get(matchup.getKey());
        if (prediction == null) {
            prediction = f_engine.predictMatchup(matchup);
            f_cache.put(matchup.getKey(), prediction);
        }
        f_results.setPrediction(prediction);
    }

    /**
     * Use case: Predict Player Performance.
     * @param player selected player-season
     * @param opponent selected opposing team-season
     */
    public void requestPlayerProjection(PlayerSeason player,
            TeamSeason opponent) {
        if (player == null || opponent == null) {
            f_results.reportError("Pick a player and an opponent.");
            return;
        }
        f_results.setProjection(f_engine.predictPlayer(player, opponent));
    }
}
