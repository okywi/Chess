package de.okywi.schach;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Arrays;

import static de.okywi.schach.Piece.checkedPlayer;
import static de.okywi.schach.Piece.checkmate;

public class Schach extends ApplicationAdapter {

	OrthographicCamera camera;
	SpriteBatch batch;
	ShapeRenderer shape;
	ShapeRenderer moveShape;
	BitmapFont font;
	// Game variables
	Color boardWhite = new Color(0xEBECD0FF);
	Color boardBlack = new Color(0x739552FF);
	public static int TILE_SIZE = 100;
	public static int BOARD_SIZE = 800;
	public static String[][] BOARD = {{"bR", "bN", "bB", "bQ", "bK", "bB", "bN", "bR"},
						{"bP", "bP", "bP", "bP", "bP", "bP", "bP", "bP"},
						{"", "", "", "", "", "", "", ""},
						{"", "", "", "", "", "", "", ""},
						{"", "", "", "", "", "", "", ""},
						{"", "", "", "", "", "", "", ""},
						{"wP", "wP", "wP", "wP", "wP", "wP", "wP", "wP"},
						{"wR", "wN", "wB", "wQ", "wK", "wB", "wN", "wR"}};
	public static ArrayList<Piece> pieces = new ArrayList<>();
	public static String currentPlayer = "w";
	public static String oppositePlayer = "b";
	
	@Override
	public void create () {
		camera = new OrthographicCamera(800,800);
		camera.setToOrtho(true);

		camera.update();
		batch = new SpriteBatch();
		shape = new ShapeRenderer();
		moveShape = new ShapeRenderer();
		moveShape.setAutoShapeType(true);
		font = new BitmapFont(true);
		font.getData().setScale(4);
		createPieces();
	}

	@Override
	public void render() {
		ScreenUtils.clear(1, 1, 1, 1);
		batch.setProjectionMatrix(camera.combined);
		shape.setProjectionMatrix(camera.combined);
		moveShape.setProjectionMatrix(camera.combined);
		camera.update();


		shape.begin(ShapeRenderer.ShapeType.Filled);
		drawBoard(boardWhite, boardBlack);
		shape.end();

		moveShape.begin();

		moveShape.end();

		batch.begin();
		handlePieces();
		drawPieces();
		batch.end();


	}
	
	@Override
	public void dispose() {
		batch.dispose();
		shape.dispose();
	}

	public void drawBoard(Color white, Color black) {
		for (int i = 0; i < BOARD_SIZE/TILE_SIZE; i++) {
			for (int j = 0; j < 8; j++) {
				if ((j + i) % 2 == 0) {
					shape.setColor(black);
				} else {
					shape.setColor(white);
				}
				shape.rect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);

				shape.setColor(new Color(0xE64553FF));
				if (BOARD[i][j].equals("bK") && checkedPlayer.equals("b")) {
					shape.rect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
				if (BOARD[i][j].equals("wK") && checkedPlayer.equals("w")) {
					shape.rect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
			}
		}

	}

	public void drawPieces() {
		Piece selectedPiece = null;
		for (int i = 0; i < pieces.size(); i++) {
			Piece piece = pieces.get(i);

			if (piece.isSelected) {
				selectedPiece = piece;
			} else {
				batch.draw(piece.tr, piece.x * Schach.TILE_SIZE, piece.y * Schach.TILE_SIZE);
			}
		}
		// Draw selected piece after other pieces to ensure its always on top
		if (selectedPiece != null) {
			if (selectedPiece.isSelected) {
				batch.draw(selectedPiece.trSelected, selectedPiece.x * Schach.TILE_SIZE, selectedPiece.y * Schach.TILE_SIZE);
			}

			if (!selectedPiece.isSelected || !selectedPiece.isDragged) {
				batch.draw(selectedPiece.tr, selectedPiece.x * Schach.TILE_SIZE, selectedPiece.y * Schach.TILE_SIZE);
			}
			if (selectedPiece.isDragged) {
				batch.draw(selectedPiece.tr, Gdx.input.getX() - ((float) Schach.TILE_SIZE / 2), Gdx.input.getY() - ((float) Schach.TILE_SIZE / 2));
			}
		}
		if (checkmate) {
			if (currentPlayer.equals("b")) {
				font.draw(batch, "Player White won.", 200, 400);
			} else {
				font.draw(batch, "Player Black won.", 200, 400);
			}
		}
	}

	public void createPieces() {
		for (int i = 0; i < BOARD.length; i++) {
			for (int j = 0; j < BOARD.length; j++) {
				if (!BOARD[i][j].isEmpty()) {
					pieces.add(new Piece(BOARD[i][j], j, i));
				}
			}
		}
	}

	public void handlePieces() {
		for (int i = 0; i < pieces.size(); i++) {
			Piece piece = pieces.get(i);
			piece.run(batch);
		}
	}


	public static void switchPlayers() {
		if (currentPlayer.equals("w")) {
			currentPlayer = "b";
		} else if (currentPlayer.equals("b")) {
			currentPlayer = "w";
		}
		if (oppositePlayer.equals("b")) {
			oppositePlayer = "w";
		} else if (oppositePlayer.equals("w")) {
			oppositePlayer = "b";
		}
	}
}