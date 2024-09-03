package com.yuedong.plugin.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorEvent;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import com.cosmos.baseutil.BuildConfig;
import com.yuedong.plugin.camera.display.BaseDisplay;
import com.yuedong.plugin.camera.display.CameraProxy;
import com.yuedong.plugin.camera.display.ChangePreviewSizeListener;
import com.yuedong.plugin.camera.display.STRotateType;
import com.yuedong.plugin.camera.display.glutils.OpenGLUtils;
import com.yuedong.plugin.camera.display.glutils.STGLRender;
import com.yuedong.plugin.camera.display.glutils.TextureRotationUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 代替之前的CameraDisplayDoubleInput模式
 */
public class CameraDisplay extends BaseDisplay implements Renderer {
    private static final String TAG = "CameraDisplay";
    protected boolean DEBUG = BuildConfig.DEBUG;
    protected int mTextureId = OpenGLUtils.NO_TEXTURE;
    protected int mImageWidth;
    protected int mImageHeight;
    protected ChangePreviewSizeListener mListener;
    protected int mSurfaceWidth;
    protected int mSurfaceHeight;
    protected Context mContext;
    public CameraProxy mCameraProxy;
    protected SurfaceTexture mSurfaceTexture;
    protected int mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    protected STGLRender mGLRender;
    protected boolean mCameraChanging = false;
    protected int mCurrentPreview = 0;
    protected ArrayList<String> mSupportedPreviewSizes;
    protected boolean mSetPreViewSizeSucceed = false;
    protected boolean mIsChangingPreviewSize = false;

    protected long mStartTime;
    protected boolean mShowOriginal = false;
    protected FloatBuffer mTextureBuffer;
    protected boolean mIsPaused = false;
    protected Object mImageDataLock = new Object();
    protected boolean mNeedSave = false;
    protected HandlerThread mProcessImageThread;
    protected Handler mProcessImageHandler;
    protected static final int MESSAGE_PROCESS_IMAGE = 100;
    protected byte[] mImageData;
    protected long mRotateCost = 0;
    protected long mObjectCost = 0;
    protected long mFaceAttributeCost = 0;
    protected float mFps;
    protected int mCount = 0;
    protected long mCurrentTime = 0;
    protected boolean mIsFirstCount = true;
    protected int mFrameCost = 0;
    protected boolean mNeedResetEglContext = false;
    protected SensorEvent mSensorEvent;

    public IFrameProcessor frameProcessor;

    public CameraDisplay(Context context, GLSurfaceView glSurfaceView) {
        super(glSurfaceView);
        mCameraProxy = new CameraProxy();
//        mListener = listener;
        mContext = context;
        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        glSurfaceView.getHolder().setFixedSize(mImageWidth,mImageHeight);
        glSurfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
        glSurfaceView.setEGLContextFactory(this);
        glSurfaceView.setRenderer(this);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mTextureBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

        mTextureBuffer.put(TextureRotationUtil.TEXTURE_NO_ROTATION).position(0);
        mGLRender = new STGLRender();

        initHandlerManager();
    }

    protected void initHandlerManager() {
        mProcessImageThread = new HandlerThread("ProcessImageThread");
        mProcessImageThread.start();
        mProcessImageHandler = new Handler(mProcessImageThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_PROCESS_IMAGE && !mIsPaused && !mCameraChanging) {
                }
            }
        };
        GLES20.glFinish();
    }

    /**
     * 工作在opengl线程, 当前Renderer关联的view创建的时候调用
     *
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated");
        //recoverParams();
        if (mIsPaused == true) {
            return;
        }
        GLES20.glEnable(GL10.GL_DITHER);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glEnable(GL10.GL_DEPTH_TEST);

        while (!mCameraProxy.isCameraOpen()) {
            if (mCameraProxy.cameraOpenFailed()) {
                return;
            }
            try {
                Thread.sleep(10, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mCameraProxy.getCamera() != null) {
            setUpCamera();
        }
    }

    @Override
    public void setShowOriginal(boolean isShow) {
        mShowOriginal = isShow;
    }

    /**
     * 工作在opengl线程, 当前Renderer关联的view尺寸改变的时候调用
     *
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged");
        if (mIsPaused == true) {
            return;
        }

        adjustViewPort(width, height);

        mGLRender.init(mImageWidth, mImageHeight);
        mStartTime = System.currentTimeMillis();
    }


    /**
     * 根据显示区域大小调整一些参数信息
     *
     * @param width
     * @param height
     */
    protected void adjustViewPort(int width, int height) {
        mSurfaceHeight = height;
        mSurfaceWidth = width;
        GLES20.glViewport(0, 0, width, height);
        mGLRender.calculateVertexBuffer(mSurfaceWidth, mSurfaceHeight, mImageWidth, mImageHeight);
    }

    protected Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(final byte[] data, Camera camera) {

            if (mCameraChanging || mCameraProxy.getCamera() == null || data == null || mIsChangingPreviewSize || data.length != mImageWidth * mImageHeight * 3 / 2) {
                return;
            }

            if (mImageData == null || mImageData.length != mImageHeight * mImageWidth * 3 / 2) {
                mImageData = new byte[mImageWidth * mImageHeight * 3 / 2];
            }
            synchronized (mImageDataLock) {
                System.arraycopy(data, 0, mImageData, 0, data.length);
            }

            mProcessImageHandler.removeMessages(MESSAGE_PROCESS_IMAGE);
            mProcessImageHandler.sendEmptyMessage(MESSAGE_PROCESS_IMAGE);

            mGlSurfaceView.requestRender();
            camera.addCallbackBuffer(data);
        }
    };

    /**
     * 工作在opengl线程, 具体渲染的工作函数
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // during switch camera
        if (mCameraChanging) {
            return;
        }

        if (mCameraProxy.getCamera() == null) {
            return;
        }

        Log.i(TAG, "onDrawFrame");

        if (mSurfaceTexture != null && !mIsPaused) {
            mSurfaceTexture.updateTexImage();
        } else {
            return;
        }

        mStartTime = System.currentTimeMillis();
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        long preProcessCostTime = System.currentTimeMillis();
        int textureId = mGLRender.preProcess(mTextureId, null);
        Log.i(TAG, "preprocess cost time: " + (System.currentTimeMillis() - preProcessCostTime));

        if (!mShowOriginal) {
            if (frameProcessor != null) {
                textureId = frameProcessor.onProcessFrame(textureId);
            }
//            if (mSTMobileEffectNative != null) {
////                if (mCurrentFilterStyle != mFilterStyle) {
////                    mCurrentFilterStyle = mFilterStyle;
////                    mSTMobileEffectNative.setBeauty(STEffectBeautyType.EFFECT_BEAUTY_FILTER, mCurrentFilterStyle);
////                }
////                if (mCurrentFilterStrength != mFilterStrength) {
////                    mCurrentFilterStrength = mFilterStrength;
////                    mSTMobileEffectNative.setBeautyStrength(STEffectBeautyType.EFFECT_BEAUTY_FILTER, mCurrentFilterStrength);
////                }
//
//                STEffectTexture stEffectTexture = new STEffectTexture(textureId, mImageWidth, mImageHeight, 0);
//                STEffectTexture stEffectTextureOut = new STEffectTexture(mBeautifyTextureId[0], mImageWidth, mImageHeight, 0);
//
//                int renderOrientation = getCurrentOrientation();
//
//                int event = mCustomEvent;
//                STEffectCustomParam customParam;
//                if (mSensorEvent != null && mSensorEvent.values != null && mSensorEvent.values.length > 0) {
//                    customParam = new STEffectCustomParam(new STQuaternion(mSensorEvent.values), mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT, event);
//                } else {
//                    customParam = new STEffectCustomParam(new STQuaternion(0f, 0f, 0f, 1f), mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT, event);
//                }
//
//                STEffectRenderInParam sTEffectRenderInParam = new STEffectRenderInParam(mSTHumanAction[0], mAnimalFaceInfo[0], renderOrientation, STRotateType.ST_CLOCKWISE_ROTATE_0, false, customParam, stEffectTexture, null);
//                STEffectRenderOutParam stEffectRenderOutParam = new STEffectRenderOutParam(stEffectTextureOut, null, mSTHumanAction[0]);
//                result = mSTMobileEffectNative.render(sTEffectRenderInParam, stEffectRenderOutParam, false);
//                if (stEffectRenderOutParam != null && stEffectRenderOutParam.getTexture() != null) {
//                    textureId = stEffectRenderOutParam.getTexture().getId();
//                }
//
//                if (event == mCustomEvent) {
//                    mCustomEvent = 0;
//                }
//            }

            Log.i(TAG, "render cost time total: " + (System.currentTimeMillis() - mStartTime + mRotateCost + mObjectCost + mFaceAttributeCost / 20));
        }


        if (mNeedSave) {
            savePicture(textureId);
            mNeedSave = false;
        }

        mFrameCost = (int) (System.currentTimeMillis() - mStartTime + mRotateCost + mObjectCost + mFaceAttributeCost / 20);

        long timer = System.currentTimeMillis();
        mCount++;
        if (mIsFirstCount) {
            mCurrentTime = timer;
            mIsFirstCount = false;
        } else {
            int cost = (int) (timer - mCurrentTime);
            if (cost >= 1000) {
                mCurrentTime = timer;
                mFps = (((float) mCount * 1000) / cost);
                mCount = 0;
            }
        }

        Log.i(TAG, "frame cost time total: %d" + (System.currentTimeMillis() - mStartTime));
        Log.i(TAG, "render fps: %f" + mFps);

        GLES20.glViewport(0, 0, mSurfaceWidth, mSurfaceHeight);
        mGLRender.onDrawFrame(textureId);
    }

    protected void savePicture(int textureId) {
        ByteBuffer mTmpBuffer = ByteBuffer.allocate(mImageHeight * mImageWidth * 4);
        mGLRender.saveTextureToFrameBuffer(textureId, mTmpBuffer);

        mTmpBuffer.position(0);
    }

    protected void saveImageBuffer2Picture(byte[] imageBuffer) {
        ByteBuffer mTmpBuffer = ByteBuffer.allocate(mImageHeight * mImageWidth * 4);
        mTmpBuffer.put(imageBuffer);
    }

    public int getCurrentOrientation() {
        int dir = Accelerometer.getDirection();
        int orientation = dir - 1;
        if (orientation < 0) {
            orientation = dir ^ 3;
        }

        return orientation;
    }


    /**
     * camera设备startPreview
     */
    protected void setUpCamera() {
        // 初始化Camera设备预览需要的显示区域(mSurfaceTexture)
        if (mTextureId == OpenGLUtils.NO_TEXTURE) {
            mTextureId = OpenGLUtils.getExternalOESTextureID();
            mSurfaceTexture = new SurfaceTexture(mTextureId);
        }

        String size = mSupportedPreviewSizes.get(mCurrentPreview);
        int index = size.indexOf('x');
        mImageHeight = Integer.parseInt(size.substring(0, index));
        mImageWidth = Integer.parseInt(size.substring(index + 1));

        if (mIsPaused) {
            return;
        }

        while (!mSetPreViewSizeSucceed) {
            try {
                mCameraProxy.setPreviewSize(mImageHeight, mImageWidth);
                mSetPreViewSizeSucceed = true;
            } catch (Exception e) {
                mSetPreViewSizeSucceed = false;
            }

            try {
                Thread.sleep(10);
            } catch (Exception e) {

            }
        }

        boolean flipHorizontal = mCameraProxy.isFlipHorizontal();
        boolean flipVertical = mCameraProxy.isFlipVertical();
        mGLRender.adjustTextureBuffer(mCameraProxy.getOrientation(), flipVertical, flipHorizontal);
        if (mIsPaused) {
            return;
        }
        mCameraProxy.startPreview(mSurfaceTexture, mPreviewCallback);
//        mCameraProxy.startFaceDetection();
    }

    public void onResume() {
        Log.i(TAG, "onResume");

        if (mCameraProxy.getCamera() == null) {
            if (mCameraProxy.getNumberOfCameras() == 1) {
                mCameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            mCameraProxy.openCamera(mCameraID);
            mSupportedPreviewSizes = mCameraProxy.getSupportedPreviewSize(new String[]{"1280x720"});
        }
        mIsPaused = false;
        mSetPreViewSizeSucceed = false;
        mNeedResetEglContext = true;

        if (mGLRender != null) {
            mGLRender = new STGLRender();
        }

        mGlSurfaceView.onResume();
        mGlSurfaceView.forceLayout();
    }

    public void onPause() {
        Log.i(TAG, "onPause");
        mSetPreViewSizeSucceed = false;
        mIsPaused = true;
        mImageData = null;
        mCameraProxy.releaseCamera();
        Log.d(TAG, "Release camera");

        mGlSurfaceView.queueEvent(() -> {
            deleteTextures();
            if (mSurfaceTexture != null) {
                mSurfaceTexture.release();
            }
            mGLRender.destroyFrameBuffers();
        });

        mGlSurfaceView.onPause();
    }

    public void onDestroy() {

        if (mEGLContextHelper != null) {
            mEGLContextHelper.eglMakeCurrent();
            mEGLContextHelper.eglMakeNoCurrent();
            mEGLContextHelper.release();
            mEGLContextHelper = null;
        }
    }

    /**
     * 释放纹理资源
     */
    protected void deleteTextures() {
        Log.i(TAG, "delete textures");
        // must in opengl thread
        if (mTextureId != OpenGLUtils.NO_TEXTURE) {
            GLES20.glDeleteTextures(1, new int[]{mTextureId}, 0);
        }
        mTextureId = OpenGLUtils.NO_TEXTURE;
    }

    public void switchCamera() {
        mNeedResetEglContext = true;
        if (Camera.getNumberOfCameras() == 1 || mCameraChanging) {
            return;
        }


        final int cameraID = 1 - mCameraID;
        mCameraChanging = true;
        mCameraProxy.openCamera(cameraID);
        if (mCameraProxy.cameraOpenFailed()) {
            return;
        }

        mSetPreViewSizeSucceed = false;

        mGlSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                deleteTextures();
                if (mCameraProxy.getCamera() != null) {
                    setUpCamera();
                }
                mCameraChanging = false;
                mCameraID = cameraID;
            }
        });
        //fix 双输入camera changing时，贴纸和画点mirrow显示
        //mGlSurfaceView.requestRender();
    }

    public int getCameraID() {
        return mCameraID;
    }

    public void changePreviewSize(int currentPreview) {
        mNeedResetEglContext = true;
        if (mCameraProxy.getCamera() == null || mCameraChanging || mIsPaused) {
            return;
        }

        mCurrentPreview = currentPreview;
        mSetPreViewSizeSucceed = false;
        mIsChangingPreviewSize = true;

        mCameraChanging = true;
        mCameraProxy.stopPreview();
        mGlSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                deleteTextures();
                if (mCameraProxy.getCamera() != null) {
                    setUpCamera();
                }

                mGLRender.init(mImageWidth, mImageHeight);
                if (DEBUG) {
                    mGLRender.initDrawPoints();
                }

                mGLRender.calculateVertexBuffer(mSurfaceWidth, mSurfaceHeight, mImageWidth, mImageHeight);
                if (mListener != null) {
                    mListener.onChangePreviewSize(mImageHeight, mImageWidth);
                }

                mCameraChanging = false;
                mIsChangingPreviewSize = false;
                //mGlSurfaceView.requestRender();
                Log.d(TAG, "exit  change Preview size queue event");
            }
        });
    }

    /**
     * 用于humanActionDetect接口。根据传感器方向计算出在不同设备朝向时，人脸在buffer中的朝向
     *
     * @return 人脸在buffer中的朝向
     */
    protected int getHumanActionOrientation() {
        boolean frontCamera = (mCameraID == Camera.CameraInfo.CAMERA_FACING_FRONT);

        //获取重力传感器返回的方向
        int orientation = Accelerometer.getDirection();

        //在使用后置摄像头，且传感器方向为0或2时，后置摄像头与前置orentation相反
        if (!frontCamera && orientation == STRotateType.ST_CLOCKWISE_ROTATE_0) {
            orientation = STRotateType.ST_CLOCKWISE_ROTATE_180;
        } else if (!frontCamera && orientation == STRotateType.ST_CLOCKWISE_ROTATE_180) {
            orientation = STRotateType.ST_CLOCKWISE_ROTATE_0;
        }

        // 请注意前置摄像头与后置摄像头旋转定义不同 && 不同手机摄像头旋转定义不同
        if (((mCameraProxy.getOrientation() == 270 && (orientation & STRotateType.ST_CLOCKWISE_ROTATE_90) == STRotateType.ST_CLOCKWISE_ROTATE_90) || (mCameraProxy.getOrientation() == 90 && (orientation & STRotateType.ST_CLOCKWISE_ROTATE_90) == STRotateType.ST_CLOCKWISE_ROTATE_0))) {
            orientation = (orientation ^ STRotateType.ST_CLOCKWISE_ROTATE_180);
        }
        return orientation;
    }

    public int getPreviewWidth() {
        return mImageWidth;
    }

    public int getPreviewHeight() {
        return mImageHeight;
    }

    public int getFrameCost() {
        return mFrameCost;
    }

    public float getFpsInfo() {
        return (float) (Math.round(mFps * 10)) / 10;
    }

    public void setSensorEvent(SensorEvent event) {
        mSensorEvent = event;
    }

    public void handleZoom(boolean isZoom) {
        mCameraProxy.handleZoom(isZoom);
    }

    public void setExposureCompensation(int progress) {
        mCameraProxy.setExposureCompensation(progress);
    }

    @Override
    public boolean setCameraNeedMirror(boolean cameraNeedMirror) {
        return true;
    }

    public interface IFrameProcessor {
        int onProcessFrame(int texture);
    }

}
