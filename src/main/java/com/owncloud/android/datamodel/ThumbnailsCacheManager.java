/*
 *   ownCloud Android client application
 *
 *   @author Tobias Kaminsky
 *   @author David A. Velasco
 *   Copyright (C) 2015 ownCloud Inc.
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License version 2,
 *   as published by the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.owncloud.android.datamodel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import com.owncloud.android.MainApp;
import com.owncloud.android.R;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.ui.adapter.DiskLruImageCache;
import com.owncloud.android.utils.BitmapUtils;
import com.owncloud.android.utils.DisplayUtils;

/**
 * Manager for concurrent access to thumbnails cache.
 */
public class ThumbnailsCacheManager {

    public static final String PREFIX_RESIZED_IMAGE = "r";
    public static final String PREFIX_THUMBNAIL = "t";

    private static final String TAG = ThumbnailsCacheManager.class.getSimpleName();
    private static final String PNG_MIMETYPE = "image/png";

    private static final Object mThumbnailsDiskCacheLock = new Object();
    private static DiskLruImageCache mThumbnailCache = null;
    private static boolean mThumbnailCacheStarting = true;


    public static Bitmap getBitmapFromDiskCache(String key) {
        synchronized (mThumbnailsDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mThumbnailCacheStarting) {
                try {
                    mThumbnailsDiskCacheLock.wait();
                } catch (InterruptedException e) {
                    Log_OC.e(TAG, "Wait in mThumbnailsDiskCacheLock was interrupted", e);
                }
            }
            if (mThumbnailCache != null) {
                return mThumbnailCache.getBitmap(key);
            }
        }
        return null;
    }


    public static Bitmap addVideoOverlay(Bitmap thumbnail){
        Bitmap playButton = BitmapFactory.decodeResource(MainApp.getAppContext().getResources(),
                R.drawable.view_play);

        Bitmap resizedPlayButton = Bitmap.createScaledBitmap(playButton,
                (int) (thumbnail.getWidth() * 0.3),
                (int) (thumbnail.getHeight() * 0.3), true);

        Bitmap resultBitmap = Bitmap.createBitmap(thumbnail.getWidth(),
                thumbnail.getHeight(),
                Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(resultBitmap);

        // compute visual center of play button, according to resized image
        int x1 = resizedPlayButton.getWidth();
        int y1 = resizedPlayButton.getHeight() / 2;
        int x2 = 0;
        int y2 = resizedPlayButton.getWidth();
        int x3 = 0;
        int y3 = 0;

        double ym = ( ((Math.pow(x3,2) - Math.pow(x1,2) + Math.pow(y3,2) - Math.pow(y1,2)) *
                (x2 - x1)) - (Math.pow(x2,2) - Math.pow(x1,2) + Math.pow(y2,2) -
                Math.pow(y1,2)) * (x3 - x1) )  /  (2 * ( ((y3 - y1) * (x2 - x1)) -
                ((y2 - y1) * (x3 - x1)) ));
        double xm = ( (Math.pow(x2,2) - Math.pow(x1,2)) + (Math.pow(y2,2) - Math.pow(y1,2)) -
                (2*ym*(y2 - y1)) ) / (2*(x2 - x1));

        // offset to top left
        double ox = - xm;


        c.drawBitmap(thumbnail, 0, 0, null);

        Paint p = new Paint();
        p.setAlpha(230);

        c.drawBitmap(resizedPlayButton, (float) ((thumbnail.getWidth() / 2) + ox),
                (float) ((thumbnail.getHeight() / 2) - ym), p);

        return resultBitmap;
    }

    private static Bitmap handlePNG(Bitmap bitmap, int pxW, int pxH) {
        Bitmap resultBitmap = Bitmap.createBitmap(pxW, pxH, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(resultBitmap);

        c.drawColor(MainApp.getAppContext().getResources().getColor(R.color.background_color));
        c.drawBitmap(bitmap, 0, 0, null);

        return resultBitmap;
    }

    public static void generateResizedImage(OCFile file) {
        Point p = DisplayUtils.getScreenDimension();
        int pxW = p.x;
        int pxH = p.y;
        String imageKey = PREFIX_RESIZED_IMAGE + String.valueOf(file.getRemoteId());

        Bitmap bitmap = BitmapUtils.decodeSampledBitmapFromFile(file.getStoragePath(), pxW, pxH);

        if (bitmap != null) {
            // Handle PNG
            if (file.getMimeType().equalsIgnoreCase(PNG_MIMETYPE)) {
                bitmap = handlePNG(bitmap, pxW, pxH);
            }

            // TODO manually add to glide cache
            // addThumbnailToCache(imageKey, bitmap, file.getStoragePath(), pxW, pxH);
        }
    }
}
