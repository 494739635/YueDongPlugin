package com.yuedong.plugin.beauty.camera.display;

import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraProxy {
    private static final String TAG = "CameraProxy";
    private int mCameraId;
    private Camera mCamera;
    private boolean isCameraOpen = false;
    private boolean mCameraOpenFailed = false;
    private int maxExposureCompensation, minExposureCompensation;
    private int previewWidth, previewHeight;
    private CameraInfo mCameraInfo = new CameraInfo();

    public CameraProxy() {
    }

    public Camera getCamera() {
        return mCamera;
    }

    public boolean openCamera(int cameraId) {
        try {
            releaseCamera();
            mCamera = Camera.open(cameraId);
            mCamera.getParameters();
            mCameraId = cameraId;
            mCamera.getCameraInfo(cameraId, mCameraInfo);
            setDefaultParameters();

            isCameraOpen = true;
            mCameraOpenFailed = false;
        } catch (Exception e) {
            mCameraOpenFailed = true;
            mCamera = null;
            Log.i(TAG, "openCamera fail msg=" + e.getMessage());
            return false;
        }
        return true;
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void startPreview(SurfaceTexture surfaceTexture, PreviewCallback previewcallback) {
        try {
            if (mCamera == null) {
                return;
            }
            if (surfaceTexture != null && mCamera != null)
                mCamera.setPreviewTexture(surfaceTexture);

            if (previewcallback != null && mCamera != null) {
                mCamera.setPreviewCallbackWithBuffer(previewcallback);
                mCamera.addCallbackBuffer(new byte[previewWidth * previewHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                mCamera.addCallbackBuffer(new byte[previewWidth * previewHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
                mCamera.addCallbackBuffer(new byte[previewWidth * previewHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8]);
            }
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPreview() {
        if (mCamera != null)
            mCamera.stopPreview();
    }

    public Size getPreviewSize() {
        if (mCamera != null) {
            return mCamera.getParameters().getPreviewSize();
        }
        return null;
    }

    public void setOneShotPreviewCallback(PreviewCallback callback) {
        mCamera.setOneShotPreviewCallback(callback);
    }

    public void addPreviewCallbackBuffer(byte[] callbackBuffer) {
        mCamera.addCallbackBuffer(callbackBuffer);
    }

    public int getOrientation() {
        if (mCameraInfo == null) {
            return 0;
        }
        return mCameraInfo.orientation;
    }

    public boolean isFlipHorizontal() {
        if (mCameraInfo == null) {
            return false;
        }
        return mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true : false;
    }

    public boolean isFlipVertical() {
        if (mCameraInfo == null) {
            return false;
        }
        return (mCameraInfo.orientation == 90 || mCameraInfo.orientation == 270) ? true : false;
    }

    public int getCameraId() {
        return mCameraId;
    }

    public boolean isFrontCamera() {
        return mCameraId == CameraInfo.CAMERA_FACING_FRONT;
    }

    public void setRotation(int rotation) {
        if (mCamera != null) {
            Parameters params = mCamera.getParameters();
            params.setRotation(rotation);
            mCamera.setParameters(params);
        }
    }

    public void takePicture(Camera.ShutterCallback shutterCallback, Camera.PictureCallback rawCallback,
                            Camera.PictureCallback jpegCallback) {
        if (mCamera != null) {
            mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
        }
    }

    public int getDisplayOrientation(int dir) {
        /**
         * 请注意前置摄像头与后置摄像头旋转定义不同
         * 请注意不同手机摄像头旋转定义不同
         */
        int newdir = dir;
        if (isFrontCamera() &&
                ((mCameraInfo.orientation == 270 && (dir & 1) == 1) ||
                        (mCameraInfo.orientation == 90 && (dir & 1) == 0))) {
            newdir = (dir ^ 2);
        }
        return newdir;
    }

    public boolean needMirror() {
        if (isFrontCamera()) {
            return true;
        } else {
            return false;
        }
    }

    private void setDefaultParameters() {
        Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(
                Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(Parameters.FLASH_MODE_OFF)) {
            parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        }

        Point previewSize = getSuitablePreviewSize();
        parameters.setPreviewSize(previewSize.x, previewSize.y);
//		parameters.setPreviewSize(640, 480);
        Point pictureSize = getSuitablePictureSize();
        parameters.setPictureSize(pictureSize.x, pictureSize.y);
//		mCamera.setFaceDetectionListener(new MyFaceDetectionListener());
        mCamera.setParameters(parameters);
        maxExposureCompensation = parameters.getMaxExposureCompensation() / 2;
        minExposureCompensation = parameters.getMinExposureCompensation() / 2;
    }

    public Parameters getParameters() {
        return mCamera.getParameters();
    }

    public void setPreviewSize(int width, int height) {
        if (mCamera == null)
            return;
        previewWidth = width;
        previewHeight = height;
        Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(width, height);
        if (mCamera == null)
            return;
        mCamera.setParameters(parameters);

    }

    private Point getSuitablePreviewSize() {
        Point defaultsize = new Point(1280, 720);
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for (Size s : sizes) {
                if ((s.width == defaultsize.x) && (s.height == defaultsize.y)) {
                    return defaultsize;
                }
            }
            return new Point(640, 480);
        }
        return null;
    }

    private Point getSuitablePreviewSize(int x, int y) {
        Point defaultsize = new Point(x, y);
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for (Size s : sizes) {
                if ((s.width == defaultsize.x) && (s.height == defaultsize.y)) {
                    return defaultsize;
                }
            }
            return new Point(1920, 1080);
        }
        return null;
    }

    public ArrayList<String> getSupportedPreviewSize(String[] previewSizes) {
        ArrayList<String> result = new ArrayList<String>();
        if (mCamera != null) {
            List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for (String candidate : previewSizes) {
                int index = candidate.indexOf('x');
                if (index == -1) continue;
                int width = Integer.parseInt(candidate.substring(0, index));
                int height = Integer.parseInt(candidate.substring(index + 1));
                for (Size s : sizes) {
                    if ((s.width == width) && (s.height == height)) {
                        result.add(candidate);
                    }
                }
            }
        }
        return result;
    }

    private Point getSuitablePictureSize() {
        Point defaultsize = new Point(4608, 3456);
        //	Point defaultsize = new Point(3264, 2448);
        if (mCamera != null) {
            Point maxSize = new Point(0, 0);
            List<Size> sizes = mCamera.getParameters().getSupportedPictureSizes();
            for (Size s : sizes) {
                if ((s.width == defaultsize.x) && (s.height == defaultsize.y)) {
                    return defaultsize;
                }
                if (maxSize.x < s.width) {
                    maxSize.x = s.width;
                    maxSize.y = s.height;
                }
            }
            return maxSize;
        }
        return null;
    }

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public boolean cameraOpenFailed() {
        return mCameraOpenFailed;
    }

    public boolean isCameraOpen() {
        return isCameraOpen;
    }

    public void setMeteringArea(Rect rect) {
        List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
        meteringAreas.add(new Camera.Area(rect, 1));

        if (mCamera != null) {
            try {
                Parameters parameters = mCamera.getParameters();
                parameters.setMeteringAreas(meteringAreas);
                mCamera.setParameters(parameters);
            } catch (Exception e) {
//				Log.e(TAG, "onFaceDetection exception: " + e.getMessage());
            }
        }

    }

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    public void handleZoom(boolean isZoomIn) {
        Parameters params = mCamera.getParameters();
        if (params.isZoomSupported()) {
            int maxZoom = params.getMaxZoom();
            int zoom = params.getZoom();
            if (isZoomIn && zoom < maxZoom) {
                zoom++;
            } else if (zoom > 0) {
                zoom--;
            }
            params.setZoom(zoom);
            mCamera.setParameters(params);
        } else {
            Log.i(TAG, "zoom not supported");
        }
    }

    public void setExposureCompensation(int progerss) {
        Parameters params = mCamera.getParameters();
        int value = 0;
        if (progerss >= 50) {
            int tmp = progerss - 50;
            tmp = tmp * maxExposureCompensation;
            value = tmp / 50;
        } else {
            int tmp = 50 - progerss;
            tmp = minExposureCompensation * tmp;
            value = tmp / 50;
        }
        params.setExposureCompensation(value);
        mCamera.setParameters(params);
    }

    public Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, List<Size> preSizeList) {
        int ReqTmpWidth;
        int ReqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
//		if (false) {
//			ReqTmpWidth = surfaceHeight;
//			ReqTmpHeight = surfaceWidth;
//		} else {
//			ReqTmpWidth = surfaceWidth;
//			ReqTmpHeight = surfaceHeight;
//		}
        ReqTmpWidth = surfaceHeight;
        ReqTmpHeight = surfaceWidth;
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Size size : preSizeList) {
            if ((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)) {
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Size retSize = null;
        for (Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;

    }

    /**
     * 计算最完美的Size
     *
     * @param sizes
     * @param surfaceWidth
     * @param surfaceHeight
     * @return
     */
    public void calculatePerfectSize(int surfaceWidth, int surfaceHeight, List<Size> sizes) {
        int ReqTmpWidth;
        int mPreviewHeight;
//		ReqTmpWidth = surfaceHeight;
//		mPreviewHeight  = surfaceWidth;
//		Camera.Parameters parameters =mCamera.getParameters();
//		//获取当前相机支持的 尺寸
//		List<Camera.Size> vSizes = parameters.getSupportedPreviewSizes();
////获取和屏幕比列相近的 一个尺寸
//		Camera.Size previewSize = getSuitableSize(vSizes);
////重新定义 宽和高 （宽始终小于高）
//		int previewWidth = Math.min(previewSize.width, previewSize.height);
//		int previewHeight = Math.max(previewSize.width, previewSize.height);
//		//获取 最终 我们需要预览的大小比列
//		float mPreviewScale = previewWidth * 1f / previewHeight;
//		//算出 最终 宽高
//		if (surfaceWidth > previewWidth) {
//			mPreviewHeight = (int) (mPreviewWidth / mPreviewScale);
//		} else if (surfaceHeight < previewWidth) {
//			mPreviewHeight = (int) (mPreviewHeight * mPreviewScale);
//		}
//
//		parameters.setPreviewSize(mPreviewWidth, mPreviewHeight); // 设置预览图像大小

    }

    public Size getOptimalSize(int w, int h) {
        Parameters cameraParameter = mCamera.getParameters();
        List<Size> sizes = cameraParameter.getSupportedPreviewSizes();
        final double ASPECT_TOLERANCE = 0.1;
        // 竖屏是 h/w, 横屏是 w/h
        double targetRatio = (double) h / w;
        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        return optimalSize;
    }

    public void setDisplayOrientation(int degrees) {
        int result;
        //前置摄像头
        if (mCameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // 前置 镜像
        } else {  // 后置摄像头
            result = (mCameraInfo.orientation - degrees + 360) % 360;
        }
        mCamera.setDisplayOrientation(result);
        mCamera.setDisplayOrientation(90);
    }

}
