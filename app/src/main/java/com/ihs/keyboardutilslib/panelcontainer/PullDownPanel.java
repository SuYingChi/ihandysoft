package com.ihs.keyboardutilslib.panelcontainer;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;

import com.ihs.keyboardutils.panelcontainer.BasePanel;
import com.ihs.keyboardutilslib.R;

/**
 * Created by Arthur on 16/11/14.
 */

public class PullDownPanel extends BasePanel {
    public PullDownPanel() {
        super();
    }

    @Override
    protected View onCreatePanelView() {
        rootView = View.inflate(context, R.layout.panel_pulldown, null);
        rootView.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerListener.showPanel(DemoPanel5.class);
            }
        });

        rootView.findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                containerListener.backToParentPanel(false);
            }
        });
        return rootView;
    }

    @Override
    public Animator getAppearAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(rootView, "rotationY", 0, 360);
        animator.setDuration(2000);
        return animator;
    }

    @Override
    public Animator getDismissAnimator() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(rootView, "rotationX", 0, 360);
        animator.setDuration(2000);
        return animator;
    }
}
