/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yuedong.plugin.beauty.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;


/**
 * Layout that adjusts to maintain a specific aspect ratio.
 */
public class AspectFrameLayout extends FrameLayout {
    private static final String TAG = "-AFL";

    private double mTargetAspect = -1.0;        // initially use default window size

    public AspectFrameLayout(Context context) {
        super(context);
        setClipChildren(false);
    }

    public AspectFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClipChildren(false);
    }

    /**
     * Sets the desired aspect ratio.  The value is <code>width / height</code>.
     */
    public void setAspectRatio(double aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException();
        }
        Log.d(TAG, "Setting aspect ratio to " + aspectRatio + " (was " + mTargetAspect + ")");
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mTargetAspect == -1.0) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View view = getChildAt(i);

            final int parenW = getMeasuredWidth();
            final int parenH = getMeasuredHeight();

            int childW = view.getMeasuredWidth();
            int childH = view.getMeasuredHeight();
            float scale = 1f;

            if (childW * 1.0f / childH < mTargetAspect) {
                childW = getMeasuredWidth();
                childH = (int) (childW / mTargetAspect);
                scale = parenH * 1.0f / childH;
            } else if (childW * 1.0f / childH > mTargetAspect) {
                childH = getMeasuredHeight();
                childW = (int) (childH * mTargetAspect);
                scale = parenW * 1.0f / childW;
            }

            childW *= scale;
            childH *= scale;

            int l = (parenW - childW) / 2;
            int r = l + childW;
            int b = (parenH - childH) / 2;
            int t = b + childH;
            view.layout(l, b, r, t);
        }
    }


}
