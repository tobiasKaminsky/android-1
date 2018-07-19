/*
 * Nextcloud Android client application
 *
 * @author Andy Scherzinger
 * @author Bartek Przybylski
 * @author David A. Velasco
 * Copyright (C) 2011  Bartek Przybylski
 * Copyright (C) 2015 ownCloud Inc.
 * Copyright (C) 2016 Andy Scherzinger
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU AFFERO GENERAL PUBLIC LICENSE
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU AFFERO GENERAL PUBLIC LICENSE for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.owncloud.android.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatDrawableManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.owncloud.android.MainApp;
import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.ArbitraryDataProvider;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.lib.common.OwnCloudAccount;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.utils.Log_OC;
import com.owncloud.android.lib.resources.files.SearchOperation;
import com.owncloud.android.lib.resources.files.ServerFileInterface;
import com.owncloud.android.ui.TextDrawable;
import com.owncloud.android.ui.activity.FileDisplayActivity;
import com.owncloud.android.ui.events.MenuItemClickEvent;
import com.owncloud.android.ui.events.SearchEvent;
import com.owncloud.android.ui.fragment.OCFileListFragment;
import com.owncloud.android.utils.glide.GlideApp;
import com.owncloud.android.utils.glide.GlideAvatar;
import com.owncloud.android.utils.glide.GlideContainer;
import com.owncloud.android.utils.glide.GlideKey;
import com.owncloud.android.utils.glide.GlideOCFileType;
import com.owncloud.android.utils.glide.GlideOcFile;
import com.owncloud.android.utils.svg.SvgSoftwareLayerSetter;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.IDN;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * A helper class for UI/display related operations.
 */
public class DisplayUtils {
    private static final String TAG = DisplayUtils.class.getSimpleName();

    private static final String[] sizeSuffixes = {"B", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
    private static final int[] sizeScales = {0, 0, 1, 1, 1, 2, 2, 2, 2};
    private static final int RELATIVE_THRESHOLD_WARNING = 90;
    private static final int RELATIVE_THRESHOLD_CRITICAL = 95;
    private static final String MIME_TYPE_UNKNOWN = "Unknown type";

    private static final String HTTP_PROTOCOL = "http://";
    private static final String HTTPS_PROTOCOL = "https://";
    private static final String TWITTER_HANDLE_PREFIX = "@";
    private static final String ETAG = "ETag";

    private static Map<String, String> mimeType2HumanReadable;

    static {
        mimeType2HumanReadable = new HashMap<>();
        // images
        mimeType2HumanReadable.put("image/jpeg", "JPEG image");
        mimeType2HumanReadable.put("image/jpg", "JPEG image");
        mimeType2HumanReadable.put("image/png", "PNG image");
        mimeType2HumanReadable.put("image/bmp", "Bitmap image");
        mimeType2HumanReadable.put("image/gif", "GIF image");
        mimeType2HumanReadable.put("image/svg+xml", "JPEG image");
        mimeType2HumanReadable.put("image/tiff", "TIFF image");
        // music
        mimeType2HumanReadable.put("audio/mpeg", "MP3 music file");
        mimeType2HumanReadable.put("application/ogg", "OGG music file");
    }

    /**
     * Converts the file size in bytes to human readable output.
     * <ul>
     *     <li>appends a size suffix, e.g. B, KB, MB etc.</li>
     *     <li>rounds the size based on the suffix to 0,1 or 2 decimals</li>
     * </ul>
     *
     * @param bytes Input file size
     * @return something readable like "12 MB", {@link com.owncloud.android.R.string#common_pending} for negative
     * byte values
     */
    public static String bytesToHumanReadable(long bytes) {
        if (bytes < 0) {
            return MainApp.getAppContext().getString(R.string.common_pending);
        } else {
            double result = bytes;
            int suffixIndex = 0;
            while (result > 1024 && suffixIndex < sizeSuffixes.length) {
                result /= 1024.;
                suffixIndex++;
            }

            return new BigDecimal(String.valueOf(result)).setScale(
                    sizeScales[suffixIndex], BigDecimal.ROUND_HALF_UP) + " " + sizeSuffixes[suffixIndex];
        }
    }

    /**
     * Converts MIME types like "image/jpg" to more end user friendly output
     * like "JPG image".
     *
     * @param mimetype MIME type to convert
     * @return A human friendly version of the MIME type, {@link #MIME_TYPE_UNKNOWN} if it can't be converted
     */
    public static String convertMIMEtoPrettyPrint(String mimetype) {
        if (mimeType2HumanReadable.containsKey(mimetype)) {
            return mimeType2HumanReadable.get(mimetype);
        }
        if (mimetype.split("/").length >= 2) {
            return mimetype.split("/")[1].toUpperCase(Locale.getDefault()) + " file";
        }
        return MIME_TYPE_UNKNOWN;
    }

    /**
     * Converts Unix time to human readable format
     *
     * @param milliseconds that have passed since 01/01/1970
     * @return The human readable time for the users locale
     */
    public static String unixTimeToHumanReadable(long milliseconds) {
        Date date = new Date(milliseconds);
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(date);
    }

    /**
     * beautifies a given URL by removing any http/https protocol prefix.
     *
     * @param url to be beautified url
     * @return beautified url
     */
    public static String beautifyURL(@Nullable String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }

        if (url.length() >= 7 && url.substring(0, 7).equalsIgnoreCase(HTTP_PROTOCOL)) {
            return url.substring(HTTP_PROTOCOL.length()).trim();
        }

        if (url.length() >= 8 && url.substring(0, 8).equalsIgnoreCase(HTTPS_PROTOCOL)) {
            return url.substring(HTTPS_PROTOCOL.length()).trim();
        }

        return url.trim();
    }

    /**
     * beautifies a given twitter handle by prefixing it with an @ in case it is missing.
     *
     * @param handle to be beautified twitter handle
     * @return beautified twitter handle
     */
    public static String beautifyTwitterHandle(@Nullable String handle) {
        if (handle != null) {
            String trimmedHandle = handle.trim();

            if (TextUtils.isEmpty(trimmedHandle)) {
                return "";
            }

            if (trimmedHandle.startsWith(TWITTER_HANDLE_PREFIX)) {
                return trimmedHandle;
            } else {
                return TWITTER_HANDLE_PREFIX + trimmedHandle;
            }
        } else {
            return "";
        }
    }

    /**
     * Converts an internationalized domain name (IDN) in an URL to and from ASCII/Unicode.
     *
     * @param url the URL where the domain name should be converted
     * @param toASCII if true converts from Unicode to ASCII, if false converts from ASCII to Unicode
     * @return the URL containing the converted domain name
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String convertIdn(String url, boolean toASCII) {

        String urlNoDots = url;
        String dots = "";
        while (urlNoDots.startsWith(".")) {
            urlNoDots = url.substring(1);
            dots = dots + ".";
        }

        // Find host name after '//' or '@'
        int hostStart = 0;
        if (urlNoDots.contains("//")) {
            hostStart = url.indexOf("//") + "//".length();
        } else if (url.contains("@")) {
            hostStart = url.indexOf('@') + "@".length();
        }

        int hostEnd = url.substring(hostStart).indexOf("/");
        // Handle URL which doesn't have a path (path is implicitly '/')
        hostEnd = (hostEnd == -1 ? urlNoDots.length() : hostStart + hostEnd);

        String host = urlNoDots.substring(hostStart, hostEnd);
        host = (toASCII ? IDN.toASCII(host) : IDN.toUnicode(host));

        return dots + urlNoDots.substring(0, hostStart) + host + urlNoDots.substring(hostEnd);
    }

    /**
     * creates the display string for an account.
     *
     * @param context the actual activity
     * @param savedAccount the actual, saved account
     * @param accountName the account name
     * @param fallbackString String to be used in case of an error
     * @return the display string for the given account data
     */
    public static String getAccountNameDisplayText(Context context, Account savedAccount, String accountName, String
            fallbackString) {
        try {
            return new OwnCloudAccount(savedAccount, context).getDisplayName()
                    + "@"
                    + convertIdn(accountName.substring(accountName.lastIndexOf('@') + 1), false);
        } catch (Exception e) {
            Log_OC.w(TAG, "Couldn't get display name for account, using old style");
            return fallbackString;
        }
    }

    /**
     * converts an array of accounts into a set of account names.
     *
     * @param accountList the account array
     * @return set of account names
     */
    public static Set<String> toAccountNameSet(Collection<Account> accountList) {
        Set<String> actualAccounts = new HashSet<>(accountList.size());
        for (Account account : accountList) {
            actualAccounts.add(account.name);
        }
        return actualAccounts;
    }

    /**
     * calculates the relative time string based on the given modification timestamp.
     *
     * @param context the app's context
     * @param modificationTimestamp the UNIX timestamp of the file modification time in milliseconds.
     * @return a relative time string
     */
    public static CharSequence getRelativeTimestamp(Context context, long modificationTimestamp) {
        return getRelativeDateTimeString(context, modificationTimestamp, DateUtils.SECOND_IN_MILLIS,
                DateUtils.WEEK_IN_MILLIS, 0);
    }


    /**
     * determines the info level color based on certain thresholds
     * {@link #RELATIVE_THRESHOLD_WARNING} and {@link #RELATIVE_THRESHOLD_CRITICAL}.
     *
     * @param context  the app's context
     * @param relative relative value for which the info level color should be looked up
     * @return info level color
     */
    public static int getRelativeInfoColor(Context context, int relative) {
        if (relative < RELATIVE_THRESHOLD_WARNING) {
            if (ThemeUtils.colorToHexString(ThemeUtils.primaryColor(context)).equalsIgnoreCase(
                    ThemeUtils.colorToHexString(context.getResources().getColor(R.color.primary)))) {
                return context.getResources().getColor(R.color.infolevel_info);
            } else {
                return Color.GRAY;
            }
        } else if (relative >= RELATIVE_THRESHOLD_WARNING && relative < RELATIVE_THRESHOLD_CRITICAL) {
            return context.getResources().getColor(R.color.infolevel_warning);
        } else {
            return context.getResources().getColor(R.color.infolevel_critical);
        }
    }

    public static CharSequence getRelativeDateTimeString(Context c, long time, long minResolution,
                                                         long transitionResolution, int flags) {

        CharSequence dateString = "";

        // in Future
        if (time > System.currentTimeMillis()) {
            return DisplayUtils.unixTimeToHumanReadable(time);
        }
        // < 60 seconds -> seconds ago
        else if ((System.currentTimeMillis() - time) < 60 * 1000 && minResolution == DateUtils.SECOND_IN_MILLIS) {
            return c.getString(R.string.file_list_seconds_ago);
        } else {
            dateString = DateUtils.getRelativeDateTimeString(c, time, minResolution, transitionResolution, flags);
        }

        String[] parts = dateString.toString().split(",");
        if (parts.length == 2) {
            if (parts[1].contains(":") && !parts[0].contains(":")) {
                return parts[0];
            } else if (parts[0].contains(":") && !parts[1].contains(":")) {
                return parts[1];
            }
        }
        // dateString contains unexpected format. fallback: use relative date time string from android api as is.
        return dateString.toString();
    }

    /**
     * Update the passed path removing the last "/" if it is not the root folder.
     *
     * @param path the path to be trimmed
     */
    public static String getPathWithoutLastSlash(String path) {

        // Remove last slash from path
        if (path.length() > 1 && path.charAt(path.length() - 1) == OCFile.PATH_SEPARATOR.charAt(0)) {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }

    /**
     * Gets the screen size in pixels.
     *
     * @param caller Activity calling; needed to get access to the {@link android.view.WindowManager}
     * @return Size in pixels of the screen, or default {@link Point} if caller is null
     */
    public static Point getScreenSize(Activity caller) {
        Point size = new Point();
        if (caller != null) {
            caller.getWindowManager().getDefaultDisplay().getSize(size);
        }
        return size;
    }

    /**
     * styling of given spanText within a given text.
     *
     * @param text     the non styled complete text
     * @param spanText the to be styled text
     * @param style    the style to be applied
     */
    public static SpannableStringBuilder createTextWithSpan(String text, String spanText, StyleSpan style) {
        if (text == null) {
            return null;
        }

        SpannableStringBuilder sb = new SpannableStringBuilder(text);
        if(spanText == null) {
            return sb;
        }

        int start = text.lastIndexOf(spanText);

        if (start < 0) {
            return sb;
        }

        int end = start + spanText.length();
        sb.setSpan(style, start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sb;
    }

    public interface AvatarGenerationListener {
        void avatarGenerated(Drawable avatarDrawable, Object callContext);

        boolean shouldCallGeneratedCallback(String tag, Object callContext);
    }

    /**
     * fetches and sets the avatar of the given account in the passed callContext
     *
     * @param account        the account to be used to connect to server
     */
    public static void setAvatar(@NonNull Account account, Context context, SimpleTarget<Drawable> target) {

        AccountManager accountManager = AccountManager.get(context);
        String userId = accountManager.getUserData(account,
                com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_USER_ID);

        setAvatar(account, userId, context, target);
    }

    /**
     * fetches and sets the avatar of the given account in the passed callContext
     *
     * @param account        the account to be used to connect to server
     */
    public static void setAvatar(@NonNull Account account, Context context, ImageView view, float radius) {

        AccountManager accountManager = AccountManager.get(context);
        String userId = accountManager.getUserData(account,
                com.owncloud.android.lib.common.accounts.AccountUtils.Constants.KEY_USER_ID);

        setAvatar(account, userId, context, view, radius);
    }

    /**
     * fetches and sets the avatar of the given account in the passed callContext
     *
     * @param account        the account to be used to connect to server
     * @param userId         the userId which avatar should be set
     * @param view           where the image is shown in
     */
    public static void setAvatar(@NonNull Account account, @NonNull String userId, Context context, ImageView view,
                                 float radius) {
        AsyncTask task = new AsyncTask<Object, Void, InputStream>() {
            GlideContainer container;

            @Override
            protected InputStream doInBackground(Object[] objects) {
                InputStream inputStream = null;
                
                // we need to create client here, as different servers can be used
                OwnCloudClient client = AccountUtils.getClientForAccount(account, context);

                int px = getAvatarDimension(context);

                ArbitraryDataProvider arbitraryDataProvider = new ArbitraryDataProvider(context.getContentResolver());
                String serverName = account.name.substring(account.name.lastIndexOf('@') + 1, account.name.length());
                String accountName = userId + "@" + serverName;
                String eTag = arbitraryDataProvider.getValue(accountName, GlideKey.AVATAR);
                Log_OC.d(TAG, "glide: old etag: " + eTag);

                GetMethod get = null;
                try {
                    String uri = client.getBaseUri() + "/index.php/avatar/" + Uri.encode(userId) + "/" + px;
                    Log_OC.d("Avatar", "URI: " + uri);
                    get = new GetMethod(uri);

                    // only use eTag if available and corresponding avatar is still there 
                    // (might be deleted from cache)
//                    if (!eTag.isEmpty() && getBitmapFromDiskCache(avatarKey) != null) {
                    // TODO check if in cache
                    if (!eTag.isEmpty()) {
                        get.setRequestHeader("If-None-Match", eTag);
                    }

                    int status = client.executeMethod(get);

                    Log_OC.d(TAG, "glide: status: " + status);

                    // we are using eTag to download a new avatar only if it changed
                    switch (status) {
                        case HttpStatus.SC_OK:
                            // new avatar
                            inputStream = get.getResponseBodyAsStream();

                            String newETag = null;
                            if (get.getResponseHeader(ETAG) != null) {
                                newETag = get.getResponseHeader(ETAG).getValue().replace("\"", "");
                                Log_OC.d(TAG, "glide: new etag: " + newETag);
                                arbitraryDataProvider.storeOrUpdateKeyValue(accountName, GlideKey.AVATAR, newETag);
                            }
                            // Add avatar to cache
                            if (inputStream != null && !TextUtils.isEmpty(newETag)) {
                                // TODO glide
                                // avatar = handlePNG(avatar, px, px);
                                String newImageKey = "a_" + userId + "_" + serverName + "_" + newETag;

                                // TODO GLIDE
                                // addBitmapToCache(newImageKey, avatar);
                            } else {
                                // TODO glide 
                                // return TextDrawable.createAvatar(account.name, mAvatarRadius);
                            }
                            break;

                        case HttpStatus.SC_NOT_MODIFIED:
                            // old avatar
                            // TODO glide
                            // avatar = getBitmapFromDiskCache(avatarKey);
                            client.exhaustResponse(get.getResponseBodyAsStream());
                            break;

                        default:
                            // everything else
                            client.exhaustResponse(get.getResponseBodyAsStream());
                            break;

                    }
                } catch (Exception e) {
                    try {
                        // TODO glide 
//                        return TextDrawable.createAvatar(mAccount.name, mAvatarRadius);
                    } catch (Exception e1) {
                        Log_OC.e(TAG, "Error generating fallback avatar");
                    }
                } finally {
                    if (get != null) {
                        //  get.releaseConnection(); // TODO glide this must not be released?
                    }
                }


//                int px = getAvatarDimension(context);
//                container = new GlideContainer();
//                container.url = client.getBaseUri() + "/index.php/avatar/" + Uri.encode(userId) + "/" + px;
//                container.client = client;
//                container.key = GlideKey.avatar(account, userId, context);

                return inputStream;
            }

            @Override
            protected void onPostExecute(InputStream inputStream) {
                int placeholder = R.drawable.ic_user;

                Drawable failback = null;
                try {
                    // TODO every time created?
                    failback = TextDrawable.createAvatar(account.name, radius);
                } catch (NoSuchAlgorithmException e) {

                }


                try {
                    GlideApp.with(context)
                            .asBitmap()
                            .load(new GlideAvatar(GlideKey.avatar(account, userId, context), inputStream))
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(placeholder)
                            .error(failback)
                            .onlyRetrieveFromCache(inputStream == null)
                            .into(view);
                } catch (Exception e) {
                    // TODO glide
                }
            }
        };

        task.execute();
    }

    /**
     * fetches and sets the avatar of the given account in the passed callContext
     *
     * @param account        the account to be used to connect to server
     * @param userId         the userId which avatar should be set
     * @param target         where the image is shown in
     */
    // TODO combine with better approach (eTag)
    public static void setAvatar(@NonNull Account account, @NonNull String userId, Context context,
                                 SimpleTarget<Drawable> target) {
        AsyncTask task = new AsyncTask() {
            GlideContainer container;

            @Override
            protected Object doInBackground(Object[] objects) {
                // we need to create client here, as different servers can be used
                OwnCloudClient client = AccountUtils.getClientForAccount(account, context);

                int px = getAvatarDimension(context);
                container = new GlideContainer();
                container.url = client.getBaseUri() + "/index.php/avatar/" + Uri.encode(userId) + "/" + px;
                container.client = client;
                container.key = GlideKey.avatar(account, userId, context);

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                int placeholder = R.drawable.ic_user;

                try {
                    GlideApp.with(context)
                            .load(container)
                            .placeholder(placeholder)
                            .error(R.drawable.ic_list_empty_error)
                            .apply(RequestOptions.circleCropTransform())
                            .into(target);
                } catch (Exception e) {
                    // TODO glide
                }
            }
        };

        task.execute();
    }

    public static void downloadIcon(Context context, String iconUrl, SimpleTarget<Drawable> imageView, int placeholder,
                                    int error) {
        try {
            if (iconUrl.endsWith(".svg")) {
                // TODO glide exception
                downloadSVG(iconUrl, placeholder, error, imageView, context);
            } else {
                downloadPNGIcon(context, iconUrl, imageView, placeholder);
            }
        } catch (Exception e) {
            Log_OC.d(TAG, "not setting image as activity is destroyed");
        }
    }

    private static void downloadPNGIcon(Context context, String iconUrl, SimpleTarget<Drawable> imageView,
                                        int placeholder) {
        GlideApp.with(context)
                .load(iconUrl)
                .centerCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .into(imageView);
    }

    public static void downloadSVG(String url, int placeholder, int error, ImageView imageView, Context context) {
        GlideApp.with(context)
                .as(PictureDrawable.class)
                .placeholder(placeholder)
                .fitCenter()
                .error(error)
                .listener(new SvgSoftwareLayerSetter<>())
                .load(url)
                .into(imageView);
    }

    // TODO glide unify with ImageView
    public static void downloadSVG(String url, int placeholder, int error, SimpleTarget<Drawable> imageView,
                                   Context context) {
        GlideApp.with(context)
                .as(PictureDrawable.class) // TODO glide needed?
                .load(url)
                .placeholder(placeholder)
                .error(error)
                .listener(new SvgSoftwareLayerSetter<>())
                .into((SimpleTarget) imageView);
    }

    public static Bitmap downloadImageSynchronous(Context context, String imageUrl) {
        try {
            // TODO glide
            return null;
//            return Glide.with(context)
//                    .load(imageUrl)
//                    .asBitmap()
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)
//                    .skipMemoryCache(true)
//                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
//                    .get();
        } catch (Exception e) {
            Log_OC.e(TAG, "Could not download image " + imageUrl);
            return null;
        }
    }

    public static void localImage(File file, int placeholder, int error, ImageView view, Key key, Context context) {
        GlideApp.with(context)
                .load(file)
                .placeholder(placeholder)
                .error(error)
                .into(view);
    }

    public static void downloadThumbnail(OCFile file, ImageView view, OwnCloudClient client, Context context) {
        // TODO glide: first try to extract thumbnail from resized image
        
        GlideContainer container = new GlideContainer();

        int placeholder = MimeTypeUtil.isVideo(file) ? R.drawable.file_movie : R.drawable.file_image;
        int pxW = DisplayUtils.getThumbnailDimension();
        int pxH = DisplayUtils.getThumbnailDimension();

        container.url = client.getBaseUri() + "/index.php/apps/files/api/v1/thumbnail/" + pxW + "/" + pxH +
                Uri.encode(file.getRemotePath(), "/");
        container.client = client;
        container.key = GlideKey.serverThumbnail(file);

        GlideApp.with(context)
                .load(container)
                .placeholder(placeholder)
                .into(view);
    }

    public static void downloadActivityThumbnail(OCFile file, ImageView view, OwnCloudClient client, Context context) {
        // TODO glide: first try to extract thumbnail from resized image

        GlideContainer container = new GlideContainer();

        int placeholder = MimeTypeUtil.isVideo(file) ? R.drawable.file_movie : R.drawable.file_image;
        int pxW = DisplayUtils.getThumbnailDimension();
        int pxH = DisplayUtils.getThumbnailDimension();

        container.url = client.getBaseUri() + "/index.php/apps/files/api/v1/thumbnail/" + pxW + "/" + pxH +
                Uri.encode(file.getRemotePath(), "/");
        container.client = client;
        container.key = GlideKey.activityThumbnail(file);

        GlideApp.with(context)
                .load(container)
                .placeholder(placeholder)
                .into(view);
    }

    public static Drawable getThumbnail(OCFile file, ImageView view, OwnCloudClient client, Context context) {
        GlideContainer container = new GlideContainer();

        int placeholder = MimeTypeUtil.isVideo(file) ? R.drawable.file_movie : R.drawable.file_image;
        int pxW = DisplayUtils.getThumbnailDimension();
        int pxH = DisplayUtils.getThumbnailDimension();

        container.url = client.getBaseUri() + "/index.php/apps/files/api/v1/thumbnail/" + pxW + "/" + pxH +
                Uri.encode(file.getRemotePath(), "/");
        container.client = client;
        container.key = GlideKey.serverThumbnail(file);

        try {
            return GlideApp.with(context)
                    .load(container)
                    .placeholder(placeholder)
                    .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            Log_OC.e(TAG, "Could not download image " + container.url);
            return context.getResources().getDrawable(placeholder);
        }
    }

    public static void downloadImage(String uri, int placeholder, int error, ImageView view, OwnCloudClient client,
                                     Key key, Context context) {
        GlideContainer container = new GlideContainer();

        container.url = uri;
        container.key = key;
        container.client = client;
        
        GlideApp.with(context)
                .load(container)
                .placeholder(placeholder)
                .error(error)
                .into(view);
    }

    public static String getThumbnailUri(OwnCloudClient client, ServerFileInterface file, int size) {
        return client.getBaseUri() + "/index.php/apps/files_trashbin/preview?fileId=" +
                file.getLocalId() + "&x=" + size + "&y=" + size;
    }

//    public static void showResizedImage(String uri, Drawable placeholder, Drawable error, ImageView view, OwnCloudClient client,
//                                     Key key, Context context) {
//        
//        
//        GlideContainer container = new GlideContainer();
//
//        container.url = uri;
//        container.key = key;
//        container.client = client;
//
//        GlideApp.with(context)
//                .load(container)
//                .placeholder(placeholder)
//                .error(error)
//                .into(view);
//    }

    public static void downloadImage(String uri, int placeholder, int error, SimpleTarget<Drawable> target, Key key,
                                     Context context) {
        GlideApp.with(context)
                .load(uri)
                .placeholder(placeholder)
                .error(error)
                .into(target);
    }

    public static void generateResizedImage(OCFile file, Context context) {
        Point p = DisplayUtils.getScreenDimension();
        int pxW = p.x;
        int pxH = p.y;

        try {
            GlideApp.with(context)
                    .load(new GlideOcFile(file, GlideOCFileType.resizedImage))
                    .downloadOnly(pxW, pxH).get();
        } catch (InterruptedException | ExecutionException e) {
            Log_OC.e(TAG, "Thumbnail generation failed", e);
        }
    }

    public static void generateThumbnail(OCFile file, String path, Context context) {
        int pxW = DisplayUtils.getThumbnailDimension();
        int pxH = DisplayUtils.getThumbnailDimension();

        try {
            GlideApp.with(context)
                    .load(new GlideOcFile(file, GlideOCFileType.thumbnail, path))
                    .downloadOnly(pxW, pxH).get();
        } catch (InterruptedException | ExecutionException e) {
            Log_OC.e(TAG, "Thumbnail generation failed", e);
        }
    }

    public static void setupBottomBar(BottomNavigationView view, Resources resources, final Activity activity,
                                      int checkedMenuItem) {

        Menu menu = view.getMenu();

        Account account = AccountUtils.getCurrentOwnCloudAccount(MainApp.getAppContext());
        boolean searchSupported = AccountUtils.hasSearchSupport(account);

        if (!searchSupported) {
            menu.removeItem(R.id.nav_bar_favorites);
            menu.removeItem(R.id.nav_bar_photos);
        }

        if (resources.getBoolean(R.bool.use_home)) {
            menu.findItem(R.id.nav_bar_files).setTitle(resources.
                    getString(R.string.drawer_item_home));
            menu.findItem(R.id.nav_bar_files).setIcon(R.drawable.ic_home);
        }

        setBottomBarItem(view, checkedMenuItem);

        view.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.nav_bar_files:
                                EventBus.getDefault().post(new MenuItemClickEvent(item));
                                if (activity != null) {
                                    activity.invalidateOptionsMenu();
                                }
                                break;
                            case R.id.nav_bar_favorites:
                                SearchEvent favoritesEvent = new SearchEvent("",
                                        SearchOperation.SearchType.FAVORITE_SEARCH,
                                        SearchEvent.UnsetType.UNSET_DRAWER);

                                switchToSearchFragment(activity, favoritesEvent);
                                break;
                            case R.id.nav_bar_photos:
                                SearchEvent photosEvent = new SearchEvent("image/%",
                                        SearchOperation.SearchType.CONTENT_TYPE_SEARCH,
                                        SearchEvent.UnsetType.UNSET_DRAWER);

                                switchToSearchFragment(activity, photosEvent);
                                break;
                            case R.id.nav_bar_settings:
                                EventBus.getDefault().post(new MenuItemClickEvent(item));
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
    }

    public static void setBottomBarItem(BottomNavigationView view, int checkedMenuItem) {
        Menu menu = view.getMenu();

        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setChecked(false);
        }

        if (checkedMenuItem != -1) {
            menu.findItem(checkedMenuItem).setChecked(true);
        }
    }

    private static void switchToSearchFragment(Activity activity, SearchEvent event) {
        if (activity instanceof FileDisplayActivity) {
            EventBus.getDefault().post(event);
        } else {
            Intent recentlyAddedIntent = new Intent(activity.getBaseContext(), FileDisplayActivity.class);
            recentlyAddedIntent.putExtra(OCFileListFragment.SEARCH_EVENT, Parcels.wrap(event));
            recentlyAddedIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(recentlyAddedIntent);
        }
    }


    /**
     * Get String data from a InputStream
     *
     * @param inputStream        The File InputStream
     */
    public static String getData(InputStream inputStream) {

        BufferedReader buffreader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        StringBuilder text = new StringBuilder();
        try {
            while ((line = buffreader.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
        } catch (IOException e) {
            Log_OC.e(TAG, e.getMessage());
        }
        return text.toString();
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the content view.
     *
     * @param activity        The {@link Activity} to which's content view the {@link Snackbar} is bound.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     */
    public static void showSnackMessage(Activity activity, @StringRes int messageResource) {
        showSnackMessage(activity.findViewById(android.R.id.content), messageResource);
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the content view.
     *
     * @param activity The {@link Activity} to which's content view the {@link Snackbar} is bound.
     * @param message  Message to show.
     */
    public static void showSnackMessage(Activity activity, String message) {
        Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the given view.
     *
     * @param view            The view the {@link Snackbar} is bound to.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     */
    public static void showSnackMessage(View view, @StringRes int messageResource) {
        Snackbar.make(view, messageResource, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the given view.
     *
     * @param view    The view the {@link Snackbar} is bound to.
     * @param message The message.
     */
    public static void showSnackMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * create a temporary message in a {@link Snackbar} bound to the given view.
     *
     * @param view            The view the {@link Snackbar} is bound to.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     */
    public static Snackbar createSnackbar(View view, @StringRes int messageResource, int length) {
        return Snackbar.make(view, messageResource, length);
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the content view.
     *
     * @param activity        The {@link Activity} to which's content view the {@link Snackbar} is bound.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     * @param formatArgs      The format arguments that will be used for substitution.
     */
    public static void showSnackMessage(Activity activity, @StringRes int messageResource, Object... formatArgs) {
        showSnackMessage(activity, activity.findViewById(android.R.id.content), messageResource, formatArgs);
    }

    /**
     * Show a temporary message in a {@link Snackbar} bound to the content view.
     *
     * @param context         to load resources.
     * @param view            The content view the {@link Snackbar} is bound to.
     * @param messageResource The resource id of the string resource to use. Can be formatted text.
     * @param formatArgs      The format arguments that will be used for substitution.
     */
    public static void showSnackMessage(Context context, View view, @StringRes int messageResource, Object... formatArgs) {
        Snackbar.make(
                view,
                String.format(context.getString(messageResource, formatArgs)),
                Snackbar.LENGTH_LONG)
                .show();
    }

    // Solution inspired by https://stackoverflow.com/questions/34936590/why-isnt-my-vector-drawable-scaling-as-expected
    // Copied from https://raw.githubusercontent.com/nextcloud/talk-android/8ec8606bc61878e87e3ac8ad32c8b72d4680013c/app/src/main/java/com/nextcloud/talk/utils/DisplayUtils.java
    // under GPL3
    public static void useCompatVectorIfNeeded() {
        if (Build.VERSION.SDK_INT < 23) {
            try {
                @SuppressLint("RestrictedApi") AppCompatDrawableManager drawableManager = AppCompatDrawableManager.get();
                Class<?> inflateDelegateClass = Class.forName("android.support.v7.widget.AppCompatDrawableManager$InflateDelegate");
                Class<?> vdcInflateDelegateClass = Class.forName("android.support.v7.widget.AppCompatDrawableManager$VdcInflateDelegate");

                Constructor<?> constructor = vdcInflateDelegateClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object vdcInflateDelegate = constructor.newInstance();

                Class<?> args[] = {String.class, inflateDelegateClass};
                Method addDelegate = AppCompatDrawableManager.class.getDeclaredMethod("addDelegate", args);
                addDelegate.setAccessible(true);
                addDelegate.invoke(drawableManager, "vector", vdcInflateDelegate);
            } catch (Exception e) {
                Log.e(TAG, "Failed to use reflection to enable proper vector scaling");
            }
        }
    }

    static public void showServerOutdatedSnackbar(Activity activity) {
        Snackbar.make(activity.findViewById(android.R.id.content),
                R.string.outdated_server, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.dismiss, v -> {
                })
                .show();
    }

    /**
     * Converts size of file icon from dp to pixel
     *
     * @return int
     */
    public static int getThumbnailDimension() {
        // Converts dp to pixel
        Resources r = MainApp.getAppContext().getResources();
        return Math.round(r.getDimension(R.dimen.file_icon_size_grid));
    }

    /**
     * Converts dimension of screen as point
     *
     * @return Point
     */
    public static Point getScreenDimension() {
        WindowManager wm = (WindowManager) MainApp.getAppContext().getSystemService(Context.WINDOW_SERVICE);

        if (wm == null) {
            // fallback to reasonable size for resized images
            return new Point(1024, 868);
        } else {
            Display display = wm.getDefaultDisplay();
            Point point = new Point();
            display.getSize(point);
            return point;
        }
    }

    private static int getAvatarDimension(Context context) {
        // Converts dp to pixel
        Resources r = context.getResources();
        return Math.round(r.getDimension(R.dimen.file_avatar_size));
    }
}
