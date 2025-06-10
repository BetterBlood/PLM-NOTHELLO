package heigvd.plm.nothello.logic;

import com.google.ortools.Loader;
import com.google.ortools.linearsolver.MPConstraint;
import com.google.ortools.linearsolver.MPObjective;
import com.google.ortools.linearsolver.MPSolver;
import com.google.ortools.linearsolver.MPVariable;
import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.Direction;

import java.util.LinkedList;
import java.util.List;

public class NotHelloMaxFlipsStrategy implements NotHelloStrategy {
    @Override
    public List<int[]> evaluate(Board board, int depth, NotHelloStrategy strategy) {

        Loader.loadNativeLibraries();

        MPSolver solver = MPSolver.createSolver("SCIP");
        if (solver == null) {
            throw new RuntimeException("Le solveur n'a pas pu être créé.");
        }

        Direction[] allDirections = Direction.getAllDirections();
        int[][] boardMatrix = board.getBoardMatrix(board.getPlayerTurn());

        final int MAX_K = Board.BOARD_SIZE - 1;

        // Variables de décision
        // - moveVars[x][y] : 1 si le coup (x, y) est joué, 0 sinon
        // - flipVars[x][y][direction][k] : 1 si le coup (x, y) retourne k - 1 pièces dans la direction donnée, 0 sinon
        // - flipMoveVars[x][y][direction][k] : 1 si le coup (x, y) retourne k - 1 pièces dans la direction donnée et est joué, 0 sinon
        MPVariable[][] moveVars = new MPVariable[Board.BOARD_SIZE][Board.BOARD_SIZE];
        MPVariable[][][][] flipVars = new MPVariable[Board.BOARD_SIZE][Board.BOARD_SIZE][allDirections.length][MAX_K + 1];
        MPVariable[][][][] flipMoveVars = new MPVariable[Board.BOARD_SIZE][Board.BOARD_SIZE][allDirections.length][MAX_K + 1];

        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                // Si la case est vide, on crée les variables de décision
                if (boardMatrix[i][j] == EMPTY) {
                    String varName = "move_" + i + "_" + j;
                    moveVars[i][j] = solver.makeBoolVar(varName);

                    // Pour chaque direction et distance k, on crée les variables de flip
                    for (int d = 0; d < allDirections.length; d++) {
                        for (int k = 2; k <= MAX_K; k++) {
                            // Pour l'efficacité, on vérifie si la longueur de flip est valide
                            if (isValidFlipLength(i, j, allDirections[d].getX(), allDirections[d].getY(), k)) {
                                String flipVarName = "flip_" + i + "_" + j + "_" + d + "_" + k;
                                flipVars[i][j][d][k] = solver.makeBoolVar(flipVarName);

                                String fmVarName = "flipmove_" + i + "_" + j + "_" + d + "_" + k;
                                flipMoveVars[i][j][d][k] = solver.makeBoolVar(fmVarName);

                                MPConstraint c1 = solver.makeConstraint(0.0, 1.0);
                                c1.setCoefficient(flipMoveVars[i][j][d][k], 1.0);
                                c1.setCoefficient(flipVars[i][j][d][k], -1.0);

                                MPConstraint c2 = solver.makeConstraint(0.0, 1.0);
                                c2.setCoefficient(flipMoveVars[i][j][d][k], 1.0);
                                c2.setCoefficient(moveVars[i][j], -1.0);

                                MPConstraint c3 = solver.makeConstraint(-1.0, Double.POSITIVE_INFINITY);
                                c3.setCoefficient(flipMoveVars[i][j][d][k], 1.0);
                                c3.setCoefficient(flipVars[i][j][d][k], -1.0);
                                c3.setCoefficient(moveVars[i][j], -1.0);

                                // Add flippable direction constraints
                                addFlipConstraints(boardMatrix, solver, i, j, allDirections[d].getX(), allDirections[d].getY(), k, flipVars[i][j][d][k]);
                            }
                        }
                    }
                }
            }
        }

        // Constraint: A move is valid if at least one direction is flippable
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (boardMatrix[i][j] == EMPTY && moveVars[i][j] != null) {
                    MPConstraint validMove = solver.makeConstraint(0.0, 0.0);
                    validMove.setCoefficient(moveVars[i][j], 1.0);

                    // Subtract all flip variables for this cell
                    for (int d = 0; d < allDirections.length; d++) {
                        for (int k = 2; k <= MAX_K; k++) {
                            if (flipVars[i][j][d][k] != null) {
                                validMove.setCoefficient(flipVars[i][j][d][k], -1.0);
                            }
                        }
                    }
                }
            }
        }

        // Constraint: Make exactly one move
        MPConstraint makeOneMove = solver.makeConstraint(1.0, 1.0);
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (moveVars[i][j] != null) {
                    makeOneMove.setCoefficient(moveVars[i][j], 1.0);
                }
            }
        }

        MPObjective objective = solver.objective();
        objective.setMaximization();

        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                for (int d = 0; d < allDirections.length; d++) {
                    for (int k = 2; k <= MAX_K; k++) {
                        if (flipMoveVars[i][j][d][k] != null) {
                            objective.setCoefficient(flipMoveVars[i][j][d][k], k - 1); // k-1 flipped pieces
                        }
                    }
                }
            }
        }

        MPSolver.ResultStatus status = solver.solve();

        if (status == MPSolver.ResultStatus.OPTIMAL || status == MPSolver.ResultStatus.FEASIBLE) {
            for (int i = 0; i < Board.BOARD_SIZE; i++) {
                for (int j = 0; j < Board.BOARD_SIZE; j++) {
                    if (moveVars[i][j] != null && moveVars[i][j].solutionValue() > 0.5) {
                        return List.of(new int[] {i, j, 0});
                    }
                }
            }
        }

        System.out.println("No solution found");
        return new LinkedList<>();
    }

    private boolean isValidFlipLength(int i, int j, int dx, int dy, int k) {
        for (int n = 1; n <= k; n++) {
            int ni = i + n * dx;
            int nj = j + n * dy;

            if (ni < 0 || ni >= Board.BOARD_SIZE || nj < 0 || nj >= Board.BOARD_SIZE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds constraints to ensure a valid flip in the specified direction.
     */
    private void addFlipConstraints(int[][] boardMatrix, MPSolver solver, int i, int j, int dx, int dy, int k, MPVariable flipVar) {
        // Check if all cells from 1 to k-1 are opponent pieces
        for (int n = 1; n < k; n++) {
            int ni = i + n * dx;
            int nj = j + n * dy;

            if (boardMatrix[ni][nj] != OPP_COLOR) {
                // If any cell doesn't have opponent's piece, this flip is invalid
                MPConstraint c = solver.makeConstraint(0.0, 0.0);
                c.setCoefficient(flipVar, 1.0);
                return;
            }
        }

        // Check if cell k has my color
        int ki = i + k * dx;
        int kj = j + k * dy;

        if (boardMatrix[ki][kj] != MY_COLOR) {
            // If the last cell doesn't have my piece, this flip is invalid
            MPConstraint c = solver.makeConstraint(0.0, 0.0);
            c.setCoefficient(flipVar, 1.0);
        }
    }
}
