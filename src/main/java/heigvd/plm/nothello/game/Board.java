package heigvd.plm.nothello.game;

import java.sql.SQLOutput;
import java.util.LinkedList;

public class Board {

    private PieceColor[][] board;
    private PieceColor currentPlayer;

    public Board() {
        // Set initial state
        this.board = new PieceColor[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = PieceColor.NONE;
            }
        }
        board[3][3] = PieceColor.WHITE;
        board[3][4] = PieceColor.BLACK;
        board[4][3] = PieceColor.BLACK;
        board[4][4] = PieceColor.WHITE;

        currentPlayer = PieceColor.WHITE;
    }

    public PieceColor getColorAt(int x, int y) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return board[x][y];
    }

    public boolean playAt(int x, int y) {
        System.out.println("try to played at: (" + x + ";" + y + ")");
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }

        if (board[x][y] != PieceColor.NONE) {
            System.out.println("Occupied: (" + x + ";" + y + ")");
            return false; // Cannot overwrite an existing piece
        }
        else if (getMoveScore(x, y, currentPlayer) < 1) {
            System.out.println("Impossible move: (" + x + ";" + y + ")");
            return false;
        }
        else {
            updateBoard(x, y);
        }
        return true;
    }

    private void updateBoard(int x, int y) { // TODO : optimisation
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if (i == 0 && j == 0) continue; // Skip these positions
                int nx = x + i;
                int ny = y + j;
                int tmp_val = 0;
                boolean ok = false;

                while(nx >= 0 && ny >= 0 && nx < 8 && ny < 8) {
                    if (board[nx][ny] == currentPlayer.opposite()) {
                        ++tmp_val;
                        nx += i;
                        ny += j;
                    }
                    else if (board[nx][ny] == currentPlayer) {
                        ok = true;
                        break;
                    }
                    else { // empty cell
                        break;
                    }
                }

                if (ok) {
                    for (int k = 0; k < tmp_val; ++k) {
                        nx -= i;
                        ny -= j;
                        System.out.println("colored: (" + nx + ";" + ny + ")");
                        board[nx][ny] = currentPlayer;
                    }
                }
            }
        }
        board[x][y] = currentPlayer;
        currentPlayer = currentPlayer.opposite();
        // TODO : if no playable move: swap player
    }

    public int setColorAt(int x, int y, PieceColor pieceColor) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != PieceColor.NONE) {
            return 0; // Cannot overwrite an existing piece
        }

        int score = getMoveScore(x, y, pieceColor);

        board[x][y] = pieceColor;
        return score;
    }

    public PieceColor getPlayerTurn() {
        return currentPlayer;
    }

    public int getMoveScore(int x, int y, PieceColor $PieceColor) { // TODO : optimisation
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != PieceColor.NONE) {
            return 0; // Cannot overwrite an existing piece
        }

        int score = 0;
        int tmpScore = 0;
        boolean ok = false;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                tmpScore = 0;
                ok = false;
                if (i == 0 && j == 0) continue; // Skip the current position
                int nx = x + i;
                int ny = y + j;
                while (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    if (board[nx][ny] == $PieceColor.opposite()) {
                        tmpScore++;
                    } else if (board[nx][ny] == $PieceColor) {
                        ok = true;
                        break;
                    } else {
                        break;
                    }
                    nx += i;
                    ny += j;
                }
                if (ok) score += tmpScore;
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
