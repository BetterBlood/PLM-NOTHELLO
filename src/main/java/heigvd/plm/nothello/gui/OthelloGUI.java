package heigvd.plm.nothello.gui;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;
import heigvd.plm.nothello.logic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class OthelloGUI extends JFrame {
    private final Board board;
    private final PredictionEvaluator evaluator;
    private final NotHelloMaxFlipsStrategy maxFlipsStrategy;
    private final JButton[][] buttons = new JButton[8][8];
    private final JComboBox<String> player1Type;
    private final JComboBox<String> player2Type;
    private final JComboBox<String> player1Strategy;
    private final JComboBox<String> player2Strategy;
    private final JLabel blackScoreLabel;
    private final JLabel whiteScoreLabel;
    private final JLabel currentPlayerLabel;
    private final JButton simulateButton;
    private final String[] playerSelection = new String[]{"Human", "Bot"};
    private final String[] strat = new String[]{"Procédural", "Contrainte"};

    private final JButton stepByStepButton;
    private final JCheckBox player1PredictionToggle;
    private final JCheckBox player2PredictionToggle;
    private final JButton showPrediction = new JButton("Compute Strategy");
    private final JLabel loadingLabel = new JLabel();

    // Possibilité d'ajouter un délai
    private static final int SIMULATION_DELAY_MS = 0;

    public OthelloGUI(){
        this.board = new Board();
        this.evaluator = new PredictionEvaluator(this.board);
        this.maxFlipsStrategy = new NotHelloMaxFlipsStrategy();

        setTitle("Othello - PLM");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Grid Panel
        JPanel gridPanel = new JPanel(new GridLayout(8, 8));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(60, 60));
                btn.setFocusPainted(false);
                btn.setMargin(new Insets(0, 0, 0, 0));
                btn.setBackground(new Color(17, 100, 7));
                final int x = i, y = j;
                btn.addActionListener(e -> handleCellClick(x, y));
                buttons[i][j] = btn;
                gridPanel.add(btn);
            }
        }

        // Control Panel
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());

        // Panels pour chaque joueur
        // noir
        JPanel player1Panel = new JPanel();
        player1Panel.setLayout(new BoxLayout(player1Panel, BoxLayout.Y_AXIS));
        player1Panel.setBorder(BorderFactory.createTitledBorder("Joueur Noir"));

        player1Type = new JComboBox<>(playerSelection);
        player1Strategy = new JComboBox<>(strat);
        blackScoreLabel= new JLabel("Noir : 2");
        player1Panel.add(new JLabel("Type :"));
        player1Panel.add(player1Type);
        player1Panel.add(Box.createVerticalStrut(5));
        player1Panel.add(new JLabel("Stratégie :"));
        player1Panel.add(player1Strategy);

        player1Panel.add(Box.createVerticalStrut(5));
        player1PredictionToggle = new JCheckBox("Afficher Prédictions");
        player1PredictionToggle.setSelected(true);
        player1Panel.add(player1PredictionToggle);

        // blanc
        JPanel player2Panel = new JPanel();
        player2Panel.setLayout(new BoxLayout(player2Panel, BoxLayout.Y_AXIS));
        player2Panel.setBorder(BorderFactory.createTitledBorder("Joueur Blanc"));

        player2Type = new JComboBox<>(playerSelection);
        player2Strategy = new JComboBox<>(strat);
        whiteScoreLabel = new JLabel("Blanc : 2");
        player2Panel.add(new JLabel("Type :"));
        player2Panel.add(player2Type);
        player2Panel.add(Box.createVerticalStrut(5));
        player2Panel.add(new JLabel("Stratégie :"));
        player2Panel.add(player2Strategy);

        player2Panel.add(Box.createVerticalStrut(5));
        player2PredictionToggle = new JCheckBox("Afficher Prédictions");
        player2PredictionToggle.setSelected(true);
        player2Panel.add(player2PredictionToggle);


        // Panel combiné des joueurs
        JPanel playersPanel = new JPanel();
        playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
        JPanel playersPanelSplit = new JPanel(new GridLayout(1, 2));
        playersPanelSplit.add(player1Panel);
        playersPanelSplit.add(player2Panel);
        playersPanel.add(playersPanelSplit);
        showPrediction.addActionListener(e -> updateBoardDisplay());// TODO: center
        playersPanel.add(showPrediction);

        // Infos partie
        currentPlayerLabel = new JLabel("Tour actuel : Blanc");
        currentPlayerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Bouton tour par tour (bot)
        stepByStepButton = new JButton("Tour par tour");
        stepByStepButton.setEnabled(false);
        stepByStepButton.addActionListener(e -> playOneBotTurn());

        // Bouton simulation
        simulateButton = new JButton("Lancer Simulation");
        simulateButton.setEnabled(false);
        simulateButton.addActionListener(e -> simulateGame());
        ItemListener typeChangeListener = e -> {
            updateSimulationButtonState();
            updateStepByStepButtonState();
        };
        player1Type.addItemListener(typeChangeListener);
        player2Type.addItemListener(typeChangeListener);

        // Panel infos & actions
        JPanel bottomControlPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        bottomControlPanel.add(currentPlayerLabel);
        bottomControlPanel.add(blackScoreLabel);
        bottomControlPanel.add(whiteScoreLabel);
        bottomControlPanel.add(stepByStepButton);
        bottomControlPanel.add(simulateButton);
        JButton resetButton = new JButton("Nouvelle Partie");
        resetButton.addActionListener(e -> resetBoard());
        bottomControlPanel.add(resetButton);

        controlPanel.add(playersPanel, BorderLayout.NORTH);
        controlPanel.add(bottomControlPanel, BorderLayout.SOUTH);

        // loading fig
        loadingLabel.setIcon(new ImageIcon(Objects.requireNonNull(
                getClass().getResource("/images/loading.gif"))));
        loadingLabel.setVisible(false);
        controlPanel.add(loadingLabel, BorderLayout.CENTER);


        // Ajout à la fenêtre
        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);

        updateBoardDisplay();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleCellClick(int x, int y) {
        if (board.playAt(x, y)) {
            System.out.println("valid placement");
            updateBoardDisplay();
            currentPlayerLabel.setText("Tour actuel : " + board.getPlayerTurn());
            updateStepByStepButtonState();
            if (board.isOver()) { // TODO : recompute score
                String blackScore = blackScoreLabel.getText().split(":")[1].trim();
                String whiteScore = whiteScoreLabel.getText().split(":")[1].trim();
                JOptionPane.showMessageDialog(this, "Game Over: " + " Scores: Noir: " + blackScore + ", Blanc: " + whiteScore + ".");
            }
        }
        updateBoardDisplay();
    }

    private void updateBoardDisplay() {
        loadingLabel.setVisible(true);
        int blackScore = 0;
        int whiteScore = 0;

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                JButton btn = buttons[i][j];
                PieceColor c = board.getColorAt(i, j);
                //System.out.print(c);
                switch (c) {
                    case BLACK -> {
                        btn.setIcon(new ImageIcon(Objects.requireNonNull(
                                getClass().getResource("/images/black.png"))));
                        ++blackScore;
                    }
                    case WHITE -> {
                        btn.setIcon(new ImageIcon(Objects.requireNonNull(
                                getClass().getResource("/images/white.png"))));
                        ++whiteScore;
                    }
                    case NONE -> btn.setIcon(null);
                }
            }
            //System.out.println();
        }

        blackScoreLabel.setText("Noir : " + blackScore);
        whiteScoreLabel.setText("Blanc : " + whiteScore);

        if (shouldShowPrediction()) {
            loadingLabel.setVisible(true);

            SwingWorker<List<int[]>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<int[]> doInBackground() {
                    // Calculer en background
                    return maxFlipsStrategy.getNormalizedScores(board);
                }

                @Override
                protected void done() {
                    try {
                        List<int[]> predictions = get(); // récupère les résultats
                        showPredictions(predictions);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        loadingLabel.setVisible(false);
                    }
                }
            };
            worker.execute(); // démarre le thread
        }
    }

    private NotHelloStrategy getSelectedStrategyForCurrentPlayer() {
        JComboBox<String> box = board.getPlayerTurn() == PieceColor.BLACK ? player1Strategy : player2Strategy;
        return switch (Objects.requireNonNull(box.getSelectedItem()).toString()) {
            case "Procédural" -> new NotHelloMaxFlipsStrategy();
            case "Contrainte" -> new NotHelloConstraintStrategy();
            default -> new NotHelloMaxFlipsStrategy();
        };
    }

    private NotHelloStrategy getSelectedStrategyForOpponent() {
        JComboBox<String> box = board.getPlayerTurn() != PieceColor.BLACK ? player1Strategy : player2Strategy;
        return switch (Objects.requireNonNull(box.getSelectedItem()).toString()) {
            case "Procédural" -> new NotHelloMaxFlipsStrategy();
            case "Contrainte" -> new NotHelloConstraintStrategy();
            default -> new NotHelloMaxFlipsStrategy();
        };
    }


    private void simulateGame() {
        simulateButton.setEnabled(false);
        stepByStepButton.setEnabled(false);

        SwingWorker<Void, Void> simulationWorker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                while (!board.isOver()) {
                    loadingLabel.setVisible(true);

                    NotHelloStrategy strategy = getSelectedStrategyForCurrentPlayer();
                    evaluator.setStrategy(strategy);
                    int[] bestMove = evaluator.evaluateMoves();

                    if (bestMove == null) {
                        // Aucun coup valide, on passe au joueur suivant
                        board.setPlayerTurn(board.getPlayerTurn().opposite());
                        continue;
                    }


                    final int moveX = bestMove[0];
                    final int moveY = bestMove[1];

                    SwingUtilities.invokeLater(() -> {
                        handleCellClick(moveX, moveY);
                        currentPlayerLabel.setText("Tour actuel : " + board.getPlayerTurn());
                        loadingLabel.setVisible(false);
                    });

                    Thread.sleep(SIMULATION_DELAY_MS);


                }
                return null;
            }

            @Override
            protected void done() {
                updateSimulationButtonState();
                updateStepByStepButtonState();
                updateBoardDisplay();
                loadingLabel.setVisible(false);
            }
        };

        simulationWorker.execute();
    }

    private void playOneBotTurn() {
        loadingLabel.setVisible(true);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                NotHelloStrategy strategy = getSelectedStrategyForCurrentPlayer();
                NotHelloStrategy opponentStrategy = getSelectedStrategyForOpponent();
                evaluator.setStrategy(strategy);

                evaluator.setStrategy(strategy);

                int[] bestMove = evaluator.evaluateMoves();

                if (bestMove == null) {
                    // Aucun coup valide, on passe au joueur suivant
                    board.setPlayerTurn(board.getPlayerTurn().opposite());
                    return null;
                }

                handleCellClick(bestMove[0], bestMove[1]);

                return null;
            }

            @Override
            protected void done() {
                loadingLabel.setVisible(false);
                updateBoardDisplay();
                currentPlayerLabel.setText("Tour actuel : " + board.getPlayerTurn());
            }
        };
        worker.execute();
    }

    /**
     * Affiche les prédictions des coups possibles sur la grille.
     * @param evaluations Liste de coups avec leur score (1 à 5). Chaque entrée est : int[0]=x, int[1]=y, int[2]=score (1=meilleur, 5=pire)
     */
    private void showPredictions(java.util.List<int[]> evaluations) {
        for (int[] eval : evaluations) {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(
                    getClass().getResource("/images/prediction_" + eval[2] + ".png")));
            buttons[eval[0]][eval[1]].setIcon(icon);
        }
    }

    private boolean shouldShowPrediction() {
        return (board.getPlayerTurn() == PieceColor.BLACK && player1PredictionToggle.isSelected())
                || (board.getPlayerTurn() == PieceColor.WHITE && player2PredictionToggle.isSelected());
    }


    private void updateSimulationButtonState() {
        simulateButton.setEnabled(
                Objects.equals(player1Type.getSelectedItem(), "Bot") &&
                Objects.equals(player2Type.getSelectedItem(), "Bot"));
    }

    private void updateStepByStepButtonState() {
        PieceColor current = board.getPlayerTurn();
        boolean isCurrentBot =
                (current == PieceColor.BLACK && Objects.equals(player1Type.getSelectedItem(), "Bot")) ||
                        (current == PieceColor.WHITE && Objects.equals(player2Type.getSelectedItem(), "Bot"));
        stepByStepButton.setEnabled(isCurrentBot);
    }

    private void resetBoard() {
        this.dispose();
        new OthelloGUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OthelloGUI::new);
    }
}

