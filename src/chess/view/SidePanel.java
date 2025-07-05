package chess.view;

import chess.controller.ChessController;
import chess.view.components.*;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.*;

public class SidePanel extends JPanel {
    private final GameLibraryPanel libraryPanel;
    private final MoveHistoryPanel historyPanel;

    public SidePanel(ChessController controller, boolean isHistoryPanel) {
        super(new BorderLayout());

        libraryPanel = new GameLibraryPanel(controller);
        historyPanel = new MoveHistoryPanel(controller);

        JTabbedPane tabbedPane = new JTabbedPane();
        if (isHistoryPanel) {
            // history focused
            tabbedPane.addTab("History", historyPanel);
            tabbedPane.addTab("Library", libraryPanel);
        } else {
            // library focused
            tabbedPane.addTab("Library", libraryPanel);
            tabbedPane.addTab("History", historyPanel);
        }

        add(tabbedPane, BorderLayout.CENTER);
        setPreferredSize(new Dimension(400, 400));
    }

    public void updateHistory() {
        historyPanel.update();
    }

    public void updateLibrary() {
        libraryPanel.refreshLibraryTree();
    }
}
