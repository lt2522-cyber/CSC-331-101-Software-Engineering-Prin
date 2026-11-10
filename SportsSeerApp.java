package sportsseer;

import java.io.IOException;
import java.nio.file.Path;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import sportsseer.controller.PredictionController;
import sportsseer.data.StatsRepository;
import sportsseer.engine.PredictionCache;
import sportsseer.engine.StatPredictionEngine;
import sportsseer.model.ResultsModel;
import sportsseer.view.SportsSeerFrame;

/**
 * Starts Sports Seer: loads the stats, wires Model, View and
 * Controller together, and opens the window.
 */
public final class SportsSeerApp {

    // folder that holds the CSV stat files
    public static final String DATA_FOLDER = "data";

    /** Not meant to be created. */
    private SportsSeerApp() {
    }

    /**
     * Program entry point.
     * @param args optional: path to the data folder
     */
    public static void main(String[] args) {
        Path folder = Path.of(args.length > 0 ? args[0] : DATA_FOLDER);
        StatsRepository repository = StatsRepository.getInstance();
        try {
            repository.load(folder);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Could not load NBA stats from "
                            + folder.toAbsolutePath(),
                    "Sports Seer", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ResultsModel results = new ResultsModel();
        PredictionController controller = new PredictionController(
                repository, new StatPredictionEngine(repository),
                new PredictionCache(), results);

        SwingUtilities.invokeLater(() -> {
            SportsSeerFrame frame = new SportsSeerFrame(controller);
            results.addListener(frame);
            frame.setVisible(true);
        });
    }
}
