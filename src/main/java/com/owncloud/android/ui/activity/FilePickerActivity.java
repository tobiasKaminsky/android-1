package com.owncloud.android.ui.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.owncloud.android.R;
import com.owncloud.android.ui.fragment.OCFileListFragment;

public class FilePickerActivity extends FolderPickerActivity {

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    protected void createFragments() {
        OCFileListFragment listOfFiles = new OCFileListFragment();
        Bundle args = new Bundle();
        args.putBoolean(OCFileListFragment.ARG_ONLY_FOLDERS_CLICKABLE, true);
        args.putBoolean(OCFileListFragment.ARG_HIDE_FAB, true);
        args.putBoolean(OCFileListFragment.ARG_HIDE_ITEM_OPTIONS, true);
        args.putBoolean(OCFileListFragment.ARG_SEARCH_ONLY_FOLDER, false);
        args.putBoolean(OCFileListFragment.ARG_FILE_SELECTABLE, true);
        listOfFiles.setArguments(args);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, listOfFiles, TAG_LIST_OF_FOLDERS);
        transaction.commit();
    }
}
