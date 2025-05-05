package chess.model;

import java.io.*;
import java.util.*;
import chess.model.util.*;

public class GameLibrary {
    private static final String LIB_DIR = "games/";
    private final Map<String, ChessGame> savedGames = new HashMap<>();

    public GameLibrary() {
        loadSavedGames();
    }

    public List<String> getSavedGames() {
        return new ArrayList<>(savedGames.keySet());
    }

    public void saveGame(String name, ChessGame game) throws IOException {
        //if (!name.endsWith(".pgn")) name += ".pgn";
        //Path path = Paths.get(LIB_DIR, name);
        //Files.createDirectories(path.getParent());
        //try (BufferedWriter writer = Files.newBufferedWriter(path)) {
        //    writeHeaders(writer, game);
        //    String movesText = generateMovesText(game);
        //    System.out.println("Moves: \n" + movesText);
        //    writer.write("\n" + movesText);
        //}
        //savedGames.put(name, game);
    }

    //private void writeHeaders(BufferedWriter writer, ChessGame game) throws IOException {
        //String[] mandatoryTags = {"Event", "Site", "Date", "Round", "White", "Black", "Result"};
        //for (String tag : mandatoryTags) {
        //    String value = game.getTag(tag) != null ? game.getTag(tag) : "?";
        //    writer.write(String.format("[%s \"%s\"]\n", tag, value));
        //}
        //for (Map.Entry<String, String> entry : game.getTags().entrySet()) {
        //    String key = entry.getKey();
        //    if (!Arrays.asList(mandatoryTags).contains(key)) {
        //        writer.write(String.format("[%s \"%s\"]\n", key, entry.getValue()));
        //    }
        //}
    //}

    //private String generateMovesText(ChessGame game) {
        //return "";
        //StringBuilder sb = new StringBuilder();
        //GameNode currentNode = game.getRootNode();
        //int moveNumber = 1;
        //boolean isWhiteMove = true;
        //List<GameNode> mainLine = new ArrayList<>();
        //
        //while (currentNode != null && !currentNode.getVariations().isEmpty()) {
        //    currentNode = currentNode.getVariations().get(0);
        //    mainLine.add(currentNode);
        //}
        //
        //currentNode = game.getRootNode();
        //for (GameNode node : mainLine) {
        //    if (isWhiteMove) {
        //        sb.append(moveNumber).append(". ");
        //    } else {
        //        sb.append(moveNumber).append("... ");
        //    }
        //    sb.append(node.getMove().getNotation());
        //    if (node.getComment() != null && !node.getComment().isEmpty()) {
        //        sb.append(" {").append(node.getComment()).append("}");
        //    }
        //    sb.append(" ");
        //
        //    if (!node.getVariations().isEmpty()) {
        //        for (int i = 1; i < node.getVariations().size(); i++) {
        //            sb.append("( ");
        //            generateVariationText(node.getVariations().get(i), moveNumber, !isWhiteMove, sb);
        //            sb.append(") ");
        //        }
        //    }
        //
        //    isWhiteMove = !isWhiteMove;
        //    if (!isWhiteMove) {
        //        moveNumber++;
        //    }
        //}
        //return sb.toString().trim();
    //}

    //private void generateVariationText(GameNode node, int currentMoveNumber, boolean isWhiteMove, StringBuilder sb) {
        //List<GameNode> variationLine = new ArrayList<>();
        //GameNode currentNode = node;
        //while (currentNode != null && !currentNode.getVariations().isEmpty()) {
        //    currentNode = currentNode.getVariations().get(0);
        //    variationLine.add(currentNode);
        //}
        //
        //currentNode = node;
        //for (GameNode n : variationLine) {
        //    if (isWhiteMove) {
        //        sb.append(currentMoveNumber).append(". ");
        //    } else {
        //        sb.append(currentMoveNumber).append("... ");
        //    }
        //    sb.append(n.getMove().getNotation());
        //    if (n.getComment() != null && !n.getComment().isEmpty()) {
        //        sb.append(" {").append(n.getComment()).append("}");
        //    }
        //    sb.append(" ");
        //    isWhiteMove = !isWhiteMove;
        //    if (!isWhiteMove) {
        //        currentMoveNumber++;
        //    }
        //}
    //}

    public ChessGame loadGame(String name) throws FileNotFoundException {
        if (!savedGames.containsKey(name)) {
            throw new FileNotFoundException("Game not found: " + name);
        }
        return savedGames.get(name);
    }

    public void deleteGame(String name) {
        File file = new File(LIB_DIR + name);
        if (file.delete()) {
            savedGames.remove(name);
        }
    }

    private void loadSavedGames() {
        File dir = new File(LIB_DIR);
        if (!dir.exists()) dir.mkdirs();

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith(".pgn")) {
                try {
                    ChessGame game = parsePgnFile(file);
                    savedGames.put(file.getName(), game);
                } catch (IOException e) {
                    System.err.println("Error loading game: " + file.getName());
                }
            }
        }
    }

    private ChessGame parsePgnFile(File file) throws IOException {
        ChessGame game = new ChessGame();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
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

                    // should be moves starting with move number
                    //1. e4 d5 2. exd5 e6 3. dxe6 Qd7 (3... Bxe6 4. d4 Qxd4 (4... Nf6 5. Be2 Bc5 (5...
                    //Bb4+ 6. Bd2 Bxd2+ 7. Nxd2 O-O 8. Ngf3 Bd5 9. O-O Bxf3 (9... Nc6 10. c4 Bxc4))))
                    //4. exd7+ *
                    movesText.append(line).append(" ");
                }
            }
            parseMovesText(movesText.toString().trim(), game);
        }
        return game;
    }

    private void parseHeaderLine(String line, ChessGame game) {
        System.out.println("Parsing header: " + line);
        int spaceIndex = line.indexOf(" ");
        String key = line.substring(1, spaceIndex);
        String value = line.substring(spaceIndex + 2, line.length() - 2);
        game.setTag(key, value);
    }

    private void parseMovesText(String movesText, ChessGame game) {
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
