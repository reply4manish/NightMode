package com.saha.nightmode;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class NightService extends Service {

    LinearLayout mView;

    public static int STATE,NOTI_STATE;

    public static final int INACTIVE=0;
    public static final int ACTIVE=0;
    static int r,g,b,a;

    static{
        STATE=INACTIVE;
        NOTI_STATE = INACTIVE;
    }

    public static int getColor(){
        String hex = String.format("%02x%02x%02x%02x",a,r,g,b);
        return (int)Long.parseLong(hex,16);
    }

    public NightService() {
    }

    @Override
    public void onDestroy() {
        STATE = INACTIVE;
        super.onDestroy();
        if(mView!=null){
            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.removeView(mView);
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll(); //clear notification when service stops
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //load value from sharedpreferences cause service is stopping once removed

        SharedPreferences mPrefs;
        if(r==0 && g==0 &&b==0  && a==0) //all are 0 probably not loaded so load from mPrefs
        {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            r = mPrefs.getInt("red",27);
            g = mPrefs.getInt("green",45);
            b = mPrefs.getInt("blue",53);
            a = mPrefs.getInt("alpha",46);

            Log.e("service",r+"");
        }
        STATE = ACTIVE;
        mView = new LinearLayout(this);
        mView.setBackgroundColor(getColor());

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        WindowManager.LayoutParams params = new  WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        //WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Log.e("asd",r+"");
        windowManager.addView(mView, params);

        createNotification();
    }

    @Override
    public IBinder onBind(Intent i) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        STATE = ACTIVE;
        mView.setBackgroundColor(getColor());
        createNotification();
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        Intent restartService = new Intent(getApplicationContext(),
                this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(), 1, restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() +1000, restartServicePI);
    }

    public void createNotification()
    {
        if(NOTI_STATE==INACTIVE)
        {
            int id=100;
            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "100");
            notification.setContentTitle(getResources().getString(R.string.app_name));
            notification.setContentText(getResources().getString(R.string.app));
            notification.setSmallIcon(R.drawable.logo_noti1);
            notification.setAutoCancel(true)
                        .setOngoing(true);
            notification.setPriority(NotificationCompat.PRIORITY_MAX);

            Intent resultIntent = new Intent(this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            notification.setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id,notification.build());
        }
    }
}
