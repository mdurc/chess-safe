package chess.view;

import chess.controller.ChessController;
import chess.view.components.*;

import java.awt.BorderLayout;

import javax.swing.*;

public class SidePanel extends JPanel {
    private final GameLibraryPanel libraryPanel;
    private final MoveHistoryPanel historyPanel;
    private final AnnotationEditor annotationEditor;

    public SidePanel(ChessController controller) {
        super(new BorderLayout());

        libraryPanel = new GameLibraryPanel(controller);
        historyPanel = new MoveHistoryPanel(controller);
        annotationEditor = new AnnotationEditor(controller);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("History", historyPanel);
        tabbedPane.addTab("Library", libraryPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    public void updateHistory() {
        historyPanel.update();
    }
}
