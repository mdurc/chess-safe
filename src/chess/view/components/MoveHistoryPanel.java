// MoveHistoryPanel.java
package chess.view.components;

import chess.controller.ChessController;
import javax.swing.*;

public class MoveHistoryPanel extends JScrollPane {
    private final JList<String> moveList;

    public MoveHistoryPanel(ChessController controller) {
        moveList = new JList<>(new DefaultListModel<>());
        setViewportView(moveList);
    }

    public void update() {
        // Update list from controller.getCurrentGame().getMoveHistory()
    }
}
