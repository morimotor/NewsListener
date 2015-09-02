package jp.ca.newslistener;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyService extends Service implements TextToSpeech.OnInitListener {

    static final String TAG = "MyService";

    // スレッドを停止するために必要
    private boolean mThreadActive = true;

    private Context mContext = this;

    WifiManager wifiManager;

    boolean first_flag = true;
    String start_time = "";
    String end_time = "";

    String start_time_n = "";
    String end_time_n  = "";

    int nonDetectCount = 0;


    TextToSpeech tts = null;

    // スレッド処理
    private Runnable mTask = new Runnable() {

        int i  = 0;

        @Override
        public void run() {

            // アクティブな間だけ処理
            while (mThreadActive) {

                // ここにサービス処理
                //Log.d(TAG, "run " + i++);

                if(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {

                    // APをスキャン
                    wifiManager.startScan();

                    // スキャン結果を取得
                    List<ScanResult> apList = wifiManager.getScanResults();
                    boolean isFind = false;
                    for(int i = 0; i < apList.size(); i++) {
                        //Log.d(TAG, "SSID:" + apList.get(i).SSID + " " + apList.get(i).frequency + "MHz " + apList.get(i).level + "dBm");
                        ArrayList<String> checkedList = getArray("array");
                        boolean isFindMatching = false;
                        for(int k = 0; k < checkedList.size(); k++){
                            if(apList.get(i).SSID.equals(checkedList.get(k))){
                                isFindMatching = true;
                                break;
                            }
                        }
                        //if(apList.get(i).SSID.equals("LILO505643")){
                        if(isFindMatching){
                            isFind = true;

                            // 現在の時刻を取得
                            Calendar cal = Calendar.getInstance();
                            int year = cal.get(Calendar.YEAR);
                            int month = cal.get(Calendar.MONTH) + 1;
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            int hour = cal.get(Calendar.HOUR_OF_DAY);
                            int minute = cal.get(Calendar.MINUTE);
                            int second = cal.get(Calendar.SECOND);
                            //yyyy-mm-dd hh:mm:ss
                            String date = year + "-" + month + "-" + day + "-" + hour + "-" + minute + "-" + second;
                            //Log.d(TAG, "date:" + date);

                            if(first_flag){
                                start_time = date;
                                start_time_n = month + "/" + String.format("%02d", day) + " " + String.format("%02d", hour) + ":" + String.format("%02d", minute);
                                end_time = start_time;
                                end_time_n = start_time_n;
                                first_flag = false;

                                boolean isChecked = getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("isVoice", false);
                                if(isChecked)
                                {
                                    // 読み上げ開始
                                    /*Bundle b = new Bundle();
                                    b.putInt("KEY_PARAM_VOLUME", 1);
                                    tts.speak("がんばってね", TextToSpeech.QUEUE_ADD, b, "ididid");*/
                                    tts.speak("がんばってね", TextToSpeech.QUEUE_ADD, null);
                                    Log.d(TAG, "run tts1");
                                }

                            }
                            else {
                                end_time = date;
                                end_time_n = month + "/" + String.format("%02d", day) + " " + String.format("%02d", hour) + ":" + String.format("%02d", minute);
                            }

                            showNotification(mContext, 1, Notification.FLAG_NO_CLEAR);




                            Log.d(TAG, "start:" + start_time + " end:" + end_time);
                            nonDetectCount = 0;
                            break;
                        }
                        else if(!start_time.equals(end_time) && nonDetectCount < 2) {
                                nonDetectCount++;
                        }
                        else if(!start_time.equals(end_time) && nonDetectCount == 2){
                            NotificationManager mgr = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

                            Intent intent = new Intent(mContext, MainActivity.class);
                            PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                            // 通知バーの内容
                            Notification n = new NotificationCompat.Builder(mContext)
                                    .setSmallIcon(R.mipmap.ic_launcher)
                                    .setTicker("仕事終了")
                                    .setWhen(System.currentTimeMillis())    // 時間
                                    .setContentTitle(getResources().getString(R.string.app_name))
                                    .setContentText("ニュースを見る")
                                    .setContentIntent(contentIntent)// インテント
                                    .build();
                            n.flags = Notification.FLAG_AUTO_CANCEL;

                            mgr.notify(2, n);

                            stopNotification(mContext, 1);

                            boolean isChecked = getSharedPreferences("setting", Context.MODE_PRIVATE).getBoolean("isVoice", false);
                            if(isChecked)
                            {

                                if (tts.isSpeaking()) {
                                    // 読み上げ中なら止める
                                    tts.stop();
                                }   // 読み上げ開始
                                tts.speak("おつかれさま", TextToSpeech.QUEUE_ADD, null);
                                Log.d(TAG, "run tts2");
                            }

                        }

                    }
                    if(!isFind) {// ない場合
                        first_flag = true;
                    }
                }




                try {
                    Thread.sleep(1 * 60 * 1000); //ms
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //showNotification(mContext, 1, Notification.FLAG_NO_CLEAR);
            }

        }
    };
    private Thread mThread;


    @Override
    public IBinder onBind(Intent intent) {

        Log.d(TAG, "サービスがバインド ");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d(TAG, "onStartCommand");

        this.mThread = new Thread(null, mTask, "APService");
        this.mThread.start();

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "サービスが開始");

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        super.onCreate();

        // 読み上げ
        tts = new TextToSpeech(mContext, this);
        tts.setSpeechRate(1.2f);
        tts.setPitch(1.0f);

    }


    @Override
    public void onDestroy() {
        Log.d(TAG, "サービスが終了");

        // スレッド停止
        this.mThread.interrupt();
        this.mThreadActive = false;

        this.stopNotification(this, 1);
        super.onDestroy();

        tts.stop();
        tts.shutdown();
    }

    // 通知バーを消す
    private void stopNotification(final Context ctx, int id) {
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(id);
    }

    // 通知バーを出す
    private void showNotification(final Context ctx, int id, int type) {

        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(ctx, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // 通知バーの内容
        Notification n = new NotificationCompat.Builder(ctx)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("お仕事開始")
                .setWhen(System.currentTimeMillis())    // 時間
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("お仕事中：" + start_time_n + " ~ " + end_time_n)
                .setContentIntent(contentIntent)// インテント
                .build();
        n.flags = type;

        mgr.notify(id, n);
    }


    private ArrayList<String> getArray(String PrefKey){

        SharedPreferences prefs2 = getSharedPreferences("setting", Context.MODE_PRIVATE);
        String stringItem = prefs2.getString(PrefKey, "");
        return new ArrayList<String>(Arrays.asList(stringItem.split(",")));
    }

    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            Locale locale = Locale.JAPANESE;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);

            } else {
                Log.d(TAG, "Error SetLocale");
                Log.d(TAG, "onInit error setlocal");
            }
        } else {
            Log.d(TAG, "onInit Error");
        }
    }
}

