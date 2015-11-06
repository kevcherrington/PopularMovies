package com.kecher.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

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
public class MainActivity extends ActionBarActivity implements PosterFragment.Callback {

    private String sortOrder;
    private final String DETAILFRAGMENT_TAG = "DFTAG";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sortOrder = Utility.getSortOrder(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new MovieDetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String storedSortOrder = Utility.getSortOrder(this);
        if (storedSortOrder != null && !storedSortOrder.equals(sortOrder)) {
            PosterFragment pf = (PosterFragment) getSupportFragmentManager().findFragmentById(R.id.movie_thumbnails);
            if (pf != null) {
                pf.onSortOrderChanged();
            }
            sortOrder = storedSortOrder;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(MoviePoster poster) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(PosterFragment.EXTRA_POSTER_PARCEL, poster);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();

        } else {
            Intent movieDetailIntent = new Intent(this, MovieDetailActivity.class)
                    .putExtra(PosterFragment.EXTRA_POSTER_PARCEL, poster);
            startActivity(movieDetailIntent);

        }
    }
}
