package com.ihs.keyboardutilslib.gif;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ihs.app.framework.HSApplication;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.keyboardutils.view.HSGifImageView;
import com.ihs.keyboardutilslib.R;

import java.util.Random;

/**
 * Created by jixiang on 16/11/3.
 */

public class GifViewDemoActivity extends HSActivity {
    private HSGifImageView hsGifImageView;
    private HSGifImageView hsGifImageView2;
    private RecyclerView recyclerView;
    private int column = 2;
    private int count = 200;

    private final static int WHAT_LOOP = 1;
    private int loopNum = 0;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WHAT_LOOP:
                    if(loopNum < 1000) {
                        switch (loopNum % 3) {
                            case 0:
                                hsGifImageView.setImageResource(R.raw.halloween);
                                break;
                            case 1:
                                hsGifImageView.setImageResource(R.raw.shutup);
                                break;
                            case 2:
                                hsGifImageView.clear();
                                break;
                        }
                        loopNum++;
                        sendEmptyMessageDelayed(WHAT_LOOP,300);
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gif_demo);

        initView();
    }

    private void initView() {
        hsGifImageView = (HSGifImageView) findViewById(R.id.gif_image_view);
        hsGifImageView2 = (HSGifImageView) findViewById(R.id.gif_image_view2);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

    }

    public void showLeftGif(View view) {
        hsGifImageView.setImageResource(R.raw.fml);

    }

    public void releaseLeft(View view) {
        hsGifImageView.clear();
    }

    public void showRightGif(View view) {
        hsGifImageView2.setImageResource(R.raw.fml);
    }


    public void stopLeftGif(View view) {
        hsGifImageView.stop();

    }
    public void startLeftGif(View view) {
        hsGifImageView.start();

    }

    public void loopShowGif(View view) {
        loopNum = 0;
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessage(WHAT_LOOP);
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
    public void changeGifListRadio(View view) {
        GifAdapter gifAdapter = new GifAdapter(count,column);
        gifAdapter.setRatio(1.0f);
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
        private float ratio = 0.7f;

        public GifAdapter(int count,int column) {
            winWidth = HSApplication.getContext().getResources().getDisplayMetrics().widthPixels;
            this.count = count;
            this.column = column;
        }

        public void setRatio(float ratio){
            this.ratio = ratio;
        }

        @Override
        public GifViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gif_view_item, parent, false);
            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            layoutParams.width = winWidth / column;
            layoutParams.height = (int) (layoutParams.width * ratio);
            layoutParams.setMargins(5, 5, 5, 5);
            GifViewHolder gifViewHolder = new GifViewHolder(view);

            if(ratio == 1.0){
                gifViewHolder.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else {
                gifViewHolder.setScaleType(ImageView.ScaleType.FIT_CENTER);
            }
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

        public void setScaleType(ImageView.ScaleType scaleType){
            hsGifImageView.setScaleType(scaleType);
        }
    }
}
