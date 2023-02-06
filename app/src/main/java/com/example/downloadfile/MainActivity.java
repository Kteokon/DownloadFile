package com.example.downloadfile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    Button musicButton;
    Button fileButton;

    TextView tvPermission;
    Button btnPermission;

    private static final int PERMISSION_STORAGE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicButton = findViewById(R.id.musicButton);
        fileButton = findViewById(R.id.fileButton);

        tvPermission = findViewById(R.id.tvPermission);
        btnPermission = findViewById(R.id.btnPermission);

        if (PermissionUtils.hasPermissions(this)) {
            tvPermission.setText("Разрешение получено");
        } else {
            tvPermission.setText("Разрешение не предоставлено");
        }

        btnPermission.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                //if (PermissionUtils.hasPermissions(MainActivity.this)) return;
                PermissionUtils.requestPermissions(MainActivity.this, PERMISSION_STORAGE);
            }
        });
    }

    public void loadFile(View v) {
        Intent intent = new Intent(this, LoadMusicActivity.class);
        if (v.getId() == R.id.musicButton) {
            intent.putExtra("button", "music");
        }
        else {
            intent.putExtra("button", "file");
        }
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PERMISSION_STORAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (PermissionUtils.hasPermissions(this)) {
                    tvPermission.setText("Разрешение получено");
                } else {
                    tvPermission.setText("Разрешение не предоставлено");
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tvPermission.setText("Разрешение получено");
            } else {
                tvPermission.setText("Разрешение не предоставлено");
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}