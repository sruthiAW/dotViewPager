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
    int pagePosition;
    boolean hasMoreFiles;

    public static DummyFragment newInstance(ArrayList<String> stringList, int pagePosition, boolean hasMoreFiles) {

        Bundle args = new Bundle();
        args.putStringArrayList("content_list", stringList);
        args.putInt("page_position", pagePosition);
        args.putBoolean("has_more_files", hasMoreFiles);
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
            pagePosition = getArguments().getInt("page_position");
            hasMoreFiles = getArguments().getBoolean("has_more_files");
        }

        //Do this only for the 4th page. Pass position in ctor
        if(pagePosition == 3 && hasMoreFiles){
            for(int i = 0; i < list.size()-1; i++){
                parent_layout.addView(new ImpFileContent(getContext(), list.get(i)));
            }
            parent_layout.addView(new PlusFileIcon(getContext()));

        } else {

            for (String content : list) {
                parent_layout.addView(new ImpFileContent(getContext(), content));
            }
        }

        return view;
    }
}
