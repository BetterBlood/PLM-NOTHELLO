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

public class NotHelloConstraintStrategy implements NotHelloStrategy {
    // Conventions utilisées dans les stratégies :
    public static final int MY_COLOR = -1;
    public static final int OPP_COLOR = 1;
    public static final int EMPTY = 0;

    public int[] evaluate(Board board) {

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
                    String moveVarName = "move_" + i + "_" + j;
                    moveVars[i][j] = solver.makeBoolVar(moveVarName);

                    // Pour chaque direction et distance k, on crée les variables de flip
                    for (int d = 0; d < allDirections.length; d++) {
                        for (int k = 2; k <= MAX_K; k++) {
                            // Pour l'efficacité, on vérifie si la longueur de flip est valide
                            if (isValidFlipLength(i, j, allDirections[d].getX(), allDirections[d].getY(), k)) {
                                String flipVarName = "flip_" + i + "_" + j + "_" + d + "_" + k;
                                flipVars[i][j][d][k] = solver.makeBoolVar(flipVarName);

                                String flipMoveVarName = "flipmove_" + i + "_" + j + "_" + d + "_" + k;
                                flipMoveVars[i][j][d][k] = solver.makeBoolVar(flipMoveVarName);

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

                                addFlipConstraints(boardMatrix, solver, i, j, allDirections[d].getX(), allDirections[d].getY(), k, flipVars[i][j][d][k]);
                            }
                        }
                    }
                }
            }
        }

        // Contrainte: un mouvement est valide ssi au moins une direction est valide
        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (boardMatrix[i][j] == EMPTY && moveVars[i][j] != null) {
                    MPConstraint validMove = solver.makeConstraint(0.0, 0.0);
                    validMove.setCoefficient(moveVars[i][j], 1.0);

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

        // Contrainte: Il faut effectuer exactement 1 mouvement
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
                            objective.setCoefficient(flipMoveVars[i][j][d][k], k - 1);
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
                        return new int[] {i, j};
                    }
                }
            }
        }

        System.out.println("Aucun mouvement valide trouvé.");
        return new int[] {};
    }

    private boolean isValidFlipLength(int i, int j, int dx, int dy, int k) {
        for (int n = 1; n <= k; n++) {

            if (
                    i + n * dx < 0 ||
                    i + n * dx >= Board.BOARD_SIZE ||
                    j + n * dy < 0 ||
                    j + n * dy >= Board.BOARD_SIZE
            ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ajoute les contraintes de flip pour un coup donné dans une direction donnée.
     */
    private void addFlipConstraints(int[][] boardMatrix, MPSolver solver, int i, int j, int dx, int dy, int k, MPVariable flipVar) {
        // Check toutes les cellules de 1 à k-1
        for (int n = 1; n < k; n++) {

            if (boardMatrix[i + n * dx][j + n * dy] != OPP_COLOR) {
                // Si aucune cellule n'est un adversaire, cette contrainte est invalide
                MPConstraint c = solver.makeConstraint(0.0, 0.0);
                c.setCoefficient(flipVar, 1.0);
                return;
            }
        }

        // Check la cellule k
        if (boardMatrix[i + k * dx][j + k * dy] != MY_COLOR) {
            // Si la cellule k n'est pas de ma couleur, cette contrainte est invalide
            MPConstraint c = solver.makeConstraint(0.0, 0.0);
            c.setCoefficient(flipVar, 1.0);
        }
    }
}
