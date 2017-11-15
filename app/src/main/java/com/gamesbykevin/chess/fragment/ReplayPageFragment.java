package com.gamesbykevin.chess.fragment;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gamesbykevin.chess.R;
import com.gamesbykevin.chess.activity.GameActivity;
import com.gamesbykevin.chess.activity.ReplayActivity;

/**
 * Created by Kevin on 11/14/2017.
 */

public class ReplayPageFragment extends PageFragment {

    public ReplayPageFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //inflate the layout to access the ui elements
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_replay_page, container, false);

        ImageView imageView =   view.findViewById(R.id.replayImage);
        TextView textView   =   view.findViewById(R.id.replayText);

        final int resIdImage;
        final int resIdText;

        //get the resource for the shared preferences
        final int resId = GameActivity.getResid(getPageNumber());

        //check if we already have a saved game
        if (GameActivity.getSharedPreferences().contains(getString(resId))) {
            resIdImage = R.mipmap.ic_launcher;
            imageView.setEnabled(true);
            textView.setEnabled(true);

            if (ReplayActivity.SAVE) {
                textView.setText("Overwrite save");
            } else {
                textView.setText("Watch Replay");
            }
        } else if (ReplayActivity.SAVE) {

            resIdImage = R.drawable.white;
            imageView.setEnabled(true);
            textView.setEnabled(true);
            textView.setText("Select to save");

        } else {
            resIdImage = R.drawable.white;
            imageView.setEnabled(false);
            textView.setEnabled(false);
            textView.setText("Replay not available");
        }

        //update bitmap accordingly
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), resIdImage));

        //assign the appropriate instruction text


        // Inflate the layout for this fragment
        return view;
    }
}