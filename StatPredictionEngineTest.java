package sportsseer.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sportsseer.data.StatsRepository;
import sportsseer.model.Matchup;
import sportsseer.model.PlayerProjection;
import sportsseer.model.PlayerSeason;
import sportsseer.model.Prediction;
import sportsseer.model.TeamSeason;

/**
 * JUnit tests for the prediction engine.
 */
class StatPredictionEngineTest {

    // allowed rounding error when comparing doubles
    private static final double DELTA = 1e-9;

    // a strong team, a weak team, and the engine under test
    private TeamSeason f_strong;
    private TeamSeason f_weak;
    private StatPredictionEngine f_engine;

    /** Builds fresh test data before every test. */
    @BeforeEach
    void setUp() {
        // made-up season so real data never mixes into the tests
        f_strong = new TeamSeason("Strong", "2098-99", 60, 22, 115, 105);
        f_weak = new TeamSeason("Weak", "2098-99", 22, 60, 105, 115);
        StatsRepository repo = StatsRepository.getInstance();
        repo.addTeam(f_strong);
        repo.addTeam(f_weak);
        f_engine = new StatPredictionEngine(repo);
    }

    /** The better team should be favored. */
    @Test
    void strongerTeamIsFavored() {
        Prediction p = f_engine.predictMatchup(
                new Matchup(f_strong, f_weak));
        assertSame(f_strong, p.getPredictedWinner());
        assertTrue(p.getWinProbabilityA() > 0.5);
    }

    /** MOV +10 vs MOV -10 gives a 20-point spread. */
    @Test
    void spreadIsDifferenceInMargins() {
        Prediction p = f_engine.predictMatchup(
                new Matchup(f_strong, f_weak));
        assertEquals(20.0, p.getPointSpread(), DELTA);
        assertEquals(120.0, p.getProjectedScoreA(), DELTA);
        assertEquals(100.0, p.getProjectedScoreB(), DELTA);
    }

    /** Swapping the order should flip the probabilities. */
    @Test
    void predictionIsSymmetric() {
        Prediction ab = f_engine.predictMatchup(
                new Matchup(f_strong, f_weak));
        Prediction ba = f_engine.predictMatchup(
                new Matchup(f_weak, f_strong));
        assertEquals(ab.getWinProbabilityA(), ba.getWinProbabilityB(),
                DELTA);
    }

    /** Equal margins means a coin flip. */
    @Test
    void evenMatchupIsFiftyFifty() {
        assertEquals(0.5, StatPredictionEngine.winProbability(0), DELTA);
    }

    /** The same team-season twice is not allowed. */
    @Test
    void sameTeamSeasonIsRejected() {
        Matchup m = new Matchup(f_strong, f_strong);
        assertFalse(m.isValid());
        assertThrows(IllegalArgumentException.class,
                () -> f_engine.predictMatchup(m));
    }

    /** Same franchise, different seasons is allowed. */
    @Test
    void sameFranchiseDifferentSeasonIsAllowed() {
        TeamSeason later = new TeamSeason("Strong", "2099-00",
                50, 32, 112, 108);
        assertTrue(new Matchup(f_strong, later).isValid());
    }

    /** A player scores more against a worse-than-average defense. */
    @Test
    void playerScoresMoreAgainstBadDefense() {
        PlayerSeason player = new PlayerSeason("Test Player", "Strong",
                "2098-99", 20.0, 5.0, 5.0, 35.0);
        PlayerProjection vsWeak = f_engine.predictPlayer(player, f_weak);
        PlayerProjection vsStrong =
                f_engine.predictPlayer(player, f_strong);
        assertTrue(vsWeak.getProjectedPoints()
                > vsStrong.getProjectedPoints());
        assertEquals(5.0, vsWeak.getProjectedRebounds(), DELTA);
    }

    /** Untracked stats (old seasons) stay untracked, not zero. */
    @Test
    void missingStatStaysMissing() {
        PlayerSeason oldTimer = new PlayerSeason("Old Timer", "Strong",
                "2098-99", 20.0, Double.NaN, Double.NaN, Double.NaN);
        PlayerProjection p = f_engine.predictPlayer(oldTimer, f_weak);
        assertTrue(Double.isNaN(p.getProjectedRebounds()));
    }
}
