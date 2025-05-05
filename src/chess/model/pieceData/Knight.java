package chess.model.pieceData;

import chess.model.boardAnalysis.BoardState;
import chess.model.Move;
import chess.utils.ImmutXY;

import java.util.*;

public class Knight extends Piece {
    public Knight(int id, boolean white, int row, int col) {
        this.id = id;
        name = white ? "wn" : "bn";
        this.white = white;
        position = new ImmutXY(col, row);

        type = PieceType.KNIGHT;
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

        if (thisPiece == null || !(thisPiece instanceof Knight) ||  !thisPiece.equals(this)) {
            throw new IllegalArgumentException("Not a pawn for input to pawn calculatePossibleMoves");
        }

        // calculate jump offsets
        int[][] offsets = {{2,1}, {1,2}, {-1,2}, {-2,1}, {-2,-1}, {-1,-2}, {1,-2}, {2,-1}};

        return filterLegalMoves(calculateJumpMoves(offsets, board), board);
    }
}
