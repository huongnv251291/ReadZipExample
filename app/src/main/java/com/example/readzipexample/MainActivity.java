package com.example.readzipexample;

import android.content.Context;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.UriUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 1111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btnopen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile();
            }
        });
    }

    private void getFile() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == PICKFILE_REQUEST_CODE) {
            Uri uri = data.getData();

            File file = UriUtils.uri2File(uri);

            if (file.exists()) {
                getResourceInThemePlugin(this, file, "theme");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s = cursor.getString(column_index);
        cursor.close();
        return s;
    }

    public static @Nullable
    Drawable getResourceInThemePlugin(Context context, File file, String identifyResourceName) {

        ReflectResource reflectResource = new ReflectResource(ApkPluginUtil.getApkResources(context, file.getAbsolutePath()), "theme");

        if (reflectResource != null) {

            try {
                XmlResourceParser xmlResourceParser = reflectResource.getResApkXml("three_d_themes");
            } catch (Exception e) {
                e.printStackTrace();
            }

//            Drawable drawable = reflectResource.getResApkDrawable(identifyResourceName);
            return null;
        } else {
            // không có trong resource apk, phải lấy trong data folder.
            File rootFolder = new File(Environment.getExternalStorageDirectory(), "Download");
            if (!rootFolder.exists()) {
                LogUtils.e("Resource not found!");
            } else {
                try {
//                        String filePath = rootFolder.getPath() + File.separator
//                                + theme + File.separator
//                                + "res" + File.separator
//                                + "drawabletest" + File.separator
//                                + identifyResourceName + ".png";


                    ReflectResource reflectResource1 = new ReflectResource(ApkPluginUtil.getApkResources(context, file.getAbsolutePath()), "theme");

                    Drawable drawable = reflectResource1.getResApkDrawable(identifyResourceName);

                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();

//                        Bitmap b = BitmapFactory.decodeFile(str);
                    return new FastBitmapDrawable(bitmap);
                } catch (Exception e) {
                    LogUtils.e(e);
                }

            }
        }
        return null;
    }
}
