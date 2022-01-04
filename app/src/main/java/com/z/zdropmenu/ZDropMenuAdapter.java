package com.z.zdropmenu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/*

 */
public class ZDropMenuAdapter extends ZBaseMenuAdapter {

    private final String[] mItems = {"类型", "品牌", "价格", "更多"};
    private final Context mContext;

    public ZDropMenuAdapter(Context context) {
        mContext = context;
    }

    @Override
    protected int getCount() {
        return mItems.length;
    }

    @Override
    protected View getTabView(int position, ViewGroup parent) {
        TextView tabView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.view_drop_tab, parent, false);
        tabView.setText(mItems[position]);
        return tabView;
    }

    @Override
    protected View getMenuView(int position, ViewGroup parent) {
        TextView menuView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.view_drop_menu, parent, false);
        menuView.setText(mItems[position]);
        return menuView;
    }

    @Override
    protected void onMenuOpen(View tabView) {
        TextView tv = (TextView) tabView;
        tv.setTextColor(Color.YELLOW);
    }

    @Override
    protected void onMenuClose(View tabView) {
        TextView tv = (TextView) tabView;
        tv.setTextColor(Color.BLACK);
    }
}
