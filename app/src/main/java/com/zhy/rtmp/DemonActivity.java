package com.zhy.rtmp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.io.File;

public class DemonActivity extends Activity implements View.OnClickListener {

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, DemonActivity.class);
        activity.startActivity(intent);
    }

    private TextView tv_push;
    private TextView tv_record;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_demon);
        initView();
        initListener();
        getPush().startCamera();
        getPush().startEncodeH264();
    }

    private void initView() {
        tv_push = findViewById(R.id.tv_push);
        tv_record = findViewById(R.id.tv_record);
    }

    private void initListener() {
        tv_push.setOnClickListener(this);
        tv_record.setOnClickListener(this);
    }

    private PushManage mPush;

    private PushManage getPush() {
        if (mPush == null) {
            mPush = new PushManage(findViewById(R.id.view_srs_camera)) {
                @Override
                public void onPushFail() {
                    super.onPushFail();
                    tv_push.setText("开始推流");
                    tv_push.setClickable(true);
                }

                @Override
                public void onPushSuccess() {
                    super.onPushSuccess();
                    tv_push.setText("结束推流");
                    tv_push.setClickable(true);
                }

                @Override
                public void onStartRecord(String msg) {
                    super.onStartRecord(msg);
                    tv_record.setClickable(true);
                    tv_record.setText("结束录像");
                }

                @Override
                public void onFinishRecord(String msg) {
                    super.onFinishRecord(msg);
                    tv_record.setClickable(true);
                    tv_record.setText("开始录像");
                }
            };
        }
        return mPush;
    }

    @Override
    public void onClick(View v) {
        if (v == tv_push) {
            clickPush();
        } else if (v == tv_record) {
            clickRecord();
        }
    }

    private void clickPush() {
        getPush().startEncodeH264();

        if (getPush().isPushing()) {
            getPush().stopPushRtp();
            tv_push.setText("开始推流");
        } else {
            tv_push.setClickable(false);
            getPush().startPushRtp("rtmp://114.115.184.191:1938/myapp/111111");
        }
    }

    private void clickRecord() {
        getPush().startEncodeH264();

        tv_record.setClickable(false);
        if (getPush().isRecording()) {
            getPush().stopRecord();
        } else {
            String dirs = Environment.getExternalStorageDirectory() + File.separator + "Record" + File.separator;
            File file = new File(dirs);
            if (!file.exists()) {
                file.mkdirs();
            }
            String record = dirs + System.currentTimeMillis() + ".mp4";
            getPush().startRecord(record);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPush().onDestroy();
    }
}
