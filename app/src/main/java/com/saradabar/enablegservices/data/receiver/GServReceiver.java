/* Output File Code List
 * CODE 0 : Success
 * CODE 1 : Failed to bind to DchaService
 * CODE 2 : Exception Occurred
 *  */

package com.saradabar.enablegservices.data.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;

import com.saradabar.enablegservices.util.PackagesXmlUtil;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import jp.co.benesse.dcha.dchaservice.IDchaService;

public class GServReceiver extends BroadcastReceiver {

    IDchaService mDchaService;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!context.getApplicationContext().bindService(new Intent("jp.co.benesse.dcha.dchaservice.DchaService").setPackage("jp.co.benesse.dcha.dchaservice"),
                new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                        mDchaService = IDchaService.Stub.asInterface(iBinder);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                    }
                }, Context.BIND_AUTO_CREATE)) {
            writeToFile("1");
            return;
        }

        Runnable runnable = () -> {
            try {
                tryEnableGService();
                writeToFile("0");
            } catch (Exception e) {
                writeToFile(e.toString());
            }
        };
        new Handler().postDelayed(runnable, 1000);
    }

    void tryEnableGService() throws Exception {
        PackagesXmlUtil pxu = PackagesXmlUtil.inputFromSystem(mDchaService);
        pxu.grantPermission("com.google.uid.shared", "android.permission.CREATE_USERS");
        pxu.outputToSystem(mDchaService);
    }

    private void writeToFile(String str) {
        try {
            FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/com.saradabar.enablegservices.txt");
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            switch (str) {
                case "0":
                    str = str + "\nRESULT: Success";
                    break;
                case "1":
                    str = str + "\nRESULT: Failed to bind to DchaService";
                    break;
                default:
                    str = "2" + "\nRESULT: " + str;
                    break;
            }
            bw.write(str);
            bw.flush();
            bw.close();
        } catch (Exception ignored) {
        }
    }
}