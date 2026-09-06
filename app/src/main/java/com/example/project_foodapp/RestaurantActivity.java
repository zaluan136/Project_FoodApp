package com.example.project_foodapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RestaurantActivity extends AppCompatActivity {

    private RecyclerView rvFoodMenu;
    private FoodAdapter foodAdapter;
    private List<Food> foodList;
    private RelativeLayout btnViewBasket;
    private TextView tvBasketItems, tvBasketTotal;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        rvFoodMenu = findViewById(R.id.rvFoodMenu);
        btnViewBasket = findViewById(R.id.btnViewBasket);
        tvBasketItems = findViewById(R.id.tvBasketItems);
        tvBasketTotal = findViewById(R.id.tvBasketTotal);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        initFoodData();
        setupRecyclerView();
        updateBasketUI();

        btnViewBasket.setOnClickListener(v -> showBasketDialog());
    }

    private void initFoodData() {
        foodList = new ArrayList<>();
        foodList.add(new Food("Black Coffee", 49000, R.drawable.ic_black_coffee));
        foodList.add(new Food("Banh Mi", 39000, R.drawable.ic_banh_mi));
        foodList.add(new Food("Milk Tea", 45000, R.drawable.ic_milk_tea));
        foodList.add(new Food("Fried Food", 55000, R.drawable.ic_food));
    }

    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(foodList, this::updateBasketUI);
        rvFoodMenu.setLayoutManager(new LinearLayoutManager(this));
        rvFoodMenu.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvFoodMenu.setAdapter(foodAdapter);
    }

    private void updateBasketUI() {
        int totalItems = 0;
        int totalPrice = 0;

        for (Food food : foodList) {
            if (food.getQuantity() > 0) {
                totalItems += food.getQuantity();
                totalPrice += (food.getQuantity() * food.getPrice());
            }
        }

        DecimalFormat formatter = new DecimalFormat("#,###");
        String formattedPrice = formatter.format(totalPrice).replace(',', '.');

        tvBasketItems.setText(totalItems + (totalItems <= 1 ? " item" : " items"));
        tvBasketTotal.setText(formattedPrice + " VND");
    }

    private void showBasketDialog() {
        int totalItems = 0;
        int totalPrice = 0;
        StringBuilder details = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,###");

        for (Food food : foodList) {
            if (food.getQuantity() > 0) {
                int itemTotal = food.getQuantity() * food.getPrice();
                totalItems += food.getQuantity();
                totalPrice += itemTotal;

                details.append("• ").append(food.getName())
                        .append("  x").append(food.getQuantity())
                        .append("  = ").append(formatter.format(itemTotal).replace(',', '.')).append(" VND\n");
            }
        }

        if (totalItems == 0) {
            Toast.makeText(this, "Giỏ hàng đang trống! Vui lòng chọn món bằng nút (+)", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.dialog_basket_bottom_sheet, null);
        dialog.setContentView(sheetView);

        TextView tvBasketDetails = sheetView.findViewById(R.id.tvBasketDetails);
        TextView tvSheetTotalPrice = sheetView.findViewById(R.id.tvSheetTotalPrice);

        tvBasketDetails.setText(details.toString().trim());
        tvSheetTotalPrice.setText(formatter.format(totalPrice).replace(',', '.') + " VND");

        sheetView.findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            Toast.makeText(this, "Đặt đơn hàng thành công!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}