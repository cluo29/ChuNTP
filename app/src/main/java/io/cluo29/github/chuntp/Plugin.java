package io.cluo29.github.chuntp;

/**
 * Created by CLUO29 on 26/04/17.
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import io.cluo29.github.chuntp.TimeResult_Provider.Time_Result;

public class Plugin extends Service
{
    public static int temp_interval = 100;
    private static String sDrift;

    public NtpAlarm alarm = new NtpAlarm();

    @Override
    public void onCreate() {
        super.onCreate();

        getDrift(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("NTP","Plugin 40");
        int interval =  1;
        if(interval!=temp_interval){
            temp_interval = interval;
            alarm.CancelAlarm(Plugin.this);
            alarm.SetAlarm(Plugin.this, interval);
            Log.d("NTP","Plugin 46");
        }

        return START_STICKY;
    }


    protected static void getDrift(Context context) {
        Intent NTPtime_Service = new Intent(context, ntptime_Service.class);
        context.startService(NTPtime_Service);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alarm.CancelAlarm(Plugin.this);
    }

    public static class ntptime_Service extends IntentService {
        public ntptime_Service() {
            super("AWARE NTPtime");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            SntpClient client = new SntpClient();
            String server = "time.nist.gov";

            if (client.requestTime(server, 5000)) {

                long ntpTime = client.getNtpTime();
                long offset = client.getClockOffset();

                ContentValues time_data = new ContentValues();
                Log.d("NTP","local= "+System.currentTimeMillis());
                Log.d("NTP","ntp= "+ntpTime);
                Log.d("NTP","offset= "+offset);


                time_data.put(Time_Result.TIMESTAMP, System.currentTimeMillis());
                time_data.put(Time_Result.DRIFT, offset);
                time_data.put(Time_Result.NTP_TIME, ntpTime);

                getContentResolver().insert(Time_Result.CONTENT_URI, time_data);

                sDrift = new BigDecimal(offset).toPlainString();

            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

}
