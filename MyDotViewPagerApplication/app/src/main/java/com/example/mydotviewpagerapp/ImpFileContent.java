package com.example.mydotviewpagerapp;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Ssurendran on 5/12/2017.
 */

public class ImpFileContent extends LinearLayout {

    String name;

    public ImpFileContent(Context context, String name) {
        super(context);
        this.name = name;
        init(context);
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.imp_file_content_layout, null);
        this.addView(view);

        TextView contentName = (TextView) view.findViewById(R.id.content_name);
        contentName.setText(name);

    }
}
