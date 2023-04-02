package com.example.qr_project.activities;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static com.example.qr_project.utils.UserManager.containsUserID;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.qr_project.R;
import com.example.qr_project.models.DatabaseResultCallback;
import com.example.qr_project.utils.Hash;
import com.example.qr_project.utils.QR_Adapter;
import com.example.qr_project.utils.QR_Code;
import com.example.qr_project.utils.UserManager;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserProfileActivity extends AppCompatActivity {
    FirebaseFirestore db;
    String userId;

    ArrayList<QR_Code> rankedQRCodes_list;
    ArrayAdapter<QR_Code> rankedQRCodes_adapter;
    ListView rankedQRCodes_view;

    AppCompatButton viewAllBtn;


    TextView usernameTxt;
    TextView emailTxt;

    TextView totalScoreTxt;

    TextView globalRankTxt;
    TextView friendRankTxt;
    TextView totalQrCodesTxt;

    TextView qrCode1NameTxt;
    TextView qrCode1ScoreTxt;
    TextView qrCode2NameTxt;
    TextView qrCode2ScoreTxt;
    TextView qrCode3NameTxt;
    TextView qrCode3ScoreTxt;

    TextView tableTitleText;

    TableRow qrCode1Row;
    TableRow qrCode2Row;
    TableRow qrCode3Row;

    AppCompatButton addFriendBtn;

    UserManager userManager;

    UserManager otherUserManager;
    private ArrayList<Map<String, Object>> friends;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        userId = getIntent().getStringExtra("userId");
        //String currentUserInstance = UserManager.getInstance().getUserId(); // User who is signed in
        db = FirebaseFirestore.getInstance();
        CollectionReference collRef = db.collection("users");
        DocumentReference docRef = collRef.document(userId);


        // Setup User classes
        userManager = UserManager.getInstance();
        otherUserManager = UserManager.newInstance(getIntent().getStringExtra("userId"));
        friends = new ArrayList<>();


        usernameTxt = findViewById(R.id.username_txt);
        emailTxt = findViewById(R.id.user_email_txt);
        totalScoreTxt = findViewById(R.id.user_total_score_txt);
        globalRankTxt = findViewById(R.id.global_user_rank);
        friendRankTxt = findViewById(R.id.friend_user_rank);
        totalQrCodesTxt = findViewById(R.id.user_total_qr_codes);
        /*qrCode1NameTxt = findViewById(R.id.qr_code_name_1);
        qrCode2NameTxt = findViewById(R.id.qr_code_name_2);
        qrCode3NameTxt = findViewById(R.id.qr_code_name_3);
        qrCode1ScoreTxt = findViewById(R.id.qr_code_score_1);
        qrCode2ScoreTxt = findViewById(R.id.qr_code_score_2);
        qrCode3ScoreTxt = findViewById(R.id.qr_code_score_3);
        qrCode1Row = findViewById(R.id.qr_code_row_1);
        qrCode2Row = findViewById(R.id.qr_code_row_2);
        qrCode3Row = findViewById(R.id.qr_code_row_3);*/
        tableTitleText = findViewById(R.id.table_header);
        addFriendBtn = findViewById(R.id.add_friend_btn);



        rankedQRCodes_list = new ArrayList<>();
        rankedQRCodes_adapter = new QR_Adapter(this, rankedQRCodes_list);
        rankedQRCodes_view = findViewById(R.id.top_qr_table);
        rankedQRCodes_view.setAdapter(rankedQRCodes_adapter);


        userManager.getFriends(new DatabaseResultCallback<List<Map<String, Object>>>() {
            @Override
            public void onSuccess(List<Map<String, Object>> result) {
                for (int i = 0; i < result.size(); i++){
                    Map<String, Object> newFriend = new HashMap<>();
                    newFriend.put("userID", result.get(i).get("userID"));
                    friends.add(newFriend);
                }
                updateAddFriendButton();
            }


            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "Error getting friends", e);
            }
        });

        // If its your own profile
        if (userManager.getUserID().equals(otherUserManager.getUserID())) {
            // Your own profile
            tableTitleText.setText("Your Top Codes");
            addFriendBtn.setVisibility(GONE);
            userManager.getGlobalRanking(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    globalRankTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error finding global ranking", e);
                }
            });
            userManager.getFriendRanking(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    friendRankTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error finding friend ranking", e);
                }
            });
            userManager.getUsername(new DatabaseResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    usernameTxt.setText(result);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to get username", e);
                }
            });
            userManager.getEmail(new DatabaseResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    emailTxt.setText(result);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to get email", e);
                }
            });
            userManager.getTotalScore(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    totalScoreTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to get total score", e);
                }
            });

            userManager.getTotalQRCodes(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    totalQrCodesTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error getting total QR Codes", e);
                }
            });

            userManager.getTop3QRCodesSorted(new DatabaseResultCallback<List<Map<String, Object>>>() {
                @Override
                public void onSuccess(List<Map<String, Object>> result) {
                    if (!result.isEmpty()) {
                        int rank = 1;
                        for (Map<String, Object> qrCode : result) {
                            if (rank > 3) break;

                            // Creating QR_Code object for the adapter
                            String name = String.valueOf(qrCode.get("name"));

                            // POTENTIAL ERROR
                            int score = qrCode.get("score") instanceof String ? Integer.parseInt((String) qrCode.get("score")) : Math.toIntExact((long) qrCode.get("score"));

                            // IDENTIFIED ERROR POINT
                            // Not all qr codes in the db have a face,
                            // so this call will fail for the older docs in docRef
                            String face = (String) qrCode.get("face");

                            Hash hash = new Hash((String) qrCode.get("hash"), name, face, score);

                            // adding QR_Code obj to the list
                            rankedQRCodes_list.add(new QR_Code(hash, score, name, face));

                            rank++;
                        }

                        // Notify adapter to update dataset
                        rankedQRCodes_adapter.notifyDataSetChanged();

                        Log.d("DOCSNAP", result.toString());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error getting top 3 codes: ", e);
                }
            });


        } else {

            addFriendBtn.setVisibility(View.VISIBLE);

            otherUserManager.getGlobalRanking(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    globalRankTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error finding global ranking", e);
                }
            });

            otherUserManager.getFriendRanking(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    if (result == 0){
                        friendRankTxt.setText("NA");
                    } else {
                        friendRankTxt.setText(String.valueOf(result));
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error finding friend ranking", e);
                }
            });

            otherUserManager.getUsername(new DatabaseResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    usernameTxt.setText(result);
                    tableTitleText.setText(result + "s Top Codes");
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to get username", e);
                }
            });
            otherUserManager.getEmail(new DatabaseResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    emailTxt.setText(result);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to get email", e);
                }
            });

            otherUserManager.getTotalScore(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    totalScoreTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Failed to get total score", e);
                }
            });

            otherUserManager.getTotalQRCodes(new DatabaseResultCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    totalQrCodesTxt.setText(String.valueOf(result));
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error getting total QR Codes", e);
                }
            });

            otherUserManager.getTop3QRCodesSorted(new DatabaseResultCallback<List<Map<String, Object>>>() {
                @Override
                public void onSuccess(List<Map<String, Object>> result) {
                    if (!result.isEmpty()) {
                        int rank = 1;
                        for (Map<String, Object> qrCode : result) {
                            if (rank > 3) break;

                            // Creating QR_Code object for the adapter
                            String name = String.valueOf(qrCode.get("name"));

                            // POTENTIAL ERROR
                            int score = qrCode.get("score") instanceof String ? Integer.parseInt((String) qrCode.get("score")) : Math.toIntExact((long) qrCode.get("score"));

                            // IDENTIFIED ERROR POINT
                            // Not all qr codes in the db have a face,
                            // so this call will fail for the older docs in docRef
                            String face = (String) qrCode.get("face");

                            Hash hash = new Hash((String) qrCode.get("hash"), name, face, score);

                            // adding QR_Code obj to the list
                            rankedQRCodes_list.add(new QR_Code(hash, score, name, face));

                            rank++;
                        }

                        // Notify adapter to update dataset
                        rankedQRCodes_adapter.notifyDataSetChanged();

                        Log.d("DOCSNAP", result.toString());
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Log.d(TAG, "Error getting top 3 codes: ", e);
                }
            });

        }
    }

    private void updateAddFriendButton() {
        Log.d(TAG, "Friends list: " + friends);

        if (friends != null && containsUserID(friends, otherUserManager.getUserID())) {
            addFriendBtn.setBackgroundResource(R.drawable.red_round_btn);
            addFriendBtn.setText("Remove friend");
            addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userManager.removeFriend(otherUserManager.getUserID());
                    addFriendBtn.setBackgroundResource(R.drawable.green_round_btn);
                    addFriendBtn.setText("Add friend");
                }
            });
        } else {
            addFriendBtn.setBackgroundResource(R.drawable.green_round_btn);
            addFriendBtn.setText("Add friend");
            addFriendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userManager.addFriend(otherUserManager.getUserID());
                    addFriendBtn.setBackgroundResource(R.drawable.red_round_btn);
                    addFriendBtn.setText("Remove friend");
                }
            });
        }
    }

    public void onViewAllClick(View view){
        Intent intent = new Intent(UserProfileActivity.this, LeaderboardActivity.class);
        intent.putExtra("filter", "user");
        startActivity(intent);
    }

    public void onClickBack(View view){
        finish();
    }



    /**
     * When the user clicks the camera button, this method will be called
     * and will open the camera to scan a QR or barcode
     *
     * @param view The text view which is pressed
     */
    public void onCameraClick(View view) {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivity(intent);
    }


    /**
     * For the use and the feature of the map button
     *
     * @param view The text view which is pressed
     */
    public void onMapClick(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the LeaderboardActivity
     * @param view The text view which is pressed
     */
    public void onLeaderboardClick(View view) {
        Intent intent = new Intent(this, LeaderboardActivity.class);
        startActivity(intent);
    }
}