package chess.view;

import chess.model.util.*;
import chess.controller.ChessController;
import chess.model.pieceData.Piece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BoardPanel extends JPanel {
    private final int tileSize;
    private final ChessController controller;
    private final Color HIGHLIGHT_COLOR = new Color(100, 200, 100, 100);
    private final Color LIGHT_COLOR = new Color(238, 238, 210);
    private final Color DARK_COLOR = new Color(118, 150, 86);

    protected BoardPanel(ChessController controller, int tileSize) {
        this.tileSize = tileSize;
        this.controller = controller;
        setupEventListeners();
        setPreferredSize(new Dimension(tileSize * 8, tileSize * 8));
    }

    protected int getTileSize() { return tileSize; }

    private void setupEventListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                controller.handlePieceSelection(new ImmutXY(e.getPoint()));
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (controller.handlePieceDrop(new ImmutXY(e.getPoint()))) {
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (controller.getDraggedPiece() != null) {
                    controller.handleMouseDrag(new ImmutXY(e.getPoint()));
                    repaint();
                }
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                controller.handleKeyPress(e);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // pull data model for getting data from model via controller
        drawBoard(g);
        drawSelectionHighlight(g);
        drawPieces(g);
        drawDraggedPiece(g);
    }

    protected void flipBoard() {
    }

    private void drawBoard(Graphics g) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                drawSquare(g, row, col);
            }
        }
    }

    private void drawSquare(Graphics g, int modelRow, int modelCol) {
        boolean orientation = controller.getBoardOrientation();
        int viewCol = orientation ? modelCol : (7 - modelCol);
        int viewRow = orientation ? modelRow : (7 - modelRow);
        boolean isLight = (viewRow + viewCol) % 2 == 0;
        g.setColor(isLight ? LIGHT_COLOR : DARK_COLOR);
        g.fillRect(viewCol * tileSize, viewRow * tileSize, tileSize, tileSize);
    }

    private void drawSelectionHighlight(Graphics g) {
        ImmutXY selectedSquare = controller.getSelectedPieceLocation();
        if (selectedSquare != null) {
            boolean orientation = controller.getBoardOrientation();
            int modelCol = selectedSquare.getX();
            int modelRow = selectedSquare.getY();
            int viewCol = orientation ? modelCol : (7 - modelCol);
            int viewRow = orientation ? modelRow : (7 - modelRow);
            g.setColor(HIGHLIGHT_COLOR);
            g.fillRect(viewCol * tileSize, viewRow * tileSize, tileSize, tileSize);
        }
    }

    private void drawPiece(Graphics g, String name, int x, int y) {
        Image image = PieceImages.getPieceImage(name);
        if (image != null) {
            g.drawImage(image, x, y, null);
        }
    }

    private void drawDraggedPiece(Graphics g) {
        ImmutXY currentDragPoint = controller.getCurrentDragPoint();
        Piece draggedPiece = controller.getDraggedPiece();
        if (draggedPiece != null && currentDragPoint != null) {
            Image image = PieceImages.getPieceImage(draggedPiece.toString());
            if (image != null) {
                // Draw piece centered under mouse cursor
                int x = currentDragPoint.getX() - (tileSize/2);
                int y = currentDragPoint.getY() - (tileSize/2);
                g.drawImage(image, x, y, null);
            }
        }
    }

    private void drawPieces(Graphics g) {
        ImmutXY selectedSquare = controller.getSelectedPieceLocation();
        ImmutXY currentDragPoint = controller.getCurrentDragPoint();
        for (int modelRow = 0; modelRow < 8; modelRow++) {
            for (int modelCol = 0; modelCol < 8; modelCol++) {
                Piece piece = controller.getPieceAt(modelRow, modelCol);
                if (piece != null) {
                    if (selectedSquare != null &&
                        selectedSquare.getX() == modelCol &&
                        selectedSquare.getY() == modelRow &&
                        currentDragPoint != null) {
                        continue;
                    }
                    boolean orientation = controller.getBoardOrientation();
                    int viewCol = orientation ? modelCol : (7 - modelCol);
                    int viewRow = orientation ? modelRow : (7 - modelRow);
                    drawPiece(g, piece.toString(), viewCol * tileSize, viewRow * tileSize);
                }
            }
        }
    }
}
