package sportsseer.view;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import sportsseer.controller.PredictionController;
import sportsseer.model.IResultsListener;
import sportsseer.model.PlayerProjection;
import sportsseer.model.PlayerSeason;
import sportsseer.model.Prediction;
import sportsseer.model.TeamSeason;

/**
 * The Sports Seer window (View). It has three tabs: Matchup
 * Prediction, Team Stats and Player Stats. Every team is picked in
 * two steps (season, then team) so the lists stay short. It sends
 * user actions to the controller and redraws itself when the
 * ResultsModel notifies it (Observer pattern).
 */
public class SportsSeerFrame extends JFrame implements IResultsListener {

    // needed because JFrame is Serializable
    private static final long serialVersionUID = 1L;

    // window size in pixels
    private static final int WIDTH = 640;
    private static final int HEIGHT = 560;

    // text shown for stats that were not tracked in early seasons
    private static final String NOT_TRACKED = "N/A";

    // the controller that handles every button click
    private final transient PredictionController f_controller;

    // seasons, newest first, shared by every season drop-down
    private final String[] f_seasons;

    // matchup tab widgets
    private final JComboBox<String> f_seasonABox;
    private final JComboBox<TeamSeason> f_teamABox;
    private final JComboBox<String> f_seasonBBox;
    private final JComboBox<TeamSeason> f_teamBBox;
    private final JTextArea f_matchupOutput;

    // team stats tab widgets
    private final JComboBox<String> f_statsSeasonBox;
    private final JComboBox<TeamSeason> f_statsTeamBox;
    private final JTextArea f_teamOutput;

    // player tab widgets
    private final JComboBox<String> f_playerSeasonBox;
    private final JComboBox<TeamSeason> f_playerTeamBox;
    private final JComboBox<PlayerSeason> f_playerBox;
    private final JComboBox<String> f_oppSeasonBox;
    private final JComboBox<TeamSeason> f_opponentBox;
    private final JTextArea f_playerOutput;

    /**
     * Builds the window.
     * @param controller controller that handles user actions
     */
    public SportsSeerFrame(PredictionController controller) {
        super("Sports Seer");
        f_controller = controller;
        f_seasons = controller.getSeasons().toArray(new String[0]);

        f_seasonABox = new JComboBox<>(f_seasons);
        f_teamABox = new JComboBox<>();
        f_seasonBBox = new JComboBox<>(f_seasons);
        f_teamBBox = new JComboBox<>();
        f_statsSeasonBox = new JComboBox<>(f_seasons);
        f_statsTeamBox = new JComboBox<>();
        f_playerSeasonBox = new JComboBox<>(f_seasons);
        f_playerTeamBox = new JComboBox<>();
        f_playerBox = new JComboBox<>();
        f_oppSeasonBox = new JComboBox<>(f_seasons);
        f_opponentBox = new JComboBox<>();
        f_matchupOutput = makeOutputArea();
        f_teamOutput = makeOutputArea();
        f_playerOutput = makeOutputArea();

        linkSeasonToTeams(f_seasonABox, f_teamABox);
        linkSeasonToTeams(f_seasonBBox, f_teamBBox);
        linkSeasonToTeams(f_statsSeasonBox, f_statsTeamBox);
        linkSeasonToTeams(f_oppSeasonBox, f_opponentBox);
        linkSeasonToTeams(f_playerSeasonBox, f_playerTeamBox);
        f_playerTeamBox.addActionListener(e -> refreshPlayers());
        f_playerSeasonBox.addActionListener(e -> refreshPlayers());
        refreshPlayers();
        if (f_teamBBox.getItemCount() > 1) {
            f_teamBBox.setSelectedIndex(1);
        }

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Matchup Prediction", buildMatchupTab());
        tabs.addTab("Team Stats", buildTeamTab());
        tabs.addTab("Player Stats", buildPlayerTab());
        add(tabs);

        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // ---- IResultsListener (Observer) ----

    /**
     * Shows a finished matchup prediction.
     * @param p the prediction
     */
    @Override
    public void predictionReady(Prediction p) {
        TeamSeason a = p.getMatchup().getTeamA();
        TeamSeason b = p.getMatchup().getTeamB();
        f_matchupOutput.setText(String.format(
                "Predicted winner: %s%n%n"
                + "Win chance:%n  %s  %.1f%%%n  %s  %.1f%%%n%n"
                + "Projected score: %.0f - %.0f%n"
                + "Point spread: %s by %.1f",
                p.getPredictedWinner(),
                a, p.getWinProbabilityA() * 100,
                b, p.getWinProbabilityB() * 100,
                p.getProjectedScoreA(), p.getProjectedScoreB(),
                p.getPredictedWinner().getTeamName(),
                p.getPointSpread()));
    }

    /**
     * Shows a finished player projection.
     * @param p the projection
     */
    @Override
    public void projectionReady(PlayerProjection p) {
        PlayerSeason pl = p.getPlayer();
        f_playerOutput.setText(String.format(
                "%s vs %s%n%n"
                + "Season averages: %s pts, %s reb, %s ast, %s 3PT%%%n%n"
                + "Projected vs this opponent:%n"
                + "  %s points%n  %s rebounds%n  %s assists",
                pl, p.getOpponent(),
                stat(pl.getPointsPerGame()), stat(pl.getReboundsPerGame()),
                stat(pl.getAssistsPerGame()),
                stat(pl.getThreePointPercentage()),
                stat(p.getProjectedPoints()),
                stat(p.getProjectedRebounds()),
                stat(p.getProjectedAssists())));
    }

    /**
     * Shows an error pop-up.
     * @param message text to show
     */
    @Override
    public void errorOccurred(String message) {
        JOptionPane.showMessageDialog(this, message, "Sports Seer",
                JOptionPane.WARNING_MESSAGE);
    }

    // ---- tab builders ----

    /** @return the Matchup Prediction tab */
    private JPanel buildMatchupTab() {
        JButton predict = new JButton("Generate Prediction");
        predict.addActionListener(e ->
                f_controller.requestMatchupPrediction(
                        (TeamSeason) f_teamABox.getSelectedItem(),
                        (TeamSeason) f_teamBBox.getSelectedItem()));
        return buildTab(
                new String[] {"Team A season", "Team A",
                    "Team B season", "Team B"},
                new JComboBox<?>[] {f_seasonABox, f_teamABox,
                    f_seasonBBox, f_teamBBox},
                predict, f_matchupOutput);
    }

    /** @return the Team Stats tab (shows win/loss record) */
    private JPanel buildTeamTab() {
        JButton show = new JButton("View Team Stats");
        show.addActionListener(e -> showTeamStats());
        return buildTab(new String[] {"Season", "Team"},
                new JComboBox<?>[] {f_statsSeasonBox, f_statsTeamBox},
                show, f_teamOutput);
    }

    /** @return the Player Stats tab */
    private JPanel buildPlayerTab() {
        JButton project = new JButton("Predict Performance");
        project.addActionListener(e ->
                f_controller.requestPlayerProjection(
                        (PlayerSeason) f_playerBox.getSelectedItem(),
                        (TeamSeason) f_opponentBox.getSelectedItem()));
        return buildTab(
                new String[] {"Season", "Team", "Player",
                    "Opponent season", "Opponent"},
                new JComboBox<?>[] {f_playerSeasonBox, f_playerTeamBox,
                    f_playerBox, f_oppSeasonBox, f_opponentBox},
                project, f_playerOutput);
    }

    // ---- helpers ----

    /** Shows the selected team's record and scoring averages. */
    private void showTeamStats() {
        TeamSeason t = (TeamSeason) f_statsTeamBox.getSelectedItem();
        if (t == null) {
            errorOccurred("No team data available.");
            return;
        }
        f_teamOutput.setText(String.format(
                "%s%n%nRecord: %d-%d%n"
                + "Points per game: %.1f%n"
                + "Points allowed per game: %.1f",
                t, t.getWins(), t.getLosses(),
                t.getPointsPerGame(), t.getPointsAllowedPerGame()));
    }

    /**
     * Makes a team drop-down list only the teams from the season
     * picked in a season drop-down.
     * @param seasonBox the season drop-down
     * @param teamBox the team drop-down it controls
     */
    private void linkSeasonToTeams(JComboBox<String> seasonBox,
            JComboBox<TeamSeason> teamBox) {
        Runnable refresh = () -> {
            String season = (String) seasonBox.getSelectedItem();
            List<TeamSeason> teams = season == null
                    ? List.of() : f_controller.getTeams(season);
            teamBox.setModel(new DefaultComboBoxModel<>(
                    teams.toArray(new TeamSeason[0])));
        };
        seasonBox.addActionListener(e -> refresh.run());
        refresh.run();
    }

    /** Fills the player drop-down from the selected team. */
    private void refreshPlayers() {
        TeamSeason team = (TeamSeason) f_playerTeamBox.getSelectedItem();
        List<PlayerSeason> players = team == null
                ? List.of() : f_controller.getPlayers(team);
        f_playerBox.setModel(new DefaultComboBoxModel<>(
                players.toArray(new PlayerSeason[0])));
    }

    /**
     * @param value a stat, or NaN if it was not tracked
     * @return the stat with one decimal, or "N/A"
     */
    private static String stat(double value) {
        return Double.isNaN(value)
                ? NOT_TRACKED : String.format("%.1f", value);
    }

    /**
     * Lays out one tab: labeled drop-downs, a button, and an output
     * area.
     * @param labels label for each drop-down
     * @param boxes the drop-downs
     * @param button the action button
     * @param output where results are shown
     * @return the finished panel
     */
    private JPanel buildTab(String[] labels, JComboBox<?>[] boxes,
            JButton button, JTextArea output) {
        JPanel inputs = new JPanel(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < boxes.length; i++) {
            inputs.add(new JLabel(labels[i]));
            inputs.add(boxes[i]);
        }
        inputs.add(new JLabel());
        inputs.add(button);

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        panel.add(inputs, BorderLayout.NORTH);
        panel.add(output, BorderLayout.CENTER);
        return panel;
    }

    /** @return a read-only text area for results */
    private JTextArea makeOutputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setBorder(BorderFactory.createEtchedBorder());
        return area;
    }
}
