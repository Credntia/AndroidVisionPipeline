/*
 * Copyright (C) The Android Open Source Project
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
package online.devliving.mobilevisionpipeline.camera;

import android.Manifest;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import androidx.annotation.RequiresPermission;

import com.google.android.gms.common.images.Size;

import online.devliving.mobilevisionpipeline.GraphicOverlay;
import online.devliving.mobilevisionpipeline.Util;

@SuppressWarnings("unused")
public class CameraSourcePreview extends ViewGroup {
    public enum PreviewScaleType {
        FIT_CENTER,
        FILL
    }

    private static final String TAG = "CameraSourcePreview";

    private SurfaceView mSurfaceView;
    private boolean mStartRequested;
    private boolean mSurfaceAvailable;
    private CameraSource mCameraSource;

    private GraphicOverlay mOverlay;
    private PreviewScaleType mPreviewScaleType = PreviewScaleType.FILL;

    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        mStartRequested = false;
        mSurfaceAvailable = false;

        mSurfaceView = new SurfaceView(context);
        mSurfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(mSurfaceView);
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource) throws Exception {
        if (cameraSource == null) {
            stop();
        }

        mCameraSource = cameraSource;

        if (mCameraSource != null) {
            mStartRequested = true;
            requestLayout();
            startIfReady();
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public void start(CameraSource cameraSource, GraphicOverlay overlay) throws Exception {
        mOverlay = overlay;
        start(cameraSource);
    }

    public void stop() {
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    public void release() {
        if (mCameraSource != null) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private void startIfReady() throws Exception {
        if (mStartRequested && mSurfaceAvailable) {
            mCameraSource.start(mSurfaceView.getHolder());
            updateOverlay();
            mStartRequested = false;
        }
    }

    void updateOverlay() {
        Size size = mCameraSource.getPreviewSize();

        if (mOverlay != null && size != null) {
            int min = Math.min(size.getWidth(), size.getHeight());
            int max = Math.max(size.getWidth(), size.getHeight());
            if (isPortraitMode()) {
                // Swap width and height sizes when in portrait, since it will be rotated by
                // 90 degrees
                mOverlay.setCameraInfo(min, max, mCameraSource.getCameraFacing());
            } else {
                mOverlay.setCameraInfo(max, min, mCameraSource.getCameraFacing());
            }
            mOverlay.clear();
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            mSurfaceAvailable = true;
            Log.d(TAG, "Surface created");
            try {
                startIfReady();
            } catch (SecurityException se) {
                Log.e(TAG, "You do not have permission to start the camera", se);
            } catch (Exception e) {
                Log.e(TAG, "Could not start camera source.", e);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            mSurfaceAvailable = false;
            Log.d(TAG, "Surface destroyed");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "Configuration changed");
        if (mCameraSource != null) {
            mCameraSource.updateRotation();
            updateOverlay();
        }
    }

    public void setScaleType(PreviewScaleType previewScaleType) {
        this.mPreviewScaleType = previewScaleType;
        postInvalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        switch (mPreviewScaleType) {
            case FILL:
                updateChildSizeForFill(layoutWidth, layoutHeight);
                break;

            case FIT_CENTER:
                updateChildSizeForCenterFit(layoutWidth, layoutHeight);
                break;
        }

        try {
            startIfReady();
        } catch (SecurityException se) {
            Log.e(TAG, "You do not have permission to start the camera", se);
        } catch (Exception e) {
            Log.e(TAG, "Could not start camera source.", e);
        }
    }

    /*
    void updateChildSizeForFill(int layoutWidth, int layoutHeight){
        int width = layoutWidth;
        int height = layoutHeight;

        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }

            Log.d(TAG, "camera source not null");
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            //noinspection SuspiciousNameCombination
            width = height;
            height = tmp;
        }

        final float aspectRatio = (float) width / (float) height;

        Log.d(TAG, "aspect ratio: " + aspectRatio);

        int childWidth;
        int childHeight;

        if (layoutHeight > layoutWidth) {
            //fit height
            childHeight = layoutHeight;
            childWidth = Math.round(childHeight * aspectRatio);
            Log.d(TAG, "fit height -> cw: " + childWidth + ", ch: " + childHeight);

            if (childWidth < layoutWidth) {
                int diff = layoutWidth - childWidth;
                childWidth = childWidth + diff;
                childHeight = childHeight + Math.round(diff / aspectRatio);

                Log.d(TAG, "fit height [nested block] -> cw: " + childWidth + ", ch: " + childHeight);
            }
        } else {
            //fit width
            childWidth = layoutWidth;
            childHeight = Math.round(childWidth / aspectRatio);
            Log.d(TAG, "fit width -> cw: " + childWidth + ", ch: " + childHeight);

            if (childHeight < layoutHeight) {
                int diff = layoutHeight - childHeight;
                childHeight = childHeight + diff;
                childWidth = childWidth + Math.round(diff * aspectRatio);

                Log.d(TAG, "fit width [nested block] -> cw: " + childWidth + ", ch: " + childHeight);
            }
        }

        Log.d(TAG, "layout size: w: " + layoutWidth + ", h: " + layoutHeight
                + " - fit size: w: " + childWidth + ", h: " + childHeight);

        for (int i = 0; i < getChildCount(); ++i) {
            getChildAt(i).layout(0, 0, childWidth, childHeight);
        }
    }
    */

    void updateChildSizeForCenterFit(int layoutWidth, int layoutHeight) {
        int previewWidth = 320;
        int previewHeight = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = previewWidth;
            //noinspection SuspiciousNameCombination
            previewWidth = previewHeight;
            previewHeight = tmp;
        }

        int childWidth;
        int childHeight;
        int childXOffset;
        int childYOffset;
        float widthRatio = (float) layoutWidth / (float) previewWidth;
        float heightRatio = (float) layoutHeight / (float) previewHeight;

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = layoutWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            //childYOffset = (childHeight - viewHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = layoutHeight;
            //childXOffset = (childWidth - viewWidth) / 2;
        }

        if (childWidth > layoutWidth) {
            while (childWidth > layoutWidth) {
                childWidth--;
                widthRatio = (float) childWidth / (float) previewWidth;
                childHeight = (int) ((float) previewHeight * widthRatio);
            }
        }

        if (childHeight > layoutHeight) {
            while (childHeight > layoutHeight) {
                childHeight--;
                heightRatio = (float) childHeight / (float) previewHeight;
                childWidth = (int) ((float) previewWidth * heightRatio);
            }
        }

        childYOffset = (childHeight - layoutHeight) / 2;
        childXOffset = (childWidth - layoutWidth) / 2;

        Log.d("PREVIEW", "layout w:" + layoutWidth + ", h:" + layoutHeight + "; child w:" + childWidth
                + ", h:" + childHeight + ", x:" + childXOffset + ", y:" + childYOffset);
        for (int i = 0; i < getChildCount(); ++i) {
            // One dimension will be cropped.  We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }
    }

    void updateChildSizeForFill(int layoutWidth, int layoutHeight) {
        int previewWidth = 320;
        int previewHeight = 240;
        if (mCameraSource != null) {
            Size size = mCameraSource.getPreviewSize();
            if (size != null) {
                previewWidth = size.getWidth();
                previewHeight = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = previewWidth;
            //noinspection SuspiciousNameCombination
            previewWidth = previewHeight;
            previewHeight = tmp;
        }

        int childWidth;
        int childHeight;
        int childXOffset = 0;
        int childYOffset = 0;
        float widthRatio = (float) layoutWidth / (float) previewWidth;
        float heightRatio = (float) layoutHeight / (float) previewHeight;

        // To fill the view with the camera preview, while also preserving the correct aspect ratio,
        // it is usually necessary to slightly oversize the child and to crop off portions along one
        // of the dimensions.  We scale up based on the dimension requiring the most correction, and
        // compute a crop offset for the other dimension.
        if (widthRatio > heightRatio) {
            childWidth = layoutWidth;
            childHeight = (int) ((float) previewHeight * widthRatio);
            childYOffset = (childHeight - layoutHeight) / 2;
        } else {
            childWidth = (int) ((float) previewWidth * heightRatio);
            childHeight = layoutHeight;
            childXOffset = (childWidth - layoutWidth) / 2;
        }
        Log.d("PREVIEW", "layout w:" + layoutWidth + ", h:" + layoutHeight + "; child w:" + childWidth
                + ", h:" + childHeight + ", x:" + childXOffset + ", y:" + childYOffset);

        for (int i = 0; i < getChildCount(); ++i) {
            // One dimension will be cropped.  We shift child over or up by this offset and adjust
            // the size to maintain the proper aspect ratio.
            getChildAt(i).layout(
                    -1 * childXOffset, -1 * childYOffset,
                    childWidth - childXOffset, childHeight - childYOffset);
        }
    }

    public boolean isPortraitMode() {
        return Util.isPortraitMode(getContext());
    }
}
