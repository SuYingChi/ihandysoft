package com.ihs.feature.resultpage;

import android.support.annotation.Nullable;

import com.acb.adadapter.AcbNativeAd;
import com.honeycomb.launcher.resultpage.data.CardData;

import java.util.List;

/**
 * Interfaces holder.
 */
class ResultPageContracts {

    /**
     * View interface for MVP architecture.
     */
    interface View {
        /**
         * Configure and show result page view of given content type.
         */
        void show(ResultController.Type content, @Nullable AcbNativeAd ad, @Nullable List<CardData> cards);
    }

    /**
     * Presenter interface.
     */
    interface Presenter {
        /**
         * Determine content type and show result page. Caller is responsible for passing a ready ads manager in case
         * an ad shall be shown.
         *
         * Implementation calls {@link View#show(ResultController.Type, AcbNativeAd, List)} and pass content type to view.
         */
        void show(AcbNativeAd ad);
    }
}
