package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;

import java.util.LinkedList;
import java.util.List;

public class NotHelloMaxFlipsStrategy implements NotHelloStrategy {
    @Override
    public List<int[]> evaluate(Board board, int depth) {
        List<int[]> result = new LinkedList<>();
        System.out.println("NotHelloMaxFlipsStrategy");
        for (Integer i = 0; i < Integer.MAX_VALUE/3; ++i){}
        // TODO: Implémenter la stratégie: retourne le nombre de pions retournés par ce coup
        return result;
    }
}
