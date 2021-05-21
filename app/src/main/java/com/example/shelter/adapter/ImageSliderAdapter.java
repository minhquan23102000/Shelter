package com.example.shelter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.shelter.R;
import com.google.firebase.storage.StorageReference;
import com.smarteist.autoimageslider.SliderViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class ImageSliderAdapter extends SliderViewAdapter<ImageSliderAdapter.ImageSliderAdapterVH> {

    private Context context;
    private List<StorageReference> mSliderItems;
    private boolean canDeleteItem = false;

    public List<StorageReference> getSliderItems() {
        return mSliderItems;
    }

    public ImageSliderAdapter(Context context) {
        this.context = context;
        mSliderItems = new ArrayList<>();
    }

    public ImageSliderAdapter(Context context, boolean canDeleteItem) {
        this.context = context;
        mSliderItems = new ArrayList<>();
        this.canDeleteItem = canDeleteItem;
    }

    public ImageSliderAdapter(Context context, List<StorageReference> sliderItems) {
        this.context = context;
        mSliderItems = sliderItems;

    }

    public ImageSliderAdapter(Context context, List<StorageReference> sliderItems, boolean canDeleteItem) {
        this.context = context;
        mSliderItems = sliderItems;
        this.canDeleteItem = canDeleteItem;
    }

    public void renewItems(List<StorageReference> sliderItems) {
        this.mSliderItems = sliderItems;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        this.mSliderItems.remove(position);
        notifyDataSetChanged();
    }


    public void addItem(StorageReference sliderItem) {
        this.mSliderItems.add(sliderItem);
        notifyDataSetChanged();
    }

    public void addMultiItems(List<StorageReference> mSliderItems) {
        if (mSliderItems != null) {
            this.mSliderItems.addAll(mSliderItems);
            notifyDataSetChanged();
        }
    }

    @Override
    public ImageSliderAdapterVH onCreateViewHolder(ViewGroup parent) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_slider_item_layout, null);
        return new ImageSliderAdapterVH(inflate);
    }


    @Override
    public void onBindViewHolder(ImageSliderAdapterVH viewHolder, final int position) {
        StorageReference sliderItem = mSliderItems.get(position);

        Glide.with(viewHolder.itemView)
                .load(sliderItem)
                .fitCenter()
                .into(viewHolder.imageViewBackground);

        viewHolder.deleteItem.setOnClickListener(v -> {
            deleteItem(position);
        });
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
            if (canDeleteItem) {
                deleteItem.setVisibility(View.VISIBLE);
            }
        }
    }
}
