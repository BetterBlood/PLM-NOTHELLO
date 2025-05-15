package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;

import java.util.List;

public interface NotHelloStrategy {
    /**
     * Évalue le score d'un coup à la position (x, y) pour un joueur donné, à une certaine profondeur.
     *
     * @param board L'état actuel du plateau
     * @param depth La profondeur de récursion
     * @return Un score entier représentant la qualité du coup
     */
    List<int[]> evaluate(Board board, int depth);
}
