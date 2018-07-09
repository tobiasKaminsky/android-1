/**
 *   ownCloud Android client application
 *
 *   @author Tobias Kaminsky
 *   Copyright (C) 2016 ownCloud Inc.
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

package com.owncloud.android.ui.adapter;

import android.accounts.Account;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.utils.DisplayUtils;
import com.owncloud.android.utils.MimeTypeUtil;
import com.owncloud.android.utils.glide.GlideKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploaderAdapter extends SimpleAdapter {
    private static final String TAG = UploaderAdapter.class.getSimpleName();
    
    private Context mContext;
    private Account mAccount;
    private FileDataStorageManager mStorageManager;
    private LayoutInflater inflater;

    public UploaderAdapter(Context context,
                           List<? extends Map<String, ?>> data, int resource, String[] from,
                           int[] to, FileDataStorageManager storageManager, Account account) {
        super(context, data, resource, from, to);
        mAccount = account;
        mStorageManager = storageManager;
        mContext = context;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.uploader_list_item_layout, null);
        }

        HashMap<String, OCFile> data = (HashMap<String, OCFile>) getItem(position);
        OCFile file = data.get("dirname");

        TextView filename = vi.findViewById(R.id.filename);
        filename.setText(file.getFileName());

        ImageView fileIcon = vi.findViewById(R.id.thumbnail);

        TextView lastModV = vi.findViewById(R.id.last_mod);
        lastModV.setText(DisplayUtils.getRelativeTimestamp(mContext, file.getModificationTimestamp()));

        TextView fileSizeV = vi.findViewById(R.id.file_size);
        TextView fileSizeSeparatorV = vi.findViewById(R.id.file_separator);

        if(!file.isFolder()) {
            fileSizeV.setVisibility(View.VISIBLE);
            fileSizeSeparatorV.setVisibility(View.VISIBLE);
            fileSizeV.setText(DisplayUtils.bytesToHumanReadable(file.getFileLength()));
        } else {
            fileSizeV.setVisibility(View.GONE);
            fileSizeSeparatorV.setVisibility(View.GONE);
        }

        if (file.isFolder()) {
            fileIcon.setImageDrawable(MimeTypeUtil.getFolderTypeIcon(file.isSharedWithMe() ||
                            file.isSharedWithSharee(), file.isSharedViaLink(), file.isEncrypted(), mAccount,
                    file.getMountType(), mContext));
        } else {
            // get Thumbnail if file is image
            if (MimeTypeUtil.isImageOrVideo(file)) {
                int placeholder = MimeTypeUtil.isImage(file) ? R.drawable.file_image : R.drawable.file_movie;

                try {
                    // TODO move baseUrl in constructor
                    String baseUrl = com.owncloud.android.lib.common.accounts.AccountUtils
                            .getBaseUrlForAccount(mContext, mAccount);

                    // todo move thumbnail uri to DisplayUtils
                    int pxW = DisplayUtils.getThumbnailDimension();
                    int pxH = DisplayUtils.getThumbnailDimension();
                    String url = baseUrl + "/index.php/apps/files/api/v1/thumbnail/" + pxW + "/" + pxH +
                            Uri.encode(file.getRemotePath(), "/");
                    DisplayUtils.downloadImage(url, placeholder, fileIcon, GlideKey.serverThumbnail(file), mContext);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            } else {
                fileIcon.setImageDrawable(
                        MimeTypeUtil.getFileTypeIcon(file.getMimeType(), file.getFileName(), mAccount, mContext)
                );
            }
        }

        return vi;
    }


}
