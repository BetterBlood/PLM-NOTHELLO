package heigvd.plm.nothello.logic;

import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.IntVar;
import com.google.ortools.sat.LinearExpr;
import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of NotHello strategy using CP-SAT solver.
 * This class finds the optimal move on a NotHello board by maximizing
 * the number of opponent's pieces flipped.
 */
public class NotHelloConstraintStrategy implements NotHelloStrategy {
    // Color constants used in the strategy
    public static final int MY_COLOR = -1;
    public static final int OPP_COLOR = 1;
    public static final int EMPTY = 0;

    /**
     * Evaluates the current board state and returns the coordinates of the best move.
     * Uses CP-SAT solver to find the move that maximizes the number of flipped pieces.
     *
     * @param board The current game board
     * @return An array containing the coordinates [x, y] of the best move, or an empty array if no valid move exists
     */
    @Override
    public int[] evaluate(Board board) {
        // Load the OR-Tools native libraries
        Loader.loadNativeLibraries();

        // Create a CP-SAT model
        CpModel model = new CpModel();

        Direction[] allDirections = Direction.getAllDirections();
        int[][] boardMatrix = board.getBoardMatrix(board.getPlayerTurn());
        final int MAX_K = Board.BOARD_SIZE - 1;

        // Decision variables:
        // - moveVars[x][y]: 1 if move (x,y) is played, 0 otherwise
        // - flipVars[x][y][direction][k]: 1 if move (x,y) flips k-1 pieces in the given direction, 0 otherwise
        // - flipMoveVars[x][y][direction][k]: 1 if move (x,y) flips k-1 pieces in the given direction AND is played, 0 otherwise
        IntVar[][] moveVars = new IntVar[Board.BOARD_SIZE][Board.BOARD_SIZE];
        IntVar[][][][] flipVars = new IntVar[Board.BOARD_SIZE][Board.BOARD_SIZE][allDirections.length][MAX_K + 1];
        IntVar[][][][] flipMoveVars = new IntVar[Board.BOARD_SIZE][Board.BOARD_SIZE][allDirections.length][MAX_K + 1];

        // Track all move variables for the "exactly one move" constraint
        IntVar[] allMoveVars = new IntVar[Board.BOARD_SIZE * Board.BOARD_SIZE];
        int moveVarCount = 0;

        // Define objective expression to maximize flipped pieces
        LinearExpr objectiveExpr = LinearExpr.constant(0);

        // Create variables and constraints for each possible move position
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                // Only consider empty cells as potential moves
                if (boardMatrix[i][j] == EMPTY) {
                    String moveVarName = String.format("move_%d_%d", i, j);
                    moveVars[i][j] = model.newBoolVar(moveVarName);
                    allMoveVars[moveVarCount++] = moveVars[i][j];

                    // Track valid flip directions for this move
                    LinearExpr validDirectionsExpr = LinearExpr.constant(0);

                    // For each direction and flip length, create flip variables
                    for (int d = 0; d < allDirections.length; d++) {
                        Direction dir = allDirections[d];

                        for (int k = 2; k <= MAX_K; k++) {
                            // Only create variables if this flip length is geometrically possible
                            if (isValidFlipLength(i, j, dir.getX(), dir.getY(), k)) {
                                String flipVarName = String.format("flip_%d_%d_%d_%d", i, j, d, k);
                                flipVars[i][j][d][k] = model.newBoolVar(flipVarName);

                                String flipMoveVarName = String.format("flipmove_%d_%d_%d_%d", i, j, d, k);
                                flipMoveVars[i][j][d][k] = model.newBoolVar(flipMoveVarName);

                                // Add flip move constraints:
                                // 1. flipMove <= flip (flipMove implies flip)
                                model.addLessOrEqual(flipMoveVars[i][j][d][k], flipVars[i][j][d][k]);

                                // 2. flipMove <= move (flipMove implies move)
                                model.addLessOrEqual(flipMoveVars[i][j][d][k], moveVars[i][j]);

                                // 3. flipMove >= flip + move - 1 (if both flip and move are 1, then flipMove must be 1)
                                model.addGreaterOrEqual(
                                        LinearExpr.sum(new LinearExpr[]{
                                                LinearExpr.term(flipMoveVars[i][j][d][k], 1),
                                                LinearExpr.constant(1)
                                        }),
                                        LinearExpr.sum(new IntVar[]{flipVars[i][j][d][k], moveVars[i][j]})
                                );

                                // Add appropriate flip constraints based on the board state
                                addFlipConstraints(boardMatrix, model, i, j, dir.getX(), dir.getY(), k, flipVars[i][j][d][k]);

                                // Track this as a valid direction for this move
                                validDirectionsExpr = LinearExpr.sum(new LinearExpr[]{
                                        validDirectionsExpr,
                                        LinearExpr.term(flipVars[i][j][d][k], 1)
                                });

                                // Add to objective: each flipMove contributes (k-1) to the objective
                                objectiveExpr = LinearExpr.sum(new LinearExpr[]{
                                        objectiveExpr,
                                        LinearExpr.term(flipMoveVars[i][j][d][k], k - 1)
                                });
                            }
                        }
                    }

                    // A move is valid if and only if at least one direction allows flipping
                    // First, collect all flip variables for this move into an array
                    List<IntVar> validFlipsForThisMove = new ArrayList<>();
                    for (int d = 0; d < allDirections.length; d++) {
                        for (int k = 2; k <= MAX_K; k++) {
                            if (flipVars[i][j][d][k] != null) {
                                validFlipsForThisMove.add(flipVars[i][j][d][k]);
                            }
                        }
                    }

                    // If there are any potential flips for this move
                    if (!validFlipsForThisMove.isEmpty()) {
                        IntVar[] flipVarsArray = validFlipsForThisMove.toArray(new IntVar[0]);

                        // moveVars[i][j] = 1 if the sum of all valid flips is at least 1
                        model.addMaxEquality(moveVars[i][j], flipVarsArray);
                    }
                    else {
                        // If no potential flips, the move is invalid
                        model.addEquality(moveVars[i][j], 0);
                    }
                }
            }
        }

        // Constraint: Exactly one move must be played
        // Adjust the array size to match the actual number of variables we created
        if (moveVarCount > 0) {
            IntVar[] actualMoveVars = new IntVar[moveVarCount];
            System.arraycopy(allMoveVars, 0, actualMoveVars, 0, moveVarCount);
            model.addEquality(LinearExpr.sum(actualMoveVars), 1);

            // Set the objective to maximize the number of flipped pieces
            model.maximize(objectiveExpr);

            // Solve the model
            CpSolver solver = new CpSolver();
            CpSolverStatus status = solver.solve(model);

            // Process the result if optimal or feasible solution found
            if (status == CpSolverStatus.OPTIMAL || status == CpSolverStatus.FEASIBLE) {
                for (int i = 0; i < Board.BOARD_SIZE; i++) {
                    for (int j = 0; j < Board.BOARD_SIZE; j++) {
                        if (moveVars[i][j] != null && solver.value(moveVars[i][j]) == 1) {
                            return new int[] {i, j};
                        }
                    }
                }
            }
        }

        System.out.println("No valid move found.");
        return new int[] {};
    }

    /**
     * Checks if a flip of length k is geometrically valid from position (i,j)
     * in direction (dx,dy).
     *
     * @param i Starting row coordinate
     * @param j Starting column coordinate
     * @param dx Row direction (-1, 0, 1)
     * @param dy Column direction (-1, 0, 1)
     * @param k Flip length
     * @return true if the flip length is valid, false otherwise
     */
    private boolean isValidFlipLength(int i, int j, int dx, int dy, int k) {
        for (int n = 1; n <= k; n++) {
            int newRow = i + n * dx;
            int newCol = j + n * dy;

            if (newRow < 0 || newRow >= Board.BOARD_SIZE ||
                    newCol < 0 || newCol >= Board.BOARD_SIZE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds constraints to determine if a flip is valid in a given direction.
     * A flip is valid if:
     * 1. All cells from 1 to k-1 contain opponent pieces
     * 2. The cell at distance k contains a piece of the current player
     *
     * @param boardMatrix The current board state
     * @param model The CP model to add constraints to
     * @param i Starting row coordinate
     * @param j Starting column coordinate
     * @param dx Row direction (-1, 0, 1)
     * @param dy Column direction (-1, 0, 1)
     * @param k Flip length
     * @param flipVar The variable representing if this flip is valid
     */
    private void addFlipConstraints(int[][] boardMatrix, CpModel model, int i, int j, int dx, int dy, int k, IntVar flipVar) {
        // Check if all cells from 1 to k-1 contain opponent pieces
        for (int n = 1; n < k; n++) {
            if (boardMatrix[i + n * dx][j + n * dy] != OPP_COLOR) {
                // If any cell doesn't contain an opponent piece, this flip is invalid
                model.addEquality(flipVar, 0);
                return;
            }
        }

        // Check if the cell at distance k contains a piece of the current player
        if (boardMatrix[i + k * dx][j + k * dy] != MY_COLOR) {
            // If the cell at distance k doesn't contain a piece of the current player, this flip is invalid
            model.addEquality(flipVar, 0);
        }
    }
}