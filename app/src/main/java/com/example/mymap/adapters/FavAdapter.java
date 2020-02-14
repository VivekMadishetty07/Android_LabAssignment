package com.example.mymap.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mymap.R;
import com.example.mymap.activities.FavoritePlaces;
import com.example.mymap.model.UserData;

import java.util.List;

public class FavAdapter extends RecyclerView.Adapter<com.example.mymap.adapters.FavAdapter.MyViewHolder>
{
    private Context context;
    List<UserData> userData;
    int count = 0;
    String catcompare="";
    public static int positioncolor = 0;

    public FavAdapter(Context contexts, List<UserData> user)
    {
        this.context = contexts;
        this.userData = user;
    }

    @Override
    public com.example.mymap.adapters.FavAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fav_layout, parent, false);
        return new com.example.mymap.adapters.FavAdapter.MyViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(final com.example.mymap.adapters.FavAdapter.MyViewHolder holder, final int position)
    {
        holder.txt_iddata.setText(""+userData.get(position).id);
        holder.txt_countrydata.setText(""+userData.get(position).getCountry());
        holder.txt_statedata.setText(""+userData.get(position).getState());
        holder.txt_citydata.setText(""+userData.get(position).getCity());
        holder.txt_addressdata.setText(""+userData.get(position).getAddress());
        holder.txt_postaldata.setText(""+userData.get(position).getPostalCode());
        holder.txt_latdata.setText(""+userData.get(position).getLatitude());
        holder.txt_longdata.setText(""+userData.get(position).getLongitude());

        holder.card_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                ((FavoritePlaces)context).deletefav(userData.get(position));
                return false;
            }
        });


        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((FavoritePlaces)context).editfav(userData.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {

        return userData.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txt_iddata
                ,txt_countrydata
                ,txt_statedata
                ,txt_citydata
                ,txt_postaldata
                ,txt_addressdata
                ,txt_latdata,txt_longdata;

        CardView card_view;

        public MyViewHolder(View view) {
            super(view);

            card_view = view.findViewById(R.id.card_view);
            txt_iddata = view.findViewById(R.id.txt_iddata);
            txt_countrydata = view.findViewById(R.id.txt_countrydata);
            txt_statedata = view.findViewById(R.id.txt_statedata);
            txt_citydata = view.findViewById(R.id.txt_citydata);
            txt_postaldata = view.findViewById(R.id.txt_postaldata);
            txt_addressdata = view.findViewById(R.id.txt_addressdata);
            txt_latdata = view.findViewById(R.id.txt_latdata);
            txt_longdata = view.findViewById(R.id.txt_longdata);
        }
    }

    public static void recposition(int position)
    {
        positioncolor = position;
    }
}
