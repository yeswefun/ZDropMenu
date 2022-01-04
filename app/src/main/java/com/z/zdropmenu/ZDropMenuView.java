package com.z.zdropmenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

/*
    ZDropMenuView
        ZDropTabLayout
        ZDropLayout
            ZDropMenuLayout
            ZDropMaskView

    观察者设计模式
        选择菜单筛选条件后关闭菜单
        如何在Adapter中调用DropMenuView的closeMenu方法?
            ListView#notifyDataChanged
 */
public class ZDropMenuView extends LinearLayout implements View.OnClickListener {

    private static final String TAG = "ZDropMenuView";

    private final Context mContext;

    /*
        适配器
     */
    private ZBaseMenuAdapter mAdapter;

    /*
        tab的布局
     */
    private LinearLayout mDropTabLayout;

    /*
        存放 菜单的布局 + 阴影
     */
    private FrameLayout mDropLayout;

    /*
        菜单的布局
     */
    private FrameLayout mDropMenuLayout;

    /*
        菜单布局的高度
     */
    private int mDropMenuLayoutHeight;

    /*
        阴影
     */
    private View mDropMaskView;

    /*
        阴影的颜色
     */
    private final int mMaskColor = 0x88888888;

    /*
        当前打开的菜单对应的索引
     */
    private int mCurrentPosition = -1;

    /*
        动画是否正在进行
     */
    private boolean mAnimationExcuting = false;

    /*
        动画持续时间
     */
    private static final int ANIMATION_DURATION = 350;

    public ZDropMenuView(Context context) {
        this(context, null);
    }

    public ZDropMenuView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZDropMenuView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mContext = context;

        initLayout();
    }

    /*
        ZDropMenuView
            ZDropTabLayout
            ZDropLayout
                ZDropMenuLayout
                ZDropMaskView

        1. 初始化布局
            先创建一个xml布局，再加载，再findViewById
            简单的效果用代码去创建
     */
    private void initLayout() {

        // ZDropMenuView(LinearLayout)
        setOrientation(VERTICAL);

        // ZDropTabLayout, 用来存放tab
        mDropTabLayout = new LinearLayout(mContext);
        mDropTabLayout.setOrientation(HORIZONTAL);
        addView(mDropTabLayout, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        // ZDropLayout, 创建FrameLayout用来存放View(阴影)和FrameLayout(菜单内容)
        mDropLayout = new FrameLayout(mContext);
        LayoutParams dropLayoutLP = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                0
        );
        dropLayoutLP.weight = 1;
        addView(mDropLayout, dropLayoutLP);

        // ZDropMaskView, 阴影, 注意阴影与菜单数据的添加顺序，这会影响层级z-index
        mDropMaskView = new View(mContext);
        mDropMaskView.setBackgroundColor(mMaskColor);
        mDropMaskView.setAlpha(0f);
        mDropMaskView.setVisibility(View.GONE);
        mDropMaskView.setOnClickListener(this);
        mDropLayout.addView(mDropMaskView);

        // ZDropMenuLayout, 菜单数据
        mDropMenuLayout = new FrameLayout(mContext);
        mDropMenuLayout.setBackgroundColor(Color.YELLOW);
        mDropLayout.addView(mDropMenuLayout);
    }

    /*
        观察者设计模式
     */
    private class ZDropMenuObserver extends ZBaseMenuObserver {
        @Override
        public void notifyCloseMenu() {
            closeMenu();
        }
    }

    private ZBaseMenuObserver mMenuObserver;

    /*
        adapter设计模式动态添加子View
     */
    public void setAdapter(ZBaseMenuAdapter adapter) {

        if (adapter == null) {
            throw new RuntimeException("adapter cannot be null");
        }

        // 注销观察者
        if (mAdapter != null && mMenuObserver != null) {
            mAdapter.unregisterDropMenuObserver(mMenuObserver);
        }

        mAdapter = adapter;

        // 注册观察者
        mMenuObserver = new ZDropMenuObserver();
        mAdapter.registerDropMenuObserver(mMenuObserver);

        // 动态添加子View
        int count = mAdapter.getCount();
        for (int i = 0; i < count; i++) {
            View tabView = mAdapter.getTabView(i, this);
            mDropTabLayout.addView(tabView);
            setOnTabClick(i, tabView);
            View menuView = mAdapter.getMenuView(i, this);
            menuView.setVisibility(View.GONE);
            mDropMenuLayout.addView(menuView);
        }
    }

    /*
        设置tab的点击事件
     */
    private void setOnTabClick(int position, View tabView) {
        tabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentPosition == -1) { // 处于关闭状态，要打开
                    openMenu(position, tabView);
                } else { // 处于打开状态，要关闭
                    if (position == mCurrentPosition) { // 点击同一个tab
                        closeMenu();
                    } else { // 点击其它tab
                        // 当菜单处于打开状态时，再次点击其它菜单，不要再执行动画，而是直接显示
                        mAdapter.onMenuClose(mDropTabLayout.getChildAt(mCurrentPosition));
                        View oldView = mDropMenuLayout.getChildAt(mCurrentPosition);
                        oldView.setVisibility(View.GONE);
                        mCurrentPosition = position;
                        mAdapter.onMenuOpen(mDropTabLayout.getChildAt(mCurrentPosition));
                        View newView = mDropMenuLayout.getChildAt(mCurrentPosition);
                        newView.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /*
        关闭菜单

        当菜单处理打开状态，多次快速点击Tab，会崩掉呀
            因为mCurrentPosition可能会在某次动画之前就变成-1，此时再去取View，就报错了呀
     */
    private void closeMenu() {

        if (mAnimationExcuting) {
            return;
        }

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mDropMaskView, "alpha", 1f, 0f);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimationExcuting = true;
                // 回调adpater
                mAdapter.onMenuClose(mDropTabLayout.getChildAt(mCurrentPosition));
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationExcuting = false;
                // 隐藏mask
                mDropMaskView.setVisibility(View.GONE);
                // 隐藏menu
                View view = mDropMenuLayout.getChildAt(mCurrentPosition);
                view.setVisibility(View.GONE);
                mCurrentPosition = -1;
            }
        });
        alphaAnimator.start();
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(mDropMenuLayout, "translationY", 0f, -mDropMenuLayoutHeight);
        translationYAnimator.setDuration(ANIMATION_DURATION);
        translationYAnimator.start();
    }

    /*
        打开菜单
     */
    private void openMenu(int position, View tabView) {

        if (mAnimationExcuting) {
            return;
        }

        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mDropMaskView, "alpha", 0f, 1f);
        alphaAnimator.setDuration(ANIMATION_DURATION);
        alphaAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mAnimationExcuting = true;
                // 显示mask
                mDropMaskView.setVisibility(View.VISIBLE);
                // 显示Menu
                View view = mDropMenuLayout.getChildAt(position);
                view.setVisibility(View.VISIBLE);
                // 回调adpater
                mAdapter.onMenuOpen(tabView);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationExcuting = false;
                mCurrentPosition = position;
            }
        });
        alphaAnimator.start();
        ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(mDropMenuLayout, "translationY", -mDropMenuLayoutHeight, 0f);
        translationYAnimator.setDuration(ANIMATION_DURATION);
        translationYAnimator.start();
    }

    /*
        onMeasure会调用多次
            会影响到mDropDataLayoutHeight，切换时就有坑了呀
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        Log.e(TAG, "onMeasure");
        /*
            menuLayout的高度是75%，而dropMask的高度是100%
            dropLayout(menuLayout + dropMask)
         */
//        int height = getMeasuredHeight();
//        mDropMenuLayoutHeight = (int) (height * 0.75f);
//        ViewGroup.LayoutParams dropMenuLayoutLP = mDropMenuLayout.getLayoutParams();
//        dropMenuLayoutLP.height = mDropMenuLayoutHeight;
    }

    /*
        布局
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (changed && mDropMenuLayoutHeight == 0) {
            Log.e(TAG, "onLayout");
            /*
                menuLayout的高度是75%，而dropMask的高度是100%
                dropLayout(menuLayout + dropMask)
             */
            mDropMenuLayoutHeight = (int) (getMeasuredHeight() * 0.75f);
            ViewGroup.LayoutParams dropMenuLayoutLP = mDropMenuLayout.getLayoutParams();
            dropMenuLayoutLP.height = mDropMenuLayoutHeight;
            mDropMenuLayout.setLayoutParams(dropMenuLayoutLP);
            mDropMenuLayout.setTranslationY(-mDropMenuLayoutHeight);
        }
    }

    /*
        点击阴影关闭菜单
     */
    @Override
    public void onClick(View v) {
        closeMenu();
    }
}
