package chess.model;

import java.util.ArrayList;
import java.util.List;

// Represents a node in the game library file tree, either a diretory or pgn file
public class GameLibraryNode {
    private final String name;
    private final String fullPath;
    private final boolean isDirectory;
    private final List<GameLibraryNode> children;

    public GameLibraryNode(String name, String fullPath, boolean isDirectory) {
        this.name = name;
        this.fullPath = fullPath;
        this.isDirectory = isDirectory;
        this.children = new ArrayList<>();
    }

    public String getName() { return name; }

    public String getFullPath() { return fullPath; }

    public boolean isDirectory() { return isDirectory; }

    public boolean isPgnFile() {
        return !isDirectory && name.toLowerCase().endsWith(".pgn");
    }

    public List<GameLibraryNode> getChildren() { return children; }

    public void addChild(GameLibraryNode child) { children.add(child); }

    public void removeChild(GameLibraryNode child) { children.remove(child); }

    public String getDisplayName() {
        if (isDirectory) {
            return name + "/";
        }
        return name;
    }

    public String getRelativePath() {
        String relativePath = fullPath.replace("games/", "");
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }
        return relativePath;
    }

    @Override
    public String toString() { return getDisplayName(); }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof GameLibraryNode)) return false;
        GameLibraryNode g = (GameLibraryNode) o;
        return fullPath.equals(g.fullPath);
    }

    @Override
    public int hashCode() { return fullPath.hashCode(); }
}
