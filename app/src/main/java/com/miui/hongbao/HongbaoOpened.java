package com.miui.hongbao;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 已打开但未拾取的红包
 * Created by biaji on 15-12-22.
 */
public class HongbaoOpened extends HongbaoState {

    private HongbaoService context;

    private AccessibilityNodeInfo node;

    public HongbaoOpened(HongbaoService context, AccessibilityNodeInfo node) {
        this.context = context;
        this.node = node;
    }

    @Override
    public void performAction() {
        if (this.node == null) {
            return;
        }

        this.node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }
}
