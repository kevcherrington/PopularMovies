package com.kecher.android.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private MoviePosterAdapter posterAdapter;

    MoviePoster[] moviePosters = {
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_0, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_1, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_2, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_3, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_4, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_5, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_6, new Double(0), "Stuff Happens!!!", 3, 3),
            new MoviePoster("MovieTitle", new Date(), R.drawable.sample_7, new Double(0), "Stuff Happens!!!", 3, 3)
    };

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        MoviePosterAdapter moviePosterAdapter = new MoviePosterAdapter(getActivity(), Arrays.asList(moviePosters));

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.posters_grid);
        gridView.setAdapter(moviePosterAdapter);

        return rootView;
    }

}
