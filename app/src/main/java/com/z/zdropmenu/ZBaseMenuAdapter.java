package com.z.zdropmenu;

import android.view.View;
import android.view.ViewGroup;

public abstract class ZBaseMenuAdapter {
    /*
        获取条目个数
     */
    protected abstract int getCount();

    /*
        获取TabView
     */
    protected abstract View getTabView(int position, ViewGroup parent);

    /*
        获取MenuView
     */
    protected abstract View getMenuView(int position, ViewGroup parent);

    /*
        当菜单打开时的回调
     */
    protected abstract void onMenuOpen(View tabView);

    /*
        当菜单关闭时的回调
     */
    protected abstract void onMenuClose(View tabView);
}
