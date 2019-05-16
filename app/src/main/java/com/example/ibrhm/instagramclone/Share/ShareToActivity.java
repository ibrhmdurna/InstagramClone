package com.example.ibrhm.instagramclone.Share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ibrhm.instagramclone.Home.HomeActivity;
import com.example.ibrhm.instagramclone.Models.Comments;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.FileOperations;
import com.example.ibrhm.instagramclone.Utils.KeyboardEvents;
import com.example.ibrhm.instagramclone.Utils.ProfilePostsAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hendraanggrian.widget.Mention;
import com.hendraanggrian.widget.MentionAdapter;
import com.hendraanggrian.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.widget.SocialEditText;
import com.vanniktech.emoji.EmojiEditText;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class ShareToActivity extends AppCompatActivity {

    private ImageView mShareImage;
    private ImageView mBackView;
    private Button mShareBtn;
    private SocialAutoCompleteTextView mCaptionText;

    private String shareType;
    private String sharePath;
    private Uri photoUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mRootRef;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_to);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        mShareImage = findViewById(R.id.share_to_image_view);
        mShareBtn = findViewById(R.id.share_toolbar_share_btn);
        mCaptionText = findViewById(R.id.share_caption_text);

        sharePath = getIntent().getStringExtra("share_photo");
        shareType = getIntent().getStringExtra("share_type");
        photoUri = Uri.parse("file://"+sharePath);

        onBack();

        if(mAuth.getCurrentUser() != null){
            setupSharePhoto();
            share();
        }

        final MentionAdapter mentionAdapter = new MentionAdapter(this);

        mCaptionText.setMentionTextChangedListener(new Function2<MultiAutoCompleteTextView, String, Unit>() {
            @Override
            public Unit invoke(MultiAutoCompleteTextView multiAutoCompleteTextView, String s) {
                FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("user_name").startAt(s).endAt(s+"\uf8ff")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                                        mentionAdapter.clear();
                                        Users user = ds.getValue(Users.class);
                                        if(user.getDetails().getProfile_image().equals("")){
                                            mentionAdapter.add(new Mention(user.getUser_name(), user.getFull_name(), R.drawable.ic_default_avatar));
                                        }
                                        else{
                                            mentionAdapter.add(new Mention(user.getUser_name(), user.getFull_name(), user.getDetails().getProfile_image()));
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                return null;
            }
        });

        mCaptionText.setMentionAdapter(mentionAdapter);
    }

    private void onBack(){
        mBackView = findViewById(R.id.share_to_back_button);

        mBackView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareToActivity.super.onBackPressed();
            }
        });
    }

    private void setupSharePhoto(){
        UniversalImageLoader.setImage(sharePath, mShareImage, null, "file://");
    }

    public void uploadPhotoPost(String filePath){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading_2, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        final ProgressBar progressBar = view.findViewById(R.id.loading_progress_bar_2);

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        Uri fileUri = Uri.parse("file://"+filePath);

        final StorageReference filepath = mStorageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Posts").child(fileUri.getLastPathSegment());

        filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            final String post_id = mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).push().getKey();
                            Map postMap = new HashMap();
                            postMap.put("user_id", mAuth.getCurrentUser().getUid());
                            postMap.put("post_id", post_id);
                            postMap.put("time", ServerValue.TIMESTAMP);
                            postMap.put("caption", mCaptionText.getText().toString());
                            postMap.put("post_url", downloadUrl);
                            postMap.put("type", "Photo");
                            postMap.put("thumb_image", "");

                            mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(!TextUtils.isEmpty(mCaptionText.getText().toString())){
                                        final String comment_id = mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).child("comments").push().getKey();
                                        Map commentMap = new HashMap();
                                        commentMap.put("comment", mCaptionText.getText().toString());
                                        commentMap.put("time", ServerValue.TIMESTAMP);
                                        commentMap.put("user_id", mAuth.getCurrentUser().getUid());
                                        commentMap.put("comment_id", comment_id);
                                        commentMap.put("caption", true);
                                        commentMap.put("send", false);

                                        mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).child("comments").child(comment_id).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                                            @Override
                                            public void onComplete(@NonNull Task task) {
                                                mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).child("comments").child(comment_id).child("send").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        dialog.dismiss();
                                                        Intent homeIntent = new Intent(ShareToActivity.this, HomeActivity.class);
                                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        startActivity(homeIntent);
                                                        finish();
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                dialog.dismiss();
                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });

                                    }
                                    else{
                                        dialog.dismiss();
                                        Intent homeIntent = new Intent(ShareToActivity.this, HomeActivity.class);
                                        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(homeIntent);
                                        finish();
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                float totalByteCount = 100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) totalByteCount);
            }
        });
    }

    public void uploadVideoPost(String filePath){

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading_2, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        final ProgressBar progressBar = view.findViewById(R.id.loading_progress_bar_2);

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        Uri fileUri = Uri.parse("file://"+filePath);

        final StorageReference filepath = mStorageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Posts").child(fileUri.getLastPathSegment());

        filepath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            String downloadUrl = uri.toString();

                            CreateVideoThumb createVideoThumb = new CreateVideoThumb("Video", downloadUrl, dialog, progressBar);
                            createVideoThumb.execute(downloadUrl);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                float totalByteCount = 50 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                progressBar.setProgress((int) totalByteCount);
            }
        });
    }

    private class CreateVideoThumb extends AsyncTask<String,Void,Bitmap> {

        private AlertDialog dialog;
        private String mType, downloadUrl;
        private ProgressBar progressBar;

        public CreateVideoThumb(String type, String downloadUrl, AlertDialog alertDialog, ProgressBar progressBar){
            this.downloadUrl = downloadUrl;
            mType = type;
            dialog = alertDialog;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            if(strings[0] != null){
                String videoPath = strings[0];

                Bitmap bitmap = null;
                MediaMetadataRetriever mediaMetadataRetriever = null;
                try {
                    mediaMetadataRetriever = new MediaMetadataRetriever();
                    mediaMetadataRetriever.setDataSource(videoPath, new HashMap<String, String>());

                    bitmap = mediaMetadataRetriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST);
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        throw new Throwable(
                                "Exception in retriveVideoFrameFromVideo(String videoPath)"
                                        + e.getMessage());
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }

                } finally {
                    if (mediaMetadataRetriever != null) {
                        mediaMetadataRetriever.release();
                    }
                }
                return bitmap;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if(bitmap != null){

                Uri thumbUri = Uri.parse("file://"+FileOperations.cropImage(bitmap));

                final StorageReference filepath = mStorageRef.child("Users").child(mAuth.getCurrentUser().getUid()).child("Posts").child(thumbUri.getLastPathSegment());

                filepath.putFile(thumbUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    String downloadThumbUrl = uri.toString();

                                    final String post_id = mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).push().getKey();
                                    Map postMap = new HashMap();
                                    postMap.put("user_id", mAuth.getCurrentUser().getUid());
                                    postMap.put("post_id", post_id);
                                    postMap.put("time", ServerValue.TIMESTAMP);
                                    postMap.put("caption", mCaptionText.getText().toString());
                                    postMap.put("post_url", downloadUrl);
                                    postMap.put("type", mType);
                                    postMap.put("thumb_image", downloadThumbUrl);

                                    mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).updateChildren(postMap).addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(!TextUtils.isEmpty(mCaptionText.getText().toString())){
                                                final String comment_id = mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).child("comments").push().getKey();
                                                Map commentMap = new HashMap();
                                                commentMap.put("comment", mCaptionText.getText().toString());
                                                commentMap.put("time", ServerValue.TIMESTAMP);
                                                commentMap.put("user_id", mAuth.getCurrentUser().getUid());
                                                commentMap.put("comment_id", comment_id);
                                                commentMap.put("caption", true);
                                                commentMap.put("send", false);

                                                mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).child("comments").child(comment_id).updateChildren(commentMap).addOnCompleteListener(new OnCompleteListener() {
                                                    @Override
                                                    public void onComplete(@NonNull Task task) {
                                                        mRootRef.child("Posts").child(mAuth.getCurrentUser().getUid()).child(post_id).child("comments").child(comment_id).child("send").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                dialog.dismiss();
                                                                Intent homeIntent = new Intent(ShareToActivity.this, HomeActivity.class);
                                                                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                                startActivity(homeIntent);
                                                                finish();
                                                            }
                                                        }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                dialog.dismiss();
                                                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                            }
                                                        });
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        dialog.dismiss();
                                                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                            }
                                            else{
                                                dialog.dismiss();
                                                Intent homeIntent = new Intent(ShareToActivity.this, HomeActivity.class);
                                                homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(homeIntent);
                                                finish();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog.dismiss();
                                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        float totalByteCount = 50 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                        totalByteCount += 50;
                        progressBar.setProgress((int) totalByteCount);
                    }
                });
            }
        }

        private Uri getImageUri(Context context, Bitmap inImage) {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
            return Uri.parse(path);
        }
    }

    private void share(){
        mShareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                KeyboardEvents.closeKeyboard(ShareToActivity.this);

                if(shareType.equals("Photo")){
                    FileOperations.compressToPhoto(ShareToActivity.this, sharePath);
                }
                else if (shareType.equals("Video")){
                    FileOperations.compressToVideo(ShareToActivity.this, sharePath);
                }
            }
        });
    }
}
