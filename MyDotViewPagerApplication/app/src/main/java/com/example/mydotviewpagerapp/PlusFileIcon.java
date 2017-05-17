package com.example.mydotviewpagerapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ssurendran on 5/12/2017.
 */

public class PlusFileIcon extends LinearLayout {

    public PlusFileIcon(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.imp_file_content_layout, null);
        this.addView(view);

    }
}
