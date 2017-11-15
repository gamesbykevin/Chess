package com.gamesbykevin.chess.fragment;

import android.app.Fragment;
import android.os.Bundle;

import com.gamesbykevin.chess.activity.PagerActivity;

/**
 * Created by Kevin on 11/11/2017.
 */

public abstract class PageFragment extends Fragment {

    /**
     * The argument key for the page number this fragment represents.
     */
    protected static final String ARG_PAGE = "page";

    //the fragment's page number
    private int pageNumber;

    public PageFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //get the arguments passed
        this.pageNumber = getArguments().getInt(ARG_PAGE);
    }

    public static PageFragment create(int pageNumber, PagerActivity.Type type) {

        //our page fragment
        PageFragment fragment = null;

        //create the appropriate instance
        switch (type) {
            case ModeSelection:
                fragment = new ModePageFragment();
                break;

            case ReplaySelection:
                fragment = new ReplayPageFragment();
                break;

            case TutorialSelection:
                fragment = new TutorialPageFragment();
                break;
        }

        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);

        //return our fragment
        return fragment;
    }

    /**
     * Returns the page number represented by this fragment object.
     */
    public int getPageNumber() {
        return pageNumber;
    }

    @Override
    public void onResume() {

        //call parent
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
