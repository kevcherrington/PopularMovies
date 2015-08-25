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

import com.squareup.picasso.Picasso;

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

    public MovieDetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            if (intent.hasExtra(PosterFragment.EXTRA_MOVIE_POSTER)) {
                ImageView posterImageView = (ImageView) rootView.findViewById(R.id.movie_detail_poster_image);
                String poster = intent.getStringExtra(PosterFragment.EXTRA_MOVIE_POSTER);
                if (poster != null) {
                    Picasso.with(getActivity()).load(poster).into(posterImageView);
                } else {
                    Bitmap noImage = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.no_image);
                    posterImageView.setImageBitmap(noImage);
                }
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
