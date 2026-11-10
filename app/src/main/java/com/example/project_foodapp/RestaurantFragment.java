package com.example.project_foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class RestaurantFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        return inflater.inflate(R.layout.fragment_restaurant, container, false);
    }
    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        LinearLayout list = view.findViewById(R.id.storeList);
        for (StoreCatalog.Store store : StoreCatalog.stores()) {
            View card = getLayoutInflater().inflate(R.layout.item_store, list, false);
            ((ImageView) card.findViewById(R.id.storeLogo)).setImageResource(store.logo);
            ((TextView) card.findViewById(R.id.storeName)).setText(store.name);
            ((TextView) card.findViewById(R.id.storeAddress)).setText(store.address);
            ((TextView) card.findViewById(R.id.storeDescription)).setText(store.description + "  ·  Xem thực đơn");
            card.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), RestaurantActivity.class);
                intent.putExtra("store_id", store.id);
                startActivity(intent);
            });
            list.addView(card);
        }
    }
}
