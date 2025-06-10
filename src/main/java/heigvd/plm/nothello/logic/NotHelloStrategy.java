package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import java.util.List;

public interface NotHelloStrategy {
    // Conventions utilisées dans les stratégies :
    public final int MY_COLOR = -1;
    public final int OPP_COLOR = 1;
    public final int EMPTY = 0;

    /**
     * Évalue le score d'un coup à la position (x, y) pour un joueur donné, à une certaine profondeur.
     *
     * @param board    L'état actuel du plateau
     * @param depth    La profondeur de récursion
     * @param strategy la stratégie de l'adversaire
     * @return Un score entier représentant la qualité du coup
     */
    List<int[]> evaluate(Board board, int depth, NotHelloStrategy strategy);

    /**
     * Clone profond du plateau.
     */
    default Board cloneBoard(Board board) {
        //System.out.println("\ncall to cloneBoard():");
        Board clone = new Board();

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                PieceColor color = board.getColorAt(i, j);
                if (!color.isNone()) {
                    clone.hardSetColorAt(i, j, color);
                }
            }
        }

        // Force le même joueur
        clone.setPlayerTurn(board.getPlayerTurn());

        /*
        System.out.println("orginal board:");
        System.out.println(board.getPlayerTurn());
        System.out.println(board.toString());
        System.out.println("copied board:");
        System.out.println(clone.getPlayerTurn());
        System.out.println(clone.toString());
        //*/

        return clone;
    }
}
