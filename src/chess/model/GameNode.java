package chess.model;

import java.util.ArrayList;
import java.util.List;
import chess.model.boardData.*;
import chess.model.util.*;

public class GameNode extends BoardState {
    private GameNode parentNode;
    private List<GameNode> children;

    private Move move;
    private String notation; // notation of the move field, located here because notation also revolves around the game state/current position, whether the move caused a check or checkmate, etc.

    private String comment;
    private static int nextId = 0;
    private final int id;

    // this will only ever be used to create the root node, that has no starting position, move, or notation
    public GameNode() {
        super();
        parentNode = null;
        children = new ArrayList<>();
        move = null;
        notation = null;
        comment = "";
        id = nextId++;
    }

    public GameNode(GameNode oldState, Move nextMove) {
        super(oldState.makeNewChange(nextMove));
        parentNode = oldState;
        children = new ArrayList<>();
        move = nextMove;
        notation = NotationParser.convertToNotation(move, oldState);
        comment = "";
        id = nextId++;
    }

    public String getNotation() { return notation; }
    public Move getMove() { return move; }
    public GameNode getParentNode() { return parentNode; }
    public GameNode getNextChild() { return children.isEmpty() ? null : children.get(0); }
    public List<GameNode> getChildren() { return children; }

    public GameNode addNode(Move move) {
        GameNode newNode = new GameNode(this, move);
        children.add(newNode);
        return newNode;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof GameNode)) return false;
        return id == ((GameNode) o).id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
