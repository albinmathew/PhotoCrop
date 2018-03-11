/*******************************************************************************
 * Copyright  2015 Albin Mathew.
 *  <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.albinmathew.photocrop.photoview.gestures;

import android.annotation.TargetApi;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

@TargetApi(8)
public class FroyoGestureDetector extends EclairGestureDetector {

    protected final ScaleGestureDetector mDetector;

    protected final RotationGestureDetector mRotationDetector;

    public FroyoGestureDetector(Context context) {
        super(context);
        ScaleGestureDetector.OnScaleGestureListener mScaleListener =
            new ScaleGestureDetector.OnScaleGestureListener() {

                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    float scaleFactor = detector.getScaleFactor();

                    if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                        return false;
                    }

                    mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector) {
                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    // NO-OP
                }
            };

        RotationGestureDetector.OnRotationGestureListener mRotateListener =
            new RotationGestureDetector.OnRotationGestureListener() {
                @Override
                public boolean onRotate(RotationGestureDetector detector) {
                    mListener.onRotate(detector.getRotationDelta(), detector.getFocusX(), detector.getFocusY());
                    return true;
                }

                @Override
                public boolean onRotationBegin(RotationGestureDetector detector) {
                    return true;
                }

                @Override
                public void onRotationEnd(RotationGestureDetector detector) {

                }
            };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
        mRotationDetector = new RotationGestureDetector(context, mRotateListener);
    }

    @Override
    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    @Override
    public boolean isRotating() {
        return mRotationDetector.isInProgress();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDetector.onTouchEvent(ev);
        mRotationDetector.onTouchEvent(ev);
        return super.onTouchEvent(ev);
    }
}
