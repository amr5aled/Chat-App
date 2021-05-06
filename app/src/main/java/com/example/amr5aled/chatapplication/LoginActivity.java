package com.example.amr5aled.chatapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;


public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView name;
    private EditText phone, birthday;
    private DatabaseReference mDatabase;
    private SharedPreferences sharedPreferences;
    String mPhoneNumber = null;
    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;
    private String path;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        name = (AutoCompleteTextView) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);
        birthday = (EditText) findViewById(R.id.birthday);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        storage = FirebaseStorage.getInstance();
        sharedPreferences = getSharedPreferences("logged", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("logged", false) || !sharedPreferences.getString("phone", "-1").equals("-1")) {
            phone.setText(sharedPreferences.getString("phone", "-1"));
            name.setText(sharedPreferences.getString("username", "-1"));
            birthday.setText(sharedPreferences.getString("birth", "-1"));
            phone.setEnabled(false);
            mEmailSignInButton.setText(R.string.save_change);
            viewprofile();
        } else {
            try {
                TelephonyManager tMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                mPhoneNumber = tMgr.getLine1Number();
                phone.setText(mPhoneNumber);
            } catch (Exception e) {
                FirebaseCrash.report(e);
            }
        }
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getdata();
            }
        });
    }

    private void viewprofile() {
        StorageReference storageRef = storage.getReferenceFromUrl(getString(R.string.firebase_storage_url));
        StorageReference islandRef = storageRef.child("profile" + sharedPreferences.getString("phone", "-1"));
        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ((ImageView) findViewById(R.id.image_profile)).setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Toast.makeText(LoginActivity.this, R.string.network_probelm, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void getdata() {

        ProgressBar progressBar = findViewById(R.id.progress);
        progressBar.setVisibility(View.VISIBLE);
        if (name.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (phone.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.enter_phone, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (phone.getText().toString().length() != 11) {
            Toast.makeText(this, R.string.phone_digits, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (phone.getText().toString().charAt(0) != '0') {
            Toast.makeText(this, R.string.start_must_zero, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }


        if (birthday.getText().toString().isEmpty()) {
            Toast.makeText(this, R.string.enter_birth_day, Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        try {


            findViewById(R.id.email_login_form).setVisibility(View.INVISIBLE);
            start_upload(phone.getText().toString().replace("+2", "").trim());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("phone", phone.getText().toString().replace("+2", "").replace("\\s", "").trim());
            editor.putString("username", name.getText().toString());
            editor.putString("birth", birthday.getText().toString());
            editor.commit();
            RealTimeDatabase realtime_database = new RealTimeDatabase(getApplicationContext());
            MainActivity.register = true;
            realtime_database.new_user_register(name.getText().toString(), phone.getText().toString(), birthday.getText().toString());
            progressBar.setVisibility(View.INVISIBLE);
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            FirebaseCrash.report(e);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void update_profile(View view) {
        Intent intent = new Intent(this, FilePicker.class);
        startActivityForResult(intent, REQUEST_PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_FILE:
                    if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {
                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        path = selectedFile.getPath();
                        ((ImageButton) findViewById(R.id.image_profile)).setImageURI(Uri.parse(path));
                    }
                    break;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putSerializable("name", name.getText().toString());
        outState.putSerializable("phone", phone.getText().toString());
        outState.putSerializable("birht", birthday.getText().toString());
        outState.putSerializable("path", path);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        name.setText(savedInstanceState.getString("name"));
        phone.setText(savedInstanceState.getString("phone"));
        birthday.setText(savedInstanceState.getString("birth"));
        path = savedInstanceState.getString("path");
        viewprofile();
    }

    private void start_upload(String mPhoneNumber) {
        if (path != null) {
            RealTimeDatabase realtime_database = new RealTimeDatabase(getApplicationContext());
            Uri file = Uri.fromFile(new File(path));
            StorageReference strageRef = storage.getReferenceFromUrl("gs://chatapp-21cbe.appspot.com/");
            StorageReference riversRef = strageRef.child("profile" + mPhoneNumber);
            UploadTask uploadTask = riversRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Toast.makeText(getApplicationContext(), R.string.fialed_upload, Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                }
            });
        }
    }
}

