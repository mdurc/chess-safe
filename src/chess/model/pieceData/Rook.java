package chess.model.pieceData;

import chess.model.Move;
import chess.model.util.ImmutXY;

import java.util.*;

import chess.model.boardData.BoardState;

public class Rook extends Piece {
    public Rook(boolean white, int row, int col) {
        name = white ? "wr" : "br";
        this.white = white;
        position = new ImmutXY(col, row);

        type = PieceType.ROOK;
    }

    public PieceType getType() {
        return type;
    }

    public double getPoints() {
        return 5.0;
    }

    public List<Move> calculatePossibleMoves(BoardState board) {
        if (board == null) {
            throw new IllegalArgumentException("Null on calculating possible moves");
        }

        Piece thisPiece = board.getPieceAt(position.getY(), position.getX());

        if (thisPiece == null || !(thisPiece instanceof Rook) ||  !thisPiece.equals(this)) {
            throw new IllegalArgumentException("Not a pawn for input to pawn calculatePossibleMoves");
        }

        int[][] directions = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        return filterLegalMoves(calculateSlideMoves(directions, board), board);
    }
}
