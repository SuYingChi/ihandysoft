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
import com.ihs.commons.connection.HSHttpConnection;
import com.ihs.commons.utils.HSError;
import com.ihs.commons.utils.HSJsonUtil;
import com.ihs.keyboardutils.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SoftGameItemFragment extends Fragment {

    public static final int SOFT_GAME_LOAD_COUNT = 50;
    public static final String JSON_GAMES = "games";
    public static final String TOP_GAMES = "http://api.famobi.com/feed?a=A-KCVWU&n=50&sort=top_games";


    private ArrayList<SoftGameItemBean> softGameItemArrayList = new ArrayList<>();

    private SoftGameItemAdapter softGameItemAdapter;

    //50 games ID
    private List<String> gameIdList = asList("smarty-bubbles", "solitaire-classic", "lovetester", "treasure-hunt",
            "hextris", "stones-of-pharaoh", "burnin-rubber", "fruita-crush", "jewelish", "fidget-spinner-high-score",
            "streetrace-fury", "running-jack", "smarty-bubbles-xmas", "soccertastic", "candy-bubble", "parking-passion",
            "endless-truck", "sprint-club-nitro", "glow-lines", "ultimate-boxing", "klondike-solitaire", "turbotastic",
            "penalty-shooters-2", "western-solitaire", "kk-jungle-chaos", "wedding-lily", "euro-penalty-2016", "world-cup-penalty",
            "foot-chinko", "slacking-school", "tiny-rifles", "hop-dont-stop", "euro-soccer-sprint", "circle-rush", "1010-animals",
            "snail-bob-3", "puppy-maker", "pizza-margherita", "butterfly-chocolate-cake", "kuli", "speed-pool-king", "street-pursuit",
            "wild-west-solitaire", "piano-steps", "chocolate-biscuits", "fruita-swipe-2", "speed-maniac", "creamy-ice"
            , "spider-solitaire", "italian-tiramisu");

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

        HSHttpConnection hsHttpConnection = new HSHttpConnection(TOP_GAMES);
        hsHttpConnection.startAsync();
        hsHttpConnection.setConnectionFinishedListener(new HSHttpConnection.OnConnectionFinishedListener() {
            @Override
            public void onConnectionFinished(HSHttpConnection hsHttpConnection) {
                JSONObject bodyJSON = hsHttpConnection.getBodyJSON();
                try {
                    List<Object> jsonMap = HSJsonUtil.toList(bodyJSON.getJSONArray(JSON_GAMES));
                    for (Object stringObjectMap : jsonMap) {
                        Map<String, String> object = (Map<String, String>) stringObjectMap;
                        if (!gameIdList.contains(object.get("package_id"))) {
                            continue;
                        }
                        String name = object.get("name");
                        String description = object.get("description");
                        String thumb = object.get("thumb");
                        String link = object.get("link");
                        SoftGameItemBean bean = new SoftGameItemBean(name, description, thumb, link);
                        softGameItemArrayList.add(bean);
                        if (softGameItemArrayList.size() >= 50) {
                            break;
                        }
                    }
                    softGameItemAdapter.refreshDataList(softGameItemArrayList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onConnectionFailed(HSHttpConnection hsHttpConnection, HSError hsError) {
                hsError.getMessage();
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