package com.kecher.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * (C) Copyright 2015 Kevin Cherrington (kevcherrington@gmail.com).
 * <p/>
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p/>
 * Contributors:
 * Kevin Cherrington
 */
public class Utility {
    private static final String LOG_TAG = Utility.class.getSimpleName();

    public static final String API_KEY = "api_key";

    public static String getApiKey(Context context) {
        String apiKey = null;
        try {
            InputStream rawResource = context.getResources().openRawResource(R.raw.popular_movies);
            Properties properties = new Properties();
            properties.load(rawResource);
            apiKey = properties.getProperty(API_KEY);
        } catch (Resources.NotFoundException e) {
            Log.e(LOG_TAG, "Unable to find resource: ", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to open resource: ", e);
        }
        return apiKey;
    }

    public static String getSortOrder(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_popular));
    }

    public static String getImageUrl(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.config_image_url),
                context.getString(R.string.config_default_image_url));
    }

    public static String getPosterSize(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        return prefs.getString(context.getString(R.string.config_poster_size),
                context.getString(R.string.config_default_poster_size));
    }

}
