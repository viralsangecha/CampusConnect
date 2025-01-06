package com.example.campusconnect;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

public class LoadingDialog {

    private Dialog dialog;

    public LoadingDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(LayoutInflater.from(context).inflate(R.layout.loading_dialog, null));
        dialog.setCancelable(false); // Prevent dismissal by tapping outside
    }

    public void show() {
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
