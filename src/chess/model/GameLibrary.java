package chess.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import chess.model.util.*;

public class GameLibrary {
    private static final String LIB_DIR = "games/";
    private final Map<String, String> gameFilePaths = new HashMap<>();  // path -> file path
    private final Map<String, ChessGame> loadedGames = new HashMap<>(); // path -> loaded game (for caching)
    private final GameLibraryNode rootNode;

    public GameLibrary() {
        rootNode = new GameLibraryNode("games", LIB_DIR, true);
        loadSavedGames();
    }

    public String getLib() { return LIB_DIR; }

    public List<String> getSavedGames() {
        return new ArrayList<>(gameFilePaths.keySet());
    }

    public GameLibraryNode getRootNode() { return rootNode; }

    public void createDirectory(String path) throws IOException {
        Path fullPath = Paths.get(LIB_DIR, path);
        Files.createDirectories(fullPath);
        updateFileTree();
    }

    public void saveGameToLibPath(String path, ChessGame game) throws IOException {
        if (!path.endsWith(".pgn")) path += ".pgn";
        Path fullPath = Paths.get(LIB_DIR, path);
        Files.createDirectories(fullPath.getParent());

        String pgn = generatePGNString(game);
        Files.writeString(fullPath, pgn);

        gameFilePaths.put(path, fullPath.toString());
        loadedGames.put(path, game); // cache the newly saved game
        updateFileTree();
    }

    public static String generatePGNString(ChessGame game) {
        StringBuilder sb = new StringBuilder();
        appendHeaders(sb, game);
        sb.append("\n");

        GameNode root = game.getFirstPosition();
        // not white turn, as this is acting as the root (before first move).
        // skip the current (blank root) move
        appendMoves(root, 1, false, sb, false, false, true);

        return sb.toString();
    }

    private static void appendHeaders(StringBuilder sb, ChessGame game) {
        String[] mandatoryTags = {"Event", "Site", "Date", "Round", "White", "Black", "Result"};
        for (String tag : mandatoryTags) {
            String value = game.getTag(tag) != null ? game.getTag(tag) : "?";
            sb.append(String.format("[%s \"%s\"]\n", tag, value));
        }
        for (Map.Entry<String, String> entry : game.getTags().entrySet()) {
            String key = entry.getKey();
            if (!Arrays.asList(mandatoryTags).contains(key)) {
                sb.append(String.format("[%s \"%s\"]\n", key, entry.getValue()));
            }
        }
    }

    private static void appendMoves(GameNode node, int moveNumber,
                                    boolean isWhiteTurn, StringBuilder sb,
                                    boolean isFirstInVariation,
                                    boolean onlyFirstMove,
                                    boolean skipCurrentMove) {
        if (node == null) return;

        // add move number before white's move, or "..." for black's move in variations
        if (!skipCurrentMove) {
            if (isWhiteTurn) {
                sb.append(moveNumber).append(". ");
            } else if (isFirstInVariation) {
                sb.append(moveNumber).append("... ");
            }

            // add the current move
            sb.append(node.getNotation());

            // add comment if present
            if (node.getComment() != null && !node.getComment().isEmpty()) {
                sb.append(" {").append(node.getComment()).append("}");
            }
        }

        if (onlyFirstMove) return;

        List<GameNode> children = node.getChildren();
        if (children.isEmpty()) return;

        int nextMoveNumber = isWhiteTurn ? moveNumber : moveNumber + 1;

        sb.append(" ");
        appendMoves(children.get(0), nextMoveNumber, !isWhiteTurn, sb, false, true, false);

        // handle variations (all children except the first one which is mainline)
        for (int i = 1; i < children.size(); i++) {
            sb.append(" (");
            appendMoves(children.get(i), nextMoveNumber, !isWhiteTurn, sb, true, false, false);
            sb.append(")");
        }

        if (children.size() > 1) sb.append(" ");

        // continue with the main line of this branch (first child)
        appendMoves(children.get(0), nextMoveNumber, !isWhiteTurn, sb, false, false, true);
    }

    public ChessGame loadGame(String name) throws FileNotFoundException, IOException {
        // check if already loaded
        if (loadedGames.containsKey(name)) {
            return loadedGames.get(name);
        }

        String withExtension = name.endsWith(".pgn") ? name : name + ".pgn";
        if (loadedGames.containsKey(withExtension)) {
            return loadedGames.get(withExtension);
        }

        // check if we have it in our paths
        if (gameFilePaths.containsKey(name)) {
            return loadGameFromFile(name, gameFilePaths.get(name));
        }
        if (gameFilePaths.containsKey(withExtension)) {
            return loadGameFromFile(withExtension, gameFilePaths.get(withExtension));
        }
        for (Map.Entry<String, String> entry : gameFilePaths.entrySet()) {
            String fileName = new File(entry.getKey()).getName();
            if (fileName.equals(withExtension) || fileName.equals(name)) {
                return loadGameFromFile(entry.getKey(), entry.getValue());
            }
        }
        throw new FileNotFoundException("Game not found: " + name);
    }

    private ChessGame loadGameFromFile(String path, String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("Game file not found: " + filePath);
        }
        ChessGame game = parsePgnFile(file);
        game.setFilename(path); // set the filename so we know where to save back to
        loadedGames.put(path, game); // cache the loaded game
        return game;
    }

    public void deleteGame(String name) {
        File file = new File(LIB_DIR + name);
        if (file.delete()) {
            gameFilePaths.remove(name);
            loadedGames.remove(name);
            updateFileTree();
        }
    }

    public void deleteDirectory(String path) {
        File dir = new File(LIB_DIR + path);
        if (dir.exists() && dir.isDirectory()) {
            deleteDirectoryRecursive(dir);
            updateFileTree();
        }
    }

    private void deleteDirectoryRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectoryRecursive(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    private void loadSavedGames() {
        File dir = new File(LIB_DIR);
        if (!dir.exists()) dir.mkdirs();

        updateFileTree();
    }

    private void updateFileTree() {
        // clear existing children
        rootNode.getChildren().clear();
        gameFilePaths.clear();
        loadedGames.clear();

        File dir = new File(LIB_DIR);
        if (!dir.exists()) return;

        buildFileTree(dir, rootNode);
    }

    private void buildFileTree(File directory, GameLibraryNode parentNode) {
        File[] files = directory.listFiles();
        if (files == null) return;

        // sort files: directories first, then files
        Arrays.sort(files, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });

        for (File file : files) {
            String relativePath = file.getPath().substring(LIB_DIR.length());
            boolean isDirectory = file.isDirectory();
            GameLibraryNode node = new GameLibraryNode(file.getName(), file.getPath(), isDirectory);
            if (isDirectory) {
                buildFileTree(file, node);
            } else if (file.getName().toLowerCase().endsWith(".pgn")) {
                // Store file path instead of parsing the game
                gameFilePaths.put(relativePath, file.getPath());
            }
            parentNode.addChild(node);
        }
    }

    public static ChessGame parsePgn(String pgn) throws IOException {
        ChessGame game = new ChessGame(null);
        try (BufferedReader reader = new BufferedReader(new StringReader(pgn))) {
            String line;
            boolean inHeaders = true;
            StringBuilder movesText = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    inHeaders = false;
                } else if (inHeaders && line.charAt(0) == '[') {
                    parseHeaderLine(line, game);
                } else {
                    // stop checking for headers after starting the first move of the pgn
                    inHeaders = false;
                    movesText.append(line).append(" ");
                }
            }
            parseMovesText(movesText.toString().trim(), game);
        }
        return game;
    }

    private ChessGame parsePgnFile(File file) throws IOException {
        String pgn = Files.readString(file.toPath());
        return parsePgn(pgn);
    }

    private static void parseHeaderLine(String line, ChessGame game) {
        int spaceIndex = line.indexOf(" ");
        String key = line.substring(1, spaceIndex);
        String value = line.substring(spaceIndex + 2, line.length() - 2);
        game.setTag(key, value);
    }

    private static void parseMovesText(String movesText, ChessGame game) {
        // game should be empty, with no children
        GameNode currentNode = game.getFirstPosition();
        assert currentNode.getNextChild() == null;

        Stack<GameNode> variationStack = new Stack<>();
        GameNode lastMoveNode = null;
        StringBuilder pendingComment = new StringBuilder();

        // split by space, before ( and after )
        // "move1 (move2) move3" ==> ["move1", "(", "move2", ")", "move3"]
        String[] tokens = movesText.split("\s|(?=[()])|(?<=[()])");
        for (int i = 0; i < tokens.length; ++i) {
            String token = tokens[i];

            token = token.trim();
            if (token.isEmpty()) continue;

            // handle comments (they may be standalone or attached to a move)
            if (token.startsWith("{")) {
                StringBuilder commentBuffer = new StringBuilder();
                while (!token.endsWith("}") && i < tokens.length - 1) {
                    commentBuffer.append(token).append(" ");
                    token = tokens[++i];
                }
                String comment = commentBuffer.append(token)
                    .toString()
                    .substring(1, commentBuffer.length() - 1)
                    .trim();

                if (!comment.contains("[%cal")) {
                    pendingComment.append(comment);
                }
                continue;
            } else if (token.contains("{")) {
                // embedded comment in move+comment token
                int start = token.indexOf("{");
                String before = token.substring(0, start).trim();

                String after = token.substring(start + 1).trim();
                StringBuilder commentBuffer = new StringBuilder();
                while (!after.endsWith("}") && i < tokens.length - 1) {
                    commentBuffer.append(after).append(" ");
                    after = tokens[++i];
                }
                String comment = commentBuffer.append(after)
                    .toString()
                    .substring(1, commentBuffer.length() - 1)
                    .trim();

                if (!comment.contains("[%cal")) {
                    pendingComment.append(comment);
                }
                token = before; // keep the move part for processing
            }

            if (token.equals("(")) {
                // new variation
                variationStack.push(currentNode);
                currentNode = currentNode.getParentNode();
                continue;
            }

            if (token.equals(")")) {
                // End variation
                currentNode = variationStack.pop();
                continue;
            }

            if (token.contains(".")) {
                // Move number indicator
                String[] parts = token.split("\\.");
                if (parts.length > 1) {
                    //moveNumber = Integer.parseInt(parts[0]);
                    token = parts[1].trim();
                } else {
                    // skip the number and dot
                    continue;
                }
            }

            // Handle "..." format for black moves in variations
            if (token.contains("...")) {
                String[] parts = token.split("\\.\\.\\.");
                if (parts.length > 1) {
                    //moveNumber = Integer.parseInt(parts[0]);
                    token = parts[1].trim();
                } else {
                    // skip the number and dots
                    continue;
                }
            }

            if (token.equals("*") || token.equals("1-0") ||
                token.equals("0-1") || token.equals("1/2-1/2")) {
                continue;
            }

            if (!token.isEmpty()) {
                Move move = NotationParser.parseMove(token, currentNode);
                if (move != null) {
                    if (pendingComment.length() != 0) {
                        assert lastMoveNode != null;
                        lastMoveNode.setComment(pendingComment.toString());
                        pendingComment.setLength(0);
                    }
                    currentNode = currentNode.addNode(move);
                    lastMoveNode = currentNode;
                }
            } else if (pendingComment != null && lastMoveNode != null) {
                // no move parsed, attach to last valid move
                lastMoveNode.setComment(pendingComment.toString());
                pendingComment.setLength(0);
            }
        }
        // attach final pending comment if nothing else was done
        if (pendingComment != null && lastMoveNode != null) {
            lastMoveNode.setComment(pendingComment.toString());
        }
    }
}
