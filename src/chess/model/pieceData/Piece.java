package chess.model.pieceData;

import java.util.ArrayList;
import java.util.List;

import chess.model.boardData.BoardState;
import chess.model.Move;
import chess.model.util.ImmutXY;

public abstract class Piece {
    protected String name = "unnamed";
    protected boolean white = true; // 1 white, 0 black
    protected ImmutXY position = new ImmutXY(0,0);
    protected int id = -1;
    protected PieceType type;
    protected boolean hasMoved = false;

    public enum PieceType { PAWN, ROOK, KNIGHT, BISHOP, QUEEN, KING }

    private static int nextId = 0;

    // factory
    public static Piece makeNewPiece(boolean pieceColor, PieceType type, int row, int col) {
        switch(type) {
            case PieceType.PAWN: return new Pawn(nextId++, pieceColor, row, col);
            case PieceType.ROOK: return new Rook(nextId++, pieceColor, row, col);
            case PieceType.KNIGHT: return new Knight(nextId++, pieceColor, row, col);
            case PieceType.BISHOP: return new Bishop(nextId++, pieceColor, row, col);
            case PieceType.QUEEN: return new Queen(nextId++, pieceColor, row, col);
            case PieceType.KING: return new King(nextId++, pieceColor, row, col);
        }
        throw new IllegalArgumentException("Invalid piece type: " + type);
    }
    public static Piece makeNewPiece(Piece other) {
        if (other == null) return null;
        ImmutXY pos = other.getPos();
        Piece ret = Piece.makeNewPiece(other.isWhite(), other.getType(), pos.getY(), pos.getX());
        if (other.hasMoved()) ret.markAsMoved();
        return ret;
    }

    public String toString() { return name; };
    public ImmutXY getPos() { return position; };
    public boolean isWhite() { return white; };
    public boolean hasMoved() { return hasMoved; };
    public void markAsMoved() { hasMoved = true; }

    public abstract PieceType getType();
    public abstract double getPoints();
    public abstract List<Move> calculatePossibleMoves(BoardState board);

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Piece)) return false;
        return id == ((Piece) o).id && type == ((Piece) o).type;
    }

    @Override
    public int hashCode() {
        return id;
    }

    protected Move getMoveWithCheckTypes(Move move, BoardState board) {
        BoardState newState = board.makeNewChange(move);
        boolean isCheck = newState.isKingInCheck(newState.isWhiteToPlay());

        if (isCheck) {
            List<Move> legalMoves = newState.getLegalMoves();
            boolean isCheckmate = isCheck && legalMoves.isEmpty();
            List<Move.MoveType> newTypes = new ArrayList<>(move.getTypes());
            if (isCheckmate) {
                newTypes.add(Move.MoveType.CHECKMATE);
            } else {
                newTypes.add(Move.MoveType.CHECK);
            }
            return new Move(move.getPiece(), move.getFrom(), move.getTo(), newTypes, move.getPromotionType());
        }
        return move; // return the original move that was provided
    }

    protected List<Move> filterLegalMoves(List<Move> moves, BoardState board) {
        List<Move> legalMoves = new ArrayList<>();
        for (Move move : moves) {
            BoardState newState = board.makeNewChange(move);
            if (!newState.isKingInCheck(white)) {
                Move moveWithAllTypes = getMoveWithCheckTypes(move, board);
                legalMoves.add(moveWithAllTypes);
            }
        }
        return legalMoves;
    }

    // Used within bishop, rook, queeen
    protected List<Move> calculateSlideMoves(int[][] directions, BoardState board) {
        List<Move> moves = new ArrayList<>();
        for (int[] dir : directions) {
            int x = position.getX() + dir[0];
            int y = position.getY() + dir[1];
            while (BoardState.isWithinBounds(x,y)) {
                ImmutXY to = new ImmutXY(x, y);
                Piece target = board.getPieceAt(y, x);
                List<Move.MoveType> types = new ArrayList<>();
                if (target == null) {
                    types.add(Move.MoveType.REGULAR);
                    moves.add(new Move(this, position, to, types, null));
                } else {
                    if (target.isWhite() != white) {
                        types.add(Move.MoveType.CAPTURE);
                        moves.add(new Move(this, position, to, types, null));
                    }
                    break;
                }
                x += dir[0];
                y += dir[1];
            }
        }
        return moves;
    }

    protected List<Move> calculateJumpMoves(int[][] offsets, BoardState board) {
        List<Move> moves = new ArrayList<>();

        for (int[] offset : offsets) {
            int x = position.getX() + offset[0];
            int y = position.getY() + offset[1];
            if (!BoardState.isWithinBounds(x,y)) continue;

            Piece target = board.getPieceAt(y, x);
            if (target != null && target.isWhite() == white) {
                continue;
            }

            ImmutXY to = new ImmutXY(x, y);
            List<Move.MoveType> types = new ArrayList<>();
            if (target != null) {
                types.add(Move.MoveType.CAPTURE);
            } else {
                types.add(Move.MoveType.REGULAR);
            }
            moves.add(new Move(this, position, to, types, null));
        }
        return moves;
    }
}
