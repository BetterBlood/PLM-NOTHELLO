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

public class NotHelloConstraintStrategy implements NotHelloStrategy {

    public static final int MY_COLOR = -1;
    public static final int OPP_COLOR = 1;
    public static final int EMPTY = 0;

    /**
     * Calcule le meilleur mouvement possible pour le joueur courant.
     * Le meilleur mouvement est celui qui maximise le nombre de pièces adverses retournées.
     *
     * @param board Le plateau de jeu actuel
     * @return un tableau des coordonnées [x, y] du meilleur coup, ou null si aucun coup n'est possible.
     */
    @Override
    public int[] evaluate(Board board) {
        Loader.loadNativeLibraries();

        CpModel model = new CpModel();

        Direction[] allDirections = Direction.getAllDirections();
        int[][] boardMatrix = board.getBoardMatrix(board.getPlayerTurn());
        final int MAX_K = Board.BOARD_SIZE - 1;

        // Variables de décision:
        // - moveVars[x][y]: 1 si le mouvement (x,y) est joué, 0 sinon
        // - flipVars[x][y][direction][k]: 1 si le mouvement (x,y) retourne k-1 pièces dans la direction d, 0 sinon
        // - flipMoveVars[x][y][direction][k]: 1 si le mouvement (x,y) retourne k-1 dans la direction et est joué, 0 sinon
        IntVar[][] moveVars = new IntVar[Board.BOARD_SIZE][Board.BOARD_SIZE];
        IntVar[][][][] flipVars = new IntVar[Board.BOARD_SIZE][Board.BOARD_SIZE][allDirections.length][MAX_K + 1];
        IntVar[][][][] flipMoveVars = new IntVar[Board.BOARD_SIZE][Board.BOARD_SIZE][allDirections.length][MAX_K + 1];

        IntVar[] allMoveVars = new IntVar[Board.BOARD_SIZE * Board.BOARD_SIZE];
        int moveVarCount = 0;

        LinearExpr objectiveExpr = LinearExpr.constant(0);

        for (int i = 0; i < Board.BOARD_SIZE; i++) {
            for (int j = 0; j < Board.BOARD_SIZE; j++) {
                if (boardMatrix[i][j] == EMPTY) {
                    String moveVarName = String.format("move_%d_%d", i, j);
                    moveVars[i][j] = model.newBoolVar(moveVarName);
                    allMoveVars[moveVarCount++] = moveVars[i][j];

                    LinearExpr validDirectionsExpr = LinearExpr.constant(0);

                    for (int d = 0; d < allDirections.length; d++) {
                        Direction dir = allDirections[d];

                        for (int k = 2; k <= MAX_K; k++) {
                            if (isValidFlipLength(i, j, dir.getX(), dir.getY(), k)) {
                                String flipVarName = String.format("flip_%d_%d_%d_%d", i, j, d, k);
                                flipVars[i][j][d][k] = model.newBoolVar(flipVarName);

                                String flipMoveVarName = String.format("flipmove_%d_%d_%d_%d", i, j, d, k);
                                flipMoveVars[i][j][d][k] = model.newBoolVar(flipMoveVarName);

                                model.addLessOrEqual(flipMoveVars[i][j][d][k], flipVars[i][j][d][k]);
                                model.addLessOrEqual(flipMoveVars[i][j][d][k], moveVars[i][j]);

                                model.addGreaterOrEqual(
                                        LinearExpr.sum(new LinearExpr[]{
                                                LinearExpr.term(flipMoveVars[i][j][d][k], 1),
                                                LinearExpr.constant(1)
                                        }),
                                        LinearExpr.sum(new IntVar[]{flipVars[i][j][d][k], moveVars[i][j]})
                                );

                                addFlipConstraints(boardMatrix, model, i, j, dir.getX(), dir.getY(), k, flipVars[i][j][d][k]);

                                validDirectionsExpr = LinearExpr.sum(new LinearExpr[]{
                                        validDirectionsExpr,
                                        LinearExpr.term(flipVars[i][j][d][k], 1)
                                });

                                // Ajouter à l'objectif, chaque flip ajoute k-1 points à la fct objectif
                                objectiveExpr = LinearExpr.sum(new LinearExpr[]{
                                        objectiveExpr,
                                        LinearExpr.term(flipMoveVars[i][j][d][k], k - 1)
                                });
                            }
                        }
                    }

                    List<IntVar> validFlipsForThisMove = new ArrayList<>();
                    for (int d = 0; d < allDirections.length; d++) {
                        for (int k = 2; k <= MAX_K; k++) {
                            if (flipVars[i][j][d][k] != null) {
                                validFlipsForThisMove.add(flipVars[i][j][d][k]);
                            }
                        }
                    }

                    if (!validFlipsForThisMove.isEmpty()) {
                        IntVar[] flipVarsArray = validFlipsForThisMove.toArray(new IntVar[0]);

                        // moveVars[i][j] = 1 si la somme des flips valides (en boolean) east >= 1
                        model.addMaxEquality(moveVars[i][j], flipVarsArray);
                    }
                    else {
                        // Si pas de flips, alors le mouvement n'est pas valide
                        model.addEquality(moveVars[i][j], 0);
                    }
                }
            }
        }

        // Ajoute la contrainte d'unicité du mouvement
        if (moveVarCount > 0) {
            IntVar[] actualMoveVars = new IntVar[moveVarCount];
            System.arraycopy(allMoveVars, 0, actualMoveVars, 0, moveVarCount);
            model.addEquality(LinearExpr.sum(actualMoveVars), 1);

            model.maximize(objectiveExpr);

            CpSolver solver = new CpSolver();
            CpSolverStatus status = solver.solve(model);

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
        return null;
    }

    /**
     * Vérifie si un flip de longueur k est valide à partir de la position (i,j) dans la direction (dx,dy).
     *
     * @param i coordonnée de la ligne
     * @param j coordonnée de la colonne
     * @param dx direction selon x
     * @param dy direction selon y
     * @param k distance du flip
     * @return true si le flip de longueur k est valide, false sinon
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
     * Ajoute les contraintes pour vérifier si un flip est valide dans une direction donnée avec une distance k.
     *
     * @param boardMatrix Le plateau de jeu sous forme de matrice
     * @param model le modèle
     * @param i coordonnée de la ligne
     * @param j coordonnée de la colonne
     * @param dx direction selon x
     * @param dy direction selon y
     * @param k distance du flip
     * @param flipVar La variable de flip à contraindre
     */
    private void addFlipConstraints(int[][] boardMatrix, CpModel model, int i, int j, int dx, int dy, int k, IntVar flipVar) {
        // Les cellules de 1 à k-1 doivent être des pièces de l'adversaire
        for (int n = 1; n < k; n++) {
            if (boardMatrix[i + n * dx][j + n * dy] != OPP_COLOR) {
                model.addEquality(flipVar, 0);
                return;
            }
        }

        // La cellule à distance k doit être une pièce du joueur actuel
        if (boardMatrix[i + k * dx][j + k * dy] != MY_COLOR) {
            model.addEquality(flipVar, 0);
        }
    }
}