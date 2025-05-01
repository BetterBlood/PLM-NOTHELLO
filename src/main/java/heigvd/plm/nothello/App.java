package heigvd.plm.nothello;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.Color;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Board board = new Board();
        System.out.println("Initial board:");
        System.out.println(board);
        board.setColorAt(3, 2, Color.BLACK);
        System.out.println(board);
        board.setColorAt(3, 1, Color.WHITE);
        System.out.println(board);
        board.setColorAt(3, 0, Color.BLACK);
        System.out.println(board);
    }
}
