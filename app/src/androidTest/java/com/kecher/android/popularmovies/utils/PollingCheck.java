package com.kecher.android.popularmovies.utils;

import junit.framework.Assert;

import java.util.concurrent.Callable;

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
public abstract class PollingCheck {
    private static final String LOG_TAG = PollingCheck.class.getSimpleName();
    private static final long TIME_SLICE = 50;
    private long mTimeout = 3000;

    public PollingCheck() {
    }

    public PollingCheck(long timeout) {
        mTimeout = timeout;
    }

    protected abstract boolean check();

    public void run() {
        if (check()) {
            return;
        }

        long timeout = mTimeout;
        while (timeout > 0) {
            try {
                Thread.sleep(TIME_SLICE);
            } catch (InterruptedException e) {
                Assert.fail("unexpected InterruptedException");
            }

            if (check()) {
                return;
            }

            timeout -= TIME_SLICE;
        }

        Assert.fail("unexpected timeout");
    }

    public static void check(CharSequence message, long timeout, Callable<Boolean> condition)
            throws Exception {
        while (timeout > 0) {
            if (condition.call()) {
                return;
            }

            Thread.sleep(TIME_SLICE);
            timeout -= TIME_SLICE;
        }

        Assert.fail(message.toString());
    }
}
