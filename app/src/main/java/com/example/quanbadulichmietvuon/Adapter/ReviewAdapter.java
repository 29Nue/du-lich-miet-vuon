package com.example.quanbadulichmietvuon.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quanbadulichmietvuon.R;
import com.example.quanbadulichmietvuon.modules.Review;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        // get the review at the current position
        Review review = reviews.get(position);

        // set user name from review
        holder.txtUserName.setText(review.getUserName());

        // set rating bar value
        holder.ratingBarReview.setRating(review.getRating());

        // set review reason (comment)
        holder.txtReviewReason.setText(review.getReason());

        // format and set timestamp
        String formattedDate = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date(review.getTimestamp()));
        holder.tvTimestamp.setText(formattedDate);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView txtUserName, txtReviewReason, tvTimestamp;
        RatingBar ratingBarReview;

        public ReviewViewHolder(View itemView) {
            super(itemView);

            // initialize views from item_review.xml
            txtUserName = itemView.findViewById(R.id.txtUserEmail);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
            txtReviewReason = itemView.findViewById(R.id.txtReviewReason);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
