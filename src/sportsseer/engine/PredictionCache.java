package sportsseer.engine;

import java.util.HashMap;
import java.util.Map;

import sportsseer.model.Prediction;

/**
 * Remembers predictions already made while the app is open, so
 * asking for the same matchup twice does not recalculate it. This is
 * the lightweight replacement for "saved predictions" in a database.
 */
public class PredictionCache {

    // matchup key -> finished prediction
    private final Map<String, Prediction> f_predictions;

    /** Creates an empty cache. */
    public PredictionCache() {
        f_predictions = new HashMap<>();
    }

    /**
     * @param key a matchup key from Matchup.getKey()
     * @return the stored prediction, or null if not made yet
     */
    public Prediction get(String key) {
        return f_predictions.get(key);
    }

    /**
     * Stores a prediction.
     * @param key a matchup key from Matchup.getKey()
     * @param prediction the prediction to remember
     */
    public void put(String key, Prediction prediction) {
        f_predictions.put(key, prediction);
    }

    /** @return how many predictions are stored */
    public int size() {
        return f_predictions.size();
    }
}
