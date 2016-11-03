package com.ihs.keyboardutilslib.gif;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.ihs.keyboardutilslib.R;

import java.util.Random;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by jixiang on 16/11/3.
 */

public class GifViewDemoActivity extends HSActivity {
    private HSGifImageView hsGifImageView;
    private RecyclerView recyclerView;
    private int column = 2;
    private int count = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_demo);

        initView();
    }

    private void initView() {
        hsGifImageView = (HSGifImageView) findViewById(R.id.gif_image_view);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

    }

    public void showOneGif(View view) {
        hsGifImageView.setImageResource(R.raw.fml);

    }
    public void recycleOneGif(View view) {
        Drawable drawable = hsGifImageView.getDrawable();
        if (drawable != null) {
            if (drawable instanceof GifDrawable) {
                ((GifDrawable) drawable).recycle();
            }
            hsGifImageView.setImageDrawable(null);
        }
    }

    public void showAndRecycleOneGif(View view) {
        for(int i = 0;i<1000;i++) {
            showOneGif(null);
            recycleOneGif(null);
        }
    }

    public void showGifList(View view) {
        GifAdapter gifAdapter = new GifAdapter(count,column);
        recyclerView.setLayoutManager(new GridLayoutManager(HSApplication.getContext(),column));
        recyclerView.setAdapter(gifAdapter);
        gifAdapter.notifyDataSetChanged();
    }

    public void randomShowGifList(View view) {
        column = new Random().nextInt(4)+2;
        count = new Random().nextInt(100)+20;
        GifAdapter gifAdapter = new GifAdapter(count,column);
        recyclerView.setLayoutManager(new GridLayoutManager(HSApplication.getContext(),column));
        recyclerView.setAdapter(gifAdapter);
        gifAdapter.notifyDataSetChanged();
    }

    public class GifAdapter extends RecyclerView.Adapter<GifViewHolder> {
        private int[] resIds = new int[]{R.raw.fml, R.raw.halloween, R.raw.raw, R.raw.raw_2, R.raw.raw_3,
                R.raw.shutup, R.raw.sorry, R.raw.thankyou};
        private int winWidth;
        private int count;
        private int column;

        public GifAdapter(int count,int column) {
            winWidth = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels;
            this.count = count;
            this.column = column;
        }

        @Override
        public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_view_item, parent, false);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.width = winWidth / column;
            layoutParams.height = (int) (layoutParams.width * 0.7);
            layoutParams.setMargins(5, 5, 5, 5);
            GifViewHolder gifViewHolder = new GifViewHolder(view);
            return gifViewHolder;
        }

        @Override
        public void onBindViewHolder(GifViewHolder holder, int position) {
            int resId = position < resIds.length ? position : position % resIds.length;
            holder.hsGifImageView.setImageResource(resIds[resId]);
        }

        @Override
        public int getItemCount() {
            return count;
        }
    }

    public class GifViewHolder extends RecyclerView.ViewHolder {
        private HSGifImageView hsGifImageView;

        public GifViewHolder(View itemView) {
            super(itemView);
            hsGifImageView = (HSGifImageView) itemView.findViewById(R.id.gif_item);
        }
    }
}
