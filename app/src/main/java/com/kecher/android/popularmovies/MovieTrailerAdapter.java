package com.kecher.android.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

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
public class MovieTrailerAdapter extends ArrayAdapter<MovieTrailer> {
    private static final String LOG_TAG = MovieTrailerAdapter.class.getSimpleName();

    private Context mContext;

    private static class ViewHolder {
        ImageButton playButton;
        TextView trailerTitleView;
    }
    public MovieTrailerAdapter(Activity context, List<MovieTrailer> movieTrailers) {
        super(context, 0, movieTrailers);
        mContext = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent, false);
            holder = new ViewHolder();

            holder.playButton = (ImageButton) convertView.findViewById(R.id.trailer_play_button);
            holder.trailerTitleView = (TextView) convertView.findViewById(R.id.trailer_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MovieTrailer trailer = getItem(position);

        holder.playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(trailer.getTrailerUrl()));
                mContext.startActivity(i);
            }
        });

        holder.trailerTitleView.setText(trailer.getTrailerTitle());

        return convertView;
    }
}
