package chess;

import javax.swing.SwingUtilities;

import chess.controller.ChessController;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;

public class Main {
    public static void main(String[] args) {
        new JFXPanel();
        Platform.runLater(() -> {
            SwingUtilities.invokeLater(() -> {
                new ChessController();
            });
        });
    }
}
