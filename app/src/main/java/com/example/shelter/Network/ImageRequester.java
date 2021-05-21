package com.example.shelter.Network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.net.Uri;
import android.os.LimitExceededException;
import android.util.Log;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.example.shelter.Data.ShelterDBContract.HouseEntry;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.IllegalBlockSizeException;


public class ImageRequester {

    static final private String LOG_TAG = ImageRequester.class.getSimpleName();
    private Context mContext;
    //  In using for separating image_name domain in order to get an unique image name with given  key and name of the Entry
    static final private String SEPARATE = "_";
    static final private String IMAGE_FOLDER = "image/";
    static final private int IMAGE_HEADER = 1;

    //Max image size can have
    static final private long MAX_IMAGE_SIZE = 1024 * 1024 * 3; // equal to 3MB

    //Value to tell upload image to cloud is successful and is completed
    boolean isAllSuccess = true;
    boolean isCompleted = true;

    // Create a Cloud Storage reference from the app
    public FirebaseStorage storage;

    public ImageRequester(Context mContext) {
        this.mContext = mContext;
        storage = FirebaseStorage.getInstance();

    }


    public StorageReference getRefHeaderImageOnCloud(int _id, String table_name) {
        String imageName = table_name + SEPARATE + _id + SEPARATE + IMAGE_HEADER;
        String imagePath = IMAGE_FOLDER + imageName + ".jpg";
        Log.d(LOG_TAG, "path_name" + imagePath);
        StorageReference ref = storage.getReference().child(imagePath);
        return ref;
    }

    public List<StorageReference> getListRefImageOnCloud(int _id, String table_name, int images_count) {

        List<StorageReference> lstRef = new ArrayList<>();
        for (int i = IMAGE_HEADER; i <= images_count; i++) {
            String imageName = table_name + SEPARATE + _id + SEPARATE + i;
            String imagePath = IMAGE_FOLDER + imageName + ".jpg";
            StorageReference ref;
            ref = storage.getReference().child(imagePath);
            lstRef.add(ref);

        }
        return lstRef;
    }

    public List<StorageReference> getListRefImageOnCloud(int _id, String table_name, int start, int end) {

        List<StorageReference> lstRef = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String imageName = table_name + SEPARATE + _id + SEPARATE + i;
            String imagePath = IMAGE_FOLDER + imageName + ".jpg";
            StorageReference ref;
            ref = storage.getReference().child(imagePath);
            lstRef.add(ref);

        }
        return lstRef;
    }

    public List<byte[]> downloadListImageFromRefs(List<StorageReference> storageReferences) {
        List<byte[]> listBytes = new ArrayList<>();

        for (StorageReference item : storageReferences) {
            item.getBytes(MAX_IMAGE_SIZE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    // Data for "images/island.jpg" is returns, use this as needed
                    listBytes.add(bytes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e(LOG_TAG, "onFailure: getListBitMapFromRef  Error when load image  for ref" + item.getName());
                }
            });

        }

        return listBytes;
    }

    /**
     * Note that when update a file that already exists on cloud it will replace it **/
    public boolean uploadListImagesBytesToCloud(List<byte[]> listBytes, String table, int id, ProgressBar progressBar) {
        isAllSuccess = true;
        progressBar.setVisibility(View.VISIBLE);
        if (table.equals(HouseEntry.TABLE_NAME)) {
            for (int i = 0; i < listBytes.size(); i++) {
                String imageName = table + SEPARATE + id + SEPARATE + (i + 1);
                String imagePath = IMAGE_FOLDER + imageName + ".jpg";
                StorageReference ref = storage.getReference().child(imagePath);
                int finalI = i;
                ref.putBytes(listBytes.get(i)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.e(LOG_TAG, "onFailure: uploadListBitmapImagesToCloud " + imagePath);
                        Log.e(LOG_TAG, "onFailure: " + exception.getMessage());
                        isAllSuccess = false;
                        if (finalI == listBytes.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Log.i(LOG_TAG, "onSuccess: file upload success " + taskSnapshot.getStorage().getPath());

                        if (finalI == listBytes.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });


            }
        }
        return isAllSuccess;
    }

    /**
     * Note that when update a file that already exists on cloud it will replace it **/
    public boolean uploadListImagesBytesToCloud(List<byte[]> listBytes, String table, int id, ProgressBar progressBar, int startFrom) {
        isAllSuccess = true;
        progressBar.setVisibility(View.VISIBLE);
        if (table.equals(HouseEntry.TABLE_NAME)) {
            for (int i = 0; i < listBytes.size(); i++) {
                String imageName = table + SEPARATE + id + SEPARATE + (startFrom + i + 1);
                String imagePath = IMAGE_FOLDER + imageName + ".jpg";
                StorageReference ref = storage.getReference().child(imagePath);
                int finalI = i;
                ref.putBytes(listBytes.get(i)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.e(LOG_TAG, "onFailure: uploadListBitmapImagesToCloud " + imagePath);
                        Log.e(LOG_TAG, "onFailure: " + exception.getMessage());
                        isAllSuccess = false;
                        if (finalI == listBytes.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Log.i(LOG_TAG, "onSuccess: file upload success " + taskSnapshot.getStorage().getPath());

                        if (finalI == listBytes.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });


            }
        }
        return isAllSuccess;
    }

    /**
     * Note that when update a file that already exists on cloud it will replace it **/
    public boolean uploadListImagesUriToCloud(List<Uri> uriList, String table, int id, ProgressBar progressBar) {
        isAllSuccess = true;
        progressBar.setVisibility(View.VISIBLE);
        if (table.equals(HouseEntry.TABLE_NAME) || table.equals("test")) {
            for (int i = 0; i < uriList.size(); i++) {
                String imageName = table + SEPARATE + id + SEPARATE + (i + 1);
                String imagePath = IMAGE_FOLDER + imageName + ".jpg";
                StorageReference ref = storage.getReference().child(imagePath);

                int finalI = i;
                ref.putFile(uriList.get(i)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.e(LOG_TAG, "onFailure: uploadListBitmapImagesToCloud " + imagePath);
                        Log.e(LOG_TAG, "onFailure: " + exception.getMessage());
                        isAllSuccess = false;

                        if (finalI == uriList.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Log.i(LOG_TAG, "onSuccess: file upload success " + taskSnapshot.getStorage().getPath());

                        if (finalI == uriList.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }

        }
        return isAllSuccess;
    }

    /**
     * Note that when update a file that already exists on cloud it will replace it **/
    public boolean uploadListImagesUriToCloud(List<Uri> uriList, String table, int id, ProgressBar progressBar, int startFrom) {
        isAllSuccess = true;
        progressBar.setVisibility(View.VISIBLE);
        if (table.equals(HouseEntry.TABLE_NAME) || table.equals("test")) {
            for (int i = 0; i < uriList.size() ; i++) {
                String imageName = table + SEPARATE + id + SEPARATE + (startFrom + i + 1);
                String imagePath = IMAGE_FOLDER + imageName + ".jpg";
                StorageReference ref = storage.getReference().child(imagePath);


                int finalI = i;
                ref.putFile(uriList.get(i)).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.e(LOG_TAG, "onFailure: uploadListBitmapImagesToCloud " + imagePath);
                        Log.e(LOG_TAG, "onFailure: " + exception.getMessage());
                        isAllSuccess = false;

                        if (finalI == uriList.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                        // ...
                        Log.i(LOG_TAG, "onSuccess: file upload success " + taskSnapshot.getStorage().getPath());

                        if (finalI == uriList.size() - 1) {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }

        }
        return isAllSuccess;
    }

    public void deleteImagesFromCloud(String table_name, int id, int start, int end) {
        for (int i = start; i <= end; i++) {
            String imageName = table_name + SEPARATE + id + SEPARATE + i;
            String imagePath = IMAGE_FOLDER + imageName + ".jpg";
            StorageReference desertRef = storage.getReference().child(imagePath);
            // Delete the file
            desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // File deleted successfully
                    Log.d(LOG_TAG, "onSuccess: deleteImagesFromCloud " + imagePath);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Uh-oh, an error occurred!
                    Log.d(LOG_TAG, "onFailure: deleteImagesFromCloud " + imagePath);
                }
            });
        }

    }

}
