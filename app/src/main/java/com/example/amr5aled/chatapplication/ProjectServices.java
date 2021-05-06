package com.example.amr5aled.chatapplication;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.example.amr5aled.chatapplication.data.Message;


public class ProjectServices extends IntentService {

    public static boolean service_started = false;
    static boolean main_activity_started = false;
    static boolean message_activity_started = false;
    public static Context context;
    public static Message last_message = new Message("non", "non");
    static int seconds = -1, minute = -1, hour = -1;
    static RealTimeDatabase realtime_database;
    static SharedPreferences.Editor editor;
    public static SharedPreferences sharedPreferences;
    static Database db;


    public ProjectServices() {
        super("ProjectServices");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        service_started = true;
        db = new Database(context);
        Log.d("", "service_started" + service_started);
    }

    public static void set_context(Context context1) {
        context = context1;
        sharedPreferences = context.getSharedPreferences("logged", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        realtime_database = new RealTimeDatabase(context);
    }

    public static void block(String phone) {
        if (isNetworkAvailable())
            realtime_database.block(phone);
        else
            Toast.makeText(context, R.string.no_conn, Toast.LENGTH_SHORT).show();
    }

    public static void Monitor_request() {
        if (isNetworkAvailable())
            realtime_database.get_messages_requests();
        else
            Toast.makeText(context, R.string.no_conn, Toast.LENGTH_SHORT).show();
    }

    public static void Send_request(String name, String phone, String statues, String birth) {
        if (isNetworkAvailable())
            realtime_database.send_message_request(name, phone, statues, birth);
        else
            Toast.makeText(context, R.string.no_conn, Toast.LENGTH_SHORT).show();
        Log.d("", "send_request" + phone);
    }

    public static void read_message(String phone) {
        if (isNetworkAvailable())
            realtime_database.read_message(phone, "");
        else
            Toast.makeText(context, R.string.no_conn, Toast.LENGTH_SHORT).show();
    }

    public static void writeMessage(String text, String phone, String birth, String username) {
        if (isNetworkAvailable()) {
            Log.d("DEBUG Service","Write Message");
            realtime_database.writeMessage(text, phone, birth);
            editor.putString("last_message", text);
            editor.putString("last_name", username);
            editor.commit();
        } else
            Toast.makeText(context, R.string.no_conn, Toast.LENGTH_SHORT).show();
    }

    public static String get_name_widgets() {
        return sharedPreferences.getString("last_name", "no com.example.amr5aled.chatapplication.data");
    }

    public static String get_message_widgets() {
        return sharedPreferences.getString("last_message", "no com.example.amr5aled.chatapplication.data");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        service_started = false;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }

    private static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
