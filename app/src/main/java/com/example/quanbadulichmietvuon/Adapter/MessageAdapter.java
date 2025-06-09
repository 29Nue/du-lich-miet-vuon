package com.example.quanbadulichmietvuon.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUser;

    // Constructor
    public MessageAdapter(List<Message> messageList, String currentUser) {
        this.messageList = messageList;
        this.currentUser = currentUser;
    }

    // Phân biệt viewType để inflate layout trái hoặc phải
    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        // Nếu sender là currentUser thì trả về 1 (tin nhắn bên phải)
        if (message.getSender().equals(currentUser)) {
            return 1;
        } else {
            return 0; // bên trái
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 1) {
            // Tin nhắn bên phải (current user)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right, parent, false);
        } else {
            // Tin nhắn bên trái (người khác)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.textMessage.setText(message.getMessage());

        if (message.getSender().equals(currentUser)) {
            holder.textSender.setText("");
        } else {
            holder.textSender.setText(message.getSender());
        }

        holder.textTime.setText(formatTime(message.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textSender, textMessage, textTime;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textSender = itemView.findViewById(R.id.textSender);
            textMessage = itemView.findViewById(R.id.textMessage);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
