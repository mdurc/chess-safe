package chess.view.components;

import chess.controller.ChessController;
import chess.model.GameNode;
import chess.model.ChessGame;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.util.Map;

public class MoveHistoryPanel extends JPanel {
    private final JList<MoveEntry> moveList;
    private final ChessController controller;
    private final DefaultListModel<MoveEntry> listModel;
    private JTextField currentEditor;
    private JLabel gameInfoLabel;

    private record MoveEntry(GameNode node, String notation, int depth, String comment) {}

    public MoveHistoryPanel(ChessController controller) {
        this.controller = controller;
        this.listModel = new DefaultListModel<>();
        this.moveList = new JList<>(listModel);

        setLayout(new BorderLayout());

        // game info panel at top
        createGameInfoPanel();

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

        JPanel buttonPanel = createButtonPanel();

        add(new JScrollPane(moveList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void createGameInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Game Info"));

        gameInfoLabel = new JLabel("New Game");
        gameInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton editTagsButton = new JButton("Edit Tags");
        editTagsButton.addActionListener(e -> showGameTagsDialog());

        infoPanel.add(gameInfoLabel, BorderLayout.CENTER);
        infoPanel.add(editTagsButton, BorderLayout.EAST);

        add(infoPanel, BorderLayout.NORTH);
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        buttonPanel.add(createButton("New Game", this::startNewGame));
        buttonPanel.add(createButton("Save", this::saveGame));
        buttonPanel.add(createButton("Save As", this::saveGameAs));
        buttonPanel.add(createButton("Copy PGN", this::copyPgnToClipboard));
        buttonPanel.add(createButton("Set Result", this::setGameResult));
        buttonPanel.add(createButton("Add ECO", this::addEcoCode));
        return buttonPanel;
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void startNewGame(ActionEvent e) {
        controller.startNewGame();
        updateGameInfo();
    }
    private void saveGame(ActionEvent e) { controller.saveCurrentGame(); }
    private void saveGameAs(ActionEvent e) { controller.saveGameAs(); }
    private void setGameResult(ActionEvent e) { showGameResultDialog(); }
    private void addEcoCode(ActionEvent e) { showEcoCodeDialog(); }

    private void copyPgnToClipboard(ActionEvent e) {
        try {
            ChessGame game = controller.getCurrentGame();
            String pgn = chess.model.GameLibrary.generatePGNString(game);

            // copy to clipboard
            java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
            java.awt.datatransfer.Clipboard clipboard = toolkit.getSystemClipboard();
            java.awt.datatransfer.StringSelection selection =
                new java.awt.datatransfer.StringSelection(pgn);
            clipboard.setContents(selection, selection);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error copying PGN: " + ex.getMessage(),
                "Copy Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showGameTagsDialog() {
        ChessGame game = controller.getCurrentGame();
        Map<String, String> tags = game.getTags();

        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Edit Game Tags", true);
        dialog.setLayout(new BorderLayout());

        JPanel tagsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        tagsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // add the common tags
        String[] tagNames = {"Event", "Site", "Date", "Round", "White", "Black", "Result", "ECO"};
        JTextField[] tagFields = new JTextField[tagNames.length];

        for (int i = 0; i < tagNames.length; i++) {
            tagsPanel.add(new JLabel(tagNames[i] + ":"));
            tagFields[i] = new JTextField(tags.getOrDefault(tagNames[i], ""), 20);
            tagsPanel.add(tagFields[i]);
        }

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            for (int i = 0; i < tagNames.length; i++) {
                String value = tagFields[i].getText().trim();
                if (!value.isEmpty()) {
                    game.setTag(tagNames[i], value);
                } else {
                    game.removeTag(tagNames[i]);
                }
            }
            updateGameInfo();
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(tagsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void showGameResultDialog() {
        String[] options = {"1-0 (White wins)", "0-1 (Black wins)", "1/2-1/2 (Draw)", "* (Unfinished)"};
        int result = JOptionPane.showOptionDialog(this,
            "Select game result:",
            "Set Game Result",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[3]);

        if (result != JOptionPane.CLOSED_OPTION) {
            String resultValue = "";
            switch (result) {
                case 0: resultValue = "1-0"; break;
                case 1: resultValue = "0-1"; break;
                case 2: resultValue = "1/2-1/2"; break;
                case 3: resultValue = "*"; break;
            }
            controller.getCurrentGame().setTag("Result", resultValue);
            updateGameInfo();
        }
    }

    private void showEcoCodeDialog() {
        ChessGame game = controller.getCurrentGame();
        String currentEco = game.getTag("ECO");

        // try to auto-detect ECO code from moves
        String detectedEco = controller.detectEcoFromMoves(game);

        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "ECO Code", true);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField ecoField = new JTextField(currentEco != null ? currentEco : "", 10);
        JLabel detectedLabel = new JLabel("Detected: " + (detectedEco != null ? detectedEco : "None"));
        JLabel openingLabel = new JLabel("Opening: " + (detectedEco != null ?
            chess.model.util.EcoDatabase.getOpeningName(detectedEco) : "Unknown"));

        panel.add(new JLabel("ECO Code:"));
        panel.add(ecoField);
        panel.add(detectedLabel);
        panel.add(openingLabel);

        JButton autoDetectButton = new JButton("Auto-Detect");
        autoDetectButton.addActionListener(e -> {
            if (detectedEco != null) {
                ecoField.setText(detectedEco);
                openingLabel.setText("Opening: " + chess.model.util.EcoDatabase.getOpeningName(detectedEco));
            }
        });

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> {
            String ecoCode = ecoField.getText().trim().toUpperCase();
            if (!ecoCode.isEmpty()) {
                game.setTag("ECO", ecoCode);
                updateGameInfo();
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(autoDetectButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void updateGameInfo() {
        ChessGame game = controller.getCurrentGame();
        Map<String, String> tags = game.getTags();

        StringBuilder info = new StringBuilder();
        info.append("<html><b>"); // make it bold formatting

        String white = tags.getOrDefault("White", "Unknown");
        String black = tags.getOrDefault("Black", "Unknown");
        String result = tags.getOrDefault("Result", "*");
        String eco = tags.getOrDefault("ECO", "");

        info.append(white).append(" vs ").append(black);
        info.append("</b><br>");
        info.append("Result: ").append(result);

        if (!eco.isEmpty()) {
            String openingName = chess.model.util.EcoDatabase.getOpeningName(eco);
            info.append(" | ECO: ").append(eco).append(" (").append(openingName).append(")");
        }

        String event = tags.getOrDefault("Event", "");
        if (!event.isEmpty()) {
            info.append("<br>Event: ").append(event);
        }

        info.append("</html>");

        gameInfoLabel.setText(info.toString());
    }

    public void update() {
        listModel.clear();
        traverseGame(controller.getCurrentGame().getFirstPosition(), 0, 1, true);
        moveList.repaint();
        updateGameInfo();
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

    private void traverseGame(GameNode node, int depth, int moveNumber, boolean isWhiteTurn) {
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
            traverseGame(children.get(0), depth, moveNumber, isWhiteTurn);
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
            traverseGame(children.get(0), depth + 1, moveNumber, isWhiteTurn);
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
