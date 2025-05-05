package chess.model;

import java.util.*;


public class ChessGame {
    // for pgn data such as event, round, date, result, etc.
    // only relevant to loaded ChessGames from PGN then to persist the pgn data
    private final Map<String, String> tags = new HashMap<>();

    private final GameNode root;

    public ChessGame() {
        root = new GameNode();
    }

    public GameNode getFirstPosition() {
        return root;
    }

    public GameNode getLastPosition() {
        GameNode n = root;
        while (n.getNextChild() != null) {
            n = n.getNextChild();
        }
        return n;
    }

    public String getTag(String key) { return tags.get(key); }
    public Map<String, String> getTags() { return new HashMap<>(tags); }
    public void setTag(String key, String value) { tags.put(key, value); }


    public void playFirstMove(Move move) {
        root.addNode(move);
    }

    public void printMainline() {
        GameNode n = root.getNextChild();

        if (n == null) {
            root.printBoard();
            return;
        }

        int i = 1;
        while (n.getNextChild() != null) {
            System.out.println(i++ + ": "+ n.getNotation());
            n = n.getNextChild();
        }
        System.out.println(i++ + ": "+ n.getNotation());
        System.out.println("Full mainline printed");
        n.printBoard();
    }
}
