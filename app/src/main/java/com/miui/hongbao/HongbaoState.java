package com.miui.hongbao;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by biaji on 15-12-22.
 */
public abstract class HongbaoState {

    public abstract void performAction(AccessibilityNodeInfo node);

    void parseNode(){

    }
}
