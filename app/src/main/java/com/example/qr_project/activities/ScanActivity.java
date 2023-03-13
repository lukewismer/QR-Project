package com.example.qr_project.activities;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.qr_project.utils.Hash;
import com.example.qr_project.utils.Player;
import com.example.qr_project.utils.QR_Code;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

import java.util.HashMap;

public class ScanActivity extends AppCompatActivity {
    QR_Code qrCode;
    Hash hash;
    FirebaseFirestore db;

    private ActivityResultLauncher<Intent> cameraLauncher;


    /**
     * Defining the cameralauncher ready to scan the QR_Code
     * The camera can scan and take a photo of the QR_Code being presented to it
     * Getting the image presented to the camera
     *
     * Here, it also creates a hashmap of the QR_Code properties you want to store
     * Also updates the location of the QR_Code being scanned
     * Connecting with the FireStore FireBase,
     * this is to fetch the user's id/ account to store it on the FireBase Cloud
     * under their account.
     *
     * Then, the code will update the qrcodes array field with the new QR code
     * The scanned QR_Code will then be stored to the FireBase Cloud under the user's account/id
     * @param savedInstanceState a package to execute the scanning job of the app
     * @see FirebaseFirestore
     * @see QRCodeActivity
     * @see Player
     *
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(CaptureActivity.class);
        integrator.setOrientationLocked(false);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan a QR/Barcode");
        integrator.initiateScan();

        db = FirebaseFirestore.getInstance();
        // Define cameraLauncher
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                assert data != null;
                Bitmap image = (Bitmap) data.getExtras().get("data");

                // Create a hashmap of the QR_Code properties you want to store
                HashMap<String, Object> qrCodeDB = qrCode.toMap();

                // Get the current user's ID
                SharedPreferences sharedPref = getSharedPreferences("QR_pref", Context.MODE_PRIVATE);

                // Retrieve the user's information
                String userID = sharedPref.getString("user_id", null);

                Log.d("MyTag", "userID " + userID);

                // Get a reference to the user's document in Firestore
                DocumentReference userRef = db.collection("users").document(userID);


                // Get the user's current location
                FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                                qrCode.setLocation(geoPoint);
                                qrCodeDB.put("location", geoPoint);

                                // Update the qrcodes array field with the new QR code
                                userRef.update("qrcodes", FieldValue.arrayUnion(qrCode))
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "QR code added to user's document in DB");
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.w(TAG, "Error adding QR code to user's document in DB", e);
                                            finish();
                                        });

                            } else {
                                Log.w(TAG, "Unable to retrieve location");
                                finish();
                            }
                        });
            } else {
                Log.d("MyTag", "Failed");
            }
        });

    }

    /**
     * After defining the cameraLauncher and the functionality of its
     * The ability of taking the photo and scanning an image as the QR_Code
     * The camera can scan and take a photo of the QR_Code being presented to it
     * Getting the image presented to the camera
     *
     * Here, it also creates a hashmap of the QR_Code properties you want to store
     * Also updates the location of the QR_Code being scanned
     * Connecting with the FireStore FireBase,
     * this is to fetch the user's id/ account to store it on the FireBase Cloud
     * under their account.
     *
     * Then, the code will update the qrcodes array field with the new QR code
     * The scanned QR_Code will then be stored to the FireBase Cloud under the user's account/id
     * @param resultCode   displays the result
     * @param requestCode   taking the code
     * @param data    the data of the QR_Code
     * @see FirebaseFirestore
     * @see QRCodeActivity
     * @see Player
     *
     */


    // Handle the scanning of the QR code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Create a hash object and pass hash into the constructor of QR_Code
                hash = new Hash(result.getContents());
                //String name = hash.generateName(result.getContents()); Fix the name
                qrCode = new QR_Code(hash, null, null);

                // Ask the user if they want to take a picture
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Would you like to take a picture?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Open the camera
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(takePictureIntent);
                });
                builder.setNegativeButton("No", (dialog, which) -> {
                    // Create a hashmap of the QR_Code properties you want to store
                    HashMap<String, Object> qrCodeDB = qrCode.toMap();

                    // Get the current user's ID
                    SharedPreferences sharedPref = getSharedPreferences("QR_pref", Context.MODE_PRIVATE);

                    // Retrieve the user's information
                    String userID = sharedPref.getString("user_id", null);

                    Log.d("MyTag", "userID " + userID);

                    // Get a reference to the user's document in Firestore
                    DocumentReference userRef = db.collection("users").document(userID);

                    qrCodeDB.put("location", null);

                    // Update the qrcodes array field with the new QR code
                    userRef.update("qrcodes", FieldValue.arrayUnion(qrCodeDB))
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "QR code added to user's document in DB");
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Error adding QR code to user's document in DB", e);
                                finish();
                            });
                });
                builder.show();
            }
        }
    }
}

