package com.example.readzipexample;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.UriUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 1111;
    private static final String TAG = "MainActivity";

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

    protected static CharSequence getTextFromResourceOrText(
            Context context, AttributeSet attrs, String attributeName) {
        final int stringResId =
                attrs.getAttributeResourceValue(null, attributeName, 0);
        if (stringResId != 0) {
            return context.getResources().getText(stringResId);
        } else {
            return attrs.getAttributeValue(null, attributeName);
        }
    }

    private int getApiVersion(Context packContext) {
        try {
            final Resources resources = packContext.getResources();
            final int identifier =
                    resources.getIdentifier(
                            "anysoftkeyboard_api_version_code",
                            "integer",
                            packContext.getPackageName());
            if (identifier == 0) return 0;

            return resources.getInteger(identifier);
        } catch (Exception e) {
            Log.w(
                    TAG,
                    "Failed to load api-version for package %s" +
                            packContext.getPackageName());
            return 0;
        }
    }

    private void createAddOnFromXmlAttributes(AttributeSet attrs, Context packContext) {
        final CharSequence prefId =
                getTextFromResourceOrText(packContext, attrs, "id");
        final CharSequence name =
                getTextFromResourceOrText(packContext, attrs, "nameResId");

//        if (!mDevAddOnsIncluded
//                && attrs.getAttributeBooleanValue(null, XML_DEV_ADD_ON_ATTRIBUTE, false)) {
//            Logger.w(
//                    mTag,
//                    "Discarding add-on %s (name %s) since it is marked as DEV addon, and we're not a TESTING_BUILD build.",
//                    prefId,
//                    name);
//            return null;
//        }

        final int apiVersion = getApiVersion(packContext);
        final boolean isHidden =
                attrs.getAttributeBooleanValue(null, "hidden", false);
        final CharSequence description =
                getTextFromResourceOrText(packContext, attrs, "description");

        final int sortIndex = attrs.getAttributeUnsignedIntValue(null, "index", 1);

        // asserting
        if (TextUtils.isEmpty(prefId) || TextUtils.isEmpty(name)) {
            Log.e(
                    TAG,
                    "External add-on does not include all mandatory details! Will not create add-on.");
            return;
        } else {
            Log.d(TAG, "External addon details: prefId:" + prefId + " name:" + name);

        }
    }

    public void parseAddOnsFromXml(Context packContext, XmlPullParser xml) {
        try {
            int event;
            boolean inRoot = false;
            while ((event = xml.next()) != XmlPullParser.END_DOCUMENT) {
                final String tag = xml.getName();
                if (event == XmlPullParser.START_TAG) {
                    if ("KeyboardThemes".equals(tag)) {
                        inRoot = true;
                    } else if (inRoot && "KeyboardTheme".equals(tag)) {
                        final AttributeSet attrs = Xml.asAttributeSet(xml);
                        createAddOnFromXmlAttributes(attrs, packContext);
                    }
                } else if (event == XmlPullParser.END_TAG && "KeyboardThemes".equals(tag)) {
                    inRoot = false;
                    break;
                }
            }
        } catch (final IOException e) {
            Log.e(TAG, "IO error:" + e);
            e.printStackTrace();
        } catch (final XmlPullParserException e) {
            Log.e(TAG, "Parse error:" + e);
            e.printStackTrace();
        }

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == PICKFILE_REQUEST_CODE) {
            Uri uri = data.getData();

            File file = UriUtils.uri2File(uri);

            if (file.exists()) {
                getResourceInThemePlugin(this, file, "three_d_themes");
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

    public @Nullable
    Drawable getResourceInThemePlugin(Context context, File file, String identifyResourceName) {
        Resources resources = ApkPluginUtil.getApkResources(context, file.getAbsolutePath());
        ReflectResource reflectResource = new ReflectResource(resources, "com.anysoftkeyboard");

        XmlResourceParser xmlResourceParser = reflectResource.getResApkXml(identifyResourceName);
        parseAddOnsFromXml(context, xmlResourceParser);
        Log.e(TAG, xmlResourceParser.toString());
//            Drawable drawable = reflectResource.getResApkDrawable(identifyResourceName);
        return null;
    }
}
