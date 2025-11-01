package com.gesekus.easysocketchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MyMessageViewHolder> {
    private final List<MessageItem> messageList;

    public MessageRecyclerAdapter(List<MessageItem> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MyMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_layout, parent, false);
        return new MyMessageViewHolder(view);
    }

    @SuppressLint({"ResourceAsColor", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull MyMessageViewHolder holder, int position) {
        MessageItem messageItem = messageList.get(position);
        Context context = holder.itemView.getContext();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        holder.messageContainer.setText(messageItem.getMessage());
        holder.date.setText(messageItem.getLocalDateTime().format(formatter));
        holder.name.setText(messageItem.getUser());

        ConstraintLayout rootLayout = holder.rootLayout;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(rootLayout);

        if (messageItem.isFromServer()) {
            holder.messageContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.darkGrey));
            if(messageItem.getUser() != null && messageItem.getUser().length() >= 2) {
                holder.messageContainer2.setText(messageItem.getUser().substring(0, 2));
            }
            holder.messageContainer2.setVisibility(View.VISIBLE);
            holder.name.setVisibility(View.VISIBLE);

            // Align message to the left
            constraintSet.clear(R.id.messageContainer, ConstraintSet.END);
            constraintSet.connect(R.id.messageContainer, ConstraintSet.START, R.id.messageContainer2, ConstraintSet.END, 8);
            constraintSet.clear(R.id.name, ConstraintSet.END);
            constraintSet.connect(R.id.name, ConstraintSet.START, R.id.messageContainer2, ConstraintSet.END, 8);

        } else {
            holder.messageContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
            holder.messageContainer2.setVisibility(View.GONE);
            holder.name.setVisibility(View.GONE);

            // Align message to the right
            constraintSet.clear(R.id.messageContainer, ConstraintSet.START);
            constraintSet.connect(R.id.messageContainer, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        }

        constraintSet.applyTo(rootLayout);
        holder.messageContainer.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rootLayout;
        MaterialButton messageContainer;
        MaterialButton messageContainer2;
        TextView date;
        TextView name;


        public MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            rootLayout = itemView.findViewById(R.id.root_layout);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageContainer2 = itemView.findViewById(R.id.messageContainer2);
            date = itemView.findViewById(R.id.date);
            name = itemView.findViewById(R.id.name);
        }
    }
}
