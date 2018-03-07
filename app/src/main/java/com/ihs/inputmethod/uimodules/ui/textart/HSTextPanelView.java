package com.ihs.inputmethod.uimodules.ui.textart;

import android.content.Context;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ihs.commons.utils.HSLog;
import com.ihs.inputmethod.api.framework.HSInputMethod;
import com.ihs.inputmethod.api.theme.HSKeyboardThemeManager;
import com.ihs.inputmethod.api.utils.HSResourceUtils;
import com.ihs.inputmethod.uimodules.R;
import com.ihs.inputmethod.uimodules.ui.common.BaseTabViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.HSEmojiTabAdapter;
import com.ihs.inputmethod.uimodules.ui.common.adapter.HSEmojiViewAdapter;
import com.ihs.inputmethod.uimodules.ui.common.model.Emoji;
import com.kc.utils.KCAnalytics;

import java.util.ArrayList;
import java.util.List;


public class HSTextPanelView extends LinearLayout implements BaseTabViewAdapter.OnTabChangeListener,
        HSEmojiViewAdapter.OnEmojiClickListener {


    private HSEmojiTabAdapter tabAdapter;
    private RecyclerView emojiView;
    private HSEmojiViewAdapter emojiAdapter;

    private final TextCategory emojiCategory;


    public HSTextPanelView(Context context) {
        this(context, null);
    }

    public HSTextPanelView(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HSTextPanelView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        final Resources res = context.getResources();

        emojiCategory = new TextCategory(PreferenceManager.getDefaultSharedPreferences(context), res);

        this.setBackgroundColor(HSKeyboardThemeManager.getCurrentTheme().getDominantColor());
        final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
                + res.getDimensionPixelSize(R.dimen.config_suggestions_strip_height)
                - res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (tabAdapter == null) {
            List<String> tabs = new ArrayList<>();
            tabs.addAll(emojiCategory.getTabs());
            tabAdapter = new HSEmojiTabAdapter(tabs, this);
            RecyclerView mTabHost = findViewById(R.id.image_category_tabhost);
            mTabHost.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL));
            mTabHost.setAdapter(tabAdapter);
        }

        final Resources res = getResources();
        final int height = HSResourceUtils.getDefaultKeyboardHeight(res)
                - res.getDimensionPixelSize(R.dimen.emoticon_panel_actionbar_height);

        final int width = HSResourceUtils.getDefaultKeyboardWidth(res);
        final int emojiCol = res.getInteger(R.integer.config_text_art_col_count);
        final int emojiRow = res.getInteger(R.integer.config_text_art_row_count);
        final int emojiHeight = height / emojiRow;
        final int emojiWidth = (int) (width / (emojiCol + 0.5f));

        emojiView = findViewById(R.id.emoji_keyboard_pager);
        emojiAdapter = new HSEmojiViewAdapter(emojiHeight, emojiWidth, 0.4f, this);
        emojiAdapter.setHasStableIds(true);
        emojiView.setLayoutManager(new StaggeredGridLayoutManager(emojiRow, StaggeredGridLayoutManager.HORIZONTAL));
        emojiView.setAdapter(emojiAdapter);
        emojiView.addOnScrollListener(new ScrollListener());
        emojiView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));

        RecyclerView.ItemAnimator animator = emojiView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
    }


    @Override
    public void onTabChanged(final String nextTab) {
        HSLog.d("change to tab " + nextTab);
        emojiCategory.setCurrentTabName(nextTab);
        if (emojiCategory.isRecentTab(nextTab) && emojiCategory.hasPendingRecent()) {
            emojiCategory.flushPendingRecentEmoji();
            emojiAdapter.setData(emojiCategory.getSortEmoji());
            setCurrentItemPosition(0, 0);
        } else {
            Pair<Integer, Integer> position = emojiCategory.getLastShownItemPositionForTab(nextTab);
            setCurrentItemPosition(position.first, position.second);
        }
    }

    @Override
    public void onEmojiClick(final Emoji key) {
        emojiCategory.pendingRecentEmoji(key);
        HSInputMethod.inputText(key.getLabel());
        KCAnalytics.logEvent("kaomoji_input", "keyLabel", key.getLabel());
    }


    public void showPanelView() {
        setHardwareAcceleratedDrawingEnabled(HSInputMethod.isHardwareAcceleratedDrawingEnabled());
        emojiAdapter.setData(emojiCategory.getSortEmoji());
        tabAdapter.setCurrentTab(emojiCategory.getCurrentTabName(), emojiCategory.getDefaultTab());
    }

    private void setHardwareAcceleratedDrawingEnabled(final boolean enabled) {
        if (!enabled)
            return;
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    private void setCurrentItemPosition(final int position, final int offset) {
        if (emojiView == null) {
            return;
        }
        ((StaggeredGridLayoutManager) emojiView.getLayoutManager()).scrollToPositionWithOffset(position, offset);
    }

    public void saveRecent() {
        emojiCategory.saveRecent();
    }


    private final class ScrollListener extends OnScrollListener {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dx == 0) {
                return;
            }
            StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) emojiView.getLayoutManager();
            int[] into = new int[getResources().getInteger(R.integer.config_text_art_row_count)];
            layoutManager.findFirstVisibleItemPositions(into);
            final String tab = emojiCategory.getTabNameForPosition(into[0]);
            if (!tab.equals(emojiCategory.getCurrentTabName())) {
                emojiCategory.setCurrentTabName(tab);
                tabAdapter.setTabSelected(tab);
                if (emojiCategory.isRecentTab(tab)) {
                    emojiCategory.flushPendingRecentEmoji();
                    emojiAdapter.setData(emojiCategory.getSortEmoji());
                }
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (!emojiView.canScrollHorizontally(-1)
                        && !emojiCategory.isRecentTab(emojiCategory.getCurrentTabName())
                        && !emojiCategory.isRecentEmpty()) {

                    emojiCategory.flushPendingRecentEmoji();
                    emojiAdapter.setData(emojiCategory.getSortEmoji());
                    Pair<Integer, Integer> itemPosition = emojiCategory.getLastShownItemPositionForTab(emojiCategory.getCurrentTabName());
                    setCurrentItemPosition(itemPosition.first, itemPosition.second);
                }
            }
        }
    }
}
