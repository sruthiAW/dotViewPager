package com.example.mydotviewpagerapp;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ssurendran on 5/12/2017.
 */

public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    Context context;
    ArrayList<String> contentList;
    List<ArrayList<String>> choppedList;

    public ViewPagerAdapter(Context context, FragmentManager fm, ArrayList<String> contentList) {
        super(fm);
        this.contentList = contentList;
        this.context = context;

        choppedList = chopped(contentList, context.getResources().getInteger(R.integer.imp_file_count_per_page));
    }

    @Override
    public Fragment getItem(int position) {

        return DummyFragment.newInstance(choppedList.get(position), position, choppedList.size() > 4);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        return fragment;
    }

    @Override
    public int getCount() {
        return choppedList.size() > 4 ? 4 : choppedList.size();
    }

    // chops a list into non-view sublists of length L
    static <T> List<ArrayList<T>> chopped(List<T> list, final int L) {
        List<ArrayList<T>> parts = new ArrayList<ArrayList<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

}
