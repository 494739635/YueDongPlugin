package com.yuedong.plugin.logic.gl;

import android.graphics.Point;
import android.opengl.GLES20;

import com.cosmos.beauty.opengl.GlUtil;
import com.cosmos.beauty.opengl.ProgramManager;

import java.nio.ByteBuffer;

import static android.opengl.GLES20.GL_RGBA;

public class ImageUtil {


    protected int[] mFrameBuffers;
    protected int[] mFrameBufferTextures;
    protected int FRAME_BUFFER_NUM = 1;
    protected Point mFrameBufferShape;
    protected int[] mTextureHandles;

    private ProgramManager mProgramManager;




    /** {zh} 
     * 默认构造函数
     */
    /** {en} 
     * Default constructor
     */

    public ImageUtil() {
    }

    /** {zh} 
     * 准备帧缓冲区纹理对象
     *
     * @param width  纹理宽度
     * @param height 纹理高度
     * @return 纹理ID
     */
    /** {en} 
     * Prepare frame buffer texture object
     *
     * @param width   texture width
     * @param height  texture height
     * @return  texture ID
     */

    public int prepareTexture(int width, int height) {
        initFrameBufferIfNeed(width, height);
        return mFrameBufferTextures[0];
    }

    /** {zh} 
     * 默认的离屏渲染绑定的纹理
     * @return 纹理id
     */
    /** {en} 
     * Default off-screen rendering bound texture
     * @return  texture id
     */

    public int getOutputTexture() {
        if (mFrameBufferTextures == null) return GlUtil.NO_TEXTURE;
        return mFrameBufferTextures[0];
    }

    /** {zh} 
     * 初始化帧缓冲区
     *
     * @param width  缓冲的纹理宽度
     * @param height 缓冲的纹理高度
     */
    /** {en} 
     * Initialize frame buffer
     *
     * @param width   buffered texture width
     * @param height  buffered texture height
     */

    private void initFrameBufferIfNeed(int width, int height) {
        boolean need = false;
        if (null == mFrameBufferShape || mFrameBufferShape.x != width || mFrameBufferShape.y != height) {
            need = true;
        }
        if (mFrameBuffers == null || mFrameBufferTextures == null) {
            need = true;
        }
        if (need) {
            destroyFrameBuffers();
            mFrameBuffers = new int[FRAME_BUFFER_NUM];
            mFrameBufferTextures = new int[FRAME_BUFFER_NUM];
            GLES20.glGenFramebuffers(FRAME_BUFFER_NUM, mFrameBuffers, 0);
            GLES20.glGenTextures(FRAME_BUFFER_NUM, mFrameBufferTextures, 0);
            for (int i = 0; i < FRAME_BUFFER_NUM; i++) {
                bindFrameBuffer(mFrameBufferTextures[i], mFrameBuffers[i], width, height);
            }
            mFrameBufferShape = new Point(width, height);
        }

    }

    /** {zh} 
     * 销毁帧缓冲区对象
     */
    /** {en} 
     * Destroy frame buffer objects
     */

    private void destroyFrameBuffers() {
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(FRAME_BUFFER_NUM, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        if (mFrameBuffers != null) {
            GLES20.glDeleteFramebuffers(FRAME_BUFFER_NUM, mFrameBuffers, 0);
            mFrameBuffers = null;
        }
    }

    /** {zh} 
     * 纹理参数设置+buffer绑定
     * set texture params
     * and bind buffer
     */
    /** {en} 
     * Texture parameter setting + buffer binding
     * set texture params
     * and binding buffer
     */

    private void bindFrameBuffer(int textureId, int frameBuffer, int width, int height) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }



    /** {zh} 
     * 释放资源，包括帧缓冲区及Program对象
     */
    /** {en} 
     * Free resources, including frame buffers and Program objects
     */



    /** {zh} 
     * 读取渲染结果的buffer
     *
     * @param imageWidth  图像宽度
     * @param imageHeight 图像高度
     * @return 渲染结果的像素Buffer 格式RGBA
     */
    /** {en} 
     * Read the buffer
     *
     * @param imageWidth   image width
     * @param imageHeight  image height
     * @return  pixel Buffer  format of the rendered result RGBA
     */

    public ByteBuffer captureRenderResult(int imageWidth, int imageHeight) {
        if (mFrameBufferTextures == null) return null;
        int textureId = mFrameBufferTextures[0];
        if (null == mFrameBufferTextures || textureId == GlUtil.NO_TEXTURE) {
            return null;
        }
        if (imageWidth * imageHeight == 0) {
            return null;
        }
        ByteBuffer mCaptureBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * 4);

        mCaptureBuffer.position(0);
        int[] frameBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glReadPixels(0, 0, imageWidth, imageHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mCaptureBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        if (null != frameBuffer) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
        return mCaptureBuffer;
    }

    /** {zh} 
     * 读取渲染结果的buffer
     *
     * @param imageWidth  图像宽度
     * @param imageHeight 图像高度
     * @return 渲染结果的像素Buffer 格式RGBA
     */
    /** {en} 
     * Read the buffer
     *
     * @param imageWidth   image width
     * @param imageHeight  image height
     * @return  pixel Buffer  format of the rendered result RGBA
     */

    public ByteBuffer captureRenderResult(int textureId, int imageWidth, int imageHeight) {
        if (textureId == GlUtil.NO_TEXTURE) {
            return null;
        }
        if (imageWidth * imageHeight == 0) {
            return null;
        }
        ByteBuffer mCaptureBuffer = ByteBuffer.allocateDirect(imageWidth * imageHeight * 4);

        mCaptureBuffer.position(0);
        int[] frameBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textureId, 0);
        GLES20.glReadPixels(0, 0, imageWidth, imageHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mCaptureBuffer);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        if (null != frameBuffer) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
        return mCaptureBuffer;
    }

    /** {zh} 
     * 纹理拷贝
     *
     * @param srcTexture
     * @param dstTexture
     * @param width
     * @param height
     * @return
     */
    /** {en} 
     * Texture copy
     *
     * @param srcTexture
     * @param dstTexture
     * @param width
     * @param height
     * @return
     */

    public boolean copyTexture(int srcTexture, int dstTexture, int width, int height) {
        if (srcTexture == GlUtil.NO_TEXTURE || dstTexture == GlUtil.NO_TEXTURE) {
            return false;
        }
        if (width * height == 0) {
            return false;
        }
        int[] frameBuffer = new int[1];
        GLES20.glGenFramebuffers(1, frameBuffer, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, frameBuffer[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, srcTexture, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, dstTexture);
        GLES20.glCopyTexImage2D(GLES20.GL_TEXTURE_2D, 0, GL_RGBA, 0, 0, width, height, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        if (null != frameBuffer) {
            GLES20.glDeleteFramebuffers(1, frameBuffer, 0);
        }
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = "copyTexture glError 0x" + Integer.toHexString(error);
            return false;
        }
        return true;


    }

    public int transferBufferToTexture(ByteBuffer buffer,  int width, int height, boolean consistent){



        int textureHandle;
        if (!consistent) {
            int[] textureHandles = new int[1];
            GLES20.glGenTextures(1, textureHandles, 0);
            textureHandle = textureHandles[0];
            GlUtil.checkGlError("glGenTextures");
        } else {
            if (mTextureHandles == null) {
                mTextureHandles = new int[1];
                GLES20.glGenTextures(1, mTextureHandles, 0);
                GlUtil.checkGlError("glGenTextures");
            }
            textureHandle = mTextureHandles[0];
        }
        create2DTexture(buffer, width,height, GL_RGBA, textureHandle);
        return textureHandle;
    }

    private void create2DTexture(ByteBuffer data, int width, int height, int format, int textureHandle) {
        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GlUtil.checkGlError("loadImageTexture");

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, format,
                width, height, /*border*/ 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        GlUtil.checkGlError("loadImageTexture");
    }

}
