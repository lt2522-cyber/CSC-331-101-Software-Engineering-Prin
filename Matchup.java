package sportsseer.model;

/**
 * Two team-seasons that the user wants to compare. The same franchise
 * is allowed if the seasons differ (2016-17 Warriors vs 2017-18
 * Warriors), but the exact same team-season twice is not.
 */
public class Matchup {

    // the two sides of the matchup
    private final TeamSeason f_teamA;
    private final TeamSeason f_teamB;

    /**
     * Creates a matchup.
     * @param teamA first team-season
     * @param teamB second team-season
     */
    public Matchup(TeamSeason teamA, TeamSeason teamB) {
        f_teamA = teamA;
        f_teamB = teamB;
    }

    /** @return the first team-season */
    public TeamSeason getTeamA() {
        return f_teamA;
    }

    /** @return the second team-season */
    public TeamSeason getTeamB() {
        return f_teamB;
    }

    /**
     * Checks that both teams were chosen and are not the same
     * team-season.
     * @return true if the matchup can be predicted
     */
    public boolean isValid() {
        return f_teamA != null && f_teamB != null
                && !f_teamA.getKey().equals(f_teamB.getKey());
    }

    /**
     * Key used by the prediction cache.
     * @return key built from both team-season keys
     */
    public String getKey() {
        return f_teamA.getKey() + " vs " + f_teamB.getKey();
    }
}
