package chess.view.components;

import chess.controller.ChessController;
import chess.model.ChessGame;
import chess.model.GameLibrary;
import chess.model.GameLibraryNode;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameLibraryPanel extends JPanel {
    private final ChessController controller;
    private final DefaultTreeModel treeModel;
    private final JTree gameTree;
    private final DefaultMutableTreeNode rootTreeNode;

    public GameLibraryPanel(ChessController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        rootTreeNode = new DefaultMutableTreeNode(controller.getLibPath());
        treeModel = new DefaultTreeModel(rootTreeNode);
        gameTree = new JTree(treeModel);
        gameTree.setRootVisible(true);
        gameTree.setShowsRootHandles(true);
        gameTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // distinguish file/directory icons in the library based on tree nodes
        gameTree.setCellRenderer(new GameLibraryTreeCellRenderer());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        buttonPanel.add(createButton("Load", this::loadGame));
        buttonPanel.add(createButton("Delete", this::deleteGame));
        buttonPanel.add(createButton("Start New Game", this::startNewGame));
        buttonPanel.add(createButton("Save Current", this::saveCurrentGame));
        buttonPanel.add(createButton("New Folder", this::createFolder));
        buttonPanel.add(createButton("Import", this::importPgn));

        JPanel searchPanel = new JPanel(new BorderLayout());
        JTextField searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(gameTree), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshGameTree();
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void refreshGameTree() {
        rootTreeNode.removeAllChildren();
        buildTreeFromNode(controller.getLibraryRootNode(), rootTreeNode);
        treeModel.reload();
        expandAllNodes();
    }

    private void buildTreeFromNode(GameLibraryNode node, DefaultMutableTreeNode treeNode) {
        for (GameLibraryNode child : node.getChildren()) {
            DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);
            treeNode.add(childTreeNode);
            if (child.isDirectory()) {
                buildTreeFromNode(child, childTreeNode);
            }
        }
    }

    private void expandAllNodes() {
        for (int i = 0; i < gameTree.getRowCount(); i++) {
            gameTree.expandRow(i);
        }
    }

    private GameLibraryNode getSelectedNode() {
        DefaultMutableTreeNode selectedNode =
            (DefaultMutableTreeNode) gameTree.getLastSelectedPathComponent();
        if (selectedNode != null &&
                selectedNode.getUserObject() instanceof GameLibraryNode) {
            return (GameLibraryNode) selectedNode.getUserObject();
        }
        return null;
    }

    private void loadGame(ActionEvent e) {
        GameLibraryNode selected = getSelectedNode();
        if (selected != null && selected.isPgnFile()) {
            controller.loadGameFromLibrary(selected.getRelativePath());
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a PGN file to load",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrentGame(ActionEvent e) {
        // saves the game to whatever the selected directory currently is
        GameLibraryNode selected = getSelectedNode();
        String targetPath = null;

        String gameName = controller.getCurrentGameName();
        if (gameName == null) {
            gameName = JOptionPane.showInputDialog(this, "Enter game name:");
        }

        boolean valid_name = gameName != null && !gameName.trim().isEmpty();

        if (selected != null && selected.isDirectory()) {
            // save to selected directory
            if (valid_name) {
                targetPath = selected.getRelativePath() + "/" + gameName;
            }
        } else if (valid_name) {
            // path is gamename, saving to lib path will be: game/name.
            // saving to root directory.
            targetPath = gameName;
        }
        if (targetPath != null) {
            controller.saveGameToLibraryPath(targetPath, controller.getCurrentGame());
            refreshGameTree();
        }
    }

    private void deleteGame(ActionEvent e) {
        GameLibraryNode selected = getSelectedNode();
        if (selected != null) {
            int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete '" + selected.getDisplayName() + "'?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if (selected.isDirectory()) {
                    controller.deleteDirectoryFromLibrary(selected.getRelativePath());
                } else {
                    controller.deleteGameFromLibrary(selected.getRelativePath());
                }
                refreshGameTree();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startNewGame(ActionEvent e) {
        controller.startNewGame();
    }

    private void createFolder(ActionEvent e) {
        GameLibraryNode selected = getSelectedNode();
        String parentPath = "";
        if (selected != null && selected.isDirectory()) {
            parentPath = selected.getRelativePath() + "/";
        }
        String folderName = JOptionPane.showInputDialog(this, "Enter folder name:");
        if (folderName != null && !folderName.trim().isEmpty()) {
            try {
                controller.createDirectoryInLibrary(parentPath + folderName);
                refreshGameTree();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error creating folder: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importPgn(ActionEvent e) {
        GameLibraryNode selected = getSelectedNode();
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
                String targetPath = "";
                if (selected != null && selected.isDirectory()) {
                    targetPath = selected.getRelativePath() + "/";
                }
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
                controller.saveGameToLibraryPath(targetPath + name, importedGame);
                refreshGameTree();
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

    private class SearchDocumentListener implements javax.swing.event.DocumentListener {
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTree(); }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTree(); }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTree(); }

        private void filterTree() {
            // todo: Implement tree filtering
        }
    }

    private static class GameLibraryTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel,
                                                     boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
                if (node.getUserObject() instanceof GameLibraryNode) {
                    GameLibraryNode gameNode = (GameLibraryNode) node.getUserObject();
                    setText(gameNode.getDisplayName());
                    if (gameNode.isDirectory()) {
                        setIcon(UIManager.getIcon("FileView.directoryIcon"));
                    } else if (gameNode.isPgnFile()) {
                        setIcon(UIManager.getIcon("FileView.fileIcon"));
                    }
                }
            }
            return this;
        }
    }
}
