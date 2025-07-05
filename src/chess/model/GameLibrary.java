package chess.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import chess.model.util.*;

public class GameLibrary {
    private static final String LIB_DIR = "games/";
    private final Map<String, ChessGame> savedGames = new HashMap<>();
    private final GameLibraryNode rootNode;

    public GameLibrary() {
        rootNode = new GameLibraryNode("games", LIB_DIR, true);
        loadSavedGames();
    }

    public String getLib() { return LIB_DIR; }

    public List<String> getSavedGames() {
        return new ArrayList<>(savedGames.keySet());
    }

    public GameLibraryNode getRootNode() { return rootNode; }

    public void saveGameToPath(String path, ChessGame game) throws IOException {
        if (!path.endsWith(".pgn")) path += ".pgn";
        Path fullPath = Paths.get(LIB_DIR, path);
        Files.createDirectories(fullPath.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(fullPath)) {
            writeHeaders(writer, game);
            writer.write("\n" + generateMovesText(game));
        }
        savedGames.put(path, game);
        updateFileTree();
    }

    public void createDirectory(String path) throws IOException {
        Path fullPath = Paths.get(LIB_DIR, path);
        Files.createDirectories(fullPath);
        updateFileTree();
    }

    private void writeHeaders(BufferedWriter writer, ChessGame game) throws IOException {
        String[] mandatoryTags = {"Event", "Site", "Date", "Round", "White", "Black", "Result"};
        for (String tag : mandatoryTags) {
            String value = game.getTag(tag) != null ? game.getTag(tag) : "?";
            writer.write(String.format("[%s \"%s\"]\n", tag, value));
        }
        for (Map.Entry<String, String> entry : game.getTags().entrySet()) {
            String key = entry.getKey();
            if (!Arrays.asList(mandatoryTags).contains(key)) {
                writer.write(String.format("[%s \"%s\"]\n", key, entry.getValue()));
            }
        }
    }

    private String generateMovesText(ChessGame game) {
        StringBuilder sb = new StringBuilder();
        int moveNumber = 1;
        boolean isWhiteMove = true;

        List<GameNode> mainLine = new ArrayList<>();
        GameNode currentNode = game.getFirstPosition();
        while (currentNode != null && !currentNode.getChildren().isEmpty()) {
            // we skip the blank root node here
            currentNode = currentNode.getNextChild();
            mainLine.add(currentNode);
        }

        for (GameNode node : mainLine) {
            if (isWhiteMove) {
                sb.append(moveNumber).append(". ");
            } else {
                sb.append(moveNumber).append("... ");
            }
            sb.append(node.getNotation());
            if (node.getComment() != null && !node.getComment().isEmpty()) {
                sb.append(" {").append(node.getComment()).append("}");
            }
            sb.append(" ");

            if (!node.getChildren().isEmpty()) {
                for (int i = 1; i < node.getChildren().size(); i++) {
                    sb.append("( ");
                    generateVariationText(node.getChildren().get(i), moveNumber, !isWhiteMove, sb);
                    sb.append(") ");
                }
            }

            isWhiteMove = !isWhiteMove;
            if (!isWhiteMove) {
                moveNumber++;
            }
        }
        return sb.toString().trim();
    }

    private void generateVariationText(GameNode node, int currentMoveNumber, boolean isWhiteMove, StringBuilder sb) {
        List<GameNode> variationLine = new ArrayList<>();
        GameNode currentNode = node;
        while (currentNode != null && !currentNode.getChildren().isEmpty()) {
            currentNode = currentNode.getChildren().get(0);
            variationLine.add(currentNode);
        }

        currentNode = node;
        for (GameNode n : variationLine) {
            if (isWhiteMove) {
                sb.append(currentMoveNumber).append(". ");
            } else {
                sb.append(currentMoveNumber).append("... ");
            }
            sb.append(n.getNotation());
            if (n.getComment() != null && !n.getComment().isEmpty()) {
                sb.append(" {").append(n.getComment()).append("}");
            }
            sb.append(" ");
            isWhiteMove = !isWhiteMove;
            if (!isWhiteMove) {
                currentMoveNumber++;
            }
        }
    }

    public ChessGame loadGame(String name) throws FileNotFoundException {
        // try exact match first
        if (savedGames.containsKey(name)) {
            return savedGames.get(name);
        }
        // try with .pgn extension
        String withExtension = name.endsWith(".pgn") ? name : name + ".pgn";
        if (savedGames.containsKey(withExtension)) {
            return savedGames.get(withExtension);
        }
        // try to find by display name
        for (Map.Entry<String, ChessGame> entry : savedGames.entrySet()) {
            String fileName = new File(entry.getKey()).getName();
            if (fileName.equals(withExtension) || fileName.equals(name)) {
                return entry.getValue();
            }
        }
        throw new FileNotFoundException("Game not found: " + name);
    }

    public void deleteGame(String name) {
        File file = new File(LIB_DIR + name);
        if (file.delete()) {
            savedGames.remove(name);
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
        savedGames.clear();

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
                try {
                    ChessGame game = parsePgnFile(file);
                    node.setGame(game);
                    savedGames.put(relativePath, game);
                } catch (IOException e) {
                    System.err.println("Error loading game: " + file.getName());
                }
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

        // split by space, before ( and after )
        // "move1 (move2) move3" ==> ["move1", "(", "move2", ")", "move3"]
        String[] tokens = movesText.split("\s|(?=[()])|(?<=[()])");
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

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
                }
                continue;
            }

            if (token.equals("*") || token.equals("1-0") || token.equals("0-1")) { // Game termination
                continue;
            }

            // Handle main move parsing
            Move move = NotationParser.parseMove(token, currentNode);
            if (move != null) {
                // Handle variations
                currentNode = currentNode.addNode(move);
            }

            // Handle comments
            if (token.contains("{")) {
                int start = token.indexOf("{");
                int end = token.indexOf("}");
                if (end > start) {
                    String comment = token.substring(start+1, end);
                    currentNode.setComment(comment);
                }
            }
        }
    }

}
