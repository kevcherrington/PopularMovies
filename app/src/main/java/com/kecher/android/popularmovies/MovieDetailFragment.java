package com.kecher.android.popularmovies;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailFragment extends Fragment {

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(PosterFragment.EXTRA_MOVIE_POSTER)) {
                byte[] byteArray = intent.getByteArrayExtra(PosterFragment.EXTRA_MOVIE_POSTER);
                Bitmap moviePoster = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                ((ImageView) rootView.findViewById(R.id.movie_detail_poster_image))
                        .setImageBitmap(moviePoster);
            }
            if (intent.hasExtra(PosterFragment.EXTRA_MOVIE_TITLE)) {
                String movieTitle = intent.getStringExtra(PosterFragment.EXTRA_MOVIE_TITLE);
                ((TextView) rootView.findViewById(R.id.movie_detail_title))
                        .setText(movieTitle);
            }
            if (intent.hasExtra(PosterFragment.EXTRA_MOVIE_RELEASE_DATE)) {
                String releaseDate = intent.getStringExtra(PosterFragment.EXTRA_MOVIE_RELEASE_DATE);
                ((TextView) rootView.findViewById(R.id.movie_detail_release_date))
                        .setText(releaseDate);
            }
            if (intent.hasExtra(PosterFragment.EXTRA_MOVIE_VOTE_AVERAGE)) {
                Double voteAverage = intent.getDoubleExtra(PosterFragment.EXTRA_MOVIE_VOTE_AVERAGE, 0);
                ((TextView) rootView.findViewById(R.id.movie_detail_vote_average))
                        .setText(Double.toString(voteAverage));
            }
            if (intent.hasExtra(PosterFragment.EXTRA_MOVIE_DETAILS)) {
                String description = intent.getStringExtra(PosterFragment.EXTRA_MOVIE_DETAILS);
                ((TextView) rootView.findViewById(R.id.movie_detail_description))
                        .setText(description);
            }
        }
        return rootView;
    }

}
