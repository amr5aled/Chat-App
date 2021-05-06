package com.example.amr5aled.chatapplication;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.example.amr5aled.chatapplication.data.User;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static android.content.Context.NOTIFICATION_SERVICE;

public class RealTimeDatabase {
    Context context;
    final String messages_node = "messages";
    final String request_message_node = "requests";
    private DatabaseReference mDatabase;
    String myphone;
    String users_node = "users";
    public static Database db;
    public static boolean just_played = false;
    int loop_index = 0;
    boolean access = true;

    public RealTimeDatabase(Context context) {
        this.context = context;
        myphone = context.getSharedPreferences("logged", Context.MODE_PRIVATE).getString("phone", "-1");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        db = new Database(context);
        SQLiteDatabase sqLiteDatabase = db.getWritableDatabase();

    }

    public void read_message(final String nextphone, final String birth) {
        try {

            mDatabase.child(messages_node).child(detemined_node(nextphone)).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot != null) {
                        String statues = "non";
                        Iterator i = dataSnapshot.getChildren().iterator();
                        while (i.hasNext()) {
                            try {
                                String body = (String) ((DataSnapshot) i.next()).getValue();
                                String sender = (String) ((DataSnapshot) i.next()).getValue();
                                statues = (String) ((DataSnapshot) i.next()).getValue();
                                Date d = new Date();
                                if (!sender.equals(myphone) && !(statues.equals("seen") || statues.equals("rec"))) {
                                    if (ProjectServices.hour == d.getHours())
                                        if (ProjectServices.minute == d.getMinutes())
                                            if (ProjectServices.seconds <= d.getSeconds() + 1 && ProjectServices.seconds >= d.getSeconds() - 1)
                                                if (ProjectServices.last_message.getSender().equalsIgnoreCase(sender))
                                                    if (ProjectServices.last_message.getBody().equals(body))
                                                        return;
                                    ProjectServices.last_message.setBody(body);
                                    ProjectServices.last_message.setSender(sender);
                                    ProjectServices.seconds = d.getSeconds();
                                    ProjectServices.minute = d.getMinutes();
                                    ProjectServices.hour = d.getHours();
                                    db.insert_message(nextphone, body, sender, statues);
                                    if (ProjectServices.message_activity_started) {
                                        mDatabase.child(messages_node).child(detemined_node(nextphone)).child(dataSnapshot.getKey()).child("statues").setValue("seen");
                                        send_message_request(context.getSharedPreferences("logged", Context.MODE_PRIVATE).getString("username", ""), "0" + nextphone, "seen", birth);
                                    } else if (!ProjectServices.message_activity_started) {
                                        mDatabase.child(messages_node).child(detemined_node(nextphone)).child(dataSnapshot.getKey()).child("statues").setValue("rec");
                                        send_message_request(context.getSharedPreferences("logged", Context.MODE_PRIVATE).getString("username", ""), "0" + nextphone, "rec", birth);
                                    }
                                }

                            } catch (Exception e) {
                                FirebaseCrash.report(e);
                            }
                        }
                        if (ProjectServices.message_activity_started) {
                            MessageActivity.list_message.clear();
                            MessageActivity.list_message = db.restore_message(nextphone);
                            MessageActivity.listView.setAdapter(new MessageAdapter(context));
                        } else if (!statues.equals("block"))
                            push_notification("new MessageActivity", nextphone);
                    }
                }

                @Override
                public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot != null) {
                        final Iterator i = dataSnapshot.getChildren().iterator();
                        while (i.hasNext()) {
                            String body = "non", sender = myphone, statues = "send";
                            try {
                                body = (String) ((DataSnapshot) i.next()).getValue();
                                sender = (String) ((DataSnapshot) i.next()).getValue();
                                statues = (String) ((DataSnapshot) i.next()).getValue();
                                {
                                    if (sender.equals(myphone) && statues.equals("seen")) {
                                        dataSnapshot.getRef().removeValue();
                                    }
                                    MessageAdapter.last_message_statues = statues;
                                    MessageActivity.listView.setAdapter(new MessageAdapter(context));
                                }
                            } catch (Exception e) {
                                FirebaseCrash.report(e);
                            }
                        }
                    }
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } catch (Exception e) {
            FirebaseCrash.report(e);
        }
        if (ProjectServices.message_activity_started) {
            MessageActivity.list_message.clear();
            MessageActivity.list_message = db.restore_message(nextphone);
            MessageActivity.listView.setAdapter(new MessageAdapter(context));
        } else
            push_notification("new MessageActivity", nextphone);
    }

    public void block(String phone) {
        if (phone.length() == 10)
            phone = "0" + phone;
        else if (phone.length() == 11)
            phone = phone;
        else return;
        mDatabase.child(request_message_node).child("-" + myphone).child(phone).child("statues").setValue("block");
        db.delete_friend(phone);
    }

    public boolean new_user_register(String name, String phone, String birthday) {
        User user = new User(name, phone, "reg", birthday);
        mDatabase.child("users").child(phone).setValue(user);
        SharedPreferences sharedPreferences = context.getSharedPreferences("logged", Context.MODE_APPEND);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("logged", true);
        editor.putString("phone", phone);
        editor.commit();
        return true;
    }

    public void writeMessage(final String data, final String nextphone, String birth) {
        String node = detemined_node(nextphone);
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            String temp_key = mDatabase.child(messages_node).child(node).push().getKey();
            mDatabase.child(messages_node).child(node).updateChildren(map);
            final DatabaseReference message_root = mDatabase.child(messages_node).child(node).child(temp_key);
            final Map<String, Object> map2 = new HashMap<String, Object>();
            map2.put("MessageActivity", data);
            map2.put("sender", myphone);
            map2.put("statues", "send");
            send_message_request(context.getSharedPreferences("logged", Context.MODE_PRIVATE).getString("username", ""), nextphone, "send", birth);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (access) {
                        message_root.updateChildren(map2);
                        db.insert_message(nextphone, data, myphone, "send");
                    }
                }
            }, 700);

        } catch (Exception e) {
            FirebaseCrash.report(e);
        }
    }

    public void get_my_contacts() {
        try {
            Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                MainActivity.users.add(new User(name, phoneNumber.replace("+2", "").replace("\\s", ""), "non", ""));
            }
            phones.close();
        } catch (Exception e) {
            FirebaseCrash.report(e);
        }
        MainActivity.listview.setAdapter(new ListAdapter(context, MainActivity.users));
    }

    public boolean send_message_request(final String name, final String nextphone, final String statues, final String birth) {
        mDatabase.child(request_message_node).child("-" + nextphone).child(myphone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        try {
                            String birth = (String) ((DataSnapshot) i.next()).getValue();
                            String name = (String) ((DataSnapshot) i.next()).getValue();
                            String sender = (String) ((DataSnapshot) i.next()).getValue();
                            String statues = (String) ((DataSnapshot) i.next()).getValue();
                            if (statues.equals("block")) {
                                Toast.makeText(context, R.string.blocked_text, Toast.LENGTH_SHORT).show();
                                set_access(false);
                            } else {
                                sendRequest(name, nextphone, statues, birth);

                            }
                        } catch (Exception e) {
                            FirebaseCrash.report(e);
                        }
                    }
                } else {
                    sendRequest(name, nextphone, statues, birth);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        return get_access();
    }

    public void set_access(boolean access) {
        this.access = access;
    }

    public boolean get_access() {
        return access;
    }

    public void sendRequest(final String name, final String nextphone, final String statues, final String birth) {
        String temp = null;
        String temp_next_phone = null;
        if (nextphone.length() == 11)
            temp_next_phone = "-" + nextphone;
        else if (nextphone.length() == 10)
            temp_next_phone = "-0" + nextphone;
        else
            return;
        if (myphone.length() == 11)
            temp = myphone;
        else if (myphone.length() == 10)
            temp = "0" + myphone;
        else return;
        set_access(true);
        Map<String, Object> map = new HashMap<String, Object>();
        mDatabase.child(request_message_node).child(temp_next_phone).updateChildren(map);
        DatabaseReference message_root = mDatabase.child(request_message_node).child(temp_next_phone).child(temp);
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("name", name);
        map2.put("phone", temp);
        map2.put("statues", statues);
        map2.put("birth", birth);
        message_root.updateChildren(map2);
    }

    public void get_messages_requests() {
        MainActivity.users.clear();
        String temp = null;
        if (myphone.length() == 11)
            temp = myphone;
        else if (myphone.length() == 10)
            temp = "0" + myphone;

        mDatabase.child(request_message_node).child("-" + temp).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        try {
                            String birth = (String) ((DataSnapshot) i.next()).getValue();
                            String name = (String) ((DataSnapshot) i.next()).getValue();
                            String sender = (String) ((DataSnapshot) i.next()).getValue();
                            String statues = (String) ((DataSnapshot) i.next()).getValue();
                            if (statues.equals("block"))
                                db.delete_friend(sender);
                            else
                                db.insert_friend(sender, name, statues, birth);
                        } catch (Exception e) {
                            FirebaseCrash.report(e);
                        }
                    }
                }
                if (ProjectServices.main_activity_started) {
                    MainActivity.users = db.restore_friends();
                    MainActivity.listview.setAdapter(new ListAdapter(context, MainActivity.users));
                } else
                    push_notification("new MessageActivity request", "");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String st = "";
                if (dataSnapshot != null) {
                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        try {
                            String birth = (String) ((DataSnapshot) i.next()).getValue();
                            String name = (String) ((DataSnapshot) i.next()).getValue();
                            String sender = (String) ((DataSnapshot) i.next()).getValue();
                            String statues = (String) ((DataSnapshot) i.next()).getValue();
                            if (statues.equals("block"))
                                db.delete_friend(sender);
                            else {
                                db.insert_friend(sender, name, statues, birth);
                                push_notification("accept new MessageActivity request", "");
                            }
                            st = statues;
                        } catch (Exception e) {
                            FirebaseCrash.report(e);
                        }
                    }
                }
                if (ProjectServices.main_activity_started) {
                    MainActivity.users = db.restore_friends();
                    MainActivity.listview.setAdapter(new ListAdapter(context, MainActivity.users));
                } else {
                    if (st.equals("add"))
                        push_notification("accept new MessageActivity request", "");

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        if (ProjectServices.main_activity_started) {
            MainActivity.users = db.restore_friends();
            MainActivity.listview.setAdapter(new ListAdapter(context, MainActivity.users));
        }
    }

    public void push_notification(String title, String body) {
        if (!just_played) {
            just_played = true;
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setAutoCancel(false);
            builder.setSmallIcon(R.drawable.message)
                    .setTicker(body)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(title)
                    .setSound(notification)
                    .setContentText(body);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(pendingIntent);
            NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            nm.notify(1000, builder.build());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    just_played = false;
                }
            }, 100);
        }
    }

    public String detemined_node(String nextphone) {

        try {
            String temp = null;
            if (Integer.parseInt(myphone.replace("+2", "").trim()) > Integer.parseInt(nextphone.replace("+2", "").trim())) {
                if (nextphone.length() == 11)
                    nextphone = "-" + nextphone;
                else if (nextphone.length() == 10)
                    nextphone = "-0" + nextphone;
                if (myphone.length() == 11)
                    temp = myphone;
                else if (myphone.length() == 10)
                    temp = "0" + myphone;
                return nextphone + temp;
            } else {
                if (myphone.length() == 11)
                    temp = "-" + myphone;
                else if (myphone.length() == 10)
                    temp = "-0" + myphone;
                if (nextphone.length() == 11)
                    nextphone = nextphone;
                else if (nextphone.length() == 10)
                    nextphone = "0" + nextphone;
                return temp + nextphone;
            }
        } catch (Exception e) {
            FirebaseCrash.report(e);
            return null;
        }
    }


}