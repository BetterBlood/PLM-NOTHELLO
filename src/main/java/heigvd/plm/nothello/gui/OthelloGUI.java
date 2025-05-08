package heigvd.plm.nothello.gui;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class OthelloGUI extends JFrame {
    private final Board board;
    private final JButton[][] buttons = new JButton[8][8];
    private final JComboBox<String> player1Type;
    private final JComboBox<String> player2Type;
    private final JComboBox<String> player1Strategy;
    private final JComboBox<String> player2Strategy;

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
                btn.setBackground(new Color(17,100,7));

                final int x = i, y = j;
                btn.addActionListener(e -> handleCellClick(x, y));

                buttons[i][j] = btn;
                gridPanel.add(btn);
            }
        }

        // Control Panel
        JPanel controlPanel = new JPanel(new GridLayout(3, 2, 10, 5));

        player1Type = new JComboBox<>(new String[]{"Humain", "Robot"});
        player2Type = new JComboBox<>(new String[]{"Humain", "Robot"});

        player1Strategy = new JComboBox<>(new String[]{"Max Flips", "Min Opponent", "Corner Priority"});
        player2Strategy = new JComboBox<>(new String[]{"Max Flips", "Min Opponent", "Corner Priority"});

        controlPanel.add(new JLabel("Joueur Noir :"));
        controlPanel.add(player1Type);
        controlPanel.add(new JLabel("Stratégie Noir :"));
        controlPanel.add(player1Strategy);

        controlPanel.add(new JLabel("Joueur Blanc :"));
        controlPanel.add(player2Type);
        controlPanel.add(new JLabel("Stratégie Blanc :"));
        controlPanel.add(player2Strategy);

        JButton resetButton = new JButton("Nouvelle Partie");
        resetButton.addActionListener(e -> resetBoard());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(resetButton);

        add(gridPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        updateBoardDisplay();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void handleCellClick(int x, int y) {
        if (board.playAt(x, y)) {
            System.out.println("valid placement");
            updateBoardDisplay();
        }
    }

    private void updateBoardDisplay() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                JButton btn = buttons[i][j];
                PieceColor c = board.getColorAt(i, j);
                //System.out.print(c);
                switch (c) {
                    case BLACK -> btn.setIcon(new ImageIcon(Objects.requireNonNull(
                            getClass().getResource("/images/black.png"))));
                    case WHITE -> btn.setIcon(new ImageIcon(Objects.requireNonNull(
                            getClass().getResource("/images/white.png"))));
                    case NONE -> btn.setIcon(null);
                }
            }
            //System.out.println();
        }
    }

    private void resetBoard() {
        this.dispose();
        new OthelloGUI();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OthelloGUI::new);
    }
}
