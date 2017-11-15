package com.gamesbykevin.chess.activity;

import android.os.Bundle;

import com.gamesbykevin.chess.R;

public class TutorialActivity extends PagerActivity {

    public TutorialActivity() {
        super(Type.TutorialSelection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //set the total number of pages
        PAGES = 1;

        //inflate content view
        setContentView(R.layout.activity_tutorial);

        //call parent
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {

        //call parent
        super.onPause();
    }

    @Override
    public void onStart() {

        //call parent
        super.onStart();
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();

        //resume audio
        super.playMenu();
    }

    @Override
    protected void onDestroy() {

        //call parent
        super.onDestroy();
    }
}