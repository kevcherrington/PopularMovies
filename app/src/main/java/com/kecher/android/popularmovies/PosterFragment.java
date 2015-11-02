package com.kecher.android.popularmovies;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

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
public class PosterFragment extends Fragment {
    private final String LOG_TAG = PosterFragment.class.getSimpleName();

    private GridView mListView;
    private int mPosition = GridView.INVALID_POSITION;

    private static final int MOVIE_LOADER = 0;

    public static final String EXTRA_MOVIE_POSTER = "extra_movie_poster";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_MOVIE_RELEASE_DATE = "extra_movie_release_date";
    public static final String EXTRA_MOVIE_VOTE_AVERAGE = "extra_movie_vote_average";
    public static final String EXTRA_MOVIE_DETAILS = "extra_movie_details";

    private MoviePosterAdapter posterAdapter;

//    private final String MOVIE_POSTERS = "movie_posters";

    private String apiKey;
//    private MoviePoster[] moviePosters;

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // load the TMDB api key from the popular_movies.properties resource file.
        if (apiKey == null) {
            apiKey = Utility.getApiKey(getActivity());
        }

        refreshConfig(false); // will not force a refresh.

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(Utility.API_KEY)) {
                apiKey = savedInstanceState.getString(Utility.API_KEY);
            }
//            if (savedInstanceState.containsKey(MOVIE_POSTERS)) {
//                moviePosters = (MoviePoster[]) savedInstanceState.getParcelableArray(MOVIE_POSTERS);
//                if (moviePosters == null) {
//                    refreshPosters();
//                }
//            }
        } else {
            refreshPosters();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.posterfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            refreshConfig(true);
            Log.d(LOG_TAG, "force refresh from Menu calling refreshPosters");
            refreshPosters();
            return true;
        } else if (id == R.id.action_settings) {
            refreshPosters();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshConfig(boolean forceRefresh) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // get the last time the config data was refreshed.
        Date lastConfigRefresh = new Date(prefs.getLong(getString(R.string.config_last_config_refresh),
                new Date().getTime()));
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.set(Calendar.DAY_OF_MONTH, -7);

        // if the config data is older that 1 week refresh the config data before refreshing
        // the posters
        if (lastConfigRefresh.before(lastWeek.getTime()) || forceRefresh) {
            FetchConfigDataTask configTask = new FetchConfigDataTask(getActivity(), posterAdapter);
            configTask.execute(apiKey, "true");
        }
    }

    private void refreshPosters() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        FetchMovieTask discoverJsonTask = new FetchMovieTask(getActivity(), posterAdapter);

        String sortOrder = Utility.getSortOrder(getActivity());

        if (sortOrder.equals(getString(R.string.pref_sort_favorites))) {
            Toast.makeText(getActivity(), "No favorites stored", Toast.LENGTH_SHORT).show();
        } else {
            discoverJsonTask.execute(apiKey, sortOrder);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (apiKey != null) {
            outState.putString(Utility.API_KEY, apiKey);
        }
//        if (moviePosters != null) {
//            outState.putParcelableArray(MOVIE_POSTERS, moviePosters);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        posterAdapter = new MoviePosterAdapter(getActivity(), null, 0);

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.posters_grid);
        gridView.setAdapter(posterAdapter);

//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // Executed in an Activity, so 'this' is the Context
//                // The fileUrl is a string URL, such as "http://www.example.com/image.png"
//
////        MoviePoster poster = posterAdapter.getItem(position);
////        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
////        String releaseDate = poster.getReleaseDate() != null ? sdf.format(poster.getReleaseDate()) : "Unavailable";
////        Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class)
////                .putExtra(EXTRA_MOVIE_POSTER, poster.getPosterUrl())
////                        .putExtra(EXTRA_MOVIE_TITLE, poster.getMovieTitle())
////                        .putExtra(EXTRA_MOVIE_RELEASE_DATE, releaseDate)
////                        .putExtra(EXTRA_MOVIE_VOTE_AVERAGE, poster.getVoteAverage())
////                        .putExtra(EXTRA_MOVIE_DETAILS, poster.getOverview());
////                startActivity(movieDetailIntent);
////            }
////        });
//                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    ((Callback) getActivity())
//                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                }
//                mPosition = position;
//            }
//        });

        return rootView;
    }

    public void onSortOrderChanged() {
        refreshPosters();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader()
    }

}
