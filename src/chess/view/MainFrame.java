package chess.view;

import chess.controller.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

public class MainFrame extends JFrame {
    private static final int TILE_SIZE = 80;
    private final BoardPanel boardPanel;
    private final SidePanel leftPanel;
    private final SidePanel rightPanel;

    public MainFrame(ChessController controller) {
        setTitle("chess");
        setLayout(new BorderLayout());

        this.boardPanel = new BoardPanel(controller, TILE_SIZE);
        this.leftPanel = new SidePanel(controller, true);
        this.rightPanel = new SidePanel(controller, false);

        add(leftPanel, BorderLayout.WEST);
        add(boardPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        int width = 1445;
        int height = 800;

        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setPreferredSize(new Dimension(width, height));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // start with the board as the focus so that keypresses are registered for it
        focusBoard();
    }
    public void focusBoard() { boardPanel.requestFocusInWindow(); }
    public void updateBoard() { boardPanel.repaint(); }
    public void updateHistory() { leftPanel.updateHistory(); rightPanel.updateHistory(); }
    public void updateLibrary() { leftPanel.updateLibrary(); rightPanel.updateLibrary(); }
    public void flipBoard() { boardPanel.flipBoard(); }
    public int getTileSize() { return TILE_SIZE; }
}
