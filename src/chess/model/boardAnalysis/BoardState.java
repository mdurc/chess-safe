package chess.model.boardAnalysis;

import chess.model.Move;
import chess.model.Move.MoveType;
import chess.model.pieceData.Piece;
import chess.model.pieceData.Piece.PieceType;
import chess.utils.ImmutXY;

import java.util.ArrayList;
import java.util.List;

// board state will always consider black to be playing at rows (0 & 1) while white is on the bottom at rows (6 & 7)
// The view handles board flips, but internally the data does not change
public class BoardState {
    private Piece[][] boardState;
    private ChessVerifier verifier;
    private Piece whiteKing = null;
    private Piece blackKing = null;
    private boolean whiteToPlay;
    private ImmutXY enPassantTarget;

    public BoardState() {
        boardState = new Piece[8][8];
        initializeBoard();
        findKings();
        whiteToPlay = true;
        enPassantTarget = null;
        verifier = new ChessVerifier(this);
    }

    // copy into this board
    public BoardState(BoardState other) {
        boardState = new Piece[8][8];
        for (int i=0; i<8; ++i) {
            for (int j=0; j<8; ++j) {
                boardState[i][j] = Piece.makeNewPiece(other.boardState[i][j]);
            }
        }
        findKings();
        whiteToPlay = other.whiteToPlay;
        enPassantTarget = other.enPassantTarget;
        verifier = new ChessVerifier(this);
    }

    private void initializeBoard() {
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                boardState[i][j] = null;
            }
        }
        for (int i = 0; i < 8; i++) {
            boardState[1][i] = Piece.makeNewPiece(false, PieceType.PAWN, 1, i);
            boardState[6][i] = Piece.makeNewPiece(true, PieceType.PAWN, 6, i);
        }
        PieceType[] pieces = {PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP,
                              PieceType.QUEEN, PieceType.KING, PieceType.BISHOP,
                              PieceType.KNIGHT, PieceType.ROOK};
        for (int i = 0; i < 8; i++) {
            boardState[0][i] = Piece.makeNewPiece(false, pieces[i], 0, i);
            boardState[7][i] = Piece.makeNewPiece(true, pieces[i], 7, i);
        }
    }

    public boolean isEmpty(int x, int y) {
        if (x < 0 || x >= 8 || y < 0 || y >= 8) return false;
        return boardState[y][x] == null;
    }

    public Piece getPieceAt(int row, int col) {
        if (row < 0 || row >= 8 || col < 0 || col >= 8) return null;
        return boardState[row][col];
    }

    public BoardState makeNewChange(Move move) {
        BoardState newBoard = new BoardState(this);

        Piece originalPiece = move.getPiece();
        if (originalPiece == null) {
            throw new IllegalArgumentException("Move has a null piece");
        }

        newBoard.whiteToPlay = !this.whiteToPlay;
        newBoard.enPassantTarget = null;

        if (originalPiece.getType() == PieceType.KING) {
            ImmutXY from = move.getFrom();
            int y = from.getY();

            if (move.getTypes().contains(Move.MoveType.CASTLE_LONG)) {
                // Queenside castling (long)
                int rookFromX = 0;
                int rookToX = 3;
                Piece rook = newBoard.boardState[y][rookFromX];
                if (rook != null && rook.getType() == PieceType.ROOK && rook.isWhite() == originalPiece.isWhite()) {
                    Piece movedRook = Piece.makeNewPiece(rook.isWhite(), rook.getType(), y, rookToX);
                    newBoard.boardState[y][rookFromX] = null;
                    newBoard.boardState[y][rookToX] = movedRook;
                    movedRook.markAsMoved();
                }
            } else if (move.getTypes().contains(Move.MoveType.CASTLE_SHORT)) {
                // Kingside castling (short)
                int rookFromX = 7;
                int rookToX = 5;
                Piece rook = newBoard.boardState[y][rookFromX];
                if (rook != null && rook.getType() == PieceType.ROOK && rook.isWhite() == originalPiece.isWhite()) {
                    Piece movedRook = Piece.makeNewPiece(rook.isWhite(), rook.getType(), y, rookToX);
                    newBoard.boardState[y][rookFromX] = null;
                    newBoard.boardState[y][rookToX] = movedRook;
                    movedRook.markAsMoved();
                }
            }
        } else if (originalPiece.getType() == PieceType.PAWN) {
            if (move.getTypes().contains(MoveType.DOUBLE_PAWN)) {
                ImmutXY to = move.getTo();
                int direction = originalPiece.isWhite() ? 1 : -1;
                newBoard.enPassantTarget = new ImmutXY(to.getX(), to.getY()+direction);
            } else if (move.getTypes().contains(MoveType.EN_PASSANT)) {
                // remove the captured-by-en-passant pawn
                ImmutXY to = move.getTo();
                int direction = originalPiece.isWhite() ? 1 : -1;
                newBoard.boardState[to.getY()+direction][to.getX()] = null;
            }
        }

        ImmutXY from = move.getFrom();
        ImmutXY to = move.getTo();

        Piece.PieceType nextType = originalPiece.getType();
        if (move.getPromotionType() != null) {
            assert originalPiece.getType() == PieceType.PAWN;
            assert move.getTypes().contains(MoveType.PROMOTION);
            nextType = move.getPromotionType();
        }
        Piece movedPiece = Piece.makeNewPiece(originalPiece.isWhite(), nextType, to.getY(), to.getX());
        movedPiece.markAsMoved();

        newBoard.boardState[from.getY()][from.getX()] = null;
        newBoard.boardState[to.getY()][to.getX()] = movedPiece;

        newBoard.findKings();
        return newBoard;
    }

    public ImmutXY getEnPassantTarget() {
        // if the last move was a pawn move forward twice, then we can mark that as possible target
        return enPassantTarget;
    }

    // can black or white castle, despite whose turn it is
    public boolean canCastle(boolean isWhite, boolean kingside) {
        if ((isWhite && whiteKing.hasMoved()) || (!isWhite && blackKing.hasMoved())) return false;

        int row = isWhite ? 7 : 0;
        int col = kingside ? 7 : 0;
        Piece rook = boardState[row][col];
        if (rook == null || rook.getType() != PieceType.ROOK || rook.hasMoved()) return false;
        if (isKingInCheck(isWhite)) return false;

        int dir = kingside ? 1 : -1;
        int kingCol = isWhite ? whiteKing.getPos().getX() : blackKing.getPos().getX();
        if (verifier.isSquareUnderAttack(new ImmutXY(kingCol + dir, row), isWhite) ||
            verifier.isSquareUnderAttack(new ImmutXY(kingCol + 2 * dir, row), isWhite)) {
            return false;
        }
        return true;
    }

    public boolean isWhiteToPlay() { return whiteToPlay; }

    public ImmutXY getKingPos(boolean isWhite) {
        if (isWhite) {
            return whiteKing.getPos();
        }
        return blackKing.getPos();
    }

    public void findKings() {
        whiteKing = blackKing = null;
        int count = 0;
        for (int i=0; i<8; ++i) {
            for (int j=0; j<8; ++j) {
                Piece p = boardState[i][j];
                if (p == null || p.getType() != PieceType.KING) continue;
                if (p.isWhite()) {
                    whiteKing = p; ++count;
                } else {
                    blackKing = p; ++count;
                }
            }
        }
        if (whiteKing == null || blackKing == null || count != 2) {
            assert false : "White or black king was not found in the board";
        }
    }

    // is black/white's king in check
    public boolean isKingInCheck(boolean isWhite) {
        return verifier.isSquareUnderAttack(isWhite ? whiteKing.getPos() : blackKing.getPos(), isWhite);
    }

    public static boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }


    public List<Piece> getPossiblePieces(ImmutXY to, boolean isWhite) {
        List<Piece> l = new ArrayList<>();
        for (int i=0; i<8; ++i) {
            for (int j=0; j<8; ++j) {
                Piece p = boardState[i][j];
                if (p == null || p.isWhite() != isWhite) continue;
                for (Move move: p.calculatePossibleMoves(this)) {
                    if (move.getTo().equals(to)) {
                        l.add(p);
                    }
                }
            }
        }
        return l;
    }

    // used to see if there are any legal moves
    public List<Move> getLegalMoves() {
        List<Move> legalMoves = new ArrayList<>();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                Piece p = boardState[y][x];
                if (p != null && p.isWhite() == whiteToPlay) {
                    legalMoves.addAll(p.calculatePossibleMoves(this));
                }
            }
        }
        return legalMoves;
    }

    public Move getMoveIfValid(ImmutXY from, ImmutXY to) {
        Piece p = boardState[from.getY()][from.getX()];
        if (p == null || p.isWhite() != whiteToPlay) return null;

        for (Move move : p.calculatePossibleMoves(this)) {
            if (move.getTo().equals(to)) {
                return move;
            }
        }
        return null;
    }

    public void printBoard() {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (boardState[y][x] != null) {
                    System.out.print(boardState[y][x] + " ");
                } else {
                    System.out.print("   ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}

