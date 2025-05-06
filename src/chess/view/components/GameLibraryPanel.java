package chess.view.components;

import chess.controller.ChessController;
import chess.model.ChessGame;
import chess.model.GameLibrary;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class GameLibraryPanel extends JPanel {
    private final ChessController controller;
    private final DefaultListModel<String> listModel;
    private final JList<String> gameList;

    public GameLibraryPanel(ChessController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        listModel = new DefaultListModel<>();
        gameList = new JList<>(listModel);
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
        buttonPanel.add(createButton("Load", this::loadGame));
        buttonPanel.add(createButton("Delete", this::deleteGame));
        buttonPanel.add(createButton("New", this::newGame));
        buttonPanel.add(createButton("Save Curr", this::saveCurrentGame));
        buttonPanel.add(createButton("Import", this::importPgn));

        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

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
        String gameName = controller.getCurrentGameName();
        if (gameName == null) {
            gameName = JOptionPane.showInputDialog(this, "Enter game name:");
        }
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

    private void newGame(ActionEvent e) {
        controller.startNewGame();
    }

    private void importPgn(ActionEvent e) {
        JDialog importDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Import PGN", true);
        importDialog.setLayout(new BorderLayout());

        JTextArea pgnTextArea = new JTextArea(10, 40);
        pgnTextArea.setLineWrap(true);
        pgnTextArea.setWrapStyleWord(true);

        JTextField nameField = new JTextField("Imported Game " + System.currentTimeMillis());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(new JLabel("PGN Text:"), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(pgnTextArea), BorderLayout.CENTER);

        JPanel namePanel = new JPanel(new BorderLayout());
        namePanel.add(new JLabel("Game Name:"), BorderLayout.WEST);
        namePanel.add(nameField, BorderLayout.CENTER);

        JButton importButton = new JButton("Import");
        importButton.addActionListener(ev -> {
            try {
                String pgn = pgnTextArea.getText().trim();
                String name = nameField.getText().trim();

                if (pgn.isEmpty()) {
                    JOptionPane.showMessageDialog(importDialog,
                        "Please enter PGN text", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(importDialog,
                        "Please enter a game name", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                ChessGame importedGame = GameLibrary.parsePgn(pgn);
                importedGame.setFilename(name);
                controller.saveGame(importedGame);
                refreshGameList();
                importDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(importDialog,
                    "Error importing PGN: " + ex.getMessage(),
                    "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(importButton);

        importDialog.add(inputPanel, BorderLayout.CENTER);
        importDialog.add(namePanel, BorderLayout.NORTH);
        importDialog.add(buttonPanel, BorderLayout.SOUTH);
        importDialog.pack();
        importDialog.setLocationRelativeTo(this);
        importDialog.setVisible(true);
    }

    private class SearchDocumentListener implements DocumentListener {
        @Override
        public void insertUpdate(DocumentEvent e) { filterList(); }
        @Override
        public void removeUpdate(DocumentEvent e) { filterList(); }
        @Override
        public void changedUpdate(DocumentEvent e) { filterList(); }

        private void filterList() {
            // TODO
        }
    }
}
