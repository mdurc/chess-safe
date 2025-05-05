package chess.view;

import chess.controller.ChessController;
import chess.view.components.*;
import javax.swing.*;

public class SidePanel extends JTabbedPane {
    private final GameLibraryPanel libraryPanel;
    private final MoveHistoryPanel historyPanel;
    private final AnnotationEditor annotationEditor;

    public SidePanel(ChessController controller) {
        libraryPanel = new GameLibraryPanel(controller);
        historyPanel = new MoveHistoryPanel(controller);
        annotationEditor = new AnnotationEditor(controller);

        addTab("History", historyPanel);
        addTab("Annotations", annotationEditor);
        addTab("Library", libraryPanel);
    }
    public void updateHistory() { historyPanel.update(); }
}
