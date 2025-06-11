package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;

public interface NotHelloStrategy {
    // Conventions utilisées dans les stratégies :
    int MY_COLOR = -1;
    int OPP_COLOR = 1;
    int EMPTY = 0;

    /**
     * Retourne le meilleur mouvement possible pour le joueur courant.
     *
     * @param board    L'état actuel du plateau
     * @return Un tableau {x, y} représentant les coordonnées du meilleur coup
     */
    int[] evaluate(Board board);
}
