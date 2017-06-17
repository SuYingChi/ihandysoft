package com.ihs.feature.junkclean.view;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.TextView;

import com.honeycomb.launcher.R;
import com.honeycomb.launcher.junkclean.JunkCleanActivity;
import com.honeycomb.launcher.junkclean.data.JunkManager;
import com.honeycomb.launcher.util.FormatSizeBuilder;
import com.honeycomb.launcher.util.PermissionUtils;

public class PermissionDialog extends AlertDialog {

    private JunkCleanActivity mJunkCleanActivity;

    public PermissionDialog(JunkCleanActivity activity) {
        super(activity);
        this.mJunkCleanActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clean_permission_dialog_layout);
        setCanceledOnTouchOutside(false);

        TextView alertMessageTv = (TextView) findViewById(R.id.alert_message_tv);
        Context context = getContext();

        FormatSizeBuilder formatSizeBuilder = new FormatSizeBuilder(JunkManager.getInstance().getHiddenSystemJunkSize());
        String appSizeText = formatSizeBuilder.sizeUnit;


        String contentText = context.getString(R.string.clean_need_authorization, appSizeText);
        SpannableString contentSpannableString = new SpannableString(contentText);
        contentSpannableString.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.notification_red)), 0, appSizeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int start = contentText.indexOf(appSizeText);
        if (start != -1) {
            contentSpannableString.setSpan(new StyleSpan(Typeface.BOLD), start, start + appSizeText.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        alertMessageTv.setText(contentSpannableString);

        findViewById(R.id.clean_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.requestAccessibilityPermission(mJunkCleanActivity, new Runnable() {
                    @Override
                    public void run() {
                        mJunkCleanActivity.onAccessibilityPermissionOpenSuccess();
                    }
                });

                dismiss();
            }
        });

        findViewById(R.id.later_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
