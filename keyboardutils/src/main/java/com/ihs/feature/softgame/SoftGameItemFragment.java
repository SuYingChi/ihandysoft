package com.ihs.feature.softgame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.commons.utils.HSJsonUtil;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.ToastUtils;
import com.millennialmedia.internal.utils.ThreadUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SoftGameItemFragment extends Fragment {

    public static final int SOFT_GAME_LOAD_COUNT = 50;
    public static final String JSON_GAMES = "games";

    private ArrayList<SoftGameItemBean> softGameItemArrayList = new ArrayList<>();

    private SoftGameItemAdapter softGameItemAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Object url = getArguments().get("url");
        if (url == null) {
            return null;
        }

        View v = inflater.inflate(R.layout.frag_game_hot, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.soft_game_main_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(HSApplication.getContext(), LinearLayoutManager.VERTICAL, false));
        softGameItemAdapter = new SoftGameItemAdapter();
        recyclerView.setAdapter(softGameItemAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));

        ThreadUtils.runOnWorkerThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject jsonObject = loadJSONFromAsset();
                    if (jsonObject != null) {
                        List<Object> jsonMap = HSJsonUtil.toList(jsonObject.getJSONArray(JSON_GAMES));
                        for (Object stringObjectMap : jsonMap) {
                            Map<String, String> object = (Map<String, String>) stringObjectMap;
                            String name = object.get("name");
                            String description = object.get("description");
                            String thumb = object.get("thumb");
                            String link = object.get("link");
                            SoftGameItemBean bean = new SoftGameItemBean(name, description, thumb, link);
                            softGameItemArrayList.add(bean);
                        }
                        softGameItemAdapter.refreshDataList(softGameItemArrayList);
                    } else {
                        ToastUtils.showToast("Game data error!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        return v;
    }

    public static SoftGameItemFragment newInstance(String text, String placementName) {

        SoftGameItemFragment f = new SoftGameItemFragment();
        Bundle b = new Bundle();
        b.putString("url", text);
        b.putString("placementName", placementName);
        f.setArguments(b);

        return f;
    }

    private JSONObject loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getActivity().getAssets().open("h5games.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}