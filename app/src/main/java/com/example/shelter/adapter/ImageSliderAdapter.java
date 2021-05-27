package com.example.shelter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.example.shelter.Network.ImageRequester;
import com.example.shelter.R;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends SliderViewAdapter<ImageSliderAdapter.ImageSliderAdapterVH> {

    private Context context;
    private boolean canEditItems = false;
    private boolean isItemsChange = false;

    private ImageRequester imageRequester;


    //List item that is delete
    private List<StorageReference> abandonRefs;
    //List item that display to layout
    private List<StorageReference> mSliderItems;

    public boolean isItemsChange() {
        return isItemsChange;
    }


    public List<StorageReference> getSliderItems() {
        return mSliderItems;
    }

    public List<StorageReference> getAbandonRefs() {
        return abandonRefs;
    }

    public ImageSliderAdapter(Context context) {
        this.context = context;
        mSliderItems = new ArrayList<>();
        imageRequester = new ImageRequester(context);
    }

    public ImageSliderAdapter(Context context, boolean canEditItems) {

        //Normal setting
        this.context = context;
        mSliderItems = new ArrayList<>();
        imageRequester = new ImageRequester(context);

        //Can edit setting
        abandonRefs = new ArrayList<>();
        this.canEditItems = canEditItems;
    }


    public void renewItems(List<StorageReference> sliderItems) {
        this.mSliderItems = sliderItems;
        isItemsChange = true;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        abandonRefs.add(mSliderItems.get(position));
        this.mSliderItems.remove(position);
        isItemsChange = true;
        notifyDataSetChanged();
    }


    public void addItem(StorageReference sliderItem) {
        this.mSliderItems.add(sliderItem);
        isItemsChange = true;
        notifyDataSetChanged();
    }

    public void addMultiItems(List<StorageReference> mSliderItems) {
        if (mSliderItems != null) {
            this.mSliderItems.addAll(mSliderItems);
            notifyDataSetChanged();
            isItemsChange = true;
        }
    }

    public void clearItems() {
        this.mSliderItems = new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public ImageSliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item_layout, null);
        return new ImageSliderAdapterVH(inflate);
    }


    @Override
    public void onBindViewHolder(ImageSliderAdapterVH viewHolder, final int position) {
        StorageReference sliderItem = mSliderItems.get(position);
        imageRequester.loadImageByRef(viewHolder.imageViewBackground, sliderItem);
        viewHolder.deleteItem.setOnClickListener(v -> ImageSliderAdapter.this.deleteItem(position));

    }

    @Override
    public int getCount() {
        //slider view count could be dynamic size
        return mSliderItems.size();
    }

    protected class ImageSliderAdapterVH extends SliderViewAdapter.ViewHolder {

        View itemView;
        ImageView imageViewBackground;
        ImageButton deleteItem;

        public ImageSliderAdapterVH(View itemView) {
            super(itemView);
            this.itemView = itemView;
            imageViewBackground = itemView.findViewById(R.id.image_slider_item);
            deleteItem = itemView.findViewById(R.id.delete_image_item);
            if (canEditItems) {
                deleteItem.setVisibility(View.VISIBLE);
            }
        }
    }
}
