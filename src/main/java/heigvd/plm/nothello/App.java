package heigvd.plm.nothello;

import heigvd.plm.nothello.game.Board;
import heigvd.plm.nothello.game.PieceColor;

import heigvd.plm.nothello.gui.OthelloGUI;

import javax.swing.*;

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
        board.setColorAt(3, 2, PieceColor.BLACK);
        System.out.println(board);
        board.setColorAt(3, 1, PieceColor.WHITE);
        System.out.println(board);
        board.setColorAt(3, 0, PieceColor.BLACK);
        System.out.println(board);

        SwingUtilities.invokeLater(OthelloGUI::new);
    }
}
