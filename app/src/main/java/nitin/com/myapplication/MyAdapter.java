package nitin.com.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
   ArrayList<POIBean> mData = new ArrayList<>();
   Context context;
    private static final int VIEW_TYPE_EMPTY_LIST_PLACEHOLDER = 0;
    private static final int VIEW_TYPE_OBJECT_VIEW = 1;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(Context context,ArrayList<POIBean> data) {
        mData = data;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (mData.isEmpty()) {
            return VIEW_TYPE_EMPTY_LIST_PLACEHOLDER;
        } else {
            return VIEW_TYPE_OBJECT_VIEW;
        }
    }
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem;
        ViewHolder viewHolder;
        switch(viewType) {
            case VIEW_TYPE_EMPTY_LIST_PLACEHOLDER:
                listItem= layoutInflater.inflate(R.layout.activity_row_empty_list, parent, false);
                viewHolder = new ViewHolder(listItem);
                break;
            case VIEW_TYPE_OBJECT_VIEW:
                listItem= layoutInflater.inflate(R.layout.activity_view_record, parent, false);
                viewHolder = new ViewHolder(listItem);
                break;
            default:
                viewHolder = new ViewHolder(null);
                break;
        }
        // create a new view


        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        if(holder instanceof ViewHolder) {
            ((ViewHolder) holder).idTv.setText(""+mData.get(position).getID());
            ((ViewHolder) holder).titleTextView.setText(mData.get(position).getName());
            ((ViewHolder) holder).etLat.setText("" +mData.get(position).getLat());
            ((ViewHolder) holder).etLng.setText("" + mData.get(position).getLng());
            ((ViewHolder) holder).relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String id = ((ViewHolder) holder).idTv.getText().toString();
                    String title = ((ViewHolder) holder).titleTextView.getText().toString();
                    String lat = ((ViewHolder) holder).etLat.getText().toString();
                    String lng = ((ViewHolder) holder).etLng.getText().toString();

                    Intent modify_intent = new Intent(context, ModifyStoreActivity.class);
                    modify_intent.putExtra("title", title);
                    modify_intent.putExtra("lat", lat);
                    modify_intent.putExtra("lng", lng);
                    modify_intent.putExtra("id", id);

                    context.startActivity(modify_intent);
                }
            });
        }else {
            ((EmptyViewHolder) holder).noData.setText("No Data");
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView,etLat,etLng,idTv;
        public RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
             relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relative_layout);
             idTv = (TextView) itemView.findViewById(R.id.idTv);
             titleTextView = (TextView) itemView.findViewById(R.id.title);
             etLat = (TextView) itemView.findViewById(R.id.et_lat);
             etLng = (TextView) itemView.findViewById(R.id.et_lng);
        }
    }
    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView noData;
        public EmptyViewHolder(View itemView) {
            super(itemView);
            noData = (TextView) itemView.findViewById(R.id.empty);

        }
    }
}