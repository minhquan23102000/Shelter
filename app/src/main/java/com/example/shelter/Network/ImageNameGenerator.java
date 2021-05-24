package com.example.shelter.Network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ImageNameGenerator {
    static final public String TAG = ImageNameGenerator.class.getName();

    static final private String SEPARATE = "_";
    static final private String IMAGE_HEADER = "1";
    static final private String HOUSE_IMAGE_PARENT_COLLECTION = "house_images";

    private final FirebaseFirestore db;
    private final Context mContext;


    public ImageNameGenerator(Context context) {
        db = FirebaseFirestore.getInstance();
        mContext = context;
    }


    public String initAnUniqueImageNameGenerate(String table_name, int id) {
        return table_name + SEPARATE + id + SEPARATE + UUID.randomUUID().toString() + ".jpg";
    }

    public void getImageHeaderName(String table_name, int id, FirebaseCallBack firebaseCallBack)  {
        String collectionName = table_name + SEPARATE + id;
         db.collection(HOUSE_IMAGE_PARENT_COLLECTION).document(collectionName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        List<String> imageHeaderName = new ArrayList<>();
                        imageHeaderName.add(documentSnapshot.getString(IMAGE_HEADER));
                        firebaseCallBack.onCallBack(imageHeaderName);

                    } else {
                        Log.d(TAG, "Failure to retrieve image name from cloud");
                        Toast.makeText(mContext,"Failure to retrieve image name from cloud: ", Toast.LENGTH_LONG ).show();
                    }


                });
    }

    public void getCollectionOfImageNames(String table_name, int id, FirebaseCallBack firebaseCallBack) {
        //Construct house images collection
        String collectionName = table_name + SEPARATE + id;

        db.collection(HOUSE_IMAGE_PARENT_COLLECTION).document(collectionName).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> mArrayList = new ArrayList<>();
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> map = document.getData();
                            if (map != null) {
                                for (Map.Entry<String, Object> entry : map.entrySet()) {
                                    mArrayList.add(entry.getValue().toString());
                                }
                            }
                            firebaseCallBack.onCallBack(mArrayList);
                        }
                    } else {
                        Log.d(TAG, "Failure to retrieve image name from cloud: ");
                        Toast.makeText(mContext,"Failure to retrieve image name from cloud: ", Toast.LENGTH_LONG ).show();
                    }
                });
    }


    public void generateCollectionOfImageNamesToFireStore(String table_name, int id, List<StorageReference> referenceList) {
        Map<String, Object> images = new HashMap<>();
        String collectionName = table_name + SEPARATE + id;

        for (int i = 0; i < referenceList.size(); i++) {
            String key = i+1 + "";
            String value = referenceList.get(i).getName();
            images.put(key, value);
        }
        db.collection(HOUSE_IMAGE_PARENT_COLLECTION).document(collectionName)
                .set(images)
                .addOnSuccessListener(unused -> Log.d(TAG, "generateCollectionOfImageNamesToFireBase successfully " + collectionName + "_count_" + referenceList.toString()))
                .addOnFailureListener(e -> Log.d(TAG, "generateCollectionOfImageNamesToFireBase failed " + collectionName + "_count_" + referenceList.toString()));
    }

    public void removeCollection(String table_name, int id) {
        String collectionName = table_name + SEPARATE + id;
        db.collection(HOUSE_IMAGE_PARENT_COLLECTION).document(collectionName).delete()
                .addOnFailureListener(e -> Log.d(TAG, "removeCollection Failed for " + collectionName));
    }

    public void renewCollection(String table_name, int id, List<StorageReference> referenceList) {
        removeCollection(table_name, id);
        generateCollectionOfImageNamesToFireStore(table_name, id,  referenceList);
    }
}
