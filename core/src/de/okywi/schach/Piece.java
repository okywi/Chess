package de.okywi.schach;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import de.okywi.schach.Schach.*;

import static de.okywi.schach.Schach.*;
import static de.okywi.schach.Schach.BOARD;

public class Piece extends Texture {
    String type;
    int x;
    int y;
    public static boolean checkmate = false;
    public static boolean isChecked = false;
    public static String checkedPlayer = "";
    boolean isSelected = false;
    boolean wasSelected = false;
    boolean isDragged = false;
    boolean pawnFirstMove = true;
    ArrayList<Integer[]> placeableFields = new ArrayList<>();
    ArrayList<Integer[]> attackedFields = new ArrayList<>();
    TextureRegion tr;
    Texture selected;
    TextureRegion trSelected;
    Texture available;
    TextureRegion trAvailable;
    Texture killable;
    TextureRegion trKillable;

    Piece(String type, int x, int y) {
        super(Gdx.files.internal("images/" + type + ".png"));
        this.type = type;
        this.x = x;
        this.y = y;

        // Texture Regions and Textures
        tr = new TextureRegion(this, TILE_SIZE, TILE_SIZE); // Fix flipped coordinate system
        tr.flip(false, true);

        selected = new Texture("images/selected.png");
        trSelected = new TextureRegion(selected, TILE_SIZE, TILE_SIZE);

        available = new Texture("images/available.png");
        trAvailable = new TextureRegion(available, TILE_SIZE, TILE_SIZE);

        killable = new Texture("images/killable.png");
        trKillable = new TextureRegion(killable, TILE_SIZE, TILE_SIZE);
    }

    public boolean checkAvailableFields(int x, int y) {
        // Check if move is valid
        boolean available = false;

        for (Integer[] field : placeableFields) {
            if (field[0] == x && field[1] == y) {
                available = true;
            }
        }

        if (!available) {
            isDragged = false;
            isSelected = false;
            return false;
        } else {
            return true;
        }
    }

    public void placeOnBoard(int x, int y) {
        if (!checkAvailableFields(x, y)) return; // Check if move is valid

        // King cant move into other kings radius -> WORKS
        for (Integer[] field : placeableFields) {
            if (this.type.contains("K") && BOARD[field[1]][field[0]].contains("K")) {
                isDragged = false;
                isSelected = false;
                return;
            }
        }

        BOARD[this.y][this.x] = ""; // replace piece on board

        this.x = x;
        this.y = y;

        BOARD[y][x] = this.type; // Replace other piece on board

        Piece removedPiece = null;

        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).x == x && pieces.get(i).y == y && pieces.get(i) != this) {
                removedPiece = pieces.get(i);
            }
        }

        pieces.remove(removedPiece);

        // Switch players
        switchPlayers();

        isDragged = false;
        isSelected = false;
        wasSelected = false;
        pawnFirstMove = false;
    }

    public void releasePiece(int x, int y) {
        String backupPieceType = BOARD[y][x];
        Piece backupPiece = null;
        for (Piece piece : pieces) {
            if (piece.x == x && piece.y == y) {
                backupPiece = piece;
            }
        }
        int backupX = this.x;
        int backupY = this.y;

        BOARD[this.y][this.x] = ""; // replace piece on board

        this.x = x;
        this.y = y;

        BOARD[y][x] = this.type; // Replace other piece on board

        System.out.println(checkedPlayer + " " + currentPlayer);
        System.out.println(BOARD[y][x]);

        Piece removedPiece = null;

        for (Piece piece : pieces) {
            if (piece.x == x && piece.y == y && piece != this) {
                removedPiece = piece;
            }
        }

        pieces.remove(removedPiece);

        // CHECK IF CHECK WILL BE REMOVED
        if (!handleCheck()) {
            if (this.type.contains("K")) { // FIX RANDOM BUG
                switchPlayers();
            }
            placeOnBoard(x, y);
            handleCheckmate(BOARD[y][x]);
            return;
        }

        // CHECK IF CHECK WILL HAPPEN WHEN MOVED
        if (isChecked && handleCheck() && checkedPlayer.equals(currentPlayer)) {
            System.out.println("MOVE BACK");
            BOARD[this.y][this.x] = backupPieceType;
            this.x = backupX;
            this.y = backupY;
            if (backupPiece != null){
                pieces.add(backupPiece);
            }
            BOARD[backupY][backupX] = this.type;
            isSelected = false;
            isDragged = false;
            return;
        }

        if (handleCheck()) {
            isChecked = true;
            System.out.println("IS CHECKED");
            placeOnBoard(x, y);
            handleCheckmate(BOARD[y][x]);
            return;
        }

        placeOnBoard(x, y);
    }

    public boolean handleCheckmate(String movedPieceType) {
        boolean checkmate = true;

        if (!isChecked) {
            return false;
        }

        for (Piece piece : pieces) {
            ArrayList<Integer[]> validPieceFields = new ArrayList<>();

            // Only get available pieces from enemy so only they can move.
            if (!movedPieceType.isEmpty()) {
                if (!piece.type.contains(movedPieceType.substring(0, 1))) {
                    validPieceFields = piece.findValidPieces(piece.x, piece.y, piece.type);
                }
            }


            for (Integer[] validField : validPieceFields) {
                if (BOARD[validField[1]][validField[0]].contains("K")) {
                    break;
                }

                String backupPieceType = BOARD[validField[1]][validField[0]];
                int backupX = piece.x;
                int backupY = piece.y;


                BOARD[piece.y][piece.x] = ""; // replace piece on board

                piece.x = validField[0];
                piece.y = validField[1];

                BOARD[validField[1]][validField[0]] = piece.type; // Replace other piece on board

                for (int i = 0; i < 8; i++) {
                    System.out.println(Arrays.toString(BOARD[i]));
                }

                if (piece.type.equals("bK")) {
                    for (int i = 0; i < validField.length - 1; i++) {
                        System.out.println("IS KING: " + Arrays.toString(validPieceFields.get(i)));
                    }
                }

                // CHECK FOR CHECK
                if (!handleCheck()) {
                    checkmate = false;
                }

                BOARD[validField[1]][validField[0]] = backupPieceType;
                piece.x = backupX;
                piece.y = backupY;

                BOARD[backupY][backupX] = piece.type;
            }
        }

        Piece.checkmate = checkmate;

        return checkmate;
    }

    public void handleInput() {
        if (checkmate) {
            return;
        }

        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();

        int x = mouseX / TILE_SIZE;
        int y = mouseY / TILE_SIZE;

        boolean onSelf = false;

        if (this.x == x && this.y == y) {
            onSelf = true;
        }

        if (wasSelected && !isDragged) { // check if pawn was selected and is not dragging -> deselect
            isSelected = false;
            wasSelected = false;
        }

        if (!Gdx.input.isButtonPressed(0) && isSelected && isDragged && !onSelf) { // Handle releasing
            if (!checkAvailableFields(x, y)) return;
            releasePiece(x, y);
            System.out.println(checkmate);
        }

        if (!Gdx.input.isButtonPressed(0) && isSelected && onSelf) { // Not dragging when just clicked pawn
            isDragged = false;
        }

        if (Gdx.input.isButtonJustPressed(0) && !onSelf && isSelected) { // If button is only clicked and then moved
            if (!checkAvailableFields(x, y)) return;
            releasePiece(x, y);
            System.out.println(checkmate);
        }

        if (isSelected && Gdx.input.isButtonPressed(0)) { // Check if mouse is held and pawn selected -> dragging
            isDragged = true;
        }

        if (isSelected && Gdx.input.isButtonJustPressed(0) && onSelf) { // check if pawn was selected to determine if it should be deselected
            wasSelected = true;
        }

        if (Gdx.input.isButtonJustPressed(0) && onSelf) { // register select on click
            if (BOARD[y][x].contains(currentPlayer)) {
                isSelected = true;
                placeableFields = findValidPieces(this.x, this.y, this.type);
            }
        }
    }

    public boolean handleCheck() {
        attackedFields.clear();
        boolean isCheck = false;

        String backupCurrentPlayer = currentPlayer;
        String backupOppositePlayer = oppositePlayer;

        for (Piece piece : pieces) {
            currentPlayer = piece.type.substring(0, 1);
            if (currentPlayer.equals("w")) {
                oppositePlayer = "b";
            } else {
                oppositePlayer = "w";
            }
            ArrayList<Integer[]> attackedPieceFields;
            attackedPieceFields = piece.findValidPieces(piece.x, piece.y, piece.type);
            attackedFields.addAll(attackedPieceFields);

            for (Integer[] field : attackedPieceFields) {
                if (BOARD[field[1]][field[0]].contains(oppositePlayer + "K")) {
                    isCheck = true;
                    checkedPlayer = oppositePlayer;
                    isChecked = true;
                }
            }
        }

        if (!isCheck) {
            checkedPlayer = "";
            isChecked = false;
        }

        currentPlayer = backupCurrentPlayer;
        oppositePlayer = backupOppositePlayer;

        return isCheck;
    }

    public void drawMoves(SpriteBatch batch) {
        for (Integer[] field : placeableFields) {
            if (isSelected) {
                if (BOARD[field[1]][field[0]].equals("")) {
                    batch.draw(trAvailable, field[0] * TILE_SIZE, field[1] * TILE_SIZE);
                } else {
                    batch.draw(trKillable, field[0] * TILE_SIZE, field[1] * TILE_SIZE);
                }
            }
        }
    }

    public ArrayList<Integer[]> findValidPieces(int x, int y, String type) {
        ArrayList<Integer[]> placeableFields = new ArrayList<>();

        // ------------------- PAWN -------------------
        if (type.contains("P")) {
            placeableFields.addAll(pawnFields(x, y, type));
        }

        // ------------------- ROOK -------------------
        if (type.contains("R")) {
            placeableFields.addAll(towerFields(x, y));
        }

        // ------------------- BISHOP -------------------
        if (type.contains("B")) {
            placeableFields.addAll(bishopFields(x, y));
        }

        // ------------------- KNIGHT -------------------
        if (type.contains("N")) {
            placeableFields.addAll(knightFields(x, y));
        }

        // ------------------- QUEEN -------------------
        if (type.contains("Q")) {
            placeableFields.addAll(bishopFields(x, y));
            placeableFields.addAll(towerFields(x, y));
        }

        // ------------------- KING -------------------
        if (type.contains("K")) {
            placeableFields.addAll(kingFields(x, y));
        }

        return placeableFields;
    }

    public ArrayList<Integer[]> pawnFields(int x, int y, String type) {
        ArrayList<Integer[]> placeableFields = new ArrayList<>();

        int yOffset = 1;
        if (type.equals("wP")) {
            yOffset = -1;
        } else if (type.equals("bP")) {
            yOffset = 1;
        }

        // BELOW OR ABOVE
        if (y > 0 && y < BOARD.length - 1 && this.type.equals("bP") || y >= 0 && y < BOARD.length - 1 && this.type.equals("wP")) {
            if (BOARD[y + yOffset][x].equals("") && !BOARD[y + yOffset][x].contains(currentPlayer)) {
                placeableFields.add(new Integer[]{x, y + yOffset});
                if (pawnFirstMove) {
                    if (BOARD[y + yOffset * 2][x].equals("")) {
                        placeableFields.add(new Integer[]{x, y + yOffset * 2});
                    }
                }
            }
            // RIGHT check if tile is not on the right
            if (x != BOARD.length - 1) {
                if (!BOARD[y + yOffset][x + 1].equals("") && !BOARD[y + yOffset][x + 1].contains(currentPlayer)) {
                    placeableFields.add(new Integer[]{x + 1, y + yOffset});
                }
            }
            // LEFT check if tile is not on the left
            if (x > 0) {
                if (!BOARD[y + yOffset][x - 1].equals("") && !BOARD[y + yOffset][x - 1].contains(currentPlayer)) {
                    placeableFields.add(new Integer[]{x - 1, y + yOffset});
                }
            }
        }

        return placeableFields;
    }

    public ArrayList<Integer[]> knightFields(int x, int y) {
        ArrayList<Integer[]> placeableFields = new ArrayList<>();

        int[][] positions = {{x - 1, y - 2}, {x + 1, y - 2}, {x - 1, y + 2}, {x + 1, y + 2},
                {x - 2, y - 1}, {x - 2, y + 1}, {x + 2, y - 1}, {x + 2, y + 1}};
        for (int i = 0; i < positions.length; i++) {
            try {
                if (!BOARD[positions[i][1]][positions[i][0]].contains(currentPlayer)) {
                    placeableFields.add(new Integer[]{positions[i][0], positions[i][1]});
                }
            } catch (IndexOutOfBoundsException e) {
            }
        }

        return placeableFields;
    }

    public ArrayList<Integer[]> kingFields(int x, int y) {
        ArrayList<Integer[]> placeableFields = new ArrayList<>();

        int[][] positions = {{x - 1, y - 1}, {x, y - 1}, {x + 1, y - 1}, {x - 1, y}, {x + 1, y}, {x - 1, y + 1}, {x , y + 1}, {x + 1, y + 1}};

        for (int i = 0; i < positions.length; i++) {
            try {
                if (BOARD[positions[i][1]][positions[i][0]].equals("") || !BOARD[positions[i][1]][positions[i][0]].contains(currentPlayer)) {
                    placeableFields.add(new Integer[]{positions[i][0], positions[i][1]});
                }
            } catch (IndexOutOfBoundsException e) {

            }
        }

        return placeableFields;
    }

    public ArrayList<Integer[]> towerFields(int x, int y) {
        ArrayList<Integer[]> placeableFields = new ArrayList<>();

        // RIGHT
        for (int i = x + 1; i < BOARD.length || i == 0; i++) {
            // CHECK TEAM
            if (BOARD[y][i].contains(currentPlayer)) {
                break;
            }
            // CHECK ENEMY
            if (BOARD[y][i].contains(oppositePlayer)) {
                placeableFields.add(new Integer[]{i, y});
                break;
            }

            placeableFields.add(new Integer[]{i, y});
        }

        // LEFT
        for (int i = x - 1; i >= 0; i--) {
            // CHECK TEAM
            if (BOARD[y][i].contains(currentPlayer)) {
                break;
            }
            // CHECK ENEMY
            if (BOARD[y][i].contains(oppositePlayer)) {
                placeableFields.add(new Integer[]{i, y});
                break;
            }
            placeableFields.add(new Integer[]{i, y});
        }
        // UP
        for (int i = y - 1; i >= 0; i--) {
            // CHECK TEAM
            if (BOARD[i][x].contains(currentPlayer)) {
                break;
            }
            // CHECK ENEMY
            if (BOARD[i][x].contains(oppositePlayer)) {
                placeableFields.add(new Integer[]{x, i});
                break;
            }
            placeableFields.add(new Integer[]{x, i});
        }
        // DOWN
        for (int i = y + 1; i < BOARD.length; i++) {
            // CHECK TEAM
            if (BOARD[i][x].contains(currentPlayer)) {
                break;
            }
            // CHECK ENEMY
            if (BOARD[i][x].contains(oppositePlayer)) {
                placeableFields.add(new Integer[]{x, i});
                break;
            }
            placeableFields.add(new Integer[]{x, i});
        }

        return placeableFields;
    }

    public ArrayList<Integer[]> bishopFields(int x, int y) {
        ArrayList<Integer[]> placeableFields = new ArrayList<>();

        // LEFT UP
        for (int i = y - 1, j = x - 1; i >= 0 && j >= 0; i--, j--) {
            if (y >= 0 && y <= BOARD.length - 1 && x >= 0 && x <= BOARD.length - 1) {
                if (BOARD[i][j].contains(oppositePlayer)) {
                    placeableFields.add(new Integer[]{j, i});
                    break;
                }
                if (BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    break;
                }
                if (!BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    placeableFields.add(new Integer[]{j, i});
                }
            }
        }
        // RIGHT UP
        for (int i = y - 1, j = x + 1; i >= 0 && j < BOARD.length; i--, j++) {
            if (y >= 0 && y <= BOARD.length - 1 && x >= 0 && x <= BOARD.length - 1) {
                if (BOARD[i][j].contains(oppositePlayer)) {
                    placeableFields.add(new Integer[]{j, i});
                    break;
                }
                if (BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    break;
                }
                if (!BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    placeableFields.add(new Integer[]{j, i});
                }
            }
        }
        // LEFT DOWN
        for (int i = y + 1, j = x - 1; i < BOARD.length && j >= 0; i++, j--) {
            if (y >= 0 && y <= BOARD.length - 1 && x >= 0 && x <= BOARD.length - 1) {
                if (BOARD[i][j].contains(oppositePlayer)) {
                    placeableFields.add(new Integer[]{j, i});
                    break;
                }
                if (BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    break;
                }
                if (!BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    placeableFields.add(new Integer[]{j, i});
                }
            }
        }
        // RIGHT DOWN
        for (int i = y + 1, j = x + 1; i < BOARD.length && j < BOARD.length; i++, j++) {
            if (y >= 0 && y <= BOARD.length - 1 && x >= 0 && x <= BOARD.length - 1) {
                if (BOARD[i][j].contains(oppositePlayer)) {
                    placeableFields.add(new Integer[]{j, i});
                    break;
                }
                if (BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    break;
                }
                if (!BOARD[i][j].contains(currentPlayer) && i != y && j != x) {
                    placeableFields.add(new Integer[]{j, i});
                }
            }
        }

        return placeableFields;
    }

    public void run(SpriteBatch batch) {
        handleInput();
        drawMoves(batch);
    }
}
