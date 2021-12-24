package com.zubov.android.bzb.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class Toaster {
    private Context mContext;
    private String text;
    @StringRes private int textId;
    private Toast mToast;

    public Toast getToast() {
        return mToast;
    }

    public void setToast(Toast toast) {
        mToast = toast;
    }

    public Toaster(Context context, int textId) {
        mContext = context;
        this.textId = textId;

        mToast = Toast.makeText(mContext,textId,Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.BOTTOM, 0, 100);
    }

    public Toaster(Context context, String text) {
        mContext = context;
        this.text = text;

        mToast = Toast.makeText(mContext,text,Toast.LENGTH_LONG);
        mToast.setGravity(Gravity.BOTTOM, 0, 100);
    }

    public void ShowToast(){
        if (mToast != null) mToast.show();
    }
}
