package heigvd.plm.nothello.logic;

import heigvd.plm.nothello.game.Board;

import java.util.LinkedList;
import java.util.List;

public class NotHelloPlacementPriorityStrategy implements NotHelloStrategy {
    @Override
    public List<int[]> evaluate(Board board, int depth, NotHelloStrategy strategy) {
        List<int[]> result = new LinkedList<>();
        System.out.println("NotHelloPlacementPriorityStrategy");
        for (Integer i = 0; i < Integer.MAX_VALUE; ++i){}
        // TODO: Implémenter la stratégie: priorité aux coins, pénaliser les bords dangereux, etc.
        return result;
    }
}
