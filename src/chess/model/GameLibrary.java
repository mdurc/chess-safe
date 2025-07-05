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

        List<GameNode> mainLine = new ArrayList<>();
        GameNode currentNode = game.getFirstPosition();
        while (currentNode != null && !currentNode.getChildren().isEmpty()) {
            // we skip the blank root node here
            currentNode = currentNode.getNextChild();
            mainLine.add(currentNode);
        }

        for (int i = 0; i < mainLine.size(); i += 2) {
            sb.append(moveNumber).append(".");

            GameNode whiteMove = mainLine.get(i);

            // add white's move
            sb.append(whiteMove.getNotation());
            if (whiteMove.getComment() != null && !whiteMove.getComment().isEmpty()) {
                sb.append(" {").append(whiteMove.getComment()).append("}");
            }

            // add variations to white's move
            if (!whiteMove.getChildren().isEmpty()) {
                for (int j = 1; j < whiteMove.getChildren().size(); j++) {
                    sb.append(" (");
                    generateVariationText(whiteMove.getChildren().get(j), moveNumber, true, sb);
                    sb.append(")");
                }
            }

            // add black's move if it exists
            if (i + 1 < mainLine.size()) {
                sb.append(" ");
                GameNode blackMove = mainLine.get(i + 1);
                sb.append(blackMove.getNotation());
                if (blackMove.getComment() != null && !blackMove.getComment().isEmpty()) {
                    sb.append(" {").append(blackMove.getComment()).append("}");
                }

                // add variations to black's move
                if (!blackMove.getChildren().isEmpty()) {
                    for (int j = 1; j < blackMove.getChildren().size(); j++) {
                        sb.append(" (");
                        generateVariationText(blackMove.getChildren().get(j), moveNumber, false, sb);
                        sb.append(")");
                    }
                }
            }
            sb.append(" ");
            moveNumber++;
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

        for (int i = 0; i < variationLine.size(); i += 2) {
            // add move number before white's move
            sb.append(currentMoveNumber).append(".");

            // add white's move
            GameNode whiteMove = variationLine.get(i);
            sb.append(whiteMove.getNotation());
            if (whiteMove.getComment() != null && !whiteMove.getComment().isEmpty()) {
                sb.append(" {").append(whiteMove.getComment()).append("}");
            }

            // add black's move if it exists
            if (i + 1 < variationLine.size()) {
                sb.append(" ");
                GameNode blackMove = variationLine.get(i + 1);
                sb.append(blackMove.getNotation());
                if (blackMove.getComment() != null && !blackMove.getComment().isEmpty()) {
                    sb.append(" {").append(blackMove.getComment()).append("}");
                }
            }
            sb.append(" ");
            currentMoveNumber++;
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
