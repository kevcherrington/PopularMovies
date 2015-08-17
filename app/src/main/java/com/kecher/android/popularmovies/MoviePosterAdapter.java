package com.kecher.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by kevin on 7/29/15.
 */
public class MoviePosterAdapter extends ArrayAdapter<MoviePoster> {
    private static final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();

    private Context mContext;

    public MoviePosterAdapter(Activity context, List<MoviePoster> moviePosters) {
        super(context, 0, moviePosters);
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {

        MoviePoster moviePoster = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.poster_item, parent, false);
        }

        ImageView posterImageView = (ImageView) convertView.findViewById(R.id.poster_image);
        posterImageView.setImageBitmap(moviePoster.getMoviePosterBitmap());

        TextView movieTitleView = (TextView) convertView.findViewById(R.id.movie_title);
        movieTitleView.setText(moviePoster.getMovieTitle());

        TextView movieRatingView = (TextView) convertView.findViewById(R.id.poster_text);
        movieRatingView.setText(moviePoster.getVoteAverage() + "");
        return convertView;
    }

}
