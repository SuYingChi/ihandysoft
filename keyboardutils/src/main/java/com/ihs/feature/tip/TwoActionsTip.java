package com.ihs.feature.tip;

import android.content.Context;
import android.view.View;

import com.ihs.feature.ui.DefaultButtonDialog2;

/**
 * This layer of inheritance is added to ensure cancelled runnables of {@link UnpackFolderTip}, {@link RemoveFolderTip}
 * and {@link BatteryLowTip} are invoked when launcher stops.
 *
 * FIXME: class hierarchy is expanding and losing clarity. Refactor if you can.
 *
 * @see FloatWindowManager#onLauncherStop()
 */
public abstract class TwoActionsTip extends DefaultButtonDialog2 {

    private Runnable mConfirmedRunnable;
    private Runnable mCancelledRunnable;

    private boolean mConfirmed;

    public TwoActionsTip(Context context, Runnable confirmedRunnable, Runnable cancelledRunnable) {
        super(context);
        mConfirmedRunnable = confirmedRunnable;
        mCancelledRunnable = cancelledRunnable;
    }

    @Override
    protected void onClickPositiveButton(View v) {
        mConfirmed = true;
        mConfirmedRunnable.run();
        super.onClickPositiveButton(v);
    }

    @Override
    protected void onCanceled() {
        fireCancelCallback();
    }

    @Override
    protected void onDismissComplete() {
        super.onDismissComplete();
        if (!mConfirmed) {
            fireCancelCallback();
        }
    }

    private void fireCancelCallback() {
        if (mCancelledRunnable != null) {
            Runnable callback = mCancelledRunnable;
            mCancelledRunnable = null;
            callback.run();
        }
    }
}
