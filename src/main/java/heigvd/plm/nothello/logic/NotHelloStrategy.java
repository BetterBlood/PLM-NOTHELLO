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
     * Retourne le meilleur mouvement possible pour le joueur courant.
     *
     * @param board    L'état actuel du plateau
     * @return Un tableau {x, y} représentant les coordonnées du meilleur coup
     */
    int[] evaluate(Board board);
}
