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
    private final JTree libraryTree;
    private final DefaultMutableTreeNode rootTreeNode;
    private JTextField searchField;

    public GameLibraryPanel(ChessController controller) {
        this.controller = controller;
        setLayout(new BorderLayout());

        rootTreeNode = new DefaultMutableTreeNode(controller.getLibPath());
        treeModel = new DefaultTreeModel(rootTreeNode);
        libraryTree = new JTree(treeModel);
        libraryTree.setRootVisible(true);
        libraryTree.setShowsRootHandles(true);
        libraryTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // distinguish file/directory icons in the library based on tree nodes
        libraryTree.setCellRenderer(new GameLibraryTreeCellRenderer());

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        buttonPanel.add(createButton("Load", this::loadGame));
        buttonPanel.add(createButton("Delete", this::deleteGame));
        buttonPanel.add(createButton("New Folder", this::createFolder));
        buttonPanel.add(createButton("Import", this::importPgn));

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField();
        searchField.getDocument().addDocumentListener(new SearchDocumentListener());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(libraryTree), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        refreshLibraryTree();
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    public void refreshLibraryTree() {
        rootTreeNode.removeAllChildren();
        buildTreeFromNode(controller.getLibraryRootNode(), rootTreeNode);
        treeModel.reload();
        // Expand all directories
        //for (int i = 0; i < libraryTree.getRowCount(); i++) {
        //    libraryTree.expandRow(i);
        //}
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

    private GameLibraryNode getSelectedNode() {
        DefaultMutableTreeNode selectedNode =
            (DefaultMutableTreeNode) libraryTree.getLastSelectedPathComponent();
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
                refreshLibraryTree();
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select an item to delete",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
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
                refreshLibraryTree();
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
                controller.saveToLibrary(targetPath + name, importedGame);
                refreshLibraryTree();
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
        private javax.swing.Timer searchTimer;

        public SearchDocumentListener() {
            searchTimer = new javax.swing.Timer(300, e -> filterTree());
            searchTimer.setRepeats(false);
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { searchTimer.restart(); }

        private void filterTree() {
            String searchText = searchField.getText().toLowerCase().trim();
            if (searchText.isEmpty()) {
                treeModel.setRoot(rootTreeNode);
                treeModel.reload();
                return;
            }

            // create a filtered tree
            DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode(controller.getLibPath());
            filterNodeRecursively(controller.getLibraryRootNode(), filteredRoot, searchText);

            // update the tree model
            treeModel.setRoot(filteredRoot);
            treeModel.reload();

            // expand all nodes to show results
            expandAllForSearch(libraryTree, new TreePath(filteredRoot));
        }

        private boolean filterNodeRecursively(GameLibraryNode sourceNode, DefaultMutableTreeNode targetNode, String searchText) {
            boolean hasMatch = false;
            for (GameLibraryNode child : sourceNode.getChildren()) {
                boolean childMatches = false;
                // check if this node matches the search
                if (child.getName().toLowerCase().contains(searchText)) {
                    childMatches = true;
                }
                // for directories, check if any children match
                if (child.isDirectory()) {
                    DefaultMutableTreeNode childTreeNode = new DefaultMutableTreeNode(child);
                    boolean childrenMatch = filterNodeRecursively(child, childTreeNode, searchText);
                    if (childrenMatch) {
                        targetNode.add(childTreeNode);
                        hasMatch = true;
                    }
                } else if (childMatches) {
                    // for files, add if they match
                    targetNode.add(new DefaultMutableTreeNode(child));
                    hasMatch = true;
                }
            }
            return hasMatch;
        }
        private void expandAllForSearch(JTree tree, TreePath parent) {
            TreeNode node = (TreeNode) parent.getLastPathComponent();
            if (node.getChildCount() >= 0) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    TreePath path = parent.pathByAddingChild(node.getChildAt(i));
                    expandAllForSearch(tree, path);
                }
            }
            tree.expandPath(parent);
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
