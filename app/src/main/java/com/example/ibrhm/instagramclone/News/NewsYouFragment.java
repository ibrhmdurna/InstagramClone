package com.example.ibrhm.instagramclone.News;


import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.baoyz.widget.PullRefreshLayout;
import com.example.ibrhm.instagramclone.Local.PostActivity;
import com.example.ibrhm.instagramclone.Local.UserProfileActivity;
import com.example.ibrhm.instagramclone.Models.Interactions;
import com.example.ibrhm.instagramclone.Models.UserPosts;
import com.example.ibrhm.instagramclone.Models.Users;
import com.example.ibrhm.instagramclone.R;
import com.example.ibrhm.instagramclone.Utils.EventBusDataEvents;
import com.example.ibrhm.instagramclone.Utils.GetTimeAgo;
import com.example.ibrhm.instagramclone.Utils.InteractionsAdapter;
import com.example.ibrhm.instagramclone.Utils.UniversalImageLoader;
import com.example.ibrhm.instagramclone.VideoRecyclerView.view.App;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class NewsYouFragment extends Fragment {

    private View mView;

    private LinearLayout mFollowReqLayout, mFollowNotReqLayout, mFollowInteractionsLayout, mFollowInteractionsContentLayout, mInteractionsLayout;
    private CircleImageView mFollowReqLastImage, mFInteractionOneView;
    private TextView mFollowReqCount;
    private RelativeLayout mFInteractionTwoView;
    private CircleImageView mF1stImage, mF2thImage;
    private TextView mFFollowView, mFFollowingView, mFReqView;
    private TextView mFInteractionText;
    private PullRefreshLayout mRefreshLayout;
    private NestedScrollView mScrollView;
    private LottieAnimationView mLoadingAnim;

    private List<String> mFollowIntList;
    private List<String> mFollowReqList;
    private List<Interactions> mIntList;

    private RecyclerView mIntView;
    private InteractionsAdapter mIntAdapter;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mRootRef;

    public static boolean goToRequests = false;

    private boolean isBackground = false;

    public NewsYouFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_news_you, container, false);


        isBackground = App.background.isThereBackground("News_You");

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mFollowReqList = new ArrayList<>();
        mFollowIntList = new ArrayList<>();

        setupView();

        if(!isBackground){
            mScrollView.setVisibility(View.GONE);
            mLoadingAnim.setVisibility(View.VISIBLE);
            mLoadingAnim.playAnimation();
            App.background.getBackgroundList().add("News_You");
        }

        getFollowRequests();
        getFollowInteractions();
        getInteractions();

        if(goToRequests){
            FragmentManager manager = getActivity().getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.replace(R.id.news_container, new FollowRequestFragment());
            transaction.addToBackStack("Follow_Request");
            transaction.commit();
        }

        mRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mScrollView.setVisibility(View.GONE);
                getFollowRequests();
                getFollowInteractions();
                getInteractions();
            }
        });

        return mView;
    }

    private void getInteractions(){
        mIntList = new ArrayList<>();
        mIntAdapter = new InteractionsAdapter(getActivity(), mIntList);
        mIntView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mIntView.setHasFixedSize(true);
        mIntView.setAdapter(mIntAdapter);
        final List<Interactions> mTempList = new ArrayList<>();

        mRootRef.child("News").child("Interactions").child(mCurrentUser.getUid()).orderByChild("time").limitToLast(50).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mIntList.clear();
                mTempList.clear();
                if(dataSnapshot.exists()){
                    mInteractionsLayout.setVisibility(View.VISIBLE);
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        Interactions interaction = ds.getValue(Interactions.class);
                        mTempList.add(interaction);
                    }
                    Collections.reverse(mTempList);

                    for(int i = 0; i < mTempList.size(); i++){
                        if(mTempList.get(i).getAction().equals("like")){
                            boolean isTrigger = true;
                            for(int x = 0; x < mIntList.size(); x++){
                                if(mIntList.get(x).getAction().equals("like")&&
                                        mIntList.get(x).getPost_id().equals(mTempList.get(i).getPost_id())){
                                    isTrigger = false;
                                    break;
                                }
                            }

                            if(isTrigger){
                                mIntList.add(mTempList.get(i));
                            }
                        }
                        else{
                            mIntList.add(mTempList.get(i));
                        }
                    }

                    mIntAdapter.notifyDataSetChanged();
                }
                else{
                    mInteractionsLayout.setVisibility(View.GONE);
                }

                if(!isBackground){
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.setVisibility(View.VISIBLE);
                            mLoadingAnim.pauseAnimation();
                            mLoadingAnim.setVisibility(View.GONE);
                            mRefreshLayout.setEnabled(true);
                            mRefreshLayout.setRefreshing(false);
                        }
                    },1000);
                }
                else{
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mScrollView.setVisibility(View.VISIBLE);
                            mRefreshLayout.setRefreshing(false);
                        }
                    },1000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mIntAdapter.setOnItemClickListener(new InteractionsAdapter.OnItemClickListener() {
            @Override
            public void onItemLikeClick(int position) {
                Intent likeIntent = new Intent(getContext(), LikesInteractionActivity.class);
                likeIntent.putExtra("interaction", mIntList.get(position));
                startActivity(likeIntent);
            }

            @Override
            public void onItemCommentClick(int position) {
                Intent commentIntent = new Intent(getContext(), CommentsInteractionActivity.class);
                commentIntent.putExtra("interaction", mIntList.get(position));
                startActivity(commentIntent);
            }
        });
    }

    public void getFollowInteractions(){
        mRootRef.child("News").child("Follow_interactions").child(mCurrentUser.getUid()).orderByChild("time").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                mFollowIntList.clear();
                if(dataSnapshot.exists()){
                    mFollowInteractionsLayout.setVisibility(View.VISIBLE);
                    if(dataSnapshot.getChildrenCount() == 1){
                        mFInteractionOneView.setVisibility(View.VISIBLE);
                        mFInteractionTwoView.setVisibility(View.GONE);
                        mFFollowView.setVisibility(View.GONE);
                        mFFollowingView.setVisibility(View.GONE);
                        mFReqView.setVisibility(View.GONE);

                        String time = null;
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            mFollowIntList.add(ds.getKey());
                            time = GetTimeAgo.getTimeAgoSmall((long) ds.child("time").getValue());
                        }

                        final String[] user_name = {null};
                        final String[] image_url = {null};
                        final String finalTime = time;
                        mRootRef.child("Users").child(mFollowIntList.get(0)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Users user = dataSnapshot.getValue(Users.class);

                                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mFInteractionOneView, null, "");
                                mFInteractionText.setText(Html.fromHtml("<b>"+user.getUser_name()+"</b> started following you. "+ finalTime));
                                user_name[0] = user.getUser_name();
                                image_url[0] = user.getDetails().getProfile_image();

                                SpannableString ss = new SpannableString(mFInteractionText.getText());
                                ForegroundColorSpan fcsGray = new ForegroundColorSpan(getActivity().getResources().getColor(R.color.colorDarkGray));
                                ss.setSpan(fcsGray, mFInteractionText.getText().length() - 3, mFInteractionText.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                mFInteractionText.setText(ss);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mRootRef.child("Follow_req").child(mCurrentUser.getUid()).child(mFollowIntList.get(0)).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    mFReqView.setVisibility(View.VISIBLE);
                                    mFFollowView.setVisibility(View.GONE);
                                    mFFollowingView.setVisibility(View.GONE);
                                }
                                else{
                                    mRootRef.child("Following").child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(mFollowIntList.get(0))){
                                                mFReqView.setVisibility(View.GONE);
                                                mFFollowView.setVisibility(View.GONE);
                                                mFFollowingView.setVisibility(View.VISIBLE);
                                            }
                                            else{
                                                mFReqView.setVisibility(View.GONE);
                                                mFFollowView.setVisibility(View.VISIBLE);
                                                mFFollowingView.setVisibility(View.GONE);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mFReqView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Map followReqMap = new HashMap();
                                followReqMap.put("News/Follow_req/"+mFollowIntList.get(0)+"/"+mCurrentUser.getUid()+"/user_id", null);
                                followReqMap.put("News/Follow_req/"+mFollowIntList.get(0)+"/"+mCurrentUser.getUid()+"/time", null);
                                followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+mFollowIntList.get(0)+"/user_id", null);
                                mRootRef.updateChildren(followReqMap);
                                mFFollowView.setVisibility(View.VISIBLE);
                                mFFollowingView.setVisibility(View.GONE);
                                mFReqView.setVisibility(View.GONE);
                            }
                        });

                        mFFollowView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mRootRef.child("Users").child(mFollowIntList.get(0)).child("private_account").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.getValue().equals(true)){
                                            Map followReqMap = new HashMap();
                                            followReqMap.put("News/Follow_req/"+mFollowIntList.get(0)+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                            followReqMap.put("News/Follow_req/"+mFollowIntList.get(0)+"/"+mCurrentUser.getUid()+"/time", ServerValue.TIMESTAMP);
                                            followReqMap.put("Follow_req/"+mCurrentUser.getUid()+"/"+mFollowIntList.get(0)+"/user_id", mFollowIntList.get(0));
                                            mRootRef.updateChildren(followReqMap);
                                            mFFollowView.setVisibility(View.GONE);
                                            mFFollowingView.setVisibility(View.GONE);
                                            mFReqView.setVisibility(View.VISIBLE);
                                        }
                                        else{
                                            String currentDate = java.text.DateFormat.getDateInstance().format(new Date());
                                            Map followMap = new HashMap();
                                            followMap.put("Following/"+mCurrentUser.getUid()+"/"+mFollowIntList.get(0)+"/user_id", mFollowIntList.get(0));
                                            followMap.put("Following/"+mCurrentUser.getUid()+"/"+mFollowIntList.get(0)+"/date", currentDate);
                                            followMap.put("Followers/"+mFollowIntList.get(0)+"/"+mCurrentUser.getUid()+"/user_id", mCurrentUser.getUid());
                                            followMap.put("Followers/"+mFollowIntList.get(0)+"/"+mCurrentUser.getUid()+"/date", currentDate);
                                            mRootRef.updateChildren(followMap);

                                            String news_key = mRootRef.child("News").child("All_users").child("Interactions").child(mFollowIntList.get(0)).push().getKey();
                                            Map allNewsMap = new HashMap();
                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/action", "following");
                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/time", ServerValue.TIMESTAMP);
                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/content", "");
                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/post_id", "");
                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/post_user_id", mFollowIntList.get(0));
                                            allNewsMap.put("News/All_users/Interactions/"+mCurrentUser.getUid()+"/"+news_key+"/user_id", mCurrentUser.getUid());
                                            mRootRef.updateChildren(allNewsMap);

                                            mFFollowView.setVisibility(View.GONE);
                                            mFFollowingView.setVisibility(View.VISIBLE);
                                            mFReqView.setVisibility(View.GONE);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                        });

                        mFFollowingView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialogUnFollow(image_url[0], user_name[0], mFollowIntList.get(0));
                            }
                        });
                    }
                    else if (dataSnapshot.getChildrenCount() > 1){
                        mFInteractionOneView.setVisibility(View.GONE);
                        mFInteractionTwoView.setVisibility(View.VISIBLE);

                        String time = null;
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            mFollowIntList.add(ds.getKey());
                            time = GetTimeAgo.getTimeAgoSmall((long) ds.child("time").getValue());
                        }

                        Collections.reverse(mFollowIntList);

                        final String[] user_name = {null};
                        final String finalTime = time;
                        mRootRef.child("Users").child(mFollowIntList.get(0)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                Users user = dataSnapshot2.getValue(Users.class);

                                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mF1stImage, null, "");
                                user_name[0] = user.getUser_name();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                        mRootRef.child("Users").child(mFollowIntList.get(1)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot3) {
                                Users user = dataSnapshot3.getValue(Users.class);

                                UniversalImageLoader.setImage(user.getDetails().getProfile_image(), mF2thImage, null, "");
                                if(dataSnapshot.getChildrenCount() == 2){
                                    mFInteractionText.setText(Html.fromHtml("<b>"+user_name[0]+", "+user.getUser_name()+"</b> started following you. "+finalTime));
                                }
                                else{
                                    mFInteractionText.setText(Html.fromHtml("<b>"+user_name[0]+", "+user.getUser_name()+"</b> and <b>"+(dataSnapshot.getChildrenCount() - 2)+ " others</b> started following you. "+finalTime));
                                }
                                SpannableString ss = new SpannableString(mFInteractionText.getText());
                                ForegroundColorSpan fcsGray = new ForegroundColorSpan(getResources().getColor(R.color.colorDarkGray));
                                ss.setSpan(fcsGray, mFInteractionText.getText().length() - 3, mFInteractionText.getText().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                ss.setSpan(new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
                                        profileIntent.putExtra("activity_no", 3);
                                        profileIntent.putExtra("user_id", mFollowIntList.get(0));
                                        startActivity(profileIntent);
                                    }

                                    @Override
                                    public void updateDrawState(@NonNull TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setColor(Color.BLACK);
                                        ds.setUnderlineText(false);
                                    }
                                }, 0, user_name[0].length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                ss.setSpan(new ClickableSpan() {
                                    @Override
                                    public void onClick(@NonNull View widget) {
                                        Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
                                        profileIntent.putExtra("activity_no", 3);
                                        profileIntent.putExtra("user_id", mFollowIntList.get(1));
                                        startActivity(profileIntent);
                                    }

                                    @Override
                                    public void updateDrawState(@NonNull TextPaint ds) {
                                        super.updateDrawState(ds);
                                        ds.setColor(Color.BLACK);
                                        ds.setUnderlineText(false);
                                    }
                                }, user_name[0].length() + 2, user_name[0].length() + user.getUser_name().length() + 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                                mFInteractionText.setText(ss);
                                mFInteractionText.setMovementMethod(LinkMovementMethod.getInstance());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    mFollowInteractionsContentLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mFollowIntList.size() > 1){
                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.news_container, new FollowInteractionsFragment());
                                transaction.addToBackStack("Follow_Interactions");
                                transaction.commit();
                            }
                            else if (mFollowIntList.size() == 1){
                                Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
                                profileIntent.putExtra("activity_no", 3);
                                profileIntent.putExtra("user_id", mFollowIntList.get(0));
                                startActivity(profileIntent);
                            }
                        }
                    });

                    mFInteractionText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(mFollowIntList.size() > 1){
                                FragmentManager manager = getActivity().getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.news_container, new FollowInteractionsFragment());
                                transaction.addToBackStack("Follow_Interactions");
                                transaction.commit();
                            }
                            else if (mFollowIntList.size() == 1){
                                Intent profileIntent = new Intent(getActivity(), UserProfileActivity.class);
                                profileIntent.putExtra("activity_no", 3);
                                profileIntent.putExtra("user_id", mFollowIntList.get(0));
                                startActivity(profileIntent);
                            }
                        }
                    });
                }
                else{
                    mFollowInteractionsLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void dialogUnFollow(String image_url, String user_name, final String uid){
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_un_follow, null);
        mBuilder.setView(view);
        final AlertDialog dialog = mBuilder.create();

        CircleImageView mImage = view.findViewById(R.id.dialog_un_follow_profile_image);
        UniversalImageLoader.setImage(image_url, mImage, null,"");

        TextView body = view.findViewById(R.id.dialog_un_follow_body);
        body.setText(Html.fromHtml("Unfollow <b>@"+user_name+"</b>?"));

        Button mCancelBtn = view.findViewById(R.id.dialog_un_follow_cancel_btn);
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button mUnFollowButton = view.findViewById(R.id.dialog_un_follow_btn);
        mUnFollowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Map followMap = new HashMap();
                followMap.put("Following/"+mCurrentUser.getUid()+"/"+uid, null);
                followMap.put("Followers/"+uid+"/"+mCurrentUser.getUid(), null);
                mRootRef.updateChildren(followMap);

                Map newsMap = new HashMap();
                newsMap.put("News/Follow_interactions/"+uid+"/"+mCurrentUser.getUid()+"/user_id", null);
                newsMap.put("News/Follow_interactions/"+uid+"/"+mCurrentUser.getUid()+"/date", null);
                newsMap.put("News/Follow_interactions/"+uid+"/"+mCurrentUser.getUid()+"/time", null);
                mRootRef.updateChildren(newsMap);

                mRootRef.child("News").child("All_users").child("Interactions").child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            for(DataSnapshot ds : dataSnapshot.getChildren()){
                                if(ds.child("action").getValue().toString().equals("following") &&
                                        ds.child("post_user_id").getValue().toString().equals(uid)){
                                    ds.getRef().removeValue();
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                mFFollowView.setVisibility(View.VISIBLE);
                mFFollowingView.setVisibility(View.GONE);
                mFReqView.setVisibility(View.GONE);
            }
        });

        dialog.getWindow().getAttributes().windowAnimations = R.style.dialogAnimation;
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    public void getFollowRequests(){
        mRootRef.child("News").child("Follow_req").child(mCurrentUser.getUid()).orderByChild("time").addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mFollowReqList.clear();
                if(dataSnapshot.exists()){
                    mFollowReqLayout.setVisibility(View.VISIBLE);
                    mFollowNotReqLayout.setVisibility(View.GONE);
                    mFollowReqCount.setText(dataSnapshot.getChildrenCount()+"");

                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        mFollowReqList.add(ds.getKey());
                    }
                    String user_id = mFollowReqList.get(mFollowReqList.size() - 1);

                    mRootRef.child("Users").child(user_id).child("details").child("profile_image").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String image = dataSnapshot.getValue().toString();
                            UniversalImageLoader.setImage(image, mFollowReqLastImage, null,"");
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    mFollowReqLayout.setVisibility(View.GONE);
                    mFollowNotReqLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mFollowReqLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getActivity().getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.news_container, new FollowRequestFragment());
                transaction.addToBackStack("Follow_Request");
                transaction.commit();
            }
        });
    }

    private void setupView(){
        mFollowReqLayout = mView.findViewById(R.id.follow_req_layout);
        mFollowNotReqLayout = mView.findViewById(R.id.follow_not_req_layout);
        mFollowInteractionsLayout = mView.findViewById(R.id.follow_interactions_layout);
        mFollowInteractionsContentLayout = mView.findViewById(R.id.follow_interactions_content_layout);
        mInteractionsLayout = mView.findViewById(R.id.interaction_layout);
        mFollowReqLastImage = mView.findViewById(R.id.follow_req_last_profile);
        mFollowReqCount = mView.findViewById(R.id.follow_req_count);
        mFInteractionOneView = mView.findViewById(R.id.f_interaction_one_profile);
        mFInteractionTwoView = mView.findViewById(R.id.f_interaction_two_profile);
        mF1stImage = mView.findViewById(R.id.f_interaction_1st_photo);
        mF2thImage = mView.findViewById(R.id.f_interaction_2th_photo);
        mFInteractionText = mView.findViewById(R.id.f_interaction_text);
        mFFollowView = mView.findViewById(R.id.f_interaction_follow_view);
        mFFollowingView = mView.findViewById(R.id.f_interaction_following_view);
        mFReqView = mView.findViewById(R.id.f_interaction_req_view);
        mIntView = mView.findViewById(R.id.news_interaction_container);
        mRefreshLayout = mView.findViewById(R.id.news_you_refresh_layout);
        mScrollView = mView.findViewById(R.id.news_you_scroll);
        mLoadingAnim = mView.findViewById(R.id.news_you_loading_anim);
    }

    @Override
    public void onStart() {
        super.onStart();
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);
    }
}
