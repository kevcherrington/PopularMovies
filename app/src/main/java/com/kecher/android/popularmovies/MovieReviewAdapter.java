package com.kecher.android.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
public class MovieReviewAdapter extends ArrayAdapter<MovieReview> {
    private static final String LOG_TAG = MovieReviewAdapter.class.getSimpleName();

    private static class ViewHolder {
        TextView authorView;
        TextView contentView;
    }

    public MovieReviewAdapter(Activity context, List<MovieReview> reviews) {
        super(context, 0, reviews);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
            holder = new ViewHolder();

            holder.authorView = (TextView) convertView.findViewById(R.id.review_author);
            holder.contentView = (TextView) convertView.findViewById(R.id.review_content);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final MovieReview review = getItem(position);

        holder.authorView.setText(review.getReviewAuthor());

        holder.contentView.setText(review.getReviewContent());

        return convertView;
    }
}
