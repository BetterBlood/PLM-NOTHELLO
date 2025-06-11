package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class PredictionEvaluator {

    private final Board board;

    private NotHelloStrategy currentStrategy = new NotHelloMaxFlipsStrategy();

    private final Random rng = new Random();

    public PredictionEvaluator(Board board) {
        this.board = board;
    }

    public void setStrategy(NotHelloStrategy strategy) {
        this.currentStrategy = strategy;
    }

    /**
     * Calcule la qualité des coups disponibles pour le joueur courant.
     * @return Le meilleur coup sous forme [x, y]
     */
    public int[] evaluateMoves() {
        return currentStrategy.evaluate(board);
    }
}
