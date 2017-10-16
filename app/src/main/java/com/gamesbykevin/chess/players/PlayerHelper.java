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

    private static final float Y = .075f;
    private static final float Z = .1f;

    public static void reset(Player player, Renderer renderer, Material material, boolean north) {

        //remove all existing pieces
        player.getPieces().clear();

        final float row1, row2;

        //if we are heading north
        if (north) {

            row1 = .5f;
            row2 = .7f;

            addPiece(player, renderer, material, Piece.Type.Pawn, -.7f, Y, row1, 0, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, -.5f, Y, row1, 1, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, -.3f, Y, row1, 2, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, -.1f, Y, row1, 3, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.1f, Y, row1, 4, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.3f, Y, row1, 5, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.5f, Y, row1, 6, 6);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.7f, Y, row1, 7, 6);

            addPiece(player, renderer, material, Piece.Type.Rook,   -.7f, Y, row2, 0, 7);
            addPiece(player, renderer, material, Piece.Type.Knight, -.5f, Y, row2, 1, 7);
            addPiece(player, renderer, material, Piece.Type.Bishop, -.3f, Y, row2, 2, 7);
            addPiece(player, renderer, material, Piece.Type.Queen,  -.1f, Y, row2, 3, 7);
            addPiece(player, renderer, material, Piece.Type.King,   0.1f, Y, row2, 4, 7);
            addPiece(player, renderer, material, Piece.Type.Bishop, 0.3f, Y, row2, 5, 7);
            addPiece(player, renderer, material, Piece.Type.Knight, 0.5f, Y, row2, 6, 7);
            addPiece(player, renderer, material, Piece.Type.Rook,   0.7f, Y, row2, 7, 7);

        } else {

            row1 = -.5f;
            row2 = -.7f;

            addPiece(player, renderer, material, Piece.Type.Rook,   -.7f, Y, row2, 0, 0);
            addPiece(player, renderer, material, Piece.Type.Knight, -.5f, Y, row2, 1, 0);
            addPiece(player, renderer, material, Piece.Type.Bishop, -.3f, Y, row2, 2, 0);
            addPiece(player, renderer, material, Piece.Type.Queen,  -.1f, Y, row2, 3, 0);
            addPiece(player, renderer, material, Piece.Type.King,   0.1f, Y, row2, 4, 0);
            addPiece(player, renderer, material, Piece.Type.Bishop, 0.3f, Y, row2, 5, 0);
            addPiece(player, renderer, material, Piece.Type.Knight, 0.5f, Y, row2, 6, 0);
            addPiece(player, renderer, material, Piece.Type.Rook,   0.7f, Y, row2, 7, 0);

            addPiece(player, renderer, material, Piece.Type.Pawn, -.7f, Y, row1, 0, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, -.5f, Y, row1, 1, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, -.3f, Y, row1, 2, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, -.1f, Y, row1, 3, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.1f, Y, row1, 4, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.3f, Y, row1, 5, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.5f, Y, row1, 6, 1);
            addPiece(player, renderer, material, Piece.Type.Pawn, 0.7f, Y, row1, 7, 1);
        }

    }

    public static void addPiece(Player player, Renderer renderer, Material material, Piece.Type type, float x, float y, float z, float col, float row) {

        try {

            LoaderSTL stlParser = new LoaderSTL(renderer.getContext().getResources(), renderer.getTextureManager(), type.getResId());
            stlParser.parse();
            Object3D obj = stlParser.getParsedObject();
            obj.rotate(Vector3.Axis.X, 90);
            obj.setPosition(x, y, z);
            obj.setScale(.05);
            obj.setMaterial(material);

            //add to the scene
            renderer.getCurrentScene().addChild(obj);

            //add piece to the player
            player.getPieces().add(new Piece(obj, type, col, row));

        } catch (Exception e) {
            UtilityHelper.handleException(e);
        }
    }
}