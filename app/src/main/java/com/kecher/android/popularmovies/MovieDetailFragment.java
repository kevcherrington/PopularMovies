package com.kecher.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

/**
 * (C) Copyright 2015 Kevin Cherrington (kevcherrington@gmail.com).
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 * Kevin Cherrington
 *
 */
public class MovieDetailFragment extends Fragment {

    private MoviePoster poster;
    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(PosterFragment.EXTRA_POSTER_PARCEL)) {
            poster = intent.getParcelableExtra(PosterFragment.EXTRA_POSTER_PARCEL);
        } else if (getArguments() != null) {
            poster = getArguments().getParcelable(PosterFragment.EXTRA_POSTER_PARCEL);
        }

        if (poster != null) {
            ImageView posterImageView = (ImageView) rootView.findViewById(R.id.movie_detail_poster_image);
            String posterUrl = poster.getPosterUrl();
            if (posterUrl != null) {
                Picasso.with(getActivity()).load(posterUrl).into(posterImageView);
            } else {
                Picasso.with(getActivity()).load(R.drawable.no_image).into(posterImageView);
            }

            String movieTitle = poster.getMovieTitle();
            ((TextView) rootView.findViewById(R.id.movie_detail_title))
                    .setText(movieTitle);

            SimpleDateFormat sdf = new SimpleDateFormat(MoviePoster.DATE_FORMAT);
            String releaseDate = poster.getReleaseDate() != null ? sdf.format(poster.getReleaseDate()) : "Unavailable";
            ((TextView) rootView.findViewById(R.id.movie_detail_release_date))
                    .setText(releaseDate);

            Double voteAverage = poster.getVoteAverage();
            ((TextView) rootView.findViewById(R.id.movie_detail_vote_average))
                    .setText(Double.toString(voteAverage));

            String overview = poster.getOverview();
            ((TextView) rootView.findViewById(R.id.movie_detail_overview))
                    .setText(overview);
        }
        return rootView;
    }

}
