package com.yuedong.plugin.ui.bubble;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.yuedong.plugin.R;
import com.yuedong.plugin.ui.bubble.bubble.BubblePopupWindow;
import com.yuedong.plugin.ui.utils.DensityUtils;

public class BubbleWindowManager {
    private Context mContext;
    private BubblePopupWindow mPopupWindow = null;
    private View mBubbleView;

    public interface BubbleCallback {
        void onBeautyVersionChanged(int id);

        void onWhiteVersionChanged(int id);

        void onBuddyVersionChanged(int id);

    }

    public BubbleWindowManager(Context context) {
        this.mContext = context;
        mPopupWindow = new BubblePopupWindow(mContext);
        mPopupWindow.setParam(DensityUtils.getScreenWidth((Activity) mContext) - mContext.getResources().getDimensionPixelSize(R.dimen.popwindow_margin) * 2, ViewGroup.LayoutParams.WRAP_CONTENT);
        mBubbleView = LayoutInflater.from(mContext).inflate(R.layout.layout_pop_view, null);
        mPopupWindow.setBubbleView(mBubbleView); //   {zh} 设置气泡内容       {en} Set bubble content  
    }


    public void show(View anchor, BubbleCallback callback) {
        if (null == anchor) {
            return;
        }
        RadioGroup rbBeauty = mBubbleView.findViewById(R.id.rg_beauty);
        rbBeauty.setOnCheckedChangeListener((group, checkedId) -> callback.onBeautyVersionChanged(checkedId));

        RadioGroup rbWhite = mBubbleView.findViewById(R.id.rg_white);
        rbWhite.setOnCheckedChangeListener((group, checkedId) -> callback.onWhiteVersionChanged(checkedId));

        RadioGroup rbBuddy = mBubbleView.findViewById(R.id.rg_buddy);
        rbBuddy.setOnCheckedChangeListener((group, checkedId) -> callback.onBuddyVersionChanged(checkedId));

        int offset = anchor.getLeft() + anchor.getWidth() / 2 - mContext.getResources().getDimensionPixelSize(R.dimen.popwindow_margin);
        mPopupWindow.show(anchor, Gravity.BOTTOM, offset); //   {zh} 显示弹窗       {en} Display popup
    }
}
