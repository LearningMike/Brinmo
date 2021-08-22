package com.brinmo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.FileSystem;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class ProfileimageActivity extends AppCompatActivity {

    Button uploadbtn;
    Button skipibtn;
    StorageReference storageRef;
    final int PICK_IMAGE_CODE = 1000;
    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1200;
    FirebaseUser mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileimage);

        mAuth = FirebaseAuth.getInstance().getCurrentUser();
        progressBar = findViewById(R.id.progress_upload);
        uploadbtn = (Button) findViewById(R.id.uploadbtn);
        uploadbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                if (ContextCompat.checkSelfPermission(ProfileimageActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {

                    try {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_IMAGE_CODE);
                    } catch (Exception errorrr){
                        FirebaseCrashlytics.getInstance().recordException(errorrr);
                    }
                } else {
                    ActivityCompat.requestPermissions(ProfileimageActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    FirebaseCrashlytics.getInstance().log("noPermission: but asking nicely again");
                }
            }
        });

        skipibtn = findViewById(R.id.skipibtn);
        skipibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent nextp = new Intent(ProfileimageActivity.this, HomeActivity.class);
                startActivity(nextp);
            }
        });

        ActivityCompat.requestPermissions(ProfileimageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    public String getRealPathFromURI(Uri uri) {
        if (uri == null) {
            return null;
        }
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
            cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                int column_index = cursor
                        .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (Exception eeer) {
            FirebaseCrashlytics.getInstance().recordException(eeer);
        } finally{
            cursor.close();
        }
        return uri.getPath();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent dataxxx) {
        super.onActivityResult(requestCode, resultCode, dataxxx);

        if (dataxxx != null){

            final Uri selectedImage = dataxxx.getData();
            String selectedImagePath = getRealPathFromURI(selectedImage);
            try {
                progressBar = findViewById(R.id.progress_upload);
                progressBar.setVisibility(View.VISIBLE);
                InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                InputStream stream = new FileInputStream(new File(selectedImagePath));
                File filex = new File(selectedImagePath);
                mAuth = FirebaseAuth.getInstance().getCurrentUser();
                storageRef = FirebaseStorage.getInstance().getReference().child("customer/" + mAuth.getUid() + "/profileImage.png");
                TextView puseri = findViewById(R.id.puseri);
                //StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/png").build();

                storageRef.putStream(stream).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        //displaying percentage in progress dialog
                        puseri.setText("Uploading...");
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        Intent nextp = new Intent(ProfileimageActivity.this, HomeActivity.class);
                        startActivity(nextp);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        puseri.setText(R.string.puseri_up);
                        progressBar.setVisibility(View.INVISIBLE);
                        FirebaseCrashlytics.getInstance().recordException(exception);
                    }
                });
            } catch (Exception e){
                FirebaseCrashlytics.getInstance().recordException(e);
            }
        } else {
            FirebaseCrashlytics.getInstance().log("uploadError: failed to get image user chose");
        }
    }
}
