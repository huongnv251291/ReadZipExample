package com.example.readzipexample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class ReflectResource {
    private Resources resources;
    private String str;

//    public Context mContext;

    public ReflectResource( Resources resources, String str) {
//        this.mContext = externalPackageContext;
        this.resources = resources;
        this.str = str;
    }

    public int getXmlId(String str) {
        return this.resources.getIdentifier(str, "xml", this.str);
    }

    public XmlResourceParser getResApkXml(String str) {
        return this.resources.getXml(getXmlId(str));
    }

    public int getResApkLayoutId(String str) {
        return this.resources.getIdentifier(str, "layout", this.str);
    }

    @SuppressLint("WrongConstant")
    public View getResApkLayoutView(Context context, String str) {
        return ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(this.resources.getLayout(getResApkLayoutId(str)), null);
    }

    public int getResApkViewID(String str) {
        return this.resources.getIdentifier(str, "id", this.str);
    }

    public View getResApkView(View view, String str) {
        return view.findViewById(getResApkViewID(str));
    }

    public int getDrawableId(String str) {
        return this.resources.getIdentifier(str, "drawable", this.str);
    }

    public Drawable getResApkDrawable(String str) {
        return this.resources.getDrawable(getDrawableId(str));
    }

    public int getResApkStringId(String str) {
        return this.resources.getIdentifier(str, "string", this.str);
    }

    public String getResApkString(String str) {
        return this.resources.getString(getResApkStringId(str));
    }

    public int getResApkAnimId(String str) {
        return this.resources.getIdentifier(str, "anim", this.str);
    }

    public XmlPullParser getResApkAnimXml(String str) {
        return this.resources.getAnimation(getResApkAnimId(str));
    }

    public Animation getResApkAnim(Context context, String str) {
        Animation animation = null;
        XmlPullParser resApkAnimXml = getResApkAnimXml(str);
        try {
            return a(context, resApkAnimXml, null, Xml.asAttributeSet(resApkAnimXml));
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return animation;
        } catch (IOException e2) {
            e2.printStackTrace();
            return animation;
        }
    }

    private Animation a(Context context, XmlPullParser xmlPullParser, AnimationSet animationSet, AttributeSet attributeSet) throws XmlPullParserException, IOException {
        Animation animation = null;
        int depth = xmlPullParser.getDepth();
        while (true) {
            int next = xmlPullParser.next();
            if ((next != 3 || xmlPullParser.getDepth() > depth) && next != 1) {
                if (next == 2) {
                    String name = xmlPullParser.getName();
//                    if (name.equals(Wallpaper3dConstants.TAG_SET)) {
//                        Animation animationSet2 = new AnimationSet(context, attributeSet);
//                        a(context, xmlPullParser, (AnimationSet) animationSet2, attributeSet);
//                        animation = animationSet2;
//                    }
//                    else if (name.equals("alpha")) {
//                        animation = new AlphaAnimation(context, attributeSet);
//                    } else if (name.equals(Wallpaper3dConstants.TAG_SCALE)) {
//                        animation = new ScaleAnimation(context, attributeSet);
//                    } else if (name.equals(Wallpaper3dConstants.TAG_ROTATE)) {
//                        animation = new RotateAnimation(context, attributeSet);
//                    } else if (name.equals("translate")) {
                    animation = new TranslateAnimation(context, attributeSet);
//                    }
//                    else
//                        {
//                        throw new RuntimeException("Unknown animation name: " + xmlPullParser.getName());
//                    }
                    if (animationSet != null) {
                        animationSet.addAnimation(animation);
                    }
                }
            }
        }
    }

    public int getResApkColorId(String str) {
        return this.resources.getIdentifier(str, "color", this.str);
    }

    public int getResApkColor(String str) {
        return this.resources.getColor(getResApkColorId(str));
    }

    public int getResApkDimensId(String str) {
        return this.resources.getIdentifier(str, "dimen", this.str);
    }

    public float getResApkDimens(String str) {
        return this.resources.getDimension(getResApkDimensId(str));
    }

    public AssetManager getAssets() {
        return this.resources.getAssets();
    }


    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }
}
