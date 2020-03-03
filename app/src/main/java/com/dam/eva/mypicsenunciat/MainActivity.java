package com.dam.eva.mypicsenunciat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    private static final int RC_GALLERY = 21;
    private static final int RC_CAMERA = 22;

    private static final int RP_CAMERA = 121;
    private static final int RP_STORAGE = 122;

    //firestore
    private static final String IMAGE_DIRECTORY = "/MyPhotoApp";
    private static final String MY_PHOTO = "my_photo";

    //firebase
    private static final String PATH_PROFILE = "profile";
    private static final String PATH_PHOTO_URL = "photoUrl";

    @BindView(R.id.imgPhoto)
    AppCompatImageView imgPhoto;
    @BindView(R.id.btnDelete)
    ImageButton btnDelete;
    @BindView(R.id.container)
    ConstraintLayout container;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    private TextView mTextMessage;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private String mCurrentPhotoPath;
    private Uri mPhotoSelectedUri;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_gallery:
                    mTextMessage.setText(R.string.main_label_gallery);

                    //fromGallery();
                    checkPermissionToApp(Manifest.permission.READ_EXTERNAL_STORAGE, RP_STORAGE);

                    return true;
                case R.id.navigation_camera:
                    mTextMessage.setText(R.string.main_label_camera);
                    //fromCamera();
                    //dispatchTakePictureIntent();
                    checkPermissionToApp(Manifest.permission.CAMERA, RP_CAMERA);
                    return true;

                // TODO: 3/03/20 show uploads i afegir nom fitxer a Firestore, cal fer
                // showUploadsActivity amb una recycler
                //https://www.youtube.com/playlist?list=PLrnPJCHvNZuBf5KH4XXOthtgo6E4Epjl8

                case R.id.navigation_showuploads:
                    mTextMessage.setText(R.string.main_label_showuploads);
                    //fromCamera();
                    //dispatchTakePictureIntent();
                    //checkPermissionToApp(Manifest.permission.CAMERA, RP_CAMERA);
                    return true;
            }
            return false;
        }
    };

    private void checkPermissionToApp(String permissionStr, int requestPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, permissionStr) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{permissionStr}, requestPermission);
                return;
            }
        }

        switch (requestPermission) {
            case RP_STORAGE:
                fromGallery();
                break;
            case RP_CAMERA:
                dispatchTakePictureIntent();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            switch (requestCode) {
                case RP_STORAGE:
                    fromGallery();
                    break;
                case RP_CAMERA:
                    dispatchTakePictureIntent();
                    break;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTextMessage = findViewById(R.id.message);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        configFirebase();

        //mostrar foto de Firestore
        configPhotoProfile();
    }

    private void configFirebase() {
        mStorageReference = FirebaseStorage.getInstance().getReference();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference().child(PATH_PROFILE).child(PATH_PHOTO_URL);
    }

    private void configPhotoProfile() {

    }

    private void fromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_GALLERY);
    }

    private void fromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RC_CAMERA);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            photoFile = createImageFile();

            if (photoFile != null) {
                Uri photoUri = FileProvider.getUriForFile(this,
                        "com.eva.dam.mypics", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, RC_CAMERA);
            }
        }
    }

    private File createImageFile() {
        final String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HHmmss", Locale.ROOT)
                .format(new Date());
        final String imageFileName = MY_PHOTO + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = null;
        try {
            image = File.createTempFile(imageFileName, ".jpg", storageDir);
            mCurrentPhotoPath = image.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RC_GALLERY:
                    if (data != null) {
                        mPhotoSelectedUri = data.getData();

                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                                    mPhotoSelectedUri);
                            imgPhoto.setImageBitmap(bitmap);
                            btnDelete.setVisibility(View.GONE);
                            mTextMessage.setText(R.string.main_message_question_upload);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case RC_CAMERA:
                    /*Bundle extras = data.getExtras();
                    Bitmap bitmap = (Bitmap)extras.get("data");*/

                    mPhotoSelectedUri = addPicGallery();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                                mPhotoSelectedUri);
                        imgPhoto.setImageBitmap(bitmap);
                        btnDelete.setVisibility(View.GONE);
                        mTextMessage.setText(R.string.main_message_question_upload);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    private Uri addPicGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        mCurrentPhotoPath = null;
        return contentUri;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @OnClick(R.id.btnUpload)
    public void onUploadPhoto() {
        progressBar.setVisibility(View.VISIBLE);
        Bitmap bitmap;


    }

    private void savePhotoUrl(Uri downloadUri) {
        mDatabaseReference.setValue(downloadUri.toString());
    }

    @OnClick(R.id.btnDelete)
    public void onDeletePhoto() {

    }
}



