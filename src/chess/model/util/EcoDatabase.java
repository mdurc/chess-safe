package chess.model.util;

import java.util.HashMap;
import java.util.Map;

// Rudimentary ECO (Encyclopedia of Chess Openings) database for openings
public class EcoDatabase {
    private static final Map<String, String> OPENING_MOVES = new HashMap<>();

    static {
        // A00-A99: Irregular Openings
        OPENING_MOVES.put("d4", "A00"); // Queen's Pawn Game
        OPENING_MOVES.put("d4 d5", "A00"); // Queen's Pawn Game
        OPENING_MOVES.put("d4 d5 c4", "D00"); // Queen's Gambit
        OPENING_MOVES.put("d4 d5 c4 e6", "D30"); // Queen's Gambit Declined
        OPENING_MOVES.put("d4 d5 c4 c6", "D10"); // Slav Defense
        OPENING_MOVES.put("d4 d5 c4 dxc4", "D20"); // Queen's Gambit Accepted

        // B00-B99: Semi-Open Games
        OPENING_MOVES.put("e4", "B00"); // King's Pawn Game
        OPENING_MOVES.put("e4 e5", "C00"); // Open Game
        OPENING_MOVES.put("e4 e5 Nf3", "C20"); // Open Game
        OPENING_MOVES.put("e4 e5 Nf3 Nc6", "C40"); // Open Game
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 Bc4", "C50"); // Italian Game
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 Bb5", "C60"); // Ruy Lopez
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 d4", "C30"); // Scotch Game
        OPENING_MOVES.put("e4 e5 Nf3 d6", "C40"); // Philidor Defense
        OPENING_MOVES.put("e4 e5 Nf3 Nf6", "C40"); // Petrov Defense
        OPENING_MOVES.put("e4 e5 Bc4", "C20"); // Bishop's Opening
        OPENING_MOVES.put("e4 e5 Nc3", "C20"); // Vienna Game
        OPENING_MOVES.put("e4 e5 f4", "C30"); // King's Gambit
        OPENING_MOVES.put("e4 e5 f4 exf4", "C30"); // King's Gambit Accepted
        OPENING_MOVES.put("e4 e5 f4 Bc5", "C30"); // King's Gambit Declined

        // Semi-Open Games (Black doesn't play 1...e5)
        OPENING_MOVES.put("e4 c5", "B20"); // Sicilian Defense
        OPENING_MOVES.put("e4 c5 Nf3", "B20"); // Sicilian Defense
        OPENING_MOVES.put("e4 c5 Nf3 d6", "B50"); // Sicilian Defense
        OPENING_MOVES.put("e4 c5 Nf3 e6", "B40"); // Sicilian Defense
        OPENING_MOVES.put("e4 c5 Nf3 Nc6", "B30"); // Sicilian Defense
        OPENING_MOVES.put("e4 c6", "B10"); // Caro-Kann Defense
        OPENING_MOVES.put("e4 d6", "B00"); // Pirc Defense
        OPENING_MOVES.put("e4 d5", "B00"); // Center Counter
        OPENING_MOVES.put("e4 Nf6", "B00"); // Alekhine Defense
        OPENING_MOVES.put("e4 g6", "B00"); // Modern Defense

        // C00-C99: Open Games and Semi-Open Games
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 Bb5 a6", "C60"); // Ruy Lopez
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 Bb5 a6 Ba4", "C60"); // Ruy Lopez
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 Bb5 a6 Ba4 Nf6", "C60"); // Ruy Lopez
        OPENING_MOVES.put("e4 e5 Nf3 Nc6 Bb5 a6 Ba4 Nf6 O-O", "C60"); // Ruy Lopez

        // D00-D99: Closed Games
        OPENING_MOVES.put("d4 Nf6", "A40"); // Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4", "A40"); // Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 e6", "E00"); // Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 g6", "E60"); // King's Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 g6 Nc3", "E60"); // King's Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 g6 Nc3 Bg7", "E60"); // King's Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 e6 Nc3", "E00"); // Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 e6 Nc3 Bb4", "E20"); // Nimzo-Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 e6 Nf3", "E00"); // Indian Defense
        OPENING_MOVES.put("d4 Nf6 c4 e6 Nf3 b6", "E10"); // Queen's Indian Defense

        // E00-E99: Indian Defenses
        OPENING_MOVES.put("d4 Nf6 c4 e6 g3", "E00"); // Catalan Opening
        OPENING_MOVES.put("d4 Nf6 c4 e6 g3 d5", "E00"); // Catalan Opening
        OPENING_MOVES.put("d4 Nf6 c4 e6 g3 d5 Bg2", "E00"); // Catalan Opening
    }

    // tries to find the longest prefix of the openings in the database
    public static String getEcoCode(String fullMoves) {
        if (fullMoves == null || fullMoves.isEmpty()) return null;

        String[] tokens = fullMoves.trim().split("\\s+");
        StringBuilder prefix = new StringBuilder();
        String longestMatch = null;

        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) prefix.append(" ");
            prefix.append(tokens[i]);

            String code = OPENING_MOVES.get(prefix.toString());
            if (code != null) {
                longestMatch = code;
            }
        }

        return longestMatch;
    }

    public static String getOpeningName(String ecoCode) {
        if (ecoCode == null) return "Unknown Opening";

        switch (ecoCode) {
            case "A00": return "Queen's Pawn Game";
            case "B00": return "King's Pawn Game";
            case "B10": return "Caro-Kann Defense";
            case "B20": return "Sicilian Defense";
            case "B30": return "Sicilian Defense";
            case "B40": return "Sicilian Defense";
            case "B50": return "Sicilian Defense";
            case "C00": return "Open Game";
            case "C20": return "Open Game";
            case "C30": return "King's Gambit";
            case "C40": return "Open Game";
            case "C50": return "Italian Game";
            case "C60": return "Ruy Lopez";
            case "D00": return "Queen's Gambit";
            case "D10": return "Slav Defense";
            case "D20": return "Queen's Gambit Accepted";
            case "D30": return "Queen's Gambit Declined";
            case "E00": return "Catalan Opening";
            case "E10": return "Queen's Indian Defense";
            case "E20": return "Nimzo-Indian Defense";
            case "E60": return "King's Indian Defense";
            default: return "Unknown Opening";
        }
    }
}
