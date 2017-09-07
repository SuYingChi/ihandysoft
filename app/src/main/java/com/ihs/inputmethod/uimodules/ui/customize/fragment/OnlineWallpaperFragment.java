package com.ihs.inputmethod.uimodules.ui.customize.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.customize.view.OnlineWallpaperPage;

/**
 * Created by guonan.lv on 17/9/7.
 */

public class OnlineWallpaperFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = (OnlineWallpaperPage) inflater.inflate(R.layout.online_wallpaper_page, container, false);
        ((OnlineWallpaperPage) view).setup(0);
        return view;
    }
}
