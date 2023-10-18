package com.saradabar.enablegservices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.ScrollView;
import android.widget.TextView;

import com.saradabar.enablegservices.util.PackagesXmlUtil;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class MainActivity extends Activity {

    IDchaService mDchaService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (!bindService(new Intent("jp.co.benesse.dcha.dchaservice.DchaService").setPackage("jp.co.benesse.dcha.dchaservice"),
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                    mDchaService = IDchaService.Stub.asInterface(iBinder);
                }

                @Override
                public void onServiceDisconnected(ComponentName componentName) {
                }
            }, Context.BIND_AUTO_CREATE)) {
            addText("RESULT: Failed to bind to DchaService");
            return;
        }

        Runnable runnable = () -> {
            try {
                tryEnableGService();
                addText("RESULT: Success");
                new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage("Do you want to restart now?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        try {
                            mDchaService.rebootPad(0, null);
                        } catch (Exception ignored) {
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            } catch (Exception e) {
                addText("RESULT: ");
                addText(e.toString());
            }
        };
        new Handler().postDelayed(runnable, 1000);
    }

    void tryEnableGService() throws Exception {
        PackagesXmlUtil pxu = PackagesXmlUtil.inputFromSystem(mDchaService);
        pxu.grantPermission("com.google.uid.shared", "android.permission.CREATE_USERS");
        pxu.outputToSystem(mDchaService);
    }

    void addText(String str) {
        TextView textView = findViewById(R.id.logger);
        textView.append(str + "\n");
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.smoothScrollBy(0, textView.getBottom() + scrollView.getPaddingBottom() - (scrollView.getScrollY() + scrollView.getHeight()));
    }

    @Override
    public void onPause() {
        super.onPause();
        finishAndRemoveTask();
    }
}