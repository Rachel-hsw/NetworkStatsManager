package com.example.rachel.networkstatsmanager;

import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;

public class TongJiActivity extends AppCompatActivity {
    private  ApplicationInfo ai;
    private int uid = -1;
    private      NetworkStats summaryStats;
    private TextView hh;
    private TextView ww;
    private Button ss;
    private long summaryTotal = 0;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tongji_layout);
        hasPermissionToReadNetworkStats();
        hh= (TextView) findViewById(R.id.hh);
        ww= (TextView) findViewById(R.id.ww);
        ss= (Button) findViewById(R.id.button);
        ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取subscriberId
                uid=getUidByPackageName(TongJiActivity.this,"com.example.rachel.networkstatsmanager" );
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String subId = tm.getSubscriberId();
                NetworkStatsManager networkStatsManager = (NetworkStatsManager) getSystemService(NETWORK_STATS_SERVICE);
                NetworkStats.Bucket bucket = null;
// 获取到目前为止设备的Wi-Fi流量统计
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        bucket = networkStatsManager.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, subId, 0, System.currentTimeMillis());
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Log.i("Info", "Total: " + (bucket.getRxBytes() + bucket.getTxBytes()));
                    hh.setText((bucket.getRxBytes() + bucket.getTxBytes())+"");
                }



                NetworkStats summaryStats;
                long summaryRx = 0;
                long summaryTx = 0;
                NetworkStats.Bucket summaryBucket = new NetworkStats.Bucket();


                try {
                    summaryStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, subId, getTimesMonthMorning(), System.currentTimeMillis());

                do {
                    summaryStats.getNextBucket(summaryBucket);
                    int summaryUid = summaryBucket.getUid();
                    if (uid == summaryUid) {
                        summaryRx += summaryBucket.getRxBytes();
                        summaryTx += summaryBucket.getTxBytes();
                        Log.i("hsw","hsw"+summaryRx+summaryTx);
                    }
                    summaryTotal += summaryRx + summaryTx;
                } while (summaryStats.hasNextBucket());
                    Log.i(TongJiActivity.class.getSimpleName(),"mainActivity"+summaryTotal);
                    float mb=((float)summaryTotal)/1024/1024;
                    ww.setText("您已经使用"+mb+"MB流量");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    //获得本月第一天0点时间
    public static long getTimesMonthMorning() {
        Calendar cal = Calendar.getInstance();
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }
    private boolean hasPermissionToReadNetworkStats() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }

        requestReadNetworkStats();
        return false;
    }

    public static int getUidByPackageName(Context context, String packageName) {
        int uid = -1;
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);

            uid = packageInfo.applicationInfo.uid;
            Log.i(TongJiActivity.class.getSimpleName(), packageInfo.packageName + " uid:" + uid);


        } catch (PackageManager.NameNotFoundException e) {
        }

        return uid;
    }
    // 打开“有权查看使用情况的应用”页面
    private void requestReadNetworkStats() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
}
