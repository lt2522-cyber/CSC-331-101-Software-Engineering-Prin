package sportsseer.engine;

import sportsseer.data.StatsRepository;
import sportsseer.model.Matchup;
import sportsseer.model.PlayerProjection;
import sportsseer.model.PlayerSeason;
import sportsseer.model.Prediction;
import sportsseer.model.TeamSeason;

/**
 * Sports Seer's prediction engine. It uses each team's season
 * scoring averages to project a neutral-court game.
 *
 * Steps for a matchup:
 * 1. Margin of victory (MOV) = points scored - points allowed.
 * 2. Projected margin for A = MOV(A) - MOV(B). Using margins instead
 *    of raw points keeps it fair across eras with different scoring.
 * 3. Projected total = average of both teams' typical game totals.
 * 4. Each team's score = (total +/- margin) / 2.
 * 5. Win probability = logistic curve of the margin.
 */
public class StatPredictionEngine implements IPredictionEngine {

    // spread of real NBA results around a projection, turned into a
    // logistic scale: about 12 points std dev x sqrt(3) / pi
    public static final double LOGISTIC_SCALE = 6.6;

    // where league-average defense numbers come from
    private final StatsRepository f_repository;

    /**
     * Creates the engine.
     * @param repository source of league-average stats
     */
    public StatPredictionEngine(StatsRepository repository) {
        f_repository = repository;
    }

    /**
     * Predicts a team-vs-team matchup on a neutral court.
     * @param matchup a valid matchup (two different team-seasons)
     * @return winner, win probability, projected scores and spread
     * @throws IllegalArgumentException if the matchup is not valid
     */
    @Override
    public Prediction predictMatchup(Matchup matchup) {
        if (matchup == null || !matchup.isValid()) {
            throw new IllegalArgumentException(
                    "Pick two different team-seasons.");
        }
        TeamSeason a = matchup.getTeamA();
        TeamSeason b = matchup.getTeamB();

        double margin = marginOfVictory(a) - marginOfVictory(b);
        double total = (gameTotal(a) + gameTotal(b)) / 2.0;

        double scoreA = (total + margin) / 2.0;
        double scoreB = (total - margin) / 2.0;
        double winProbA = winProbability(margin);

        return new Prediction(matchup, winProbA, scoreA, scoreB);
    }

    /**
     * Projects a player's line against an opponent. Points and
     * assists are scaled by how tough the opponent's defense was
     * compared with the average team that season. Rebounds are kept
     * at the player's season average.
     * @param player the player-season to project
     * @param opponent the opposing team-season
     * @return projected points, rebounds and assists
     * @throws IllegalArgumentException if either input is null
     */
    @Override
    public PlayerProjection predictPlayer(PlayerSeason player,
            TeamSeason opponent) {
        if (player == null || opponent == null) {
            throw new IllegalArgumentException(
                    "Pick a player and an opponent.");
        }
        double factor = defenseFactor(opponent);
        return new PlayerProjection(player, opponent,
                player.getPointsPerGame() * factor,
                player.getReboundsPerGame(),
                player.getAssistsPerGame() * factor);
    }

    /**
     * Turns a projected margin into a win chance between 0 and 1.
     * A margin of 0 gives 50%; bigger margins get closer to 100%.
     * @param margin projected points team A wins by (can be negative)
     * @return probability that team A wins
     */
    public static double winProbability(double margin) {
        return 1.0 / (1.0 + Math.exp(-margin / LOGISTIC_SCALE));
    }

    // ---- private helpers ----

    /**
     * @param team a team-season
     * @return points scored minus points allowed, per game
     */
    private double marginOfVictory(TeamSeason team) {
        return team.getPointsPerGame() - team.getPointsAllowedPerGame();
    }

    /**
     * @param team a team-season
     * @return combined points in a typical game for that team
     */
    private double gameTotal(TeamSeason team) {
        return team.getPointsPerGame() + team.getPointsAllowedPerGame();
    }

    /**
     * Above 1.0 means the opponent's defense was worse than average
     * (easier to score on); below 1.0 means it was better.
     * @param opponent the opposing team-season
     * @return opponent points allowed / league average points allowed
     */
    private double defenseFactor(TeamSeason opponent) {
        double leagueAvg = f_repository
                .getLeagueAveragePointsAllowed(opponent.getSeason());
        if (leagueAvg <= 0.0) {
            return 1.0;
        }
        return opponent.getPointsAllowedPerGame() / leagueAvg;
    }
}
