package com.example.amr5aled.chatapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FriendDetails extends AppCompatActivity {

    String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_details);
        phone = getIntent().getStringExtra("phone");
        ((TextView) findViewById(R.id.name)).setText(getIntent().getStringExtra("name"));
        ((TextView) findViewById(R.id.phone)).setText(phone);
        ((TextView) findViewById(R.id.bithdayt)).setText(getIntent().getStringExtra("birth"));
        if (phone.length() == 10)
            phone = "0" + phone;
        viewprofile(phone);
    }

    public void finish_activity(View view) {
        finish();
    }


    private void viewprofile(String phone) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage_url));
        StorageReference islandRef = storageRef.child("profile" + phone);
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ((ImageView) findViewById(R.id.profile)).setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(getApplicationContext(), R.string.cant_view_profile, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
