package com.kecher.android.popularmovies;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;


/**
 * A placeholder fragment containing a simple view.
 */
public class PosterFragment extends Fragment {
    private final String LOG_TAG = PosterFragment.class.getSimpleName();

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
            FetchConfigDataTask configTask = new FetchConfigDataTask();
            configTask.execute(apiKey);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        posterAdapter = new MoviePosterAdapter(getActivity(), new ArrayList<MoviePoster>());
        // TODO cache the config data and then execute the FetchMoviePostersTask directly.
        FetchConfigDataTask configTask = new FetchConfigDataTask();
        configTask.execute(apiKey);

        // Get a reference to the ListView, and attach this adapter to it.
        GridView gridView = (GridView) rootView.findViewById(R.id.posters_grid);
        gridView.setAdapter(posterAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Executed in an Activity, so 'this' is the Context
                // The fileUrl is a string URL, such as "http://www.example.com/image.png"
                Intent movieDetailIntent = new Intent(getActivity(), MovieDetailActivity.class)
                .putExtra(Intent.EXTRA_TEXT, posterAdapter.getItem(position).getMovieTitle());
                startActivity(movieDetailIntent);
            }
        });

        return rootView;
    }

    public class FetchMoviePostersTask extends AsyncTask<String, Void, MoviePoster[]> {
        private final String LOG_TAG = FetchMoviePostersTask.class.getSimpleName();

        private MoviePoster[] parseMoviePosterJson(String json) throws JSONException {
            final String RESULTS = "results";
            final String OVERVIEW = "overview"; // plotSynopsis
            final String RELEASE_DATE = "release_date";
            final String POSTER_PATH = "poster_path";
            final String POPULARITY = "popularity";
            final String TITLE = "title";
            final String VOTE_AVERAGE = "vote_average";

            JSONObject posterJson = new JSONObject(json);
            JSONArray results = posterJson.getJSONArray(RESULTS);

            MoviePoster[] posters = new MoviePoster[results.length()];

            SimpleDateFormat sdf = new SimpleDateFormat(MoviePoster.DATE_FORMAT);
            for (int i = 0; i < results.length(); i++) {
                try {
                    JSONObject poster = results.getJSONObject(i);
                    posters[i] = new MoviePoster(poster.getString(TITLE), sdf.parse(poster.getString(RELEASE_DATE)),
                            poster.getString(POSTER_PATH), poster.getDouble(VOTE_AVERAGE), poster.getString(OVERVIEW),
                            poster.getInt(POPULARITY));
                } catch (ParseException e) {
                    Log.e(LOG_TAG, "Unable to parse date ", e);
                }
            }

            return posters;
        }

        private MoviePoster[] getMoviePosterImages(MoviePoster[] moviePosters, String posterSize, String configUrl, String apiKey) {
            for (MoviePoster poster : moviePosters) {
                poster.setMoviePosterBitmap(downloadBitmap(poster.getPosterPath(), posterSize, configUrl, apiKey));
            }
            return moviePosters;
        }

        private Bitmap downloadBitmap(String posterPath, String posterSize, String configUrl, String api_key) {
            HttpURLConnection urlConnection = null;

            Bitmap poster = null;

            try {
                final String API_KEY_PARAM = "api_key";


                Uri builtUri = Uri.parse(configUrl).buildUpon()
                        .appendPath(posterSize)
                        .appendPath(posterPath.replaceAll("/", ""))
                        .appendQueryParameter(API_KEY_PARAM, api_key).build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();

                poster = BitmapFactory.decodeStream(inputStream);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return poster;
        }

        /**
         * Retrieves the movie poster discover information
         * @param params First param is the discovery url, Second param is the API_KEY, third param is the configuration url, fourth posterSize
         * @return an array of MoviePoster obj
         */
        @Override
        protected MoviePoster[] doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String postersJsonStr = downloadJsonString(params);
            if (postersJsonStr == null) return null;

            try {
                return getMoviePosterImages(parseMoviePosterJson(postersJsonStr), params[3], params[2], params[1]);
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

        private String downloadJsonString(String[] params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String jsonStr = null;

            try {
                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(params[0]).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, params[1]).build();

                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                jsonStr = buffer.toString();

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
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                configurationJsonStr = buffer.toString();
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
                Iterator<Map.Entry<String, String[]>> it = result.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, String[]> pair = it.next();
                    Log.d(LOG_TAG, pair.getKey());
                    for (String item : Arrays.asList(pair.getValue())) {
                        Log.d(LOG_TAG, " -" + item);
                    }
                }
            }
            FetchMoviePostersTask posterTask= new FetchMoviePostersTask();
            posterTask.execute(DISCOVER_URL, apiKey, configData.get(TMDB_SECURE_BASE_URL)[0],
                    configData.get(TMDB_POSTER_SIZES)[3]); // 0th index contains the smallest poster size.
        }
    }
}
