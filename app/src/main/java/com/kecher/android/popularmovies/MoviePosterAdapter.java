package com.kecher.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

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
public class MoviePosterAdapter extends CursorAdapter {
    private static final String LOG_TAG = MoviePosterAdapter.class.getSimpleName();

    public static class viewHolder {

    }

    public MoviePosterAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    // create a new ImageView for each item referenced by the Adapter
//    public View getView(int position, View convertView, ViewGroup parent) {
//
//        MoviePoster moviePoster = getItem(position);
//
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.poster_item, parent, false);
//        }
//
//        ImageView posterImageView = (ImageView) convertView.findViewById(R.id.poster_image);
//
//        Picasso.with(mContext).load(moviePoster.getPosterUrl()).placeholder(R.drawable.no_image)
//                .error(R.drawable.no_image).into(posterImageView);
//
//        return convertView;
//    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate()
        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

}
