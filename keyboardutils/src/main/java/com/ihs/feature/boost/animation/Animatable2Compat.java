package com.ihs.feature.boost.animation;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Abstract class that drawables supporting animations and callbacks should extend.
 *
 * This interface is added to the framework since API 23, {@link android.graphics.drawable.Animatable2}.
 * Copied here for compatibility.
 */
public interface Animatable2Compat extends Animatable {

    /**
     * Adds a callback to listen to the animation events.
     *
     * @param callback Callback to add.
     */
    void registerAnimationCallback(@NonNull AnimationCallback callback);

    /**
     * Removes the specified animation callback.
     *
     * @param callback Callback to remove.
     * @return {@code false} if callback didn't exist in the call back list, or {@code true} if
     *         callback has been removed successfully.
     */
    boolean unregisterAnimationCallback(@NonNull AnimationCallback callback);

    /**
     * Removes all existing animation callbacks.
     */
    void clearAnimationCallbacks();

    abstract class AnimationCallback {
        /**
         * Called when the animation starts.
         *
         * @param drawable The drawable started the animation.
         */
        public void onAnimationStart(Drawable drawable) {}
        /**
         * Called when the animation ends.
         *
         * @param drawable The drawable finished the animation.
         */
        public void onAnimationEnd(Drawable drawable) {}
    }
}
