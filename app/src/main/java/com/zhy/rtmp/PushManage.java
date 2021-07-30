package com.zhy.rtmp;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.text.TextUtils;
import android.util.Log;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;

public class PushManage {

    private static final String TAG = PushManage.class.getSimpleName();

    private static final int mPreviewWidth = 1920;
    private static final int mPreviewHeight = 1080;
    private static final int mWidth_OUT = 1280;
    private static final int mHeight_OUT = 720;

    private boolean mOpenEncode;//开启编码
    private boolean mIsPushing;
    private boolean mIsRecording;
    private boolean mIsEncodeFrame;
    private String mRtpUrl;

    private final SrsPublisher mPublisher;

    public PushManage(SrsCameraView view) {
        mPublisher = new SrsPublisher(view) {
            @Override
            public void onGetFrame(byte[] data) {
                super.onGetFrame(data);
                onFrameToBitmap(data);
            }
        };
        mPublisher.setEncodeHandler(new SrsEncodeHandler(mEncodeListener));
        mPublisher.setRtmpHandler(new RtmpHandler(mRtpListener));
        mPublisher.setRecordHandler(new SrsRecordHandler(mSrsRecordListener));
        mPublisher.setPreviewResolution(mPreviewWidth, mPreviewHeight);
        mPublisher.setScreenOrientation(Configuration.ORIENTATION_LANDSCAPE);
        mPublisher.setOutputResolution(mWidth_OUT, mHeight_OUT);
        mPublisher.setVideoHDMode();
    }

    public void startCamera() {
        mPublisher.startCamera();
    }

    public void startEncodeH264() {
        if (mOpenEncode) {
            return;
        }
        mOpenEncode = true;
        mPublisher.startEncode();
    }

    public void stopEncode() {
        if (!mOpenEncode) {
            return;
        }
        mOpenEncode = false;
        mPublisher.stopEncode();
    }

    public boolean isPushing() {
        return mIsPushing;
    }

    public void startPushRtp(String rtp_url) {
        if (TextUtils.isEmpty(rtp_url)) {
            return;
        }

        if (mIsPushing) {
            return;
        }

        mIsPushing = true;
        mRtpUrl = rtp_url;
        mPublisher.startPublish2(rtp_url);
    }

    public void stopPushRtp() {
        if (!mIsPushing) {
            return;
        }
        mPublisher.stopPublish2();
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void startRecord(String path) {
        if (mIsRecording) {
            return;
        }
        mIsRecording = true;
        mPublisher.startRecord(path);
    }

    public void stopRecord() {
        if (!mIsRecording) {
            return;
        }
        mPublisher.stopRecord();
    }


    private boolean mStartResolution;
    private int mResolution;
    private boolean mPushing;

    public void startResolution(int resolution) {
        mStartResolution = true;
        mResolution = resolution;
        mPushing = isPushing();
        onDestroy();
    }

    private void resetResolution() {
        Log.e(TAG, "resetResolution");
        int width_out = mWidth_OUT;
        int height_out = mHeight_OUT;
        if (mResolution == 1) {
            mPublisher.setVideoSmoothMode();
            width_out = 640;
            height_out = 360;
        } else if (mResolution == 2) {
            mPublisher.setVideoMode720();
        } else if (mResolution == 3) {
            mPublisher.setVideoHDMode();
            width_out = 1920;
            height_out = 1080;
        }
        mPublisher.setOutputResolution(width_out, height_out);
        startCamera();
        startEncodeH264();
        if (mPushing) {
            startPushRtp(mRtpUrl);
        }
        mStartResolution = false;
    }

    public void onDestroy() {
        stopPushRtp();
        stopRecord();
        stopEncode();
    }

    final SrsEncodeHandler.SrsEncodeListener mEncodeListener = new SrsEncodeHandler.SrsEncodeListener() {
        @Override
        public void onNetworkWeak() {

        }

        @Override
        public void onNetworkResume() {

        }

        @Override
        public void onEncodeIllegalArgumentException(IllegalArgumentException e) {

        }
    };

    final RtmpHandler.RtmpListener mRtpListener = new RtmpHandler.RtmpListener() {
        @Override
        public void onRtmpConnecting(String msg) {

        }

        @Override
        public void onRtmpConnected(String msg) {
            mIsPushing = true;
            onPushSuccess();
        }

        @Override
        public void onRtmpVideoStreaming() {

        }

        @Override
        public void onRtmpAudioStreaming() {

        }

        @Override
        public void onRtmpStopped() {
            mIsPushing = false;
        }

        @Override
        public void onRtmpDisconnected() {
            mIsPushing = false;
            if (mStartResolution && !mIsRecording) {
                resetResolution();
            }
        }

        @Override
        public void onRtmpVideoFpsChanged(double fps) {

        }

        @Override
        public void onRtmpVideoBitrateChanged(double bitrate) {

        }

        @Override
        public void onRtmpAudioBitrateChanged(double bitrate) {

        }

        @Override
        public void onRtmpSocketException(SocketException e) {
            mIsPushing = false;
            onPushFail();
        }

        @Override
        public void onRtmpIOException(IOException e) {
            mIsPushing = false;
            onPushFail();
        }

        @Override
        public void onRtmpIllegalArgumentException(IllegalArgumentException e) {
            mIsPushing = false;
            onPushFail();
        }

        @Override
        public void onRtmpIllegalStateException(IllegalStateException e) {
            mIsPushing = false;
            onPushFail();
        }
    };

    final SrsRecordHandler.SrsRecordListener mSrsRecordListener = new SrsRecordHandler.SrsRecordListener() {
        @Override
        public void onRecordPause() {

        }

        @Override
        public void onRecordResume() {

        }

        @Override
        public void onRecordStarted(String msg) {
            onStartRecord(msg);
        }

        @Override
        public void onRecordFinished(String msg) {
            mIsRecording = false;
            onFinishRecord(msg);

            if (mStartResolution && !mIsPushing) {
                resetResolution();
            }
        }

        @Override
        public void onRecordIllegalArgumentException(IllegalArgumentException e) {
            mIsRecording = false;
        }

        @Override
        public void onRecordIOException(IOException e) {
            mIsRecording = false;
        }
    };

    public void startEncodeFrame() {
        if (mIsEncodeFrame) {
            return;
        }
        mIsEncodeFrame = true;
    }

    private void onFrameToBitmap(byte[] data) {
        if (!mIsEncodeFrame) {
            return;
        }
        mIsEncodeFrame = false;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, mPreviewWidth, mPreviewHeight, null);
        yuvimage.compressToJpeg(new Rect(0, 0, mPreviewWidth, mPreviewHeight), 100, stream);

        byte[] b = stream.toByteArray();

        if (b.length != 0) {
            Bitmap result = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, b.length);
            //生成Bitmap图片
        }
    }

    public void onPushSuccess() {

    }

    public void onPushFail() {

    }

    public void onStartRecord(String msg) {

    }

    public void onFinishRecord(String msg) {

    }
}
