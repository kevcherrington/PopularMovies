package com.kecher.android.popularmovies;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.Toast;

import com.kecher.android.popularmovies.data.MovieContract;
import com.kecher.android.popularmovies.data.MovieDbHelper;

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
import java.util.List;
import java.util.Map;

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

    public static final String RELEASE_DATE_FORMAT = "MMM dd yyyy";

    public static final String EXTRA_POSTER_PARCEL = "extra_poster_parcel";

    private MoviePosterAdapter posterAdapter;

    private final String MOVIE_POSTERS = "movie_posters";

    private String apiKey;
    private String imageUrl;
    private String posterSize;
    private String discoverJson;
    private MoviePoster[] moviePosters;

    public static String DISCOVER_URL = "https://api.themoviedb.org/3/discover/movie";

    Map<String, String[]> configData;

    // Callback interface to be implemented by the main activity so that we can have access to the two pane variable.
    public interface Callback {
        void onItemSelected(MoviePoster poster);
    }

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
            if (savedInstanceState.containsKey(MOVIE_POSTERS)) {
                moviePosters = (MoviePoster[]) savedInstanceState.getParcelableArray(MOVIE_POSTERS);
                if (moviePosters == null) {
                    refreshPosters();
                }
            }
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
            FetchConfigDataTask configTask = new FetchConfigDataTask();
            configTask.execute(apiKey, "true");
        }
    }

    private void refreshPosters() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        FetchDiscoverJsonTask discoverJsonTask = new FetchDiscoverJsonTask();

        String sortOrder = Utility.getSortOrder(getActivity());
        imageUrl = Utility.getImageUrl(getActivity());
        posterSize = Utility.getPosterSize(getActivity());

        if (sortOrder.equals(getString(R.string.pref_sort_favorites))) {
            MovieDbHelper dbHelper = new MovieDbHelper(getActivity());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            List<MoviePoster> posters = getPosters(db);
            db.close();
            dbHelper.close();

            if (posters != null) {
                moviePosters = new MoviePoster[posters.size()];
                posters.toArray(moviePosters);

                if (posterAdapter != null) { // update poster adapter. Will be null if view has not yet been drawn.
                    posterAdapter.clear();
                    for (MoviePoster poster : moviePosters) {
                        posterAdapter.add(poster);
                    }
                }
            } else {
                Toast.makeText(getActivity(), "No favorites stored", Toast.LENGTH_SHORT).show();
            }

        } else {
            discoverJsonTask.execute(apiKey, sortOrder);
        }
    }

    private List<MoviePoster> getPosters(SQLiteDatabase db) {
        List<MoviePoster> posters = new ArrayList<>();

        Cursor cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                MoviePoster.PosterProjection,
                "1 = 1",
                null,
                null,
                null,
                MovieContract.MovieEntry._ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                MoviePoster poster = new MoviePoster(cursor.getString(1),//  TMDB Movie Id
                        cursor.getString(2), // movie Title
                        cursor.getLong(3), // release date
                        cursor.getString(4), // poster url
                        cursor.getDouble(5), // vote average
                        cursor.getString(6), // overview
                        cursor.getInt(7) // popularity
                );
                poster.setReviews(getReviews(cursor.getLong(0), db));
                poster.setTrailers(getTrailers(cursor.getLong(0), db));
                posters.add(poster);
            } while (cursor.moveToNext());
        }

        return posters;
    }

    private List<MovieTrailer> getTrailers(Long movieId, SQLiteDatabase db) {
        if (movieId != null && movieId != 0L && db != null) {
            List<MovieTrailer> trailers = new ArrayList<>();
            Cursor cursor = db.query(MovieContract.TrailerEntry.TABLE_NAME,
                    MovieTrailer.trailerProjection,
                    MovieContract.TrailerEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)},
                    null,
                    null,
                    null);

            if (cursor.moveToFirst()) {
                do {
                    MovieTrailer trailer = new MovieTrailer(cursor.getString(1), cursor.getString(2));
                    trailers.add(trailer);
                } while (cursor.moveToNext());
            }
            return trailers;
        } else {
            return null;
        }
    }

    private List<MovieReview> getReviews(Long movieId, SQLiteDatabase db) {
        if (movieId != null && movieId != 0L && db != null) {
            List<MovieReview> reviews = new ArrayList<>();
            Cursor cursor = db.query(MovieContract.ReviewEntry.TABLE_NAME,
                    MovieReview.reviewProjection,
                    MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?",
                    new String[]{String.valueOf(movieId)},
                    null,
                    null,
                    null);

            if (cursor.moveToFirst()) {
                do {
                    MovieReview review = new MovieReview(cursor.getString(1), cursor.getString(2));
                    reviews.add(review);
                } while (cursor.moveToNext());
            }
            return reviews;
        } else {
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (apiKey != null) {
            outState.putString(Utility.API_KEY, apiKey);
        }
        if (moviePosters != null) {
            outState.putParcelableArray(MOVIE_POSTERS, moviePosters);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        posterAdapter = new MoviePosterAdapter(getActivity(), new ArrayList<MoviePoster>());

        if (moviePosters != null) {
            posterAdapter.clear();
            posterAdapter.addAll(moviePosters);
        }

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.posters_grid);
        gridView.setAdapter(posterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"

                ((Callback) getActivity()).onItemSelected(posterAdapter.getItem(position));
            }
        });

        return rootView;
    }

    public void onSortOrderChanged() {
        refreshPosters();
    }

    public class ParseDiscoverJsonTask extends AsyncTask<String, Void, MoviePoster[]> {
        private final String LOG_TAG = ParseDiscoverJsonTask.class.getSimpleName();

        @Override
        protected MoviePoster[] doInBackground(String... params) {
            final String MOVIE_ID = "id";
            final String RESULTS = "results";
            final String OVERVIEW = "overview"; // overview
            final String RELEASE_DATE = "release_date";
            final String POSTER_PATH = "poster_path";
            final String POPULARITY = "popularity";
            final String TITLE = "title";
            final String VOTE_AVERAGE = "vote_average";
            final String API_KEY_PARAM = "api_key";

            if (params.length == 0) {
                Log.w(LOG_TAG, "No JSON string passed into ParseDiscoverJsonTask");
                return null;
            }

            MoviePoster[] posters = null;

            try {
                JSONObject posterJson = new JSONObject(params[0]);
                JSONArray results = posterJson.getJSONArray(RESULTS);

                posters = new MoviePoster[results.length()];

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

                    posters[i] = new MoviePoster(poster.getString(MOVIE_ID), poster.getString(TITLE), releaseDate,
                            builtUri.toString(), poster.getDouble(VOTE_AVERAGE), poster.getString(OVERVIEW),
                            poster.getInt(POPULARITY));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Unable to parse Discover JSON", e);
            }
            return posters;
        }

        protected void onPostExecute(MoviePoster[] posters) {
            if (posters != null) {
                moviePosters = posters;

                if (posterAdapter != null) { // update poster adapter. Will be null if view has not yet been drawn.
                    posterAdapter.clear();
                    for (MoviePoster poster : moviePosters) {
                        posterAdapter.add(poster);
                    }
                }
            }
        }
    }

    public class FetchDiscoverJsonTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = FetchDiscoverJsonTask.class.getSimpleName();

        // parameters in the order they were passed.
        private String apiKey;
        private String sortOrder;

        /**
         * Retrieves the movie poster discover information
         * @param params First param is the discovery url, Second param is the API_KEY,
         *               third param is the configuration url, fourth posterSize, fifth sortOrder
         * @return an array of MoviePoster obj
         */
        @Override
        protected String doInBackground(String... params) {

            if (params.length > 4) {
                return null;
            }

            apiKey = params[0];
            sortOrder = params[1];

            return downloadDiscoverJsonString();
        }

        @Override
        protected void onPostExecute(String json) {
            if (json != null) {
                discoverJson = json;
                ParseDiscoverJsonTask parseJsonTask = new ParseDiscoverJsonTask();
                parseJsonTask.execute(discoverJson);
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

                Log.d(LOG_TAG, "CONNECTION URL: " + builtUri.toString());
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

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(getString(R.string.config_image_url), configData.get(TMDB_SECURE_BASE_URL)[0]);
            editor.putString(getString(R.string.config_poster_size), configData.get(TMDB_POSTER_SIZES)[3]);
            editor.putLong(getString(R.string.config_last_config_refresh), new Date().getTime());
            editor.commit();

            String sortOrder = prefs.getString(getString(R.string.pref_sort_key),
                    getString(R.string.pref_sort_popular));

            imageUrl = configData.get(TMDB_SECURE_BASE_URL)[0];
            posterSize = configData.get(TMDB_POSTER_SIZES)[3];

            if (fetchPosterImages) {
                FetchDiscoverJsonTask posterTask = new FetchDiscoverJsonTask();
                posterTask.execute(apiKey, sortOrder);
            }
        }
    }
}
