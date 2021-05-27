package com.example.shelter.Network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.LimitExceededException;
import android.util.Log;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;


import com.bumptech.glide.Glide;
import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.example.shelter.R;
import com.example.shelter.adapter.ImageSliderAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.smarteist.autoimageslider.SliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;


public class ImageRequester {

    static final private String LOG_TAG = ImageRequester.class.getSimpleName();
    private final Context mContext;
    //  In using for separating image_name domain in order to get an unique image name with given  key and name of the Entry
    static final private String SEPARATE = "_";
    static final private String IMAGE_FOLDER = "image/";
    static final private int IMAGE_HEADER = 1;

    //Max image size can have
    static final private long MAX_IMAGE_SIZE = 1024 * 1024 * 3; // equal to 3MB

    //Value to tell upload image to cloud is successful and is completed
    boolean isAllSuccess = true;

    // Create a Cloud Storage reference from the app
    private final FirebaseStorage storage;

    //Image GET AND EDIT Name Helper
    private final ImageNameGenerator imageNameGenerator;

    //List ref of image headers for adapter view. For preventing reduplicate loading
    private HashMap <Integer,StorageReference> refHeaders;

    //Loading animation
    public CircularProgressDrawable loadingAnimation;



    public ImageRequester(Context mContext) {
        this.mContext = mContext;
        storage = FirebaseStorage.getInstance();
        imageNameGenerator = new ImageNameGenerator(mContext);
        refHeaders = new HashMap<>();

        loadingAnimation = new CircularProgressDrawable(mContext);
        loadingAnimation.setStrokeWidth(5f);
        loadingAnimation.setCenterRadius(30f);
    }

    public void loadImageByRef(ImageView imageView, StorageReference riverRef) {
        loadingAnimation.start();
        Glide.with(imageView.getContext())
                .load(riverRef)
                .placeholder(loadingAnimation)
                .fitCenter()
                .into(imageView);
    }

    public void loadHeaderImage(int _id, String table_name, ImageView imageView) {

        //On load call back FireStore
        FirebaseCallBack<String> onCallBackHeaderName = items -> {
            //Note this is a list, but has one item. So we get 0;
            if (items.get(0) != null) {
                StorageReference refTemp = storage.getReference().child(IMAGE_FOLDER).child(items.get(0));
                refHeaders.put(_id, refTemp);
                loadImageByRef(imageView, refTemp);
            } else {
                imageView.setImageResource(R.drawable.ic_empty_shelter);
            }
        };



        //If we already load image name, we can re use it by this hash map
        if (refHeaders.get(_id) != null) {
            loadImageByRef(imageView, refHeaders.get(_id));
        } else {
            imageNameGenerator.getImageHeaderName(table_name, _id, onCallBackHeaderName);
        }
    }


    public void loadListRefToSliderAdapter(int _id, String table_name,
                                           ImageSliderAdapter imageSliderAdapter, SliderView sliderView) {

        imageNameGenerator.getCollectionOfImageNames(table_name, _id, new FirebaseCallBack<String>() {
            @Override
            public void onCallBack(List<String> items) {
                List<StorageReference> lstRef = new ArrayList<>();
                for (String item : items) {
                    StorageReference refTemp = storage.getReference().child(IMAGE_FOLDER).child(item);
                    lstRef.add(refTemp);
                }
                imageSliderAdapter.renewItems(lstRef);
                sliderView.setInfiniteAdapterEnabled(true);
            }
        });

    }



    public List<StorageReference> updateListUriFileToCloud(int id, String table_name, List<Uri> files) {
        Log.d(LOG_TAG, "updateListUriFileToCloud: house id " + id);
        List<StorageReference> tempRefs = new ArrayList<>();
        for (Uri file : files) {
            //Create unique image name
            String uniqueName = imageNameGenerator.initAnUniqueImageNameGenerate(table_name, id);

            //Init file
            StorageReference riverRef = storage.getReference().child(IMAGE_FOLDER).child(uniqueName);

            //Store ref as temp file for later use
            tempRefs.add(riverRef);

            //Put file to cloud
            riverRef.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> Log.d(LOG_TAG, "onSuccess: upload image for " + taskSnapshot.getStorage().getName()))
                    .addOnFailureListener(e -> Log.d(LOG_TAG, "onFailure: upload image failure: " + e.getMessage()));

        }
        return tempRefs;
    }

   public void deleteARefOnCloud(StorageReference abandonRef) {
        abandonRef.delete()
                .addOnFailureListener(e -> Log.d(LOG_TAG, "onFailure: delete image for " + abandonRef.getName() + " Exception: " + e.getMessage()))
                .addOnSuccessListener(unused -> Log.d(LOG_TAG, "onSuccess: delete image for " + abandonRef.getName()));
   }

   public void deleteAListOfRef(List<StorageReference> abandonRef) {
        for (StorageReference ref : abandonRef) {
            deleteARefOnCloud(ref);
        }
   }
}
