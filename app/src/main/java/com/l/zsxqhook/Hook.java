package com.l.zsxqhook;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {

    public static String URLDecoderString(String str) {
        String result = "";
        if (null == str) {
            return "";
        }
        try {
            result = java.net.URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.unnoo.quan")) {
            return;
        }

        final ClassLoader classLoader = lpparam.classLoader;

        if (classLoader == null) {
            log("classloader null");
            return;
        }
        final Class<?> topicPreviewImplClass = classLoader.loadClass("com.unnoo.quan.views.TopicPreviewImpl");
        final Class<?> cClass = classLoader.loadClass("com.unnoo.quan.x.c");
        if (topicPreviewImplClass != null) {
            XposedHelpers.findAndHookMethod(topicPreviewImplClass, "a", cClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);

//                    Method iMethod = param.args[0].getClass().getMethod("i");
//                    SpannableStringBuilder iSpannableStringBuilder = (SpannableStringBuilder) iMethod.invoke(param.args[0], new Object[]{});
//                    if (iSpannableStringBuilder != null) {
//                        log("i:" + iSpannableStringBuilder.toString());
//                    } else {
//                        log("i:null");
//                    }
//
//                    Method fMethod = param.args[0].getClass().getMethod("f");
//                    SpannableStringBuilder fSpannableStringBuilder = (SpannableStringBuilder) fMethod.invoke(param.args[0], new Object[]{});
//                    if (fSpannableStringBuilder != null) {
//                        log("f:" + fSpannableStringBuilder.toString());
//                    } else {
//                        log("f:null");
//                    }

//                    Field jField = param.args[0].getClass().getDeclaredField("j");
//                    jField.setAccessible(true);
//                    SpannableStringBuilder jSpannableStringBuilder = (SpannableStringBuilder) jField.get(param.args[0]);
//                    if (jSpannableStringBuilder != null) {
//                        log("i:" + jSpannableStringBuilder.toString());
//                    } else {
//                        log("i:null");
//                    }
//
//                    Field gField = param.args[0].getClass().getDeclaredField("g");
//                    gField.setAccessible(true);
//                    SpannableStringBuilder gSpannableStringBuilder = (SpannableStringBuilder) gField.get(param.args[0]);
//                    if (gSpannableStringBuilder != null) {
//                        log("i:" + gSpannableStringBuilder.toString());
//                    } else {
//                        log("i:null");
//                    }

//                    Method toStringMethod = param.args[0].getClass().getMethod("toString");
//                    String toString = (String) toStringMethod.invoke(param.args[0], new Object[]{});
//                    if (toString != null) {
//                        log("toString:" + toString);
//                    } else {
//                        log("toString:null");
//                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);

                    Method toStringMethod = param.args[0].getClass().getMethod("toString");
                    String toString = (String) toStringMethod.invoke(param.args[0], new Object[]{});
                    if (toString != null) {
                        String[] split = toString.split(",");
                        boolean isFound = false;
                        String targetStr = "";
                        for (String str : split) {
                            //log(str);
                            if (str.contains("text=(")) {
                                isFound = true;
                                continue;
                            }
                            if (isFound) {
                                isFound = false;
                                targetStr = str;
                                //log("============"+targetStr);
                                targetStr = URLDecoderString(targetStr);
                                //log("============"+targetStr);
                            }
                        }

                        Field mTextViewField = topicPreviewImplClass.getDeclaredField("mTextView");
                        mTextViewField.setAccessible(true);
                        TextView mTextView = (TextView) mTextViewField.get(param.thisObject);
                        if (!TextUtils.isEmpty(targetStr)) {
                            mTextView.setText(targetStr);
                        }

                    } else {
                        log("toString:null");
                    }
                }
            });

            XposedHelpers.findAndHookMethod(topicPreviewImplClass, "a", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    param.args[0] = false;
                }
            });

            XposedHelpers.findAndHookMethod(topicPreviewImplClass, "onInterceptTouchEvent", MotionEvent.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Field aField = topicPreviewImplClass.getDeclaredField("a");
                    aField.setAccessible(true);
                    boolean is = (boolean) aField.get(param.thisObject);
                    if (is) {
                        aField.setBoolean(param.thisObject, false);
                    }
                }
            });

            XposedHelpers.findAndHookMethod(topicPreviewImplClass, "a", Context.class, AttributeSet.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    log("hook a success");

                    Field mTopicContentField = topicPreviewImplClass.getDeclaredField("mTopicContent");
                    mTopicContentField.setAccessible(true);
                    RelativeLayout relativeLayout = (RelativeLayout) mTopicContentField.get(param.thisObject);
                    relativeLayout.setClickable(true);


                    Field mTextViewField = topicPreviewImplClass.getDeclaredField("mTextView");
                    mTextViewField.setAccessible(true);
                    final TextView mTextView = (TextView) mTextViewField.get(param.thisObject);

                    String string = mTextView.getText().toString();
                    log(string);

                    mTextView.setEnabled(true);
                    mTextView.setClickable(true);
                    mTextView.setLongClickable(true);
                    mTextView.setFocusable(true);
                    mTextView.setTextIsSelectable(true);

                    mTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String string = mTextView.getText().toString();
                            log(string);
                            if (!TextUtils.isEmpty(string)) {
                                Pattern pattern = Patterns.WEB_URL;
                                Matcher matcher = pattern.matcher(string);
                                if (matcher.find()) {
                                    String link = matcher.group(0);
                                    log(link);

                                    try {
                                        Class<?> aClass = classLoader.loadClass("com.unnoo.quan.activities.TSBrowserActivity");
                                        XposedHelpers.callStaticMethod(aClass, "start", mTextView.getContext(), link);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private void log(String text) {
        Log.e("zsxqhook", text);
    }
}
