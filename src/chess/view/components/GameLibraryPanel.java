package chess.view.components;

import chess.controller.ChessController;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GameLibraryPanel extends JPanel {
    private final ChessController controller;
    private final DefaultListModel<String> listModel;
    private final JList<String> gameList;

    public GameLibraryPanel(ChessController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        // Game list
        listModel = new DefaultListModel<>();
        gameList = new JList<>(listModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Toolbar buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(createButton("Load Game", this::loadGame));
        buttonPanel.add(createButton("Save Current", this::saveCurrentGame));
        buttonPanel.add(createButton("Delete Game", this::deleteGame));

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // Layout components
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(gameList), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshGameList();
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void refreshGameList() {
        listModel.clear();
        // Get game names from controller
        controller.getLibrarySavedGames().forEach(listModel::addElement);
    }

    private void loadGame(ActionEvent e) {
        String selected = gameList.getSelectedValue();
        if (selected != null) {
            controller.loadGameFromLibrary(selected);
        } else {
            JOptionPane.showMessageDialog(this, "No game selected", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrentGame(ActionEvent e) {
        String gameName = JOptionPane.showInputDialog(this, "Enter game name:");
        if (gameName != null && !gameName.trim().isEmpty()) {
            controller.saveCurrentGameToLibrary(gameName);
            refreshGameList();
        }
    }

    private void deleteGame(ActionEvent e) {
        String selected = gameList.getSelectedValue();
        if (selected != null) {
            controller.deleteGameFromLibrary(selected);
            refreshGameList();
        }
    }

    private class SearchDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) { filterList(); }
        @Override
        public void removeUpdate(DocumentEvent e) { filterList(); }
        @Override
        public void changedUpdate(DocumentEvent e) { filterList(); }

        private void filterList() {
            // Implement search filtering if needed
        }
    }
}
