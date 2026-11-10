package sportsseer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the latest results and tells every registered listener when
 * they change (Observer pattern). The Controller writes here; the
 * View only listens, so the Model never needs to know about Swing.
 */
public class ResultsModel {

    // registered observers
    private final List<IResultsListener> f_listeners;

    // most recent results
    private Prediction f_lastPrediction;
    private PlayerProjection f_lastProjection;

    /** Creates a model with no listeners. */
    public ResultsModel() {
        f_listeners = new ArrayList<>();
    }

    /**
     * Registers an observer.
     * @param listener the object to notify
     */
    public void addListener(IResultsListener listener) {
        f_listeners.add(listener);
    }

    /** @return the most recent prediction, or null */
    public Prediction getLastPrediction() {
        return f_lastPrediction;
    }

    /** @return the most recent player projection, or null */
    public PlayerProjection getLastProjection() {
        return f_lastProjection;
    }

    /**
     * Saves a new prediction and notifies listeners.
     * @param prediction the new prediction
     */
    public void setPrediction(Prediction prediction) {
        f_lastPrediction = prediction;
        for (IResultsListener listener : f_listeners) {
            listener.predictionReady(prediction);
        }
    }

    /**
     * Saves a new player projection and notifies listeners.
     * @param projection the new projection
     */
    public void setProjection(PlayerProjection projection) {
        f_lastProjection = projection;
        for (IResultsListener listener : f_listeners) {
            listener.projectionReady(projection);
        }
    }

    /**
     * Notifies listeners of an error.
     * @param message text to show the user
     */
    public void reportError(String message) {
        for (IResultsListener listener : f_listeners) {
            listener.errorOccurred(message);
        }
    }
}
