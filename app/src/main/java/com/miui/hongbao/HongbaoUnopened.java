package com.miui.hongbao;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 未打开的红包
 * Created by biaji on 15-12-22.
 */
public class HongbaoUnopened extends HongbaoState {

    private HongbaoService context;

    private AccessibilityNodeInfo node;

    public HongbaoUnopened(HongbaoService context, AccessibilityNodeInfo node) {
        this.context = context;
        this.node = node;
    }

    @Override
    public void performAction() {
        if (this.node == null) {
            return;
        }

        this.node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }
}
