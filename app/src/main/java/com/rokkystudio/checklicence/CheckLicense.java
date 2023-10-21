package com.rokkystudio.checklicence;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rokkystudio.checklicence.google.AESObfuscator;
import com.rokkystudio.checklicence.google.LicenseChecker;
import com.rokkystudio.checklicence.google.LicenseCheckerCallback;
import com.rokkystudio.checklicence.google.ServerManagedPolicy;

import java.util.Calendar;

// TODO Сделать отдельную активити для проверки лицензии
// Сделать генератор кодов с проверкой онлайн по номеру устройства
public class CheckLicense
{
    private final Context mContext;
    private String mKey;
    private byte[] mSalt;
    private String mPhone;

    private int mDelay = 0; // ms

    public CheckLicense(@NonNull Context context) {
        mContext = context;
    }

    public void setKey(String key, byte[] salt) {
        mKey = key; mSalt = salt;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public void setDelay(int minutes) {
        mDelay = minutes * 60 * 1000;
    }

    @SuppressLint("HardwareIds")
    public void check()
    {
        // Выполняем только через заданное время после установки приложения
        if ((getCurrentTime() - getInstallTime()) < mDelay) return;

        String deviceID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        AESObfuscator obfuscator = new AESObfuscator(mSalt, mContext.getPackageName(), deviceID);
        ServerManagedPolicy policy = new ServerManagedPolicy(mContext, obfuscator);
        LicenseChecker checker = new LicenseChecker(mContext, policy, mKey);
        checker.checkAccess(new LicenseCallback());
    }

    private long getInstallTime()
    {
        long time = 0;
        try {
            time = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).firstInstallTime;
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return time;
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void showLicenseDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(R.string.license_buy_title);
        builder.setMessage(R.string.license_buy_message);

        // Не важно какую версию скачал юзер, если у него проблемы с лицензией, пусть покупает ее напрямую через ватсаб
        /* ДЛЯ ПРОДАЖ В GOOGLE PLAY
        builder.setPositiveButton(mActivity.getResources().getString(R.string.license_buy_button), (dialog, which) -> {
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                "http://market.android.com/details?id=" + mActivity.getPackageName()));
            mActivity.startActivity(marketIntent);
            mActivity.finish();
        });
        */

        // ДЛЯ ПРОДАЖ В WHATSAPP
        builder.setPositiveButton(R.string.license_contact_button, (dialog, which) -> {
            String url = "https://api.whatsapp.com/send?phone=" + mPhone;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            mContext.startActivity(intent);
            System.exit(0);
        });

        builder.setNegativeButton(R.string.license_exit_button, (dialog, which) -> System.exit(0));

        builder.setCancelable(false);

        AlertDialog alert = builder.create();
        alert.show();

        TextView messageView = alert.findViewById(android.R.id.message);
        messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
    }

    private class LicenseCallback implements LicenseCheckerCallback
    {
        @Override
        public void allow(int reason) {
            // License Accepted!
        }

        @Override
        public void dontAllow(int reason) {
            // License Denied!
            showLicenseDialog();
        }

        @Override
        public void applicationError(int reason) {
            // License Error (отсутствует Google Play или нет соединения с интернет)
            showLicenseDialog();
        }
    }
}
