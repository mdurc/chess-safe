package chess.view;

import chess.controller.ChessController;
import chess.view.components.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

public class SidePanel extends JPanel {
    private final GameLibraryPanel libraryPanel;
    private final MoveHistoryPanel historyPanel;

    public SidePanel(ChessController controller) {
        super(new BorderLayout());

        libraryPanel = new GameLibraryPanel(controller);
        historyPanel = new MoveHistoryPanel(controller);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("History", historyPanel);
        tabbedPane.addTab("Library", libraryPanel);

        add(tabbedPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(500,400));
    }

    public void updateHistory() {
        historyPanel.update();
    }
}
