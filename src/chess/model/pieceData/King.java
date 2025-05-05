package chess.model.pieceData;

import chess.model.Move;
import chess.model.util.ImmutXY;

import java.util.*;

import chess.model.boardData.BoardState;

public class King extends Piece {
    public King(int id, boolean white, int row, int col) {
        this.id = id;
        name = white ? "wk" : "bk";
        this.white = white;
        position = new ImmutXY(col, row);

        type = PieceType.KING;
    }

    public PieceType getType() {
        return type;
    }

    public double getPoints() {
        return 10.0;
    }

    public List<Move> calculatePossibleMoves(BoardState board) {
        if (board == null) {
            throw new IllegalArgumentException("Null on calculating possible moves");
        }

        Piece thisPiece = board.getPieceAt(position.getY(), position.getX());

        if (thisPiece == null || !(thisPiece instanceof King) ||  !thisPiece.equals(this)) {
            throw new IllegalArgumentException("Not a king for input to king calculatePossibleMoves");
        }

        int[][] offsets = new int[][]{{1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}, {-1,-1}, {0,-1}, {1,-1}};
        List<Move> moves = calculateJumpMoves(offsets, board);

        int currY = position.getY();

        // castling
        if (board.canCastle(white, true)) {
            if (board.isEmpty(5, currY) && board.isEmpty(6, currY)) {
                ImmutXY to = new ImmutXY(6, currY);
                moves.add(new Move(this, position, to, List.of(Move.MoveType.CASTLE_SHORT), null));
            }
        }
        if (board.canCastle(white, false)) {
            if (board.isEmpty(3, currY) && board.isEmpty(2, currY) && board.isEmpty(1, currY)) {
                ImmutXY to = new ImmutXY(2, currY);
                moves.add(new Move(this, position, to, List.of(Move.MoveType.CASTLE_LONG), null));
            }
        }

        return filterLegalMoves(moves, board);
    }
}
