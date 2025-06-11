package heigvd.plm.nothello.logic;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NotHelloMaxFlipsStrategy implements NotHelloStrategy {

    @Override
    public int[] evaluate(Board board) {
        List<int[]> evaluations = evaluateAllMoves(board);
        if (evaluations.isEmpty()) {
            return null; // Aucun coup possible
        }
        // Sélectionne le coup avec le score le plus élevé
        evaluations.sort((a, b) -> Integer.compare(b[2], a[2]));
        int[] bestMove = evaluations.get(0);
        return new int[]{bestMove[0], bestMove[1]};
    }

    public List<int[]> getNormalizedScores(Board board) {
        return normalizeScores(evaluateAllMoves(board));
    }

    /**
     * Évalue le score d'un coup à la position (x, y) pour un joueur donné, à une certaine profondeur.
     *
     * @param board    L'état actuel du plateau
     * @return Un score entier représentant la qualité du coup
     */
    private List<int[]> evaluateAllMoves(Board board) {

        // 1. Cloner le plateau
        // 2. Appliquer le coup du joueur
        // 3. Retourner une évaluation brute

        System.out.println("NotHelloMaxFlipsStrategy:evaluate() for player: " + board.getPlayerTurn());
        Loader.loadNativeLibraries();

        List<int[]> validMoves = board.getValidMovesForCurrentPlayer();
        List<int[]> evaluatedMoves = new LinkedList<>();

        for (int[] move : validMoves) {
            int x = move[0];
            int y = move[1];

            // Pour chaque coup, on crée un solveur et une seule variable
            CpModel model = new CpModel();
            BoolVar moveVar = model.newBoolVar("move_" + x + "_" + y);

            int score = board.getMoveScore(x, y, board.getPlayerTurn());

            // Score objectif pour CE coup uniquement
            model.maximize(LinearExpr.weightedSum(new BoolVar[]{moveVar}, new long[]{score}));
            model.addEquality(moveVar, 1); // on force à "jouer" ce coup

            CpSolver solver = new CpSolver();
            CpSolverStatus status = solver.solve(model);

            if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
                evaluatedMoves.add(new int[]{x, y, score});
            }
        }

        System.out.print("Possible moves, evaluatedMoves.size(): " + evaluatedMoves.size() + ", scores: ");
        for (int[] evalMove : evaluatedMoves) {
            System.out.print(Arrays.toString(evalMove) + " ");
        }
        System.out.println();

        return evaluatedMoves;
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