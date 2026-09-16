package com.example.project_foodapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RelativeLayout btnViewBasket;
    private RecyclerView rvFoodMenu;
    private TextView tvBasketItems, tvBasketTotal;
    private FoodAdapter foodAdapter;
    private List<Food> foodList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        initViews();
        setupListeners();
        setupRecyclerView();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnViewBasket = findViewById(R.id.btnViewBasket);
        rvFoodMenu = findViewById(R.id.rvFoodMenu);
        tvBasketItems = findViewById(R.id.tvBasketItems);
        tvBasketTotal = findViewById(R.id.tvBasketTotal);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnViewBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });
    }

    private void setupRecyclerView() {
        rvFoodMenu.setLayoutManager(new LinearLayoutManager(this));
        foodList = new ArrayList<>();

        loadDummyData();

        foodAdapter = new FoodAdapter(foodList, new FoodAdapter.OnQuantityChangeListener() {
            @Override
            public void onChange() {
                updateBasketSummary();
            }
        });
        rvFoodMenu.setAdapter(foodAdapter);
    }

    private void loadDummyData() {
        foodList.add(new Food("Black Coffee", 49000, R.drawable.ic_black_coffee));
        foodList.add(new Food("Banh Mi", 39000, R.drawable.ic_banh_mi));
        foodList.add(new Food("Milk Tea", 45000, R.drawable.ic_milk_tea));
    }

    private void updateBasketSummary() {
        int totalItems = 0;
        double totalPrice = 0;

        for (Food food : foodList) {
            totalItems += food.getQuantity();
            totalPrice += (food.getPrice() * food.getQuantity());
        }

        tvBasketItems.setText(totalItems + " item");

        DecimalFormat formatter = new DecimalFormat("#,###");
        tvBasketTotal.setText(formatter.format(totalPrice).replace(',', '.') + " VND");
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_basket_bottom_sheet, null);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();
    }
}