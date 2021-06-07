package com.zhy.rtmp;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.yanzhenjie.permission.AndPermission;


public class InitActivity extends FragmentActivity {

    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndPermission.with(this)
                .runtime()
                .permission(REQUIRED_PERMISSION_LIST)
                .onGranted(permissions ->
                {
                    DemonActivity.start(InitActivity.this);
                    finish();
                })
                .onDenied(permissions ->
                {
                    Toast.makeText(InitActivity.this, "缺少权限！！", Toast.LENGTH_LONG).show();
                    finish();
                })
                .start();
    }
}
