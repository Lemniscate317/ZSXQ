package com.l.zsxqhook;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Hook implements IXposedHookLoadPackage {
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
        if (topicPreviewImplClass != null) {
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
