package com.example.readzipexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.UriUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 1111;
    private static final String TAG = "MainActivity";
    private KeyboardTheme mKeyboardTheme;
    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btnopen);
        button.findViewById(R.id.btnopen).setOnClickListener(new View.OnClickListener() {
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
            Context context, ReflectResource reflectResource, AttributeSet attrs, String attributeName) {
        final int stringResId =
                attrs.getAttributeResourceValue(null, attributeName, 0);
        if (stringResId != 0) {
            return reflectResource.getResources().getText(stringResId);
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

    private void createAddOnFromXmlAttributes(AttributeSet attrs, ReflectResource reflectResource, Context packContext) {
        final CharSequence prefId =
                getTextFromResourceOrText(packContext, reflectResource, attrs, "id");
        final CharSequence name =
                getTextFromResourceOrText(packContext, reflectResource, attrs, "nameResId");

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
                getTextFromResourceOrText(packContext, reflectResource, attrs, "description");

        final int sortIndex = attrs.getAttributeUnsignedIntValue(null, "index", 1);

        // asserting
        if (TextUtils.isEmpty(prefId) || TextUtils.isEmpty(name)) {
            Log.e(
                    TAG,
                    "External add-on does not include all mandatory details! Will not create add-on.");
            return;
        } else {
            Log.d(TAG, "External addon details: prefId:" + prefId + " name:" + name);
            createConcreteAddOn(reflectResource, prefId, attrs);
        }
    }

    public void parseAddOnsFromXml(Context packContext, ReflectResource reflectResource, XmlPullParser xml) {
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
                        createAddOnFromXmlAttributes(attrs, reflectResource, packContext);
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

    private static final String XML_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "themeRes";
    private static final String XML_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE = "iconsThemeRes";
    private static final String XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE = "popupThemeRes";
    private static final String XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE =
            "popupIconsThemeRes";

    @SuppressLint("ResourceType")
    protected void createConcreteAddOn(ReflectResource reflectResource,
                                       CharSequence prefId,
                                       AttributeSet attrs) {
        final int keyboardThemeResId =
                attrs.getAttributeResourceValue(null, XML_KEYBOARD_THEME_RES_ID_ATTRIBUTE, 0);
        final int popupKeyboardThemeResId =
                attrs.getAttributeResourceValue(null, XML_POPUP_KEYBOARD_THEME_RES_ID_ATTRIBUTE, 0);
        final int iconsThemeResId =
                attrs.getAttributeResourceValue(null, XML_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE, 0);
        final int popupKeyboardIconThemeResId =
                attrs.getAttributeResourceValue(
                        null, XML_POPUP_KEYBOARD_ICONS_THEME_RES_ID_ATTRIBUTE, 0);

        if (keyboardThemeResId == -1) {
            String detailMessage =
                    String.format(
                            Locale.US,
                            "Missing details for creating Keyboard theme! prefId %s, keyboardThemeResId: %d",
                            prefId,
                            keyboardThemeResId);

            throw new RuntimeException(detailMessage);
        }
        mKeyboardTheme = new KeyboardTheme(
                reflectResource,
                keyboardThemeResId,
                popupKeyboardThemeResId,
                iconsThemeResId,
                popupKeyboardIconThemeResId);
        Log.e(TAG, mKeyboardTheme.toString());
        setTheme();
    }

    @SuppressLint("ResourceType")
    private void setTheme() {
        final int keyboardThemeStyleResId = mKeyboardTheme.getPopupThemeResId();
//        final int[] remoteKeyboardThemeStyleable = R.styleable.AnyKeyboardViewTheme;
//        String xml = mKeyboardTheme.mReflectResource.getResources().getResourceName(keyboardThemeStyleResId);
//        AttributeSet attributeSet = Xml.asAttributeSet(xml);
//        TypedArray a =
//                mKeyboardTheme.mReflectResource.getResources()
//                        .obtainAttributes(attributeSet, remoteKeyboardThemeStyleable);
//        final int n = a.getIndexCount();
        Log.e(TAG, mKeyboardTheme.mReflectResource.getResources().getResourceEntryName(keyboardThemeStyleResId));
        final int[] remoteKeyboardThemeStyleable = R.styleable.AnyKeyboardViewTheme;
        TypedArray typedArray = mKeyboardTheme.mReflectResource.getResources().newTheme().obtainStyledAttributes(keyboardThemeStyleResId,remoteKeyboardThemeStyleable);
        final int n = typedArray.getIndexCount();
        Log.e(TAG, "getIndexCount " + n + " - " + typedArray.toString());
        for (int i = 0; i < n; i++) {
            TypedValue value = new TypedValue();
            typedArray.getValue(i, value);

            final int remoteIndex = typedArray.getIndex(i);
            final int localAttrId =
                    remoteKeyboardThemeStyleable[remoteIndex];
            switch (localAttrId) {
                case android.R.attr.background:
                    Drawable keyboardBackground = typedArray.getDrawable(remoteIndex);
                    button.setBackground(keyboardBackground);
                    Log.e(TAG,"background :"+keyboardBackground);
                    break;
                case android.R.attr.paddingTop:
                    break;
                case android.R.attr.paddingRight:
                    break;
                case android.R.attr.paddingBottom:
                    break;
                case R.attr.keyBackground:
                    break;
                case R.attr.keyHysteresisDistance:
                    break;
                case R.attr.verticalCorrection:
                    break;
                case R.attr.keyTextSize:
                    break;
                case R.attr.keyTextColor:
                    break;
                case R.attr.labelTextSize:
                    break;
                case R.attr.keyboardNameTextSize:
                    break;
                case R.attr.keyboardNameTextColor:
                    break;
                case R.attr.shadowColor:
                    break;
                case R.attr.shadowRadius:
                    break;
                case R.attr.shadowOffsetX:
                    break;
                case R.attr.shadowOffsetY:
                    break;
                case R.attr.backgroundDimAmount:
                    break;
                case R.attr.keyPreviewBackground:
                    break;
                case R.attr.keyPreviewTextColor:
                    break;
                case R.attr.keyPreviewTextSize:
                    int keyPreviewTextSize =
                            typedArray.getDimensionPixelSize(remoteIndex, -1);
                    Log.e(TAG,"keyPreviewTextSize :"+keyPreviewTextSize);
                    break;
                case R.attr.keyPreviewLabelTextSize:
                    break;
                case R.attr.keyPreviewOffset:
                    break;
                case R.attr.previewAnimationType:
                    break;
                case R.attr.keyTextStyle:
                    break;
                case R.attr.keyHorizontalGap:
                    break;
                case R.attr.keyVerticalGap:
                    break;
                case R.attr.keyNormalHeight:
                    break;
                case R.attr.keyLargeHeight:
                    break;
                case R.attr.keySmallHeight:
                    break;
                case R.attr.hintTextSize:
                    break;
                case R.attr.hintTextColor:
                    break;
                case R.attr.hintLabelVAlign:
                    break;
                case R.attr.hintLabelAlign:
                    break;
                case R.attr.keyTextCaseStyle:
                    break;
            }
        }
        typedArray.recycle();
//        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && requestCode == PICKFILE_REQUEST_CODE) {
            Uri uri = data.getData();

            File file = UriUtils.uri2File(uri);

            if (file.exists()) {
                try {
                    getResourceInThemePlugin(this, file, "three_d_themes");
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
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
    Drawable getResourceInThemePlugin(Context context, File file, String identifyResourceName) throws PackageManager.NameNotFoundException {
        PackageManager pm = getPackageManager();

        PackageInfo info = pm.getPackageArchiveInfo(file.getAbsolutePath(),
                PackageManager.GET_ACTIVITIES);
        //Log.i("ActivityInfo", "Package name is " + info.packageName);

        for (android.content.pm.ActivityInfo a : info.activities) {
            Log.i("ActivityInfo", a.name);
        }
//        Context externalPackageContext = createPackageContext(
//                        info.activities[0].packageName, Context.CONTEXT_IGNORE_SECURITY);
        Resources resources = ApkPluginUtil.getApkResources(context, file.getAbsolutePath());
        ReflectResource reflectResource = new ReflectResource(resources, "com.anysoftkeyboard");
        XmlResourceParser xmlResourceParser = reflectResource.getResApkXml(identifyResourceName);
        parseAddOnsFromXml(context, reflectResource, xmlResourceParser);
//        Log.e(TAG, xmlResourceParser.toString());
//            Drawable drawable = reflectResource.getResApkDrawable(identifyResourceName);
        return null;
    }

}
