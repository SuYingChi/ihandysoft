package com.ihs.feature.resultpage;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artw.lockscreen.common.NavUtils;
import com.ihs.feature.battery.BatteryActivity;
import com.ihs.feature.boost.plus.BoostPlusActivity;
import com.ihs.feature.common.DeviceManager;
import com.ihs.feature.common.Thunk;
import com.ihs.feature.common.VectorCompat;
import com.ihs.feature.common.ViewUtils;
import com.ihs.feature.cpucooler.CpuCoolerManager;
import com.ihs.feature.cpucooler.CpuCoolerScanActivity;
import com.ihs.feature.junkclean.JunkCleanActivity;
import com.ihs.feature.junkclean.util.JunkCleanConstant;
import com.ihs.feature.junkclean.util.JunkCleanUtils;
import com.ihs.feature.resultpage.data.CardData;
import com.ihs.feature.resultpage.data.ResultConstants;
import com.ihs.keyboardutils.R;
import com.ihs.keyboardutils.utils.CommonUtils;
import com.ihs.keyboardutils.utils.ToastUtils;
import com.kc.utils.KCAnalytics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ResultListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ResultPageActivity mResultPageActivity;
    private final int mResultType;
    private final List<CardData> mCardDataList;
    private final Set<Integer> mUsedTokens;

    ResultListAdapter(ResultPageActivity activity, int resultType, List<CardData> cardDataList) {
        mResultPageActivity = activity;
        mResultType = resultType;
        mCardDataList = cardDataList;
        mUsedTokens = new HashSet<>(8);
    }

    @Override
    public int getItemCount() {
        return null == mCardDataList ? 0 : mCardDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        CardData cardData = null;
        if (null != mCardDataList && mCardDataList.size() > position) {
            cardData = mCardDataList.get(position);
        }
        if (null == cardData) {
            return ResultConstants.CARD_VIEW_TYPE_INVALID;
        }
        return cardData.getCardType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
            case ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS:
            case ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER:
            case ResultConstants.CARD_VIEW_TYPE_SECURITY:
            case ResultConstants.CARD_VIEW_TYPE_CPU_COOLER:
            case ResultConstants.CARD_VIEW_TYPE_MAX_GAME_BOOSTER:
            case ResultConstants.CARD_VIEW_TYPE_MAX_APP_LOCKER:
            case ResultConstants.CARD_VIEW_TYPE_MAX_DATA_THIEVES:
            case ResultConstants.CARD_VIEW_TYPE_ACCESSIBILITY:
                return new FeatureCardViewHolder(LayoutInflater.from(mResultPageActivity).inflate(R.layout.result_page_card_function, viewGroup, false));
            case ResultConstants.CARD_VIEW_TYPE_DEFAULT:
                return new DescriptionCardViewHolder(LayoutInflater.from(mResultPageActivity).inflate(R.layout.result_page_card_description, viewGroup, false));
            case ResultConstants.CARD_VIEW_TYPE_INVALID:
                break;
            default:
                break;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Context context = mResultPageActivity;
        int viewType = getItemViewType(position);
        if (position == 0) {
            if (holder instanceof BaseCardViewHolder) {
                ViewUtils.setMargins(((BaseCardViewHolder) holder).cardItemView, 0, CommonUtils.pxFromDp(8), 0, CommonUtils.pxFromDp(4));
            }
        }
        if (position == getItemCount() - 1) {
            if (holder instanceof BaseCardViewHolder) {
                ViewUtils.setMargins(((BaseCardViewHolder) holder).cardItemView, 0, CommonUtils.pxFromDp(4), 0, CommonUtils.pxFromDp(8));
            }
        }
        switch (viewType) {
            case ResultConstants.CARD_VIEW_TYPE_BATTERY:
                final int batteryLevel = DeviceManager.getInstance().getBatteryLevel();
                CharSequence content;
                @DrawableRes int iconResId;
                if (batteryLevel >= 50) {
                    content = context.getString(R.string.result_page_card_battery_saver_description_normal);
                    iconResId = R.drawable.result_page_battery_high;
                } else {
                    String contentText = context.getString(R.string.result_page_card_battery_saver_description_low_battery,
                            batteryLevel + "%");
                    int percentageIndex = contentText.indexOf('%');
                    SpannableString contentSpannable = new SpannableString(contentText);
                    contentSpannable.setSpan(
                            new ForegroundColorSpan(Color.RED),
                            0, percentageIndex + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    content = contentSpannable;
                    iconResId = R.drawable.result_page_battery_low;
                }

                bindFeatureCardViewHolder((FeatureCardViewHolder) holder, iconResId, R.color.result_card_battery_bg,
                        R.string.result_page_card_battery_saver_title, content,
                        R.string.battery_optimize, v -> onClickBatteryView());
                doOnce(position, () -> KCAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.BATTERY));
                break;
            case ResultConstants.CARD_VIEW_TYPE_BOOST_PLUS:
                bindFeatureCardViewHolder((FeatureCardViewHolder) holder, R.drawable.result_page_boost_plus,
                        R.color.result_card_boost_plus_bg,
                        R.string.result_page_card_boost_plus_title,
                        context.getString(R.string.result_page_card_boost_plus_description),
                        R.string.result_page_card_boost_plus_btn, v -> onClickBoostPlusView());
                doOnce(position, () -> KCAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.BOOST_PLUS));
                break;
            case ResultConstants.CARD_VIEW_TYPE_JUNK_CLEANER:
                bindFeatureCardViewHolder((FeatureCardViewHolder) holder, R.drawable.result_page_junk_cleaner,
                        R.color.result_card_junk_cleaner_bg,
                        R.string.clean_title, context.getString(R.string.clean_card_content),
                        R.string.clean_capital, v -> onClickJunkCleanerView());
                doOnce(position, () -> {
                    KCAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.JUNK_CLEANER);
                });
                break;
            case ResultConstants.CARD_VIEW_TYPE_CPU_COOLER:
                int temperature  = CpuCoolerManager.getInstance().fetchCpuTemperature();
                String temperatureText = String.valueOf(temperature + " " + context.getString(R.string.cpu_cooler_temperature_quantifier_celsius));
                String contentText = context.getString(R.string.cpu_cooler_card_description, temperatureText);
                SpannableString contentSpannableString = new SpannableString(contentText);
                contentSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, temperatureText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                contentSpannableString.setSpan(new StyleSpan(Typeface.BOLD), 0, temperatureText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                bindFeatureCardViewHolder((FeatureCardViewHolder) holder, R.drawable.result_page_cpu_cooler,
                        R.color.result_card_cpu_cooler_bg,
                        R.string.promotion_max_card_title_cpu_cooler, contentSpannableString,
                        R.string.cool_capital, v -> onClickCpuCoolerView());
                doOnce(position, () -> KCAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.CPU_COOLER));
                break;
            case ResultConstants.CARD_VIEW_TYPE_ACCESSIBILITY:
                bindFeatureCardViewHolder((FeatureCardViewHolder) holder, R.drawable.result_page_accessibility,
                        R.color.result_card_accessibility_bg,
                        R.string.promotion_max_card_title_accessibility,
                        context.getString(R.string.promotion_max_card_description_accessibility),
                        R.string.promotion_enable_btn, v -> onClickAccessibilityView());
                break;
            case ResultConstants.CARD_VIEW_TYPE_DEFAULT:
                final DescriptionCardViewHolder descriptionCardViewHolder = (DescriptionCardViewHolder) holder;
                KCAnalytics.logEvent("ResultPage_Cards_Show", "type", ResultConstants.DEFAULT);
                switch (mResultType) {
                    case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                        descriptionCardViewHolder.titleTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_boost_plus_title));
                        descriptionCardViewHolder.contentTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_boost_plus_description));
                        break;
                    case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                        descriptionCardViewHolder.titleTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_junk_cleaner_title));
                        descriptionCardViewHolder.contentTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_junk_cleaner_description));
                        break;
                    case ResultConstants.RESULT_TYPE_CPU_COOLER:
                        descriptionCardViewHolder.titleTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_cpu_cooler_title));
                        descriptionCardViewHolder.contentTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_cpu_cooler_description));
                        break;
                    case ResultConstants.RESULT_TYPE_BATTERY:
                        descriptionCardViewHolder.titleTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_battery_title));
                        descriptionCardViewHolder.contentTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_battery_description));
                        break;
                    case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
                        descriptionCardViewHolder.titleTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_notification_center_title));
                        descriptionCardViewHolder.contentTv.setText(mResultPageActivity.getString(R.string.result_page_card_default_notification_center_description));
                        break;
                }
                break;
            case ResultConstants.CARD_VIEW_TYPE_INVALID:
                break;
            default:
                break;
        }
    }

    private void bindFeatureCardViewHolder(FeatureCardViewHolder holder,
                                           @DrawableRes int vectorIconResId, @ColorRes int flavorColorResId,
                                           @StringRes int titleResId, CharSequence content,
                                           @StringRes int buttonTextResId, View.OnClickListener buttonAction) {
        Context context = mResultPageActivity;
        holder.iconIv.setImageDrawable(VectorCompat.createVectorDrawable(context, vectorIconResId));
        holder.iconContainer.setBackgroundColor(ContextCompat.getColor(context, flavorColorResId));
        holder.titleTv.setText(context.getString(titleResId));
        holder.contentTv.setText(content);
        holder.functionTv.setText(context.getString(buttonTextResId));
        holder.functionTv.setTextColor(ContextCompat.getColor(context, getPrimaryColorResId()));
        holder.functionTv.setOnClickListener(buttonAction);
        if (null != holder.cardItemView) {
            holder.cardItemView.setOnClickListener(buttonAction);
        }
    }

    private @ColorRes int getPrimaryColorResId() {
        switch (mResultType) {
            case ResultConstants.RESULT_TYPE_BOOST_PLUS:
                return R.color.boost_plus_blue;
            case ResultConstants.RESULT_TYPE_BATTERY:
                return R.color.battery_green;
            case ResultConstants.RESULT_TYPE_JUNK_CLEAN:
                return R.color.clean_primary_blue;
            case ResultConstants.RESULT_TYPE_CPU_COOLER:
                return R.color.cpu_cooler_primary_blue;
            case ResultConstants.RESULT_TYPE_NOTIFICATION_CLEANER:
                return R.color.cpu_cooler_primary_blue;
            default:
                throw new IllegalStateException("Unsupported result type.");
        }
    }

    private void onClickBatteryView() {
        KCAnalytics.logEvent("Battery_OpenFrom", "type", "From Card");
        NavUtils.startActivity(mResultPageActivity, BatteryActivity.class);
        mResultPageActivity.finishSelfAndParentActivity();
    }

    private void onClickBoostPlusView() {
        NavUtils.startActivity(mResultPageActivity, BoostPlusActivity.class);
        KCAnalytics.logEvent("BoostPlus_Open", "Type", "Result Page");
        mResultPageActivity.finishSelfAndParentActivity();
    }

    private void onClickJunkCleanerView() {
        JunkCleanUtils.FlurryLogger.logOpen(JunkCleanConstant.RESULTPAGE);
        NavUtils.startActivity(mResultPageActivity, JunkCleanActivity.class);
        mResultPageActivity.finishSelfAndParentActivity();
    }

    private void onClickCpuCoolerView() {
        Intent cpuCoolerIntent = new Intent(mResultPageActivity, CpuCoolerScanActivity.class);
        cpuCoolerIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        NavUtils.startActivitySafely(mResultPageActivity, cpuCoolerIntent);
        mResultPageActivity.finishSelfAndParentActivity();
        KCAnalytics.logEvent("CPUCooler_Open", "Type", "ResultPage");
    }

//    private void onClickSecurityView() {
//        KCAnalytics.logEvent("Promotion_Clicked", "Type", "SecurityCard");
//        PromotionTracker.startTracking(HSConfig.getString("Application", "Promotions", "SecurityPackage"),
//                PromotionTracker.EVENT_LOG_APP_NAME_SECURITY, true);
//    }
//
//    private void onClickMaxView(String eventLogParam) {
//        KCAnalytics.logEvent("Promotion_Clicked", "Type", eventLogParam);
//        PromotionTracker.startTracking(HSConfig.getString("Application", "Promotions", "MaxPackage"),
//                PromotionTracker.EVENT_LOG_APP_NAME_MAX, true);
//    }

    private void onClickAccessibilityView() {

        ToastUtils.showToast("这里请求Accessbility");
    }

    private class BaseCardViewHolder extends RecyclerView.ViewHolder {
        @Thunk
        View cardItemView;

        BaseCardViewHolder(View itemView) {
            super(itemView);
            cardItemView = ViewUtils.findViewById(itemView, R.id.result_card_item_view);
        }
    }

    private class FeatureCardViewHolder extends BaseCardViewHolder {
        private AppCompatImageView iconIv;
        private View iconContainer;
        private TextView titleTv;
        private TextView contentTv;
        private TextView functionTv;

        FeatureCardViewHolder(View itemView) {
            super(itemView);
            iconIv = ViewUtils.findViewById(itemView, R.id.function_icon_iv);
            iconContainer = ViewUtils.findViewById(itemView, R.id.result_image_container);
            titleTv = ViewUtils.findViewById(itemView, R.id.title_tv);
            contentTv = ViewUtils.findViewById(itemView, R.id.content_tv);
            functionTv = ViewUtils.findViewById(itemView, R.id.function_tv);
        }
    }

    private class DescriptionCardViewHolder extends BaseCardViewHolder {
        private TextView titleTv;
        private TextView contentTv;

        DescriptionCardViewHolder(View itemView) {
            super(itemView);
            titleTv = ViewUtils.findViewById(itemView, R.id.title_tv);
            contentTv = ViewUtils.findViewById(itemView, R.id.content_tv);
        }
    }

    private void doOnce(int token, Runnable action) {
        if (!mUsedTokens.contains(token)) {
            action.run();
            mUsedTokens.add(token);
        }
    }
}
