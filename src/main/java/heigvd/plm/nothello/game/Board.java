package heigvd.plm.nothello.game;

import java.util.Arrays;

public class Board {

    private Color[][] board;

    public Board() {
        // Set initial state
        this.board = new Color[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = Color.NONE;
            }
        }
        board[3][3] = Color.WHITE;
        board[3][4] = Color.BLACK;
        board[4][3] = Color.BLACK;
        board[4][4] = Color.WHITE;
    }

    public Color getColorAt(int x, int y) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return board[x][y];
    }

    public int setColorAt(int x, int y, Color color) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != Color.NONE) {
            return 0; // Cannot overwrite an existing piece
        }

        int score = getMoveScore(x, y, color);

        board[x][y] = color;
        return score;
    }

    public int getMoveScore(int x, int y, Color $color) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != Color.NONE) {
            return 0; // Cannot overwrite an existing piece
        }

        int score = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the current position
                int nx = x + i;
                int ny = y + j;
                while (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    if (board[nx][ny] == $color.opposite()) {
                        score++;
                    } else if (board[nx][ny] == $color) {
                        break;
                    } else {
                        break;
                    }
                    nx += i;
                    ny += j;
                }
            }
        }
        return score;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("  0 1 2 3 4 5 6 7\n");
        sb.append(" ┌────────────────┐\n");
        for (int i = 0; i < 8; i++) {
            sb.append(i).append("│");
            for (int j = 0; j < 8; j++) {
                sb.append(board[i][j].getChar());
                sb.append(" ");
            }
            sb.append("│\n");
        }
        sb.append(" └────────────────┘");
        return sb.toString();
    }
}
