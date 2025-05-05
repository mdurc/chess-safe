package chess.model.boardAnalysis;

import java.util.Arrays;

import chess.model.pieceData.Piece;
import chess.model.pieceData.Piece.PieceType;
import chess.utils.ImmutXY;

public class ChessVerifier {
    private final BoardState board;

    public ChessVerifier(BoardState board) { this.board = board; }


    // isWhite is the team that is being attacked by !isWhite
    public boolean isSquareUnderAttack(ImmutXY target, boolean isWhite) {
        // Check for pawn attacks
        int pawnDir = isWhite ? -1 : 1;
        int[][] pawnOffsets = {{-1, pawnDir}, {1, pawnDir}};
        for (int[] offset : pawnOffsets) {
            int x = target.getX() + offset[0];
            int y = target.getY() + offset[1];
            if (BoardState.isWithinBounds(x, y)) {
                Piece p = board.getPieceAt(y, x);
                if (p != null && p.isWhite() != isWhite && p.getType() == PieceType.PAWN) {
                    return true;
                }
            }
        }

        // Check for knight attacks
        int[][] knightOffsets = {{2,1}, {1,2}, {-1,2}, {-2,1}, {-2,-1}, {-1,-2}, {1,-2}, {2,-1}};
        for (int[] offset : knightOffsets) {
            int x = target.getX() + offset[0];
            int y = target.getY() + offset[1];
            if (BoardState.isWithinBounds(x, y)) {
                Piece p = board.getPieceAt(y, x);
                if (p != null && p.isWhite() != isWhite && p.getType() == PieceType.KNIGHT) {
                    return true;
                }
            }
        }
        
        int[][] kingOffsets = {{-1,-1}, {0,-1}, {1,-1}, {-1,0}, {1,0},{-1,1}, {0,1}, {1,1}};
        for (int[] offset : kingOffsets) {
            int x = target.getX() + offset[0];
            int y = target.getY() + offset[1];
            if (BoardState.isWithinBounds(x, y)) {
                Piece p = board.getPieceAt(y, x);
                if (p != null && p.isWhite() != isWhite && p.getType() == PieceType.KING) {
                    return true;
                }
            }
        }

        // Check for sliding pieces (rook, bishop, queen)
        int[][] rookDirs = {{0,1}, {1,0}, {0,-1}, {-1,0}};
        int[][] bishopDirs = {{1,1}, {1,-1}, {-1,1}, {-1,-1}};
        return checkSlidingAttack(target, isWhite, rookDirs, PieceType.ROOK, PieceType.QUEEN)
            || checkSlidingAttack(target, isWhite, bishopDirs, PieceType.BISHOP, PieceType.QUEEN);
    }

    private boolean checkSlidingAttack(ImmutXY target, boolean isWhite, int[][] dirs, PieceType... validTypes) {
        for (int[] dir : dirs) {
            int x = target.getX() + dir[0];
            int y = target.getY() + dir[1];
            while (BoardState.isWithinBounds(x, y)) {
                Piece p = board.getPieceAt(y, x);
                if (p != null) {
                    if (p.isWhite() != isWhite && Arrays.asList(validTypes).contains(p.getType())) {
                        return true;
                    }
                    break; // Blocked by a piece
                }
                x += dir[0];
                y += dir[1];
            }
        }
        return false;
    }
}
