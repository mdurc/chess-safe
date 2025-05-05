package chess.model.pieceData;

import chess.model.boardAnalysis.BoardState;
import chess.model.Move;
import chess.utils.ImmutXY;

import java.util.*;

public class Pawn extends Piece {
    private PieceType defaultPromotionType = PieceType.QUEEN;

    public Pawn(int id, boolean white, int row, int col) {
        this.id = id;
        name = white ? "wp" : "bp";
        this.white = white;
        position = new ImmutXY(col, row);

        type = PieceType.PAWN;
    }

    public PieceType getType() {
        return type;
    }

    public double getPoints() {
        return 1.0;
    }

    public List<Move> calculatePossibleMoves(BoardState board) {
        if (board == null) {
            throw new IllegalArgumentException("Null on calculating possible moves");
        }

        Piece thisPiece = board.getPieceAt(position.getY(), position.getX());

        if (thisPiece == null || !(thisPiece instanceof Pawn) ||  !thisPiece.equals(this)) {
            throw new IllegalArgumentException("Not a pawn for input to pawn calculatePossibleMoves");
        }

        List<Move> moves = new ArrayList<>();

        int dir = white ? -1 : 1;
        int startRow = white ? 6 : 1;

        // Normal moves
        int nextY = position.getY() + dir;
        if (board.isEmpty(position.getX(), nextY)) {
            ImmutXY to = new ImmutXY(position.getX(), nextY);
            List<Move.MoveType> types = new ArrayList<>();
            types.add(Move.MoveType.REGULAR);

            PieceType promotionType = null;
            if ((white && nextY == 0) || (!white && nextY == 7)) {
                types.add(Move.MoveType.PROMOTION);
                promotionType = defaultPromotionType;
            }            
            moves.add(new Move(thisPiece, position, to, types, promotionType));

            // Two-square move
            if (position.getY() == startRow && board.isEmpty(position.getX(), position.getY() + 2*dir)) {
                ImmutXY to2 = new ImmutXY(position.getX(), position.getY() + 2*dir);
                moves.add(new Move(thisPiece, position, to2, List.of(Move.MoveType.DOUBLE_PAWN), null));
            }
        }

        addPawnCapture(position.getX() - 1, position.getY() + dir, board, moves);
        addPawnCapture(position.getX() + 1, position.getY() + dir, board, moves);

        // En passant
        ImmutXY epTarget = board.getEnPassantTarget();
        if (epTarget != null && Math.abs(position.getX() - epTarget.getX()) == 1 && position.getY() == (white ? 3 : 4)) {
            moves.add(new Move(thisPiece, position, epTarget, List.of(Move.MoveType.EN_PASSANT, Move.MoveType.CAPTURE), null));
        }

        return filterLegalMoves(moves, board);
    }

    private void addPawnCapture(int x, int y, BoardState board, List<Move> moves) {
        if (!BoardState.isWithinBounds(x, y)) {
            return;
        }

        Piece target = board.getPieceAt(y, x);
        if (target != null && target.isWhite() != white) {
            ImmutXY to = new ImmutXY(x, y);
            List<Move.MoveType> types = new ArrayList<>();
            types.add(Move.MoveType.CAPTURE);

            PieceType promotionType = null;
            if ((white && y == 0) || (!white && y == 7)) {
                types.add(Move.MoveType.PROMOTION);
                promotionType = defaultPromotionType;
            }
            Piece piece = board.getPieceAt(position.getY(), position.getX());
            moves.add(new Move(piece, position, to, types, promotionType));
        }
    }
}
