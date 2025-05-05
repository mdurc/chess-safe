package chess.model.util;

import chess.model.pieceData.Piece;
import chess.model.pieceData.Piece.PieceType;
import chess.model.util.ImmutXY;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

import chess.model.GameNode;
import chess.model.Move;
import chess.model.boardData.*;

import java.util.HashSet;


public class NotationParser {
    // Encode a move object into algebraic notation String
    public static String convertToNotation(Move move, GameNode currentNode) {
        if (move == null || currentNode == null) return "";

        StringBuilder notation = new StringBuilder();

        List<Move.MoveType> moveTypes = move.getTypes();
        if (moveTypes.contains(Move.MoveType.CASTLE_SHORT)) {
            notation.append("O-O");
        } else if (moveTypes.contains(Move.MoveType.CASTLE_LONG)) {
            notation.append("O-O-O");
        } else {
            Piece movingPiece = move.getPiece();
            PieceType type = movingPiece.getType();
            boolean capture = moveTypes.contains(Move.MoveType.CAPTURE);

            ImmutXY from = move.getFrom();
            ImmutXY to = move.getTo();
            String destSquare = to.toAlgebraic();

            boolean hasPromotion = moveTypes.contains(Move.MoveType.PROMOTION);
            PieceType promotionType = move.getPromotionType();

            if (type == PieceType.PAWN) {
                if (capture) {
                    String fromFile = from.getAlgebraicFile();
                    notation.append(fromFile).append("x");
                }
                notation.append(destSquare);
                if (hasPromotion) {
                    notation.append("=").append(getPieceSymbol(promotionType));
                }
            } else {
                notation.append(getPieceSymbol(type));

                List<Piece> candidates = new ArrayList<>();
                for (Piece p: currentNode.getPossiblePieces(to, movingPiece.isWhite())) {
                    if (p.getType() == type) {
                        candidates.add(p);
                    }
                }

                if (candidates.size() > 1) {
                    String disambig = getDisambiguation(movingPiece, candidates);
                    notation.append(disambig);
                }

                if (capture) {
                    notation.append("x");
                }
                notation.append(destSquare);
            }
        }

        BoardState newState = currentNode.makeNewChange(move);
        boolean isCheck = newState.isKingInCheck(newState.isWhiteToPlay());
        List<Move> legalMoves = newState.getLegalMoves();
        boolean isCheckmate = isCheck && legalMoves.isEmpty();

        if (isCheckmate) {
            notation.append("#");
        } else if (isCheck) {
            notation.append("+");
        }

        return notation.toString();
    }

    private static String getPieceSymbol(PieceType type) {
        switch (type) {
            case KING: return "K";
            case QUEEN: return "Q";
            case ROOK: return "R";
            case BISHOP: return "B";
            case KNIGHT: return "N";
            case PAWN: return "";
            default: throw new IllegalArgumentException("Invalid piece type");
        }
    }

    private static String getDisambiguation(Piece movingPiece, List<Piece> candidates) {
        ImmutXY pos = movingPiece.getPos();
        int fromX = pos.getX();
        int fromY = pos.getY();

        Set<Integer> candidateFiles = new HashSet<>();
        Set<Integer> candidateRanks = new HashSet<>();
        for (Piece p : candidates) {
            candidateFiles.add(p.getPos().getX());
            candidateRanks.add(p.getPos().getY());
        }

        if (candidateFiles.size() == candidates.size()) {
            return pos.getAlgebraicFile();
        } else if (candidateRanks.size() == candidates.size()) {
            return String.valueOf(8 - fromY);
        } else {
            boolean fileUnique = true;
            for (Piece p : candidates) {
                if (p != movingPiece && p.getPos().getX() == fromX) {
                    fileUnique = false;
                    break;
                }
            }
            if (fileUnique) {
                return pos.getAlgebraicFile();
            } else {
                boolean rankUnique = true;
                for (Piece p : candidates) {
                    if (p != movingPiece && p.getPos().getY() == fromY) {
                        rankUnique = false;
                        break;
                    }
                }
                if (rankUnique) {
                    return String.valueOf(8 - fromY);
                } else {
                    return pos.getAlgebraicFile() + (8 - fromY);
                }
            }
        }
    }


    // Decode algebraic notation into a Move object
    // We need a currentNode to gain context of the state of the board on this move
    public static Move parseMove(String notation, GameNode currentNode) {
        if (notation == null || notation.isEmpty()) return null;

        boolean wtm = currentNode.isWhiteToPlay();

        // Check to see if it was a castle, then it was the king that moved.
        int lastO = notation.lastIndexOf('O');
        if (lastO != -1) {
            Move.MoveType castleType = (lastO == 2) ? Move.MoveType.CASTLE_SHORT : Move.MoveType.CASTLE_LONG;

            ImmutXY kingPos = currentNode.getKingPos(wtm);
            assert kingPos != null;

            Piece king = currentNode.getPieceAt(kingPos.getY(), kingPos.getX());
            assert king != null;

            int newX = castleType == Move.MoveType.CASTLE_SHORT ? 6 : 2;
            ImmutXY to = new ImmutXY(newX, kingPos.getY());
            return new Move(king, kingPos, to, List.of(castleType), null);
        }

        // Otherwise we want to make a standard move, parsing from the back
        List<Move.MoveType> moveTypes = new ArrayList<>();
        String originalNotation = notation;

        // check and checkmate
        char endChar = notation.charAt(notation.length()-1);
        if (endChar == '+') {
            moveTypes.add(Move.MoveType.CHECK);
            notation = notation.substring(0, notation.length() - 1);
        } else if (endChar == '#') {
            moveTypes.add(Move.MoveType.CHECKMATE);
            notation = notation.substring(0, notation.length() - 1);
        }

        // promotion
        String promotionPiece = null;
        int eqIndex = notation.indexOf('=');
        if (eqIndex != -1) {
            promotionPiece = notation.substring(eqIndex + 1);
            notation = notation.substring(0, eqIndex);
            moveTypes.add(Move.MoveType.PROMOTION);
        }

        // captures
        boolean capture = notation.contains("x");
        if (capture) {
            moveTypes.add(Move.MoveType.CAPTURE);
            notation = notation.replace("x", "");
        }

        // this moved piece type
        char t = notation.charAt(0);
        Piece.PieceType pieceType;
        switch (t) {
            case 'R': pieceType = Piece.PieceType.ROOK; break;
            case 'N': pieceType = Piece.PieceType.KNIGHT; break;
            case 'B': pieceType = Piece.PieceType.BISHOP; break;
            case 'Q': pieceType = Piece.PieceType.QUEEN; break;
            case 'K': pieceType = Piece.PieceType.KING; break;
            default:  pieceType = Piece.PieceType.PAWN; break;
        }

        // find destination square
        String destination = null;
        int destStart = -1;
        for (int i = notation.length() - 1; i >= 1; i--) {
            char c = notation.charAt(i);
            char prev = notation.charAt(i - 1);
            if ((prev >= 'a' && prev <= 'h') && (c >= '1' && c <= '8')) {
                destStart = i - 1;
                destination = notation.substring(destStart, destStart + 2);
                break;
            }
        }
        if (destination == null) {
            throw new IllegalArgumentException("Error: No destination in " + originalNotation);
        }

        int destX = destination.charAt(0) - 'a';
        int destY = 8 - Integer.parseInt(destination.substring(1));
        ImmutXY to = new ImmutXY(destX, destY);

        String remainingAfterDest = notation.substring(0, destStart);

        // Handle possibly ambiguous notation
        String disambig = "";
        if (pieceType != Piece.PieceType.PAWN) {
            disambig = remainingAfterDest.length() > 1 ? remainingAfterDest.substring(1) : "";
        } else if (capture) {
            disambig = remainingAfterDest.isEmpty() ? "" : remainingAfterDest;
        }

        // find all possible pieces that could have reached the destination square
        List<Piece> allPossiblePieces = currentNode.getPossiblePieces(to, wtm);

        List<Piece> candidates = new ArrayList<>();
        for (Piece p: allPossiblePieces) {
            if (p.getType() == pieceType) {
                candidates.add(p);
            }
        }

        List<Piece> filtered = new ArrayList<>();
        for (Piece p : candidates) {
            ImmutXY pos = p.getPos();
            boolean match = true;
            for (int i = 0; i < disambig.length(); i++) {
                char c = disambig.charAt(i);
                if (Character.isLetter(c)) {
                    int expectedX = c - 'a';
                    if (pos.getX() != expectedX) {
                        match = false;
                        break;
                    }
                } else if (Character.isDigit(c)) {
                    int expectedY = 8 - Character.getNumericValue(c);
                    if (pos.getY() != expectedY) {
                        match = false;
                        break;
                    }
                } else {
                    match = false;
                    break;
                }
            }
            if (match) {
                filtered.add(p);
            }
        }

        if (filtered.isEmpty()) {
            if (allPossiblePieces.isEmpty()) {
                throw new IllegalArgumentException("Absolutely no matching pieces from this move: " + originalNotation);
            } else {
                System.out.println(allPossiblePieces);
            }
            throw new IllegalArgumentException("Error: No matching pieces for filtered move: " + originalNotation);
        } else if (filtered.size() > 1) {
            throw new IllegalArgumentException("Error: Ambiguous move " + originalNotation + ", multiple candidates: " + filtered);
        }

        Piece movingPiece = filtered.get(0);
        if (movingPiece == null) {
            throw new IllegalArgumentException("Error: Moving piece is null for " + originalNotation);
        }

        ImmutXY from = movingPiece.getPos();

        // Check if this is an en passant move
        if (movingPiece.getType() == Piece.PieceType.PAWN && capture) {
            ImmutXY enPassantSquare = currentNode.getEnPassantTarget();
            if (enPassantSquare != null && enPassantSquare.equals(to)) {
                moveTypes.add(Move.MoveType.EN_PASSANT);
            }
        }

        // check if it is a double pawn move
        if (movingPiece.getType() == Piece.PieceType.PAWN) {
            int deltaY = Math.abs(to.getY() - from.getY());
            int startRank = wtm ? 6 : 1;
            if (deltaY == 2 && from.getY() == startRank) { moveTypes.add(Move.MoveType.DOUBLE_PAWN);
            }
        }

        // Handle promotion piece
        Piece.PieceType promotedTo = null;
        if (promotionPiece != null) {
            switch (promotionPiece) {
                case "Q": promotedTo = Piece.PieceType.QUEEN; break;
                case "R": promotedTo = Piece.PieceType.ROOK; break;
                case "B": promotedTo = Piece.PieceType.BISHOP; break;
                case "N": promotedTo = Piece.PieceType.KNIGHT; break;
                default: throw new IllegalArgumentException("Invalid promotion: " + promotionPiece);
            }
        }

        if (moveTypes.isEmpty()) {
            moveTypes.add(Move.MoveType.REGULAR);
        }

        return new Move(movingPiece, from, to, moveTypes, promotedTo);
    }
}
