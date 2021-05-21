package com.example.shelter.staggeredgridlayout;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shelter.adapter.HouseCursorAdapter;
import com.example.shelter.R;

/**
 * Adapter used to show an asymmetric grid of products, with 2 items in the first column, and 1
 * item in the second column, and so on.
 */
public class StaggeredHouseCardRecyclerViewAdapter extends RecyclerView.Adapter<StaggeredHouseCardRecyclerViewAdapter.StaggeredHouseCardViewHolder> {

    public HouseCursorAdapter mCursorAdapter;
    public Context mContext;
    private RecyclerViewOnClickListener listener;

    public StaggeredHouseCardRecyclerViewAdapter (Context context, Cursor cursor) {
        mContext = context;
        mCursorAdapter = new HouseCursorAdapter(context, cursor);
    }

    public StaggeredHouseCardRecyclerViewAdapter (Context context, Cursor cursor, RecyclerViewOnClickListener listener) {
        mContext = context;
        mCursorAdapter = new HouseCursorAdapter(context, cursor);
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 3;
    }



    @NonNull
    @Override
    public StaggeredHouseCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = R.layout.staggered_house_card_first;
        if (viewType == 1) {
            layoutId = R.layout.staggered_house_card_second;
        } else if (viewType == 2) {
            layoutId = R.layout.staggered_house_card_third;
        }
        mCursorAdapter.setLayoutId(layoutId);
        View layoutView = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new StaggeredHouseCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull StaggeredHouseCardViewHolder holder, int position)  {
        Log.d(StaggeredHouseCardRecyclerViewAdapter.class.getSimpleName(), "item position" + position + "item count" + getItemCount());

        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }



    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    public interface RecyclerViewOnClickListener {
        void onClick (View v, int position, long id);
    }

    public class StaggeredHouseCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView houseImage;
        private TextView houseName;
        private TextView housePrice;
        private TextView houseDistance;
        private TextView houseArea;

        public StaggeredHouseCardViewHolder(@NonNull View itemView) {
            super(itemView);
            houseImage = itemView.findViewById(R.id.house_image);
            houseName = itemView.findViewById(R.id.house_card_name);
            //housePrice = itemView.findViewById(R.id.house_card_rent_cost);
            houseDistance = itemView.findViewById(R.id.house_card_distance);
            houseArea = itemView.findViewById(R.id.house_card_area);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Cursor cursor = mCursorAdapter.getCursor();
            cursor.moveToPosition(getAdapterPosition());
            listener.onClick(v, getAdapterPosition(), cursor.getLong(0));
        }
    }
}
