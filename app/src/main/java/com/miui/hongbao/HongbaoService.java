package com.miui.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayList;
import java.util.List;


public class HongbaoService extends AccessibilityService {

    private final static String TAG = "GOTCHA";
    /**
     * 解析的红包列表
     */
    private List<AccessibilityNodeInfo> mReceiveNodes = new ArrayList<>();
    /**
     * 未打开的红包列表
     */
    private List<AccessibilityNodeInfo> mUnpackNode = new ArrayList<>();
    /**
     * 已抢过的红包
     */
    private List<AccessibilityNodeInfo> finishedNode = new ArrayList<>();

    private boolean mLuckyMoneyPicked, mLuckyMoneyReceived, mNeedUnpack, mNeedBack;

    private AccessibilityNodeInfo rootNodeInfo;

    private final static String WECHAT_DETAILS_EN = "Details";
    private final static String WECHAT_DETAILS_CH = "红包详情";
    private final static String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
    private final static String WECHAT_BETTER_LUCK_CH = "手慢了";
    private final static String WECHAT_OPEN_EN = "Open";
    private final static String WECHAT_OPENED_EN = "opened";
    private final static String WECHAT_OPEN_CH = "拆红包";
    private final static String WECHAT_VIEW_SELF_CH = "查看红包";
    private final static String WECHAT_VIEW_OTHERS_CH = "领取红包";
    private final static String NOTIFICATION_TIP = "[微信红包]";
    /**
     * 用来鉴别对象是否为微信红包的字符串
     */
    private final static String VERIFY_TEXT = "微信红包";


    /**
     * AccessibilityEvent的回调方法
     *
     * @param event 事件
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.d(TAG, event.getText().toString());
            String tip = event.getText().toString();
            if (!tip.contains(NOTIFICATION_TIP)) {
                return;
            }
            Parcelable parcelable = event.getParcelableData();
            if (parcelable instanceof Notification) {
                Notification notification = (Notification) parcelable;
                Log.d(TAG, "Notification: " + notification.contentIntent.toString());
                try {
                    ((Notification) parcelable).contentIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    Log.e(TAG, "", e);
                }
            }
            return;
        }


        rootNodeInfo = event.getSource();

        if (rootNodeInfo == null) return;

        checkNodeInfo();


        if (mNeedBack) {
            performGlobalAction(GLOBAL_ACTION_BACK);
            mNeedBack = false;
        }

        /* 如果已经接收到红包并且还没有戳开 */
        if (mLuckyMoneyReceived && !mLuckyMoneyPicked && (mReceiveNodes.size() > 0)) {
            int size = mReceiveNodes.size();
            if (size > 0) {
                AccessibilityNodeInfo cellNode = mReceiveNodes.get(size - 1);


                if (finishedNode.contains(cellNode)) {
                    return;
                }

                if (cellNode != null && cellNode.getParent() != null) {
                    cellNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }

                mReceiveNodes.remove(cellNode);
                mUnpackNode.add(cellNode);

                mLuckyMoneyReceived = false;
                mLuckyMoneyPicked = true;
            }
        }
        /* 如果戳开但还未领取 */
        if (mNeedUnpack && (mUnpackNode != null)) {
            int size = mUnpackNode.size();
            if (size > 0) {
                AccessibilityNodeInfo cellNode = mUnpackNode.get(size - 1);
                cellNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                finishedNode.add(cellNode);
                mNeedUnpack = false;
            }
        }

    }

    @Override
    public void onInterrupt() {

    }

    /**
     * 检查节点信息
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void checkNodeInfo() {
        if (this.rootNodeInfo == null) return;

        /* 聊天会话窗口，遍历节点匹配“领取红包”和"查看红包" */
        List<AccessibilityNodeInfo> nodesWaiting = findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                this.WECHAT_VIEW_SELF_CH, this.WECHAT_VIEW_OTHERS_CH});

        if (!nodesWaiting.isEmpty()) {

            Log.v(TAG, "Got " + nodesWaiting.size() + " nodes");

            for (AccessibilityNodeInfo info : nodesWaiting) {

                if (!isHongbaoObj(info)) {
                    Log.d(TAG, "Not Real Node, bypass");
                    continue;
                }

                if (finishedNode.contains(info)) {
                    Log.d(TAG, "Already opened, bypass");
                    continue;
                }

                if (mReceiveNodes.contains(info)) {
                    Log.d(TAG, "Already Added, bypass");
                    continue;
                }

                mLuckyMoneyReceived = true;
                Log.v(TAG, "Add " + info.getText() + " to mReceiveNodes");
                mReceiveNodes.add(info);
            }

            return;
        }

        /* 戳开红包，红包还没抢完，遍历节点匹配“拆红包” */
        List<AccessibilityNodeInfo> unpackedNode = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                this.WECHAT_OPEN_CH, this.WECHAT_OPEN_EN});
        if (!unpackedNode.isEmpty()) {
            mUnpackNode.addAll(unpackedNode);
            mNeedUnpack = true;
            return;
        }

        /* 戳开红包，红包已被抢完，遍历节点匹配“红包详情”和“手慢了” */
        if (mLuckyMoneyPicked) {
            List<AccessibilityNodeInfo> nodes3 = this.findAccessibilityNodeInfosByTexts(this.rootNodeInfo, new String[]{
                    this.WECHAT_BETTER_LUCK_CH, this.WECHAT_DETAILS_CH,
                    this.WECHAT_BETTER_LUCK_EN, this.WECHAT_DETAILS_EN});
            if (!nodes3.isEmpty()) {
                mNeedBack = true;
                mLuckyMoneyPicked = false;
                AccessibilityNodeInfo node = mUnpackNode.get(mUnpackNode.size() - 1); //这种情况应该只有一个匹配
                mUnpackNode.remove(node);
                finishedNode.add(node);
            }
        }
    }

    /**
     * 将节点对象的id和红包上的内容合并
     * 用于表示一个唯一的红包
     *
     * @param node 任意对象
     * @return 红包标识字符串(Hash值+红包文本)
     */
    private String getHongbaoText(AccessibilityNodeInfo node) {
        /* 获取红包上的文本 */
        String content = "";
        try {
            String time = node.getParent().getParent().getParent().getChild(0).getChild(0).getText().toString();
            AccessibilityNodeInfo i = node.getParent().getChild(0);
            content += i.getText().toString();
        } catch (NullPointerException npe) {
            return null;
        }

        return content;
    }

    /**
     * 判断是否为红包Node：其父ViewGroup包含三个元素,且第三个元素为TextView：微信红包
     *
     * @param node
     * @return
     */
    private boolean isHongbaoObj(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node.getParent();
        boolean result = false;
        try {
            result = parent.getChildCount() == 3 &&
                    parent.getChild(2).getClassName().toString().contains("TextView") &&
                    parent.getChild(2).getText().toString().equals(VERIFY_TEXT);
        } catch (NullPointerException e) {
            Log.e(TAG, "", e);
        }

        return result;
    }


    /**
     * 批量化执行AccessibilityNodeInfo.findAccessibilityNodeInfosByText(text).
     * 由于这个操作影响性能,将所有需要匹配的文字一起处理,尽早返回
     *
     * @param nodeInfo 窗口根节点
     * @param texts    需要匹配的字符串们
     * @return 匹配到的节点数组
     */
    private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(AccessibilityNodeInfo nodeInfo, String[] texts) {

        List<AccessibilityNodeInfo> result = new ArrayList<>();

        for (String text : texts) {
            List<AccessibilityNodeInfo> nodes = nodeInfo.findAccessibilityNodeInfosByText(text);

            if (!nodes.isEmpty()) {
                result.addAll(nodes);
            }
        }
        return result;
    }

}
