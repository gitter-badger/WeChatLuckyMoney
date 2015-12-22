package com.miui.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.os.Build;

/**
 * 已经完成的红包
 * Created by biaji on 15-12-22.
 */
public class HongbaoPicked implements HongbaoState {

    private HongbaoService context;

    public HongbaoPicked(HongbaoService context) {
        this.context = context;
    }


    @Override
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void performAction() {

        if (context == null) {
            return;
        }

        context.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);

    }
}
