package com.yuedong.plugin.beauty.camera.display;

import android.opengl.GLSurfaceView;

import com.yuedong.plugin.beauty.camera.display.glutils.EGLContextHelper;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public abstract class BaseDisplay implements GLSurfaceView.EGLContextFactory {
    private static final String TAG = "BaseDisplay";
    protected GLSurfaceView mGlSurfaceView;
    protected EGLContext mEglContext;
    protected EGLContextHelper mEGLContextHelper = new EGLContextHelper();

    public BaseDisplay(GLSurfaceView glSurfaceView) {
        initEglContext();
        if (mEGLContextHelper != null) {
            mEGLContextHelper.eglMakeCurrent();
            mEglContext = mEGLContextHelper.getEGLContext();
            mEGLContextHelper.eglMakeNoCurrent();
        }
        this.mGlSurfaceView = glSurfaceView;
        glSurfaceView.setEGLContextFactory(this);
    }

    @Override
    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
        EGLContext shareContext = mEglContext;
        return shareContext;
    }

    @Override
    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
    }

    private void initEglContext() {
        try {
            mEGLContextHelper.initEGL();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mEGLContextHelper.eglMakeCurrent();
        mEglContext = mEGLContextHelper.getEGLContext();
        mEGLContextHelper.eglMakeNoCurrent();
    }

    public abstract void setShowOriginal(boolean isShow);

    public abstract boolean setCameraNeedMirror(boolean cameraNeedMirror);
}
