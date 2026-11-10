package sportsseer.model;

/**
 * Observer pattern: anything that wants to hear about new results
 * (the View) implements this and registers with ResultsModel.
 */
public interface IResultsListener {

    /**
     * Called when a new matchup prediction is ready.
     * @param prediction the new prediction
     */
    void predictionReady(Prediction prediction);

    /**
     * Called when a new player projection is ready.
     * @param projection the new projection
     */
    void projectionReady(PlayerProjection projection);

    /**
     * Called when something went wrong (bad selection, missing data).
     * @param message text to show the user
     */
    void errorOccurred(String message);
}
