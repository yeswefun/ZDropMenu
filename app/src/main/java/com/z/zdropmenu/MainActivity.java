package com.z.zdropmenu;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private ZDropMenuView mDropMenuView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mDropMenuView = findViewById(R.id.z_drop_menu);
        mDropMenuView.setAdapter(new ZDropMenuAdapter(this));
    }

    private int cnt = 0;

    /*
        如果不隐藏阴影，阴影的点击事件还会有
            mDropMaskVies.setVisibility(View.GONE);

        case:
            mCurrentPositon == 有效值

            点击阴影，
                此时mCurrentPosition == 有效值，拿有效值去获取View，最后变为mCurrentPosition == -1

            再点击阴影(如果只是设置透明度的话，则阴影的点击事件还在呀)
                此时mCurrentPosition == -1，拿-1去获取View，报错了呀
     */
    public void handleClick(View view) {
        Toast.makeText(this, "click: " + ++cnt, Toast.LENGTH_SHORT).show();
    }
}