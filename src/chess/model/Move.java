package chess.model;

import chess.model.pieceData.Piece;
import chess.utils.ImmutXY;
import java.util.List;
import java.util.ArrayList;

public class Move {
    private final Piece piece;
    private final List<MoveType> types;
    private final ImmutXY from, to;
    private final Piece.PieceType promotionType; // only if this is a pawn promotion, otherwise null

    public Move(Piece piece, ImmutXY from, ImmutXY to, List<MoveType> types, Piece.PieceType promotionType) {
        this.piece = piece;
        this.types = types;
        this.from = from;
        this.to = to;
        this.promotionType = promotionType;
    }

    public enum MoveType {
        REGULAR,
        CAPTURE,
        CASTLE_SHORT,
        CASTLE_LONG,
        CHECK,
        PROMOTION,
        CHECKMATE,
        DOUBLE_PAWN,
        EN_PASSANT;
    
        @Override
        public String toString() {
            switch (this) {
                case REGULAR: return "Regular Move";
                case CAPTURE: return "Capture";
                case CASTLE_SHORT: return "Short Castle";
                case CASTLE_LONG: return "Long Castle";
                case CHECK: return "Check";
                case PROMOTION: return "Promotion";
                case CHECKMATE: return "Checkmate";
                case DOUBLE_PAWN: return "Double Pawn Move";
                case EN_PASSANT: return "En Passant";
                default: return name();
            }
        }
    }
    
    public Piece.PieceType getPromotionType() { return promotionType; }
    public Piece getPiece() { return piece; }
    public ImmutXY getFrom() { return from; }
    public ImmutXY getTo() { return to; }
    public List<MoveType> getTypes() { return new ArrayList<>(types); }

    public static ImmutXY getPos(String p) { return new ImmutXY(getCol(p), getRow(p)); }
    public static int getRow(String r) { return 8 - Character.getNumericValue(r.charAt(1)); }
    public static int getCol(String c) { return c.charAt(0) - 'a'; }

    public String toString() {
        return from.toAlgebraic() + "-" + to.toAlgebraic();
    }
}
