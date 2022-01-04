package com.z.zdropmenu;

/*
    观察者设计模式
 */
public abstract class ZBaseMenuObserver {
    /*
        触发关闭菜单的回调
     */
    public abstract void notifyCloseMenu();
}