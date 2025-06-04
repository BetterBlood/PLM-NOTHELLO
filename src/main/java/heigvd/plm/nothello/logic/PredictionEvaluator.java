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
     * @param recursionDepth 1 = 1 coup, 2 = joueur -> adversaire -> joueur, etc.
     * @return Liste de coups sous forme [x, y, score de 1 (meilleur) à 5 (pire)]
     */
    public List<int[]> evaluateMoves(int recursionDepth) {
        return normalizeScores(currentStrategy.evaluate(board, recursionDepth));
    }

    /**
     * Normalise les scores sur une échelle de 1 (meilleur) à 5 (pire).
     * @param evaluations liste de coups (x, y, rawScore)
     * @return liste de coups (x, y, normalizedScore)
     */
    private List<int[]> normalizeScores(List<int[]> evaluations) {
        if (evaluations.isEmpty()) return evaluations;

        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for (int[] eval : evaluations) {
            int score = eval[2];
            if (score < min) min = score;
            if (score > max) max = score;
        }

        List<int[]> normalizedList = new ArrayList<>();
        for (int[] eval : evaluations) {
            int x = eval[0];
            int y = eval[1];
            int rawScore = eval[2];
            int normalized = 3;

            if (max != min) {
                normalized = 1 + (int) Math.round(4.0 * (max - rawScore) / (double) (max - min)); // Inversé pour que 1 = meilleur
            }

            normalizedList.add(new int[]{x, y, normalized});
        }

        return normalizedList;
    }
}
