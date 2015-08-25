package com.kecher.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    public static final String EXTRA_MOVIE_POSTER = "extra_movie_poster";
    public static final String EXTRA_MOVIE_TITLE = "extra_movie_title";
    public static final String EXTRA_MOVIE_RELEASE_DATE = "extra_movie_release_date";
    public static final String EXTRA_MOVIE_VOTE_AVERAGE = "extra_movie_vote_average";
    public static final String EXTRA_MOVIE_DETAILS = "extra_movie_details";

    private MoviePosterAdapter posterAdapter;

    private final String API_KEY_PROP_NAME = "api_key";
    private String apiKey;

    // for the youtube preview use this url... https://www.youtube.com/watch?v={key}
    // for the image configuration json http://api.themoviedb.org/3/configuration?api_key=XXX
    // For the poster images use this url... http://image.tmdb.org/t/p/w1280/{img_Loc}.jpg?api_key=XXX (http://docs.themoviedb.apiary.io/#reference/configuration/configuration/get)

    public static String DISCOVER_URL = "https://api.themoviedb.org/3/discover/movie";

    Map<String, String[]> configData;

    public PosterFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // load the TMDB api key from the popular_movies.properties resource file.
        try {
            InputStream rawResource = this.getResources().openRawResource(R.raw.popular_movies);
            Properties properties = new Properties();
            properties.load(rawResource);
            apiKey = properties.getProperty(API_KEY_PROP_NAME);
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Unable to find resource: ", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to open resource: ", e);
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
            refreshPosters();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshConfig(boolean forceRefresh) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // get the last time the config data was refreshed.
        Date lastConfigRefresh = new Date(prefs.getLong(getString(R.string.pref_last_config_refresh),
                new Date().getTime()));
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.set(Calendar.DAY_OF_MONTH, -7);

        // if the config data is older that 1 week refresh the config data before refreshing
        // the posters
        if (lastConfigRefresh.before(lastWeek.getTime()) || forceRefresh) {
            FetchConfigDataTask configTask = new FetchConfigDataTask();
            configTask.execute(apiKey, "true"); // api Key and should it fetch the movie posters after
        }
    }

    private void refreshPosters() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        FetchMoviePostersTask postersTask = new FetchMoviePostersTask();

        String sortOrder = prefs.getString(getString(R.string.pref_sort_order_key),
                getString(R.string.pref_sort_order_popular));
        String imageUrl = prefs.getString(getString(R.string.config_image_url),
                getString(R.string.config_default_image_url));
        String posterSize = prefs.getString(getString(R.string.config_poster_size),
                getString(R.string.config_default_poster_size));

        postersTask.execute(apiKey, imageUrl, posterSize, sortOrder);
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshConfig(false);
        refreshPosters();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        posterAdapter = new MoviePosterAdapter(getActivity(), new ArrayList<MoviePoster>());

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.posters_grid);
        gridView.setAdapter(posterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"

                MoviePoster poster = posterAdapter.getItem(position);
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd yyyy");
                String releaseDate = poster.getReleaseDate() != null ? sdf.format(poster.getReleaseDate()) : "Unavailable";
                Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                        .putExtra(EXTRA_MOVIE_POSTER, poster.getPosterUrl())
                        .putExtra(EXTRA_MOVIE_TITLE, poster.getMovieTitle())
                        .putExtra(EXTRA_MOVIE_RELEASE_DATE, releaseDate)
                        .putExtra(EXTRA_MOVIE_VOTE_AVERAGE, poster.getVoteAverage())
                        .putExtra(EXTRA_MOVIE_DETAILS, poster.getPlotSynopsis());
                startActivity(movieDetailIntent);
            }
        });

        return rootView;
    }

    public class FetchMoviePostersTask extends AsyncTask<String, Void, MoviePoster[]> {
        private final String LOG_TAG = FetchMoviePostersTask.class.getSimpleName();

        // parameters in the order they were passed.
        private String apiKey;
        private String imageUrl;
        private String posterSize;
        private String sortOrder;

        private MoviePoster[] parseMoviePosterJson(String json) throws JSONException {
            final String RESULTS = "results";
            final String OVERVIEW = "overview"; // plotSynopsis
            final String RELEASE_DATE = "release_date";
            final String POSTER_PATH = "poster_path";
            final String POPULARITY = "popularity";
            final String TITLE = "title";
            final String VOTE_AVERAGE = "vote_average";
            final String API_KEY_PARAM = "api_key";

            JSONObject posterJson = new JSONObject(json);
            JSONArray results = posterJson.getJSONArray(RESULTS);

            MoviePoster[] posters = new MoviePoster[results.length()];

            SimpleDateFormat sdf = new SimpleDateFormat(MoviePoster.DATE_FORMAT);
            for (int i = 0; i < results.length(); i++) {
                JSONObject poster = results.getJSONObject(i);
                Date releaseDate = null;
                try {
                    if (poster.getString(RELEASE_DATE) != null && !poster.getString(RELEASE_DATE).equals("null")) {
                        releaseDate = sdf.parse(poster.getString(RELEASE_DATE));
                    }
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Unable to parse date ", e);
                }

                Uri builtUri = Uri.parse(imageUrl).buildUpon()
                        .appendPath(posterSize)
                        .appendPath(poster.getString(POSTER_PATH).replaceAll("/", ""))
                        .appendQueryParameter(API_KEY_PARAM, apiKey).build();

                posters[i] = new MoviePoster(poster.getString(TITLE), releaseDate,
                        builtUri.toString(), poster.getDouble(VOTE_AVERAGE), poster.getString(OVERVIEW),
                        poster.getInt(POPULARITY));
            }

            return posters;
        }

        /**
         * Retrieves the movie poster discover information
         * @param params First param is the discovery url, Second param is the API_KEY,
         *               third param is the configuration url, fourth posterSize, fifth sortOrder
         * @return an array of MoviePoster obj
         */
        @Override
        protected MoviePoster[] doInBackground(String... params) {

            if (params.length > 4) {
                return null;
            }

            apiKey = params[0];
            imageUrl = params[1];
            posterSize = params[2];
            sortOrder = params[3];

            String postersJsonStr = downloadDiscoverJsonString();
            if (postersJsonStr == null) return null;

            try {
                return parseMoviePosterJson(postersJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(MoviePoster[] posters) {
            if (posters != null) {
                posterAdapter.clear();
                for (MoviePoster poster : posters) {
                    posterAdapter.add(poster);
                }
            }
        }

        private String downloadDiscoverJsonString() {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String API_KEY_PARAM = "api_key";
                final String SORT_BY_PARAM = "sort_by";

                Uri builtUri = Uri.parse(DISCOVER_URL).buildUpon()
                        .appendQueryParameter(SORT_BY_PARAM, sortOrder)
                        .appendQueryParameter(API_KEY_PARAM, apiKey).build();

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
                jsonStr = builder.toString();

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
            return jsonStr;
        }
    }


    public class FetchConfigDataTask extends AsyncTask<String, Void, Map<String, String[]>> { // AsyncTask<params, progress, result>
        private final String LOG_TAG = FetchConfigDataTask.class.getSimpleName();

        final String TMDB_IMAGES = "images";
        final String TMDB_SECURE_BASE_URL = "secure_base_url";
        final String TMDB_BACKDROP_SIZES = "backdrop_sizes";
        final String TMDB_POSTER_SIZES = "poster_sizes";
        final String TMDB_CHANGE_KEYS = "change_keys";
        private boolean fetchPosterImages;

        private Map<String, String[]> getConfigurationJson(String configurationJsonStr) throws JSONException {
            // the names of the JSON objects to be extracted.

            JSONObject configurationJson = new JSONObject(configurationJsonStr);
            JSONObject imagesJson = configurationJson.getJSONObject(TMDB_IMAGES);
            JSONArray changeKeys = configurationJson.getJSONArray(TMDB_CHANGE_KEYS);

            String secureBaseUrl = imagesJson.getString(TMDB_SECURE_BASE_URL);
            JSONArray backdropSizes = imagesJson.getJSONArray(TMDB_BACKDROP_SIZES);
            JSONArray posterSizes = imagesJson.getJSONArray(TMDB_POSTER_SIZES);

            Map<String, String[]> results = new HashMap<>();
            results.put(TMDB_SECURE_BASE_URL, new String[]{secureBaseUrl});

            String[] backdropSizesAry = new String[backdropSizes.length()];
            for (int i = 0; i < backdropSizes.length(); i++) {
                backdropSizesAry[i] = backdropSizes.getString(i);
            }

            String[] posterSizesAry = new String[posterSizes.length()];
            for (int i = 0; i < posterSizes.length(); i++) {
                posterSizesAry[i] = posterSizes.getString(i);
            }

            String[] changeKeysAry = new String[changeKeys.length()];
            for (int i = 0; i < changeKeys.length(); i++) {
                changeKeysAry[i] = changeKeys.getString(i);
            }

            results.put(TMDB_BACKDROP_SIZES, backdropSizesAry);
            results.put(TMDB_POSTER_SIZES, posterSizesAry);
            results.put(TMDB_CHANGE_KEYS, changeKeysAry);

            return results;
        }

        @Override
        protected Map<String, String[]> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            fetchPosterImages = "true".equals(params[1]);

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String configurationJsonStr = null;

            try {
                final String CONFIGURATION_BASE_URL = "https://api.themoviedb.org/3/configuration";
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(CONFIGURATION_BASE_URL).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, params[0]).build();

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
                configurationJsonStr = builder.toString();
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
                return getConfigurationJson(configurationJsonStr);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Map<String, String[]> result) {
            if (result != null) {
                configData = result;
            }

            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.config_image_url), configData.get(TMDB_SECURE_BASE_URL)[0]);
            editor.putString(getString(R.string.config_poster_size), configData.get(TMDB_POSTER_SIZES)[3]);
            editor.putLong(getString(R.string.pref_last_config_refresh), new Date().getTime());
            editor.commit();

            String sortOrder = prefs.getString(getString(R.string.pref_sort_order_key),
                    getString(R.string.pref_sort_order_popular));

            if (fetchPosterImages) {
                FetchMoviePostersTask posterTask = new FetchMoviePostersTask();
                posterTask.execute(apiKey, configData.get(TMDB_SECURE_BASE_URL)[0],
                        configData.get(TMDB_POSTER_SIZES)[3], sortOrder); // 0th index contains the smallest poster size.
            }
        }
    }
}
