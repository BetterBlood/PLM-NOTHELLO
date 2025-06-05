package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;

import java.util.LinkedList;
import java.util.List;

public class NotHelloMinOpponentOpportunitiesStrategy implements NotHelloStrategy {
    @Override
    public List<int[]> evaluate(Board board, int depth, NotHelloStrategy strategy) {
        List<int[]> result = new LinkedList<>();
        System.out.println("NotHelloMinOpponentOpportunitiesStrategy");
        for (Integer i = 0; i < Integer.MAX_VALUE/2; ++i){}
        // TODO: Implémenter la stratégie: minimiser le nombre de coups valides de l'adversaire après ce coup
        return result;
    }
}
