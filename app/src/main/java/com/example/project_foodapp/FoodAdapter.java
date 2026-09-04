package com.example.project_foodapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DecimalFormat;
import java.util.List;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    public interface OnQuantityChangeListener {
        void onChange();
    }

    private List<Food> foodList;
    private OnQuantityChangeListener listener;

    public FoodAdapter(List<Food> foodList, OnQuantityChangeListener listener) {
        this.foodList = foodList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        Food item = foodList.get(position);
        DecimalFormat formatter = new DecimalFormat("#,###");

        holder.tvFoodName.setText(item.getName());
        holder.tvFoodPrice.setText(formatter.format(item.getPrice()).replace(',', '.') + " VND");
        holder.ivFoodThumb.setImageResource(item.getImageResId());

        if (item.getQuantity() > 0) {
            holder.btnMinus.setVisibility(View.VISIBLE);
            holder.tvQuantity.setVisibility(View.VISIBLE);
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        } else {
            holder.btnMinus.setVisibility(View.GONE);
            holder.tvQuantity.setVisibility(View.GONE);
        }

        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            notifyItemChanged(position);
            if (listener != null) listener.onChange();
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 0) {
                item.setQuantity(item.getQuantity() - 1);
                notifyItemChanged(position);
                if (listener != null) listener.onChange();
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList != null ? foodList.size() : 0;
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodThumb;
        TextView tvFoodName, tvFoodPrice, tvQuantity;
        Button btnPlus, btnMinus;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodThumb = itemView.findViewById(R.id.ivFoodThumb);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}