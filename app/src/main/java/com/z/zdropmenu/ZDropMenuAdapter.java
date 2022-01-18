package com.z.zdropmenu;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

    /*
        不同的位置显示的布局不一样
            一开始就创建一个集合存放好，通过position来获取
            或switch进行判断
     */
    @Override
    protected View getMenuView(int position, ViewGroup parent) {
        TextView menuView = (TextView) LayoutInflater.from(mContext).inflate(R.layout.view_drop_menu, parent, false);
        menuView.setText(mItems[position]);
        /*
            此时只是为了作测试，触发的应该是菜单数据项的点击事件
                点击事件触发菜单的关闭
            TODO: 事件拦截
                点击menuData，如果不处理点击事件则会传到mask
         */
        menuView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "closeMenu", Toast.LENGTH_SHORT).show();
                notifyCloseMenu();
            }
        });
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
