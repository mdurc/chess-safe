package chess.model.pieceData;

import chess.model.Move;
import chess.model.util.ImmutXY;

import java.util.*;

import chess.model.boardData.BoardState;

public class Bishop extends Piece {
    public Bishop(int id, boolean white, int row, int col) {
        this.id = id;
        name = white ? "wb" : "bb";
        this.white = white;
        position = new ImmutXY(col, row);

        type = PieceType.BISHOP;
    }

    public PieceType getType() {
        return type;
    }

    public double getPoints() {
        return 3.0;
    }

    public List<Move> calculatePossibleMoves(BoardState board) {
        if (board == null) {
            throw new IllegalArgumentException("Null on calculating possible moves");
        }

        Piece thisPiece = board.getPieceAt(position.getY(), position.getX());

        if (thisPiece == null || !(thisPiece instanceof Bishop) ||  !thisPiece.equals(this)) {
            throw new IllegalArgumentException("Not a pawn for input to pawn calculatePossibleMoves");
        }

        int[][] directions = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        return filterLegalMoves(calculateSlideMoves(directions, board), board);
    }
}
