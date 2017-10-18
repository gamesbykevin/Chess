package com.gamesbykevin.chess.players;

import com.gamesbykevin.chess.piece.Piece;
import com.gamesbykevin.chess.util.UtilityHelper;

import org.rajawali3d.Object3D;
import org.rajawali3d.loader.LoaderSTL;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.renderer.Renderer;

/**
 * Created by Kevin on 10/15/2017.
 */
public class PlayerHelper {

    //default height for all pieces so they look like they are placed on the board
    private static final float Y = .075f;

    //size of a chess board
    private static final int COLS = 8;
    private static final int ROWS = 8;

    //the west/east/north/south coordinates on the board
    public static final float COORDINATE_MIN = -.7f;
    public static final float COORDINATE_MAX = .7f;

    //distance between each col/row
    private static final float COORDINATE_INCREMENT = .2f;

    public static void correctPiece(Piece piece) {

        //keep the column in boundary
        if (piece.getObj().getX() < COORDINATE_MIN)
            piece.getObj().setX(getCoordinate(0));
        if (piece.getObj().getX() > COORDINATE_MAX)
            piece.getObj().setX(getCoordinate(COLS - 1));

        //keep the row in boundary
        if (piece.getObj().getZ() < COORDINATE_MIN)
            piece.getObj().setZ(getCoordinate(0));
        if (piece.getObj().getZ() > COORDINATE_MAX)
            piece.getObj().setZ(getCoordinate(ROWS - 1));
    }

    public static int getRow(final double coordinate) {
        return getRow((float)coordinate);
    }

    public static int getRow(final float coordinate) {

        for (int i = 0; i < ROWS; i++) {
            final float min = COORDINATE_MIN + (COORDINATE_INCREMENT * i) - .1f;
            final float max = COORDINATE_MIN + (COORDINATE_INCREMENT * i) + .1f;

            if (coordinate >= min && coordinate <= max)
                return i;
        }

        return -1;
    }

    public static int getCol(final double coordinate) {
        return getCol((float)coordinate);
    }

    public static int getCol(final float coordinate) {

        for (int i = 0; i < COLS; i++) {
            final float min = COORDINATE_MIN + (COORDINATE_INCREMENT * i) - .1f;
            final float max = COORDINATE_MIN + (COORDINATE_INCREMENT * i) + .1f;

            if (coordinate >= min && coordinate <= max)
                return i;
        }

        return -1;
    }

    public static float getCoordinate(final int row_col) {
        return COORDINATE_MIN + (COORDINATE_INCREMENT * row_col);
    }

    protected static void reset(Player player, Renderer renderer, Material material) {

        //remove all existing pieces
        player.getPieces().clear();

        final float row1, row2;

        //the rows will depend on the direction the player is targeting
        if (player.hasDirection(Player.Direction.North)) {
            row1 = COORDINATE_MAX - COORDINATE_INCREMENT;
            row2 = COORDINATE_MAX;
        } else {
            row1 = COORDINATE_MIN + COORDINATE_INCREMENT;
            row2 = COORDINATE_MIN;
        }

        //populate all the pieces for row 1
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(0), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(1), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(2), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(3), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(4), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(5), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(6), Y, row1);
        addPiece(player, renderer, material, Piece.Type.Pawn, getCoordinate(7), Y, row1);

        //populate all the pieces for row 2
        addPiece(player, renderer, material, Piece.Type.Rook,   getCoordinate(0), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Knight, getCoordinate(1), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Bishop, getCoordinate(2), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Queen,  getCoordinate(3), Y, row2);
        addPiece(player, renderer, material, Piece.Type.King,   getCoordinate(4), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Bishop, getCoordinate(5), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Knight, getCoordinate(6), Y, row2);
        addPiece(player, renderer, material, Piece.Type.Rook,   getCoordinate(7), Y, row2);
    }

    private static void addPiece(Player player, Renderer renderer, Material material, Piece.Type type, float x, float y, float z) {

        try {

            //parse our 3d model
            LoaderSTL stlParser = new LoaderSTL(renderer.getContext().getResources(), renderer.getTextureManager(), type.getResId());
            stlParser.parse();

            //get our 3d object
            Object3D obj = stlParser.getParsedObject();

            //rotate piece 90 degrees so it is standing up
            obj.rotate(Vector3.Axis.X, 90);

            //assign the location
            obj.setPosition(x, y, z);

            //our model is too large so we need to shrink it
            obj.setScale(.05);

            //assign the texture to the model
            obj.setMaterial(material);

            //add to the scene
            renderer.getCurrentScene().addChild(obj);

            //add piece to the player
            player.getPieces().add(new Piece(obj, type, getCol(x), getRow(z)));

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }
}