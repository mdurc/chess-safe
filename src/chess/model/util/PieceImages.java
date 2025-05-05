package chess.model.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PieceImages {
    private static final Map<String, Image> pieces = new HashMap<>();
    private static final int PIECE_SIZE = 80;

    static {
        try {
            loadPieces();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load piece images", e);
        }
    }

    private static void loadPieces() throws IOException {
        String[] colors = {"w", "b"};
        String[] types = {"k", "q", "r", "b", "n", "p"};

        for (String color : colors) {
            for (String type : types) {
                String key = color + type;
                try {
                    pieces.put(key, loadImage("/pieces/" + key + ".png"));
                } catch (IOException e) {
                    System.err.println("Error loading piece: " + key + " - " + e.getMessage());
                    throw e;
                }
            }
        }
    }

    private static Image loadImage(String path) throws IOException {
        return ImageIO.read(PieceImages.class.getResourceAsStream(path))
                     .getScaledInstance(PIECE_SIZE, PIECE_SIZE, Image.SCALE_SMOOTH);
    }

    public static Image getPieceImage(String name) {
        return pieces.get(name);
    }
}
