// MainFrame.java
package chess.view;

import chess.controller.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

public class MainFrame extends JFrame {
    private static final int TILE_SIZE = 80;
    private final BoardPanel boardPanel;
    private final SidePanel sidePanel;

    public MainFrame(ChessController controller) {
        setTitle("chess");
        setLayout(new BorderLayout());

        this.boardPanel = new BoardPanel(controller, TILE_SIZE);
        this.sidePanel = new SidePanel(controller);

        add(boardPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(new Dimension(1200, 800));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // start with the board as the focus so that keypresses are registered for it
        focusBoard();
    }
    public void focusBoard() { boardPanel.requestFocusInWindow(); }
    public void updateBoard() { boardPanel.repaint(); }
    public void updateHistory() { sidePanel.updateHistory(); }
    public void flipBoard() { boardPanel.flipBoard(); }
    public int getTileSize() { return TILE_SIZE; }
}
