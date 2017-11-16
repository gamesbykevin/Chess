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

import static com.gamesbykevin.chess.game.GameHelper.getResidDesc;
import static com.gamesbykevin.chess.game.GameHelper.getResidFile;

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

        //access our ui elements
        ImageView imageView = view.findViewById(R.id.replayImage);
        TextView textView   = view.findViewById(R.id.replayText);

        final int resIdImage;
        final int resIdText;
        final boolean enabled;

        //our additional description
        String desc = "";

        //check if we already have a saved game
        if (GameActivity.getSharedPreferences().contains(getString(getResidFile(getPageNumber())))) {

            //get the file save description
            desc = GameActivity.getSharedPreferences().getString(getActivity().getString(getResidDesc(getPageNumber())), "");

            //assign our resource values
            resIdImage = R.drawable.saved_replay;
            resIdText = (ReplayActivity.SAVE) ? R.string.overwrite_save : R.string.watch_replay;
            enabled = true;

        } else if (ReplayActivity.SAVE) {

            //assign our resource values
            resIdImage = R.drawable.unsaved_replay;
            resIdText = R.string.select_save;
            enabled = true;

        } else {

            //assign our resource values
            resIdImage = R.drawable.unsaved_replay;
            resIdText = R.string.replay_not_available;
            enabled = false;
        }

        //enable / disable our ui components
        imageView.setEnabled(enabled);
        textView.setEnabled(enabled);

        //update bitmap accordingly
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), resIdImage));

        //assign the appropriate instruction text
        textView.setText(getText(resIdText) + " " + desc);

        // Inflate the layout for this fragment
        return view;
    }
}