package com.example.mydotviewpagerapp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ssurendran on 5/12/2017.
 */

public class DummyFragment extends Fragment {

    ArrayList<String> list = new ArrayList<>();

    public static DummyFragment newInstance(ArrayList<String> stringList) {

        Bundle args = new Bundle();
        args.putStringArrayList("content_list", stringList);
        DummyFragment fragment = new DummyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_layout, container, false);

        LinearLayout parent_layout = (LinearLayout) view.findViewById(R.id.parent_layout);

        if(getArguments() != null){
            list = getArguments().getStringArrayList("content_list");
        }

        for(String content : list){
            parent_layout.addView(new ImpFileContent(getContext(), content));
        }

        return view;
    }
}
