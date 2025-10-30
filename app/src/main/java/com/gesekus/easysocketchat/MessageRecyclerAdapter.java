package com.gesekus.easysocketchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MyMessageViewHolder> {
    private List<MessageItem> messageList;

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
        holder.messageContainer2.setText(messageItem.getMessage());
        holder.date.setText(messageItem.getLocalDateTime().format(formatter));
        holder.name.setText(messageItem.getUser());

        ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) holder.messageContainer.getLayoutParams();

        if (messageItem.isFromServer()) {
            holder.messageContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.darkGrey));
            holder.messageContainer2.setText(messageItem.getUser().substring(0, 2));
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        } else {
            holder.messageContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.blue));
            layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET;
            layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        }

        holder.messageContainer.setLayoutParams(layoutParams);
        holder.messageContainer.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MyMessageViewHolder extends RecyclerView.ViewHolder {
        MaterialButton messageContainer;
        MaterialButton messageContainer2;
        TextView date;
        TextView name;


        public MyMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageContainer2 = itemView.findViewById(R.id.messageContainer2);
            date = itemView.findViewById(R.id.date);
            name = itemView.findViewById(R.id.name);
        }
    }
}
