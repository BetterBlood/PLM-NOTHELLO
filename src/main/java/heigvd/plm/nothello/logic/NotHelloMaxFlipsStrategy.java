package heigvd.plm.nothello.logic;

import com.google.ortools.Loader;
import com.google.ortools.sat.*;
import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class NotHelloMaxFlipsStrategy implements NotHelloStrategy {
    @Override
    public List<int[]> evaluate(Board board, int depth, NotHelloStrategy strategy) {

        // 1. Cloner le plateau
        // 2. Appliquer le coup du joueur
        // 3. Si depth > 1, générer les coups de l’adversaire et évaluer leur meilleur contre-coup
        // 4. Retourner une évaluation brute

        System.out.println("NotHelloMaxFlipsStrategy:evaluate() with depth: " + depth + " for player: " + board.getPlayerTurn());
        Loader.loadNativeLibraries();

        List<int[]> validMoves = board.getValidMovesForCurrentPlayer();
        List<int[]> evaluatedMoves = new LinkedList<>();

        for (int[] move : validMoves) {
            int x = move[0];
            int y = move[1];

            // Pour chaque coup, on crée un solveur et une seule variable
            CpModel model = new CpModel();
            BoolVar moveVar = model.newBoolVar("move_" + x + "_" + y);

            int score = simulateMoveWithDepth(board, x, y, depth, strategy);

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
     * Simule le coup joué et évalue récursivement jusqu'à la profondeur donnée.
     */
    private int simulateMoveWithDepth(Board originalBoard, int x, int y, int depth, NotHelloStrategy enemy_strategy) {
        System.out.println("simulateMoveWithDepth(x=" + x + ", y=" + y + ", depth=" + depth + ", player=" + originalBoard.getPlayerTurn() + ")");
        if (depth <= 0) return 0;
        if (depth == 1) return originalBoard.getMoveScore(x, y, originalBoard.getPlayerTurn());

        // odd : player, otherwise: opponent

        // Clone du plateau
        Board cloned = cloneBoard(originalBoard);
        PieceColor currentPlayer = cloned.getPlayerTurn();

        int baseScore = originalBoard.getMoveScore(x, y, currentPlayer);
        // Joue le coup sur le plateau cloné
        boolean movePlayed = cloned.playAt(x, y);
        if (!movePlayed) {
            System.out.println(cloned.toString());
            System.out.println("Coup invalide (" + x + ";" + y + ") à depth " + depth); // wtf ??
            return 0;
        }

        if (cloned.getPlayerTurn() == currentPlayer) { // opponent can't play
            PredictionEvaluator playerAgainEvaluator = new PredictionEvaluator(cloned);
            playerAgainEvaluator.setStrategy(this);

            List<int[]> nextMoves = cloned.getValidMovesForCurrentPlayer();
            if (nextMoves.isEmpty()) return baseScore;

            int bestResponse = 0;
            for (int[] againMove : nextMoves) {
                int res = simulateMoveWithDepth(cloned, againMove[0], againMove[1], depth - 2, enemy_strategy);
                bestResponse = Math.max(bestResponse, res);
            }
            return baseScore + bestResponse;
        }

        PredictionEvaluator enemyEvaluator = new PredictionEvaluator(cloned);
        enemyEvaluator.setStrategy(enemy_strategy);

        List<int[]> adversaryMoves = cloned.getValidMovesForCurrentPlayer();
        if (adversaryMoves.isEmpty()) {
            System.out.println("Impossible output, normaly condition should be deleted if this is never displayed");
            return baseScore; // Aucun coup adverse possible
        }

        int worstOutcome = Integer.MIN_VALUE;
        // toujours 1 coup adverse au minimum puisque le cas où l'ennemi ne peut pas jouer est traîté
        for (int[] advMove : adversaryMoves) {
            int advX = advMove[0];
            int advY = advMove[1];

            // Recurse sur la réponse adverse
            int responseScore = simulateMoveWithDepth(cloned, advX, advY, depth - 1, this);
            worstOutcome = Math.max(worstOutcome, responseScore);
        }

        return baseScore - worstOutcome; // impact négatif de l’adversaire
    }
}
