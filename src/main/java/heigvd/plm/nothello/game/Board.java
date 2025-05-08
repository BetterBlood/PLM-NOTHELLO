package heigvd.plm.nothello.game;

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
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != PieceColor.NONE) {
            return false; // Cannot overwrite an existing piece
        }

        board[x][y] = currentPlayer;
        currentPlayer = currentPlayer.opposite();
        return true;
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

    public int getMoveScore(int x, int y, PieceColor $PieceColor) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != PieceColor.NONE) {
            return 0; // Cannot overwrite an existing piece
        }

        int score = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue; // Skip the current position
                int nx = x + i;
                int ny = y + j;
                while (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    if (board[nx][ny] == $PieceColor.opposite()) {
                        score++;
                    } else if (board[nx][ny] == $PieceColor) {
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
