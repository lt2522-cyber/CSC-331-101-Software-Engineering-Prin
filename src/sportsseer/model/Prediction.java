package sportsseer.model;

/**
 * The result the prediction engine produces for one matchup:
 * who wins, how likely it is, and the point spread.
 */
public class Prediction {

    // the matchup this prediction is for
    private final Matchup f_matchup;

    // probability (0.0-1.0) that team A wins
    private final double f_winProbabilityA;

    // projected final score for each side
    private final double f_projectedScoreA;
    private final double f_projectedScoreB;

    /**
     * Creates a prediction.
     * @param matchup the matchup that was predicted
     * @param winProbabilityA chance team A wins, 0.0 to 1.0
     * @param projectedScoreA projected points for team A
     * @param projectedScoreB projected points for team B
     */
    public Prediction(Matchup matchup, double winProbabilityA,
            double projectedScoreA, double projectedScoreB) {
        f_matchup = matchup;
        f_winProbabilityA = winProbabilityA;
        f_projectedScoreA = projectedScoreA;
        f_projectedScoreB = projectedScoreB;
    }

    /** @return the predicted matchup */
    public Matchup getMatchup() {
        return f_matchup;
    }

    /** @return chance team A wins (0.0-1.0) */
    public double getWinProbabilityA() {
        return f_winProbabilityA;
    }

    /** @return chance team B wins (0.0-1.0) */
    public double getWinProbabilityB() {
        return 1.0 - f_winProbabilityA;
    }

    /** @return projected points for team A */
    public double getProjectedScoreA() {
        return f_projectedScoreA;
    }

    /** @return projected points for team B */
    public double getProjectedScoreB() {
        return f_projectedScoreB;
    }

    /**
     * Point spread: how many points the favorite should win by.
     * @return projected margin, always zero or positive
     */
    public double getPointSpread() {
        return Math.abs(f_projectedScoreA - f_projectedScoreB);
    }

    /** @return the team-season predicted to win */
    public TeamSeason getPredictedWinner() {
        return f_winProbabilityA >= 0.5
                ? f_matchup.getTeamA() : f_matchup.getTeamB();
    }
}
