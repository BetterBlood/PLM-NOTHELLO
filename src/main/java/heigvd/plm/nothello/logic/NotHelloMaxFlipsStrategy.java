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
    public List<int[]> evaluate(Board board, int depth) {

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

            int score = simulateMoveWithDepth(board, x, y, depth);

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
    private int simulateMoveWithDepth(Board originalBoard, int x, int y, int depth) {
        if (depth <= 0) return 0;

        // Clone du plateau
        Board cloned = cloneBoard(originalBoard);

        PieceColor current = cloned.getPlayerTurn();

        // Joue le coup actuel
        boolean movePlayed = cloned.playAt(x, y);
        if (!movePlayed) {
            System.out.println("coup invalide ???? (" + x + ";" + y + ")");
            return 1; // TODO: understand why board contains wrong color
        }

        int baseScore = originalBoard.getMoveScore(x, y, current);

        if (depth == 1) return baseScore;

        // Tour de l’adversaire // TODO : take care of the real opponent strategy
        PredictionEvaluator enemyEvaluator = new PredictionEvaluator(cloned);
        enemyEvaluator.setStrategy(new NotHelloMaxFlipsStrategy());
        List<int[]> counterMoves = enemyEvaluator.evaluateMoves(1);

        if (counterMoves.isEmpty()) return baseScore;

        // On prend le meilleur coup pour l'adversair (score max) le pire coup qu'il puisse faire pour le joueur courant
        int worstCounterScore = 0;
        int enemy_x = 0;
        int enemy_y = 0;
        for (int[] counterMove : counterMoves) {
            int cur_worst = worstCounterScore;
            worstCounterScore = Math.max(worstCounterScore, counterMove[2]);
            if (cur_worst != worstCounterScore)  {
                enemy_x = counterMove[0];
                enemy_y = counterMove[1];
            }
        }
        movePlayed = cloned.playAt(enemy_x, enemy_y);
        if (!movePlayed) {
            System.out.println("coup invalide ???? (" + x + ";" + y + ")");
            return 0; //TODO: check if necessary
        }
        // TODO : recursion !!!!!

        return baseScore - worstCounterScore; // TODO : discuss if the worstcount shouldn't have a coefficient (between 0 and 1)
    }

    /**
     * Clone profond du plateau.
     */
    private Board cloneBoard(Board board) {
        Board clone = new Board();

        for (int i = 0; i < 8; ++i) {
            for (int j = 0; j < 8; ++j) {
                PieceColor color = board.getColorAt(i, j);
                if (!color.isNone()) {
                    clone.setColorAt(i, j, color);
                }
            }
        }

        // Force le même joueur
        clone.setPlayerTurn(board.getPlayerTurn());

        return clone;
    }
}
