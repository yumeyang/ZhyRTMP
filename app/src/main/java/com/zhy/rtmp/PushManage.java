package com.zhy.rtmp;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.text.TextUtils;

import com.github.faucamp.simplertmp.RtmpHandler;

import net.ossrs.yasea.SrsCameraView;
import net.ossrs.yasea.SrsEncodeHandler;
import net.ossrs.yasea.SrsPublisher;
import net.ossrs.yasea.SrsRecordHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;

public class PushManage {

    private static final int mPreviewWidth = 1920;
    private static final int mPreviewHeight = 1080;
    private static final int mWidth_OUT = 1280;
    private static final int mHeight_OUT = 720;

    private boolean mIsEncode;//开启编码
    private boolean mIsPushing;
    private boolean mIsRecording;
    private boolean mIsEncodeFrame;

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

    public boolean isEncodeH264() {
        return mIsEncode;
    }

    public void startEncodeH264() {
        if (mIsEncode) {
            return;
        }
        mIsEncode = true;
        mPublisher.startEncode();
    }

    public void stopEncode() {
        if (!mIsEncode) {
            return;
        }
        mIsEncode = false;
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
            onFrameBitmap(result);
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

    protected void onFrameBitmap(Bitmap bitmap) {

    }
}
