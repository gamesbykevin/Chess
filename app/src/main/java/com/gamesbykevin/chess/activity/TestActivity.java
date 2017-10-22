package com.gamesbykevin.chess.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import com.gamesbykevin.chess.R;

import org.rajawali3d.renderer.Renderer;
import org.rajawali3d.view.ISurface;
import org.rajawali3d.view.SurfaceView;

public class TestActivity extends Activity {

    BasicRenderer renderer;

    SurfaceView surface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        surface = findViewById(R.id.mySurfaceView);
        surface.setFrameRate(60.0);
        surface.setRenderMode(ISurface.RENDERMODE_WHEN_DIRTY);

        renderer = new BasicRenderer(this, surface);
        surface.setSurfaceRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean val = super.onTouchEvent(event);
        renderer.onTouchEvent(event);
        return val;
    }

    public void onClickResetCamera(View view) {
        renderer.resetCamera();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}