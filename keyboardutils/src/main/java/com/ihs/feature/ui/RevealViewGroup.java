package com.ihs.feature.ui;

import android.view.ViewGroup;

/**
 * Indicator for internal API that {@link ViewGroup} support
 * Circular Reveal animation
 */
public interface RevealViewGroup {

    /**
     * @return Bridge between view and circular reveal animation
     */
    ViewRevealManager getViewRevealManager();

    void setColor(int color);

    void setCenterPoint(float x, float y);

    void updateRadius(float radius);
}