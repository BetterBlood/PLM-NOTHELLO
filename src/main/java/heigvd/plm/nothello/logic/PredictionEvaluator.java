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
        List<int[]> result = new ArrayList<>();

        // Étapes prévues dans currentStrategy.evaluate:
        // 1. Cloner le plateau
        // 2. Appliquer le coup du joueur
        // 3. Si depth > 1, générer les coups de l’adversaire et évaluer leur meilleur contre-coup
        // 4. Retourner une évaluation brute
        result = currentStrategy.evaluate(board, recursionDepth);

        List<int[]> validMoves = board.getValidMovesForCurrentPlayer(); // TODO: tmp

        for (int[] move : validMoves) { // TODO: tmp
            result.add(new int[]{move[0], move[1], evaluateMove(move[0], move[1], recursionDepth)});
        }

        return normalizeScores(result);
    }

    /**
     * Évalue un coup avec récursion.
     * Implémentation prévue via OR-Tools.
     */
    private int evaluateMove(int x, int y, int depth) { // TODO: tmp
        return rng.nextInt()%5; // TODO: tmp
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
