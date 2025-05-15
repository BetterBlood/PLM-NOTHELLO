package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;

import java.util.LinkedList;
import java.util.List;

public class NotHelloMinOpponentFlipsStrategy implements NotHelloStrategy {
    @Override
    public List<int[]> evaluate(Board board, int depth) {
        List<int[]> result = new LinkedList<>();
        System.out.println("NotHelloMinOpponentFlipsStrategy");
        for (Integer i = 0; i < Integer.MAX_VALUE/4; ++i){}
        // TODO: Implémenter la stratégie: simule le coup et retourne le max de flips possibles pour l'adversaire ensuite
        return result;
    }
}
