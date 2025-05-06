package chess.view.components;

import chess.controller.ChessController;
import chess.model.GameNode;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.*;
import java.awt.event.MouseEvent;

public class MoveHistoryPanel extends JScrollPane {
    private final JList<MoveEntry> moveList;
    private final ChessController controller;
    private final DefaultListModel<MoveEntry> listModel;
    private JTextField currentEditor;

    private record MoveEntry(GameNode node, String notation, int depth, String comment) {}

    public MoveHistoryPanel(ChessController controller) {
        this.controller = controller;
        this.listModel = new DefaultListModel<>();
        this.moveList = new JList<>(listModel);

        moveList.setCellRenderer(new MoveListRenderer());
        moveList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    startInlineEditing(e.getPoint());
                }
            }
        });
        moveList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                MoveEntry entry = moveList.getSelectedValue();
                if (entry != null) {
                    // callback to controller
                    controller.setCurrentPosition(entry.node());
                }
            }
            controller.focusBoard();
        });
        setViewportView(moveList);
    }

    public void update() {
        listModel.clear();
        traverseGameTree(controller.getCurrentGame().getFirstPosition(), 0, 1, true);
        moveList.repaint();
    }

    private void startInlineEditing(Point clickLocation) {
        int index = moveList.locationToIndex(clickLocation);
        if (index == -1) return;

        MoveEntry entry = listModel.get(index);
        Rectangle cellBounds = moveList.getCellBounds(index, index);

        currentEditor = new JTextField(entry.comment());
        currentEditor.setFont(moveList.getFont());
        currentEditor.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        currentEditor.setBounds(0, cellBounds.y, moveList.getWidth(), cellBounds.height);

        currentEditor.addActionListener(e -> finishEditing(index));
        currentEditor.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                finishEditing(index);
            }
        });

        moveList.add(currentEditor);
        currentEditor.requestFocus();
        moveList.repaint();
    }

    private void finishEditing(int index) {
        if (currentEditor == null) return;

        String newComment = currentEditor.getText().trim();
        MoveEntry entry = listModel.get(index);
        entry.node().setComment(newComment);

        moveList.remove(currentEditor);
        currentEditor = null;
        update();
    }

    private void traverseGameTree(GameNode node, int depth, int moveNumber, boolean isWhiteTurn) {
        if (node == null) return;

        if (node.getMove() != null) {
            String prefix = createMovePrefix(moveNumber, isWhiteTurn);
            listModel.addElement(new MoveEntry(node, prefix + node.getNotation(), depth, node.getComment()));
            if (isWhiteTurn) {
                isWhiteTurn = false;
            } else {
                moveNumber++;
                isWhiteTurn = true;
            }
        }

        List<GameNode> children = node.getChildren();
        if (!children.isEmpty()) {
            for (int i = 1; i < children.size(); i++) {
                GameNode variation = children.get(i);
                addVariation(variation, depth + 1, moveNumber, !isWhiteTurn);
            }

            // process main line last
            traverseGameTree(children.get(0), depth, moveNumber, isWhiteTurn);
        }
    }

    private void addVariation(GameNode node, int depth, int moveNumber, boolean isWhiteTurn) {
        String prefix = createMovePrefix(moveNumber, isWhiteTurn);
        listModel.addElement(new MoveEntry(node, "(" + prefix + node.getNotation(), depth, node.getComment()));
        List<GameNode> children = node.getChildren();
        if (!children.isEmpty()) {
            for (int i = 1; i < children.size(); i++) {
                addVariation(children.get(i), depth + 1, moveNumber, !isWhiteTurn);
            }
            // process variation's main line
            traverseGameTree(children.get(0), depth + 1, moveNumber, isWhiteTurn);
        }
    }

    private String createMovePrefix(int moveNumber, boolean isWhiteTurn) {
        return isWhiteTurn ? moveNumber + ". " : moveNumber + "... ";
    }

    private class MoveListRenderer extends DefaultListCellRenderer {
        private final Color VARIATION_COLOR = new Color(100, 100, 100);
        private final int INDENT_SIZE = 20;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            MoveEntry entry = (MoveEntry) value;
            String text = entry.notation();

            if (entry.comment() != null && !entry.comment().isEmpty()) {
                text += "  | " + entry.comment();
            }

            JLabel label = (JLabel) super.getListCellRendererComponent(list, entry.notation(), index, isSelected, cellHasFocus);

            label.setBorder(BorderFactory.createEmptyBorder(0, INDENT_SIZE * entry.depth(), 0, 0));
            label.setFont(new Font("SansSerif", Font.PLAIN, 12));

            if (isSelected) {
                label.setBackground(new Color(180, 200, 255));
            }

            if (entry.depth() > 0) {
                label.setForeground(VARIATION_COLOR);
                label.setFont(label.getFont().deriveFont(Font.ITALIC));
            } else {
                label.setForeground(Color.BLACK);
            }

            if (text.contains("|")) {
                String[] parts = text.split("\\|");
                label.setText("<html>" + parts[0] +
                    "<font color='#666666'><i>" + "|" + parts[1] + "</i></font></html>");
            }

            if (entry.node().equals(controller.getCurrentPosition())) {
                label.setBackground(new Color(240, 240, 255));
                label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.BLUE, 1),
                    label.getBorder()
                ));
            }
            return label;
        }
    }
}
