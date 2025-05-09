package heigvd.plm.nothello.gui;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Objects;

public class OthelloGUI extends JFrame {
    private final Board board;
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
    private final String[] strat = new String[]{"Max Flips", "Min Opponent", "Corner Priority"};

    private final JButton stepByStepButton;
    private final JSpinner player1Depth;
    private final JSpinner player2Depth;

    public OthelloGUI(){
        this.board = new Board();
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
        player1Panel.add(new JLabel("Profondeur :"));
        player1Depth = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        player1Panel.add(player1Depth);

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
        player2Panel.add(new JLabel("Profondeur :"));
        player2Depth = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        player2Panel.add(player2Depth);

        // Panel combiné des joueurs
        JPanel playersPanel = new JPanel(new GridLayout(1, 2));
        playersPanel.add(player1Panel);
        playersPanel.add(player2Panel);

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
        }
    }

    private void updateBoardDisplay() {
        int blackScore = 0;
        int whiteScore = 0;

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                JButton btn = buttons[i][j];
                PieceColor c = board.getColorAt(i, j);
                //System.out.print(c);
                switch (c) {
                    case BLACK ->  {
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
    }

    private void simulateGame() {
        JOptionPane.showMessageDialog(this, "Simulation entre bots à implémenter !");
        // TODO: Implémenter le jeu automatique entre bots
    }

    private void playOneBotTurn() {
        JOptionPane.showMessageDialog(this, "Tour de bot unique à implémenter !");
        // TODO: Implémenter le coup unique entre bots (en fonction des stratégies + profondeur)
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
