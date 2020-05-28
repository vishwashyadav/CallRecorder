package com.callrecorder.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.callrecorder.R;


public class StickyDialog extends Dialog {
    public StickyDialog(@NonNull Context context, String title, String message) {
        this(context, title, message, null);
    }

    public StickyDialog(@NonNull Context context, String title, String message, Okay okay) {
        this(context, title, message, okay, false);
    }

    public StickyDialog(@NonNull Context context, String title, String message, Okay okay, boolean isCancelable) {
        this(context, title, message, null, okay, isCancelable, null, null);
    }

    public StickyDialog(@NonNull Context context, String title, String message, @Nullable String OkayActionName, Okay okay, boolean isCancelable) {
        this(context, title, message, OkayActionName, okay, isCancelable, null, null);
    }


    public StickyDialog(@NonNull Context context, String title, String message, @Nullable String OkayActionName, final Okay okay, boolean isCancelable, @Nullable String CancelActionName, final Cancel cancel) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_with_action);
        TextView greet = (TextView) findViewById(R.id.greet_message);
        TextView explanation = (TextView) findViewById(R.id.explanation);
        TextView okayButton = (TextView) findViewById(R.id.okay);
        TextView cancelButton = (TextView) findViewById(R.id.cancel);

        if (OkayActionName != null)
            okayButton.setText(OkayActionName);
        if (CancelActionName != null)
            cancelButton.setText(CancelActionName);

        greet.setText(title);
        explanation.setText(message);
        setCancelable(isCancelable);
        okayButton.setOnClickListener(v -> {
            dismiss();
            if (okay != null)
                okay.onOkay();
        });

        cancelButton.setVisibility(isCancelable ? View.VISIBLE : View.GONE);

        cancelButton.setOnClickListener(v -> {
            dismiss();
            if (cancel != null)
                cancel.onCancel();
        });
    }

    public interface Okay {
        void onOkay();
    }

    public interface Cancel {
        void onCancel();
    }
}
