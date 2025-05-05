package chess.controller;

import chess.view.MainFrame;
import chess.model.pieceData.Piece;

import javax.swing.JOptionPane;
import chess.model.*;
import chess.model.util.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.io.*;

public class ChessController {
    private ChessGame currentGame;
    private GameNode currentPosition; // node within the current Game
    private GameLibrary gameLibrary;
    private MainFrame view;

    private ImmutXY selectedSquare = null;
    private Piece draggedPiece = null;
    private ImmutXY currentDragPoint = null;

    private boolean boardOrientation = true; // true is white at the bottom

    public ChessController() {
        startNewGame();
        gameLibrary = new GameLibrary();
        view = new MainFrame(this);

        // initialize the sound playing to remove delay
        SoundManager.playSound(SoundManager.SoundType.APP_LOAD);
    }

    public void startNewGame() {
        currentGame = new ChessGame();
        currentPosition = currentGame.getFirstPosition();
        if (view != null) {
            refresh();
        }
    }

    private void refresh() { view.updateBoard(); view.updateHistory(); }
    public void focusBoard() { view.focusBoard(); }

    public ChessGame getCurrentGame() { return currentGame; }
    public GameNode getCurrentPosition() { return currentPosition; }
    public void setCurrentGame(ChessGame game) { this.currentGame = game; refresh(); }
    public void setCurrentPosition(GameNode position) { this.currentPosition = position; refresh(); }

    public Piece getPieceAt(int row, int col) { return currentPosition.getPieceAt(row, col); }
    public ImmutXY getSelectedPieceLocation() { return selectedSquare; }
    public ImmutXY getCurrentDragPoint() { return currentDragPoint; }
    public Piece getDraggedPiece() { return draggedPiece; }
    public boolean getBoardOrientation() { return boardOrientation; }

    public void handleKeyPress(KeyEvent e) {
        GameNode nextPos = null;
        boolean skipSound = false;
        switch(e.getKeyCode()) {
            case 37: // left
                nextPos = currentPosition.getParentNode(); skipSound = true;
                break;
            case 38: // up
                nextPos = currentGame.getLastPosition();
                break;
            case 39: // right
                nextPos = currentPosition.getNextChild();
                break;
            case 40: // down
                nextPos = currentGame.getFirstPosition();
                break;
            case 70: // 'f'
                view.flipBoard();
                view.updateBoard();
                boardOrientation = !boardOrientation;
                break;
        }
        if (nextPos != null && !nextPos.equals(currentPosition)) {
            currentPosition = nextPos;
            if (!skipSound) {
                if (currentPosition.equals(currentGame.getFirstPosition())) {
                    SoundManager.playSound(SoundManager.SoundType.MOVE_SELF);
                } else {
                    SoundManager.playSoundForMove(currentPosition.getMove());
                }
            }
            refresh();
        }
    }

    public void handleMouseDrag(ImmutXY mouseLocation) {
        if (selectedSquare != null) {
            currentDragPoint = mouseLocation;
        }
    }

    public void clearDragState() {
        selectedSquare = null;
        draggedPiece = null;
        currentDragPoint = null;
        view.updateBoard();
    }

    public Move handleMove(ImmutXY from, ImmutXY to) {
        Move m = currentPosition.getMoveIfValid(from, to);
        if (m == null) return null;

        currentPosition = currentPosition.addNode(m);

        System.out.println(currentPosition.getNotation());

        refresh();
        return m;
    }

    public void handlePieceSelection(ImmutXY mouseLocation) {
        int tileSize = view.getTileSize();
        int viewCol = mouseLocation.getX() / tileSize;
        int viewRow = mouseLocation.getY() / tileSize;
        int modelCol = boardOrientation ? viewCol : (7 - viewCol);
        int modelRow = boardOrientation ? viewRow : (7 - viewRow);
        Piece piece = currentPosition.getPieceAt(modelRow, modelCol);
        if (piece != null) {
            selectedSquare = new ImmutXY(modelCol, modelRow);
            draggedPiece = piece;
            currentDragPoint = mouseLocation;
        }
    }

    public boolean handlePieceDrop(ImmutXY mouseLocation) {
        if (selectedSquare == null){
            return false;
        }
        int tileSize = view.getTileSize();
        int modelFromCol = selectedSquare.getX();
        int modelFromRow = selectedSquare.getY();

        int viewToCol = mouseLocation.getX() / tileSize;
        int viewToRow = mouseLocation.getY() / tileSize;
        int modelToCol = boardOrientation ? viewToCol : (7 - viewToCol);
        int modelToRow = boardOrientation ? viewToRow : (7 - viewToRow);

        if (modelFromCol == modelToCol && modelFromRow == modelToRow) {
            // Clicked but didn't move - just deselect
            clearDragState(); // clear drag and repaint board
            return false;
        }

        if (modelToCol < 0 || modelToCol >= 8 || modelToRow < 0 || modelToRow >= 8) {
            SoundManager.playSound(SoundManager.SoundType.ILLEGAL);
            clearDragState(); // clear drag and repaint board
            return false;
        }

        Move move = handleMove(new ImmutXY(modelFromCol, modelFromRow), new ImmutXY(modelToCol, modelToRow));

        if (move == null) {
            clearDragState();
            return false;
        }

        SoundManager.playSoundForMove(move);
        clearDragState();
        return true;
    }


    public List<String> getLibrarySavedGames() { return gameLibrary.getSavedGames(); }
    public void deleteGameFromLibrary(String name) { gameLibrary.deleteGame(name); }

    public void saveCurrentGameToLibrary(String name) {
        try {
            gameLibrary.saveGame(name, currentGame);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "Error saving game: " + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadGameFromLibrary(String name) {
        try {
            ChessGame loadedGame = gameLibrary.loadGame(name);
            currentGame = loadedGame;
            currentPosition = loadedGame.getFirstPosition();
            view.focusBoard();
            refresh();
            loadedGame.printMainline();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Error loading game: " + e.getMessage(),
                "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
