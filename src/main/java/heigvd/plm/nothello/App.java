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
        SwingUtilities.invokeLater(OthelloGUI::new);
    }
}
