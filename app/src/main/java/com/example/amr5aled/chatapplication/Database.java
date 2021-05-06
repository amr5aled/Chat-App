package com.example.amr5aled.chatapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import com.example.amr5aled.chatapplication.data.Message;
import com.example.amr5aled.chatapplication.data.User;

public class Database extends SQLiteOpenHelper {


    public Database(Context context) {
        super(context, "Database", null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE FRIENDS ( phone VARCHAR(11) PRIMARY KEY, name VARCHAR(50) , statues VARCHAR(30) , birth VARCHAR(30))");
        db.execSQL("CREATE TABLE MESSAGES ( phone VARCHAR(11) , message VARCHAR(50) , sender VARCHAR(30) ,statues VARCHAR(30) )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS FRIENDS ");
        db.execSQL("DROP TABLE IF EXISTS MESSAGES ");
        onCreate(db);
    }

    public long insert_friend(String phone, String name, String stat, String birthday) {
        SQLiteDatabase sqLitewrite = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("name", name);
        contentValues.put("statues", stat);
        contentValues.put("birth", birthday);
        long x = sqLitewrite.insert("FRIENDS", null, contentValues);
        sqLitewrite.close();
        return x;
    }

    public long insert_message(String phone, String message, String sender, String stat) {
        SQLiteDatabase sqLitewrite = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone", phone);
        contentValues.put("message", message);
        contentValues.put("sender", sender);
        contentValues.put("statues", stat);
        long x = sqLitewrite.insert("MESSAGES", null, contentValues);
        sqLitewrite.close();
        return x;

    }

    public ArrayList restore_message(String phone) {
        ArrayList<Message> array_values = new ArrayList<>();
        SQLiteDatabase sql = this.getReadableDatabase();
        Cursor cursor = sql.rawQuery("SELECT * FROM MESSAGES WHERE phone = ? ", new String[]{phone});
        //cursor.moveToFirst();
        Log.d("Database ", " start loop ");
        Log.d("Database","size: " + cursor.getCount());
        while (cursor.moveToNext()) {

            Log.d("Database","Mess: " + cursor.getString(cursor.getColumnIndex("message")));
            Log.d("Database","Sender: " + cursor.getString(cursor.getColumnIndex("sender")));
            Log.d("Database","Statuses: " + cursor.getString(cursor.getColumnIndex("statues")));


            array_values.add(new Message(cursor.getString(cursor.getColumnIndex("message")), cursor.getString(cursor.getColumnIndex("sender"))));
            MessageAdapter.last_message_statues = cursor.getString(cursor.getColumnIndex("statues"));

        }

        return array_values;
    }

    public ArrayList restore_friends() {
        ArrayList<User> array_values = new ArrayList<>();
        SQLiteDatabase sql = this.getReadableDatabase();
        Cursor cursor = sql.rawQuery(" SELECT * FROM FRIENDS ", null);
        cursor.moveToFirst();
        while (cursor.isAfterLast() == false) {
            array_values.add(new User(cursor.getString(cursor.getColumnIndex("name")), cursor.getString(cursor.getColumnIndex("phone")), cursor.getString(cursor.getColumnIndex("statues")), cursor.getString(cursor.getColumnIndex("birth"))));
            cursor.moveToNext();
        }
        return array_values;
    }

    public void delete_friend(String nextphone) {
        if (nextphone.length() == 10)
            nextphone = "0" + nextphone;
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete("FRIENDS", "phone=?", new String[]{nextphone});
        sqLiteDatabase.delete("MESSAGES", "phone=?", new String[]{nextphone});
    }

}