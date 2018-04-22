package com.idealist.stocks;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by idealist88888 on 21.04.18.
 */

public class RecAdapter extends RecyclerView.Adapter<RecAdapter.ViewHolder> {


    private Context context;
    private List<User> userList;

    public RecAdapter(Context context) {

        this.context = context;
        userList = new ArrayList<>();
    }

    public void addContent(List<User> newUserList) {

        if (true) {

            userList = newUserList;
            notifyDataSetChanged();
            return;
        }

        int size = userList.size();

        List<User> addList = newUserList.subList(size, newUserList.size());

        userList.addAll(addList);
        notifyItemRangeInserted(size, addList.size());
    }

    public void addItem(User user) {

        userList.add(user);
        notifyItemInserted(userList.size() - 1);
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        int layout = R.layout.item_user;

        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        User user = userList.get(position);

        holder.textName.setText(user.getName());
        holder.textScore.setText(user.getBalance() + "");
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textName;
        private TextView textScore;

        public ViewHolder(View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.item_user_name);
            textScore = itemView.findViewById(R.id.item_user_score);
        }

    }


}
