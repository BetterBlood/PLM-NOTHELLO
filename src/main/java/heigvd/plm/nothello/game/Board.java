package heigvd.plm.nothello.game;

import heigvd.plm.nothello.logic.NotHelloConstraintStrategy;
import heigvd.plm.nothello.logic.NotHelloStrategy;

import java.sql.SQLOutput;
import java.util.LinkedList;

public class Board {
    public static final int BOARD_SIZE = 8;

    private PieceColor[][] board;
    private PieceColor currentPlayer;
    private boolean gameOver = false;

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
        //System.out.println("try to played at: (" + x + ";" + y + ")");

        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }

        int score = setColorAt(x, y, currentPlayer);
        if (score >= 1) {
            updateBoard(x, y, currentPlayer);
            currentPlayer = currentPlayer.opposite();

            if (!isPlayable()) {
                System.out.println("No more moves available for " + currentPlayer);
                currentPlayer = currentPlayer.opposite();
                if (!isPlayable()) {
                    System.out.println("Game over, no one can play");
                    end();
                }
            }
            return true;
        }
        else {
            System.out.println("Invalid placement at (" + x + ";" + y + "), score: " + score);
            return false;
        }
    }

    public java.util.List<int[]> getValidMovesForCurrentPlayer() { // TODO : voir si on ne devrait pas utiliser OR-Tools pour ça
        java.util.List<int[]> validMoves = new java.util.ArrayList<>();
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board[i][j] == PieceColor.NONE && getMoveScore(i, j, currentPlayer) > 0) {
                    validMoves.add(new int[]{i, j});
                }
            }
        }
        return validMoves;
    }

    private void end() {
        int blackScore = 0;
        int whiteScore = 0;

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                if (board[i][j] == PieceColor.BLACK) {
                    ++blackScore;
                } else if (board[i][j] == PieceColor.WHITE) {
                    ++whiteScore;
                }
            }
        }

        System.out.println("Game over! Final score: Black: " + blackScore + ", White: " + whiteScore);
        gameOver = true;
    }

    public boolean isOver() {
        return gameOver;
    }

    private boolean isPlayable() {
        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                // TODO : optimisation car getMoveScore va calculer le score complet
                //  alors qu'on a juste besoin de savoir si un coup est possible pour le joueur courant

                if (board[i][j] == PieceColor.NONE && getMoveScore(i, j, currentPlayer) > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateBoard(int x, int y, PieceColor color) { // TODO : optimisation
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if (i == 0 && j == 0) continue; // Skip these positions
                int nx = x + i;
                int ny = y + j;
                int tmp_val = 0;
                boolean ok = false;

                while(nx >= 0 && ny >= 0 && nx < 8 && ny < 8) {
                    if (board[nx][ny] == color.opposite()) {
                        ++tmp_val;
                        nx += i;
                        ny += j;
                    }
                    else if (board[nx][ny] == color) {
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
                        System.out.println("played at: (" + x + ";" + y + "), colored: (" + nx + ";" + ny + ")");
                        board[nx][ny] = color;
                    }
                }
            }
        }
    }

    public int setColorAt(int x, int y, PieceColor pieceColor) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != PieceColor.NONE) {
            //System.out.println("at: (" + x + ";" + y + ") contain piece");
            return 0; // Cannot overwrite an existing piece
        }

        int score = getMoveScore(x, y, pieceColor);
        if (score == 0) {
            //System.out.println("at: (" + x + ";" + y + ") Invalid move");
            return 0; // Invalid move
        }
        board[x][y] = pieceColor;
        return score;
    }

    public void hardSetColorAt(int x, int y, PieceColor pieceColor) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }

        board[x][y] = pieceColor;
    }


    /**
     * Force player turn
     * @param pieceColor the new player color desired
     * @return false if nothing changed
     */
    public boolean setPlayerTurn(PieceColor pieceColor) {
        if (getPlayerTurn() == pieceColor) return false;
        currentPlayer = pieceColor;
        return true;
    }

    public PieceColor getPlayerTurn() {
        return currentPlayer;
    }

    public int getMoveScore(int x, int y, PieceColor pieceColor) { // TODO : optimisation
        if (x < 0 || x >= 8 || y < 0 || y >= 8) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        if (board[x][y] != PieceColor.NONE) {
            return 0; // Cannot overwrite an existing piece
        }

        int score = 0;
        int tmpScore;
        boolean ok;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                tmpScore = 0;
                ok = false;
                if (i == 0 && j == 0) continue; // Skip the current position
                int nx = x + i;
                int ny = y + j;
                if (i == 1 && j == -1 && x == 2 && y == 5) {
                    //System.out.println("for (2, 5), pieceColor: " + pieceColor);
                }
                while (nx >= 0 && nx < 8 && ny >= 0 && ny < 8) {
                    if (i == 1 && j == -1 && x == 2 && y == 5) {
                        //System.out.println("nx: " + nx + ", ny: " + ny + ", board[nx][ny]: " + board[nx][ny]);
                    }
                    if (board[nx][ny] == pieceColor.opposite()) {
                        if (i == 1 && j == -1 && x == 2 && y == 5) {
                            //System.out.println("tmpScore: " + (tmpScore + 1));
                        }
                        tmpScore++;
                    } else if (board[nx][ny] == pieceColor) {
                        ok = true;
                        if (i == 1 && j == -1 && x == 2 && y == 5) {
                            //System.out.println("ok");
                        }
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
        //System.out.println("for: (" + x + ";" + y + "), score: " + score);
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

    /*
        * Retourne une matrice qui représente l'état du plateau de jeu.
        * 0 représente une case vide
        * 1 représente une pièce de la couleur du joueur qui demande
        * 2 représente une pièce de l'adversaire.
     */
    public int[][] getBoardMatrix(PieceColor forColor) {
        int[][] matrix = new int[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == PieceColor.NONE) {
                    matrix[i][j] = NotHelloConstraintStrategy.EMPTY;
                } else if (board[i][j] == forColor) {
                    matrix[i][j] = NotHelloConstraintStrategy.MY_COLOR;
                } else {
                    matrix[i][j] = NotHelloConstraintStrategy.OPP_COLOR;
                }
            }
        }
        return matrix;
    }
}
