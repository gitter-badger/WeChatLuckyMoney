package com.miui.hongbao;

import android.view.accessibility.AccessibilityNodeInfo;

/**
 * 未打开的红包
 * Created by biaji on 15-12-22.
 */
public class HongbaoUnopened implements HongbaoState {

    private HongbaoService context;

    private AccessibilityNodeInfo node;

    @Override
    public void performAction() {
        if (node == null) {
            return;
        }

        node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
    }
}
