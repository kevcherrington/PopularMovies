package com.kecher.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

    final String API_KEY_PARAM = "api_key";

    final String TMDB_RESULTS = "results";
    final String TMDB_KEY = "key";
    final String TMDB_NAME = "name";
    final String TMDB_SITE = "site";
    final String TMDB_AUTHOR = "author";
    final String TMDB_CONTENT = "content";


    private MoviePoster poster;
    private MovieTrailerAdapter trailerAdapter;
    private MovieReviewAdapter reviewAdapter;
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle bundle) {
        Intent intent = getActivity().getIntent();
        super.onCreate(bundle);

        if (intent != null & intent.hasExtra(PosterFragment.EXTRA_POSTER_PARCEL)) {
            poster = intent.getParcelableExtra(PosterFragment.EXTRA_POSTER_PARCEL);

            if (poster.getTrailers().isEmpty() && poster.getTmdbMovieId() != null) {
                String videoUrl = String.format(getResources().getString(R.string.the_movie_db_videos), poster.getTmdbMovieId());

                FetchTrailerTask trailerTask = new FetchTrailerTask();
                trailerTask.execute(Utility.getApiKey(getActivity()), videoUrl);
            }

            if (poster.getReviews().isEmpty() && poster.getTmdbMovieId() != null) {
                String reviewUrl = String.format(getResources().getString(R.string.the_movie_db_reviews), poster.getTmdbMovieId());

                FetchReviewTask reviewTask = new FetchReviewTask();
                reviewTask.execute(Utility.getApiKey(getActivity()), reviewUrl);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);

        trailerAdapter = new MovieTrailerAdapter(getActivity(), new ArrayList<MovieTrailer>());
        reviewAdapter = new MovieReviewAdapter(getActivity(), new ArrayList<MovieReview>());

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

            List<MovieTrailer> trailers = poster.getTrailers();

            if (trailers != null) {
                trailerAdapter.clear();
                trailerAdapter.addAll(trailers);
            }

            ((ListView) rootView.findViewById(R.id.trailer_list)).setAdapter(trailerAdapter);

            List<MovieReview> reviews = poster.getReviews();

            if (reviews != null) {
                reviewAdapter.clear();
                reviewAdapter.addAll(reviews);
            }

            ((ListView) rootView.findViewById(R.id.review_list)).setAdapter(reviewAdapter);
        }
        return rootView;
    }

    private void updateTrailers(List<MovieTrailer> trailers) {
        poster.setTrailers(trailers);
        trailerAdapter.clear();
        trailerAdapter.addAll(trailers);
    }

    private void updateReviews(List<MovieReview> reviews) {
        poster.setReviews(reviews);
        reviewAdapter.clear();
        reviewAdapter.addAll(reviews);
    }

    private class FetchTrailerTask extends AsyncTask<String, Void, List<MovieTrailer>> {
        private final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

        /*
        Param 0: api key, Param 1: url to videos
         */
        @Override
        protected List<MovieTrailer> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            final String API_KEY = params[0];
            final String MOVIE_URL = params[1];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String trailersJsonStr = null;

            try {
                Uri builtUri = Uri.parse(MOVIE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, API_KEY).build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    return null;
                }
                trailersJsonStr = builder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getTrailersJson(trailersJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse Trailer Json.", e);
            }

            return null;
        }

        private List<MovieTrailer> getTrailersJson(String trailersJsonStr) throws JSONException {
            List<MovieTrailer> movieTrailers = new ArrayList<>();
            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            JSONArray resultsJson = trailersJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < resultsJson.length(); i++) {
                MovieTrailer movieTrailer = new MovieTrailer();
                JSONObject trailerJson = resultsJson.getJSONObject(i);

                if (trailerJson.getString(TMDB_SITE).equals("YouTube")) { // only YouTube is currently supported.
                    movieTrailer.setTrailerTitle(trailerJson.getString(TMDB_NAME));
                    movieTrailer.setTrailerUrl("http://www.youtube.com/watch?v=" + trailerJson.getString(TMDB_KEY));
                    movieTrailers.add(movieTrailer);
                }
            }

            return movieTrailers;
        }

        @Override
        protected void onPostExecute(List<MovieTrailer> movieTrailers) {
            updateTrailers(movieTrailers);
        }

    }

    public class FetchReviewTask extends AsyncTask<String, Void, List<MovieReview>> {
        private final String LOG_TAG = FetchReviewTask.class.getSimpleName();

        /*
        Param 0: api key, Param 1: url to reviews
         */
        @Override
        protected List<MovieReview> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            final String API_KEY = params[0];
            final String REVIEW_URL = params[1];

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String reviewsJsonStr = null;

            try {
                Uri builtUri = Uri.parse(REVIEW_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, API_KEY).build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder builder = new StringBuilder();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }

                if (builder.length() == 0) {
                    return null;
                }
                reviewsJsonStr = builder.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getReviewsJson(reviewsJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse Review Json.", e);
            }

            return null;
        }

        private List<MovieReview> getReviewsJson(String reviewsJsonStr) throws JSONException {
            List<MovieReview> movieReviews = new ArrayList<>();
            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray resultsJson = reviewsJson.getJSONArray(TMDB_RESULTS);

            for (int i = 0; i < resultsJson.length(); i++) {
                MovieReview movieReview = new MovieReview();
                JSONObject reviewObject = resultsJson.getJSONObject(i);

                movieReview.setReviewAuthor(reviewObject.getString(TMDB_AUTHOR));
                movieReview.setReviewContent(reviewObject.getString(TMDB_CONTENT));
                movieReviews.add(movieReview);
            }

            return movieReviews;
        }

        @Override
        protected void onPostExecute(List<MovieReview> reviews) {
            updateReviews(reviews);
        }
    }
}
