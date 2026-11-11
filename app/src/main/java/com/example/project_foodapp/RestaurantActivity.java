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
    private StoreCatalog.Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        store = StoreCatalog.find(getIntent().getStringExtra("store_id"));
        initViews();
        setupListeners();
        setupRecyclerView();
    }

    private void initViews() {
        store.showCover(findViewById(R.id.ivRestaurantBanner));
        btnBack = findViewById(R.id.btnBack);
        btnViewBasket = findViewById(R.id.btnViewBasket);
        rvFoodMenu = findViewById(R.id.rvFoodMenu);
        tvBasketItems = findViewById(R.id.tvBasketItems);
        tvBasketTotal = findViewById(R.id.tvBasketTotal);
        ((TextView) findViewById(R.id.tvRestaurantName)).setText(store.name);
        ((TextView) findViewById(R.id.tvRestaurantAddress)).setText(store.address);
        ((TextView) findViewById(R.id.tvRestaurantHours)).setText(store.description);
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
        foodList = store.menu();

        foodAdapter = new FoodAdapter(foodList, new FoodAdapter.OnQuantityChangeListener() {
            @Override
            public void onChange() {
                updateBasketSummary();
            }
        });
        rvFoodMenu.setAdapter(foodAdapter);
    }

    private void updateBasketSummary() {
        int totalItems = 0;
        double totalPrice = 0;

        for (Food food : foodList) {
            totalItems += food.getQuantity();
            totalPrice += (food.getPrice() * food.getQuantity());
        }

        tvBasketItems.setText(totalItems + " món");

        DecimalFormat formatter = new DecimalFormat("#,###");
        tvBasketTotal.setText(formatter.format(totalPrice).replace(',', '.') + " VND");
    }

    private void showOrderSuccess() {
        if (isFinishing() || isDestroyed()) return;
        View successView = getLayoutInflater().inflate(R.layout.dialog_order_success, null);
        androidx.appcompat.app.AlertDialog successDialog = new androidx.appcompat.app.AlertDialog.Builder(this).setView(successView).create();
        successDialog.setOnShowListener(ignored -> {
            if (successDialog.getWindow() != null) successDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        });
        successView.findViewById(R.id.closeSuccess).setOnClickListener(ignored -> successDialog.dismiss());
        successDialog.show();
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_basket_bottom_sheet, null);
        TextView empty = view.findViewById(R.id.tvEmptyBasket);
        android.widget.LinearLayout basketItems = view.findViewById(R.id.basketItems);
        ((TextView) view.findViewById(R.id.tvBasketStore)).setText(store.name + " · Món bạn đã chọn");
        int selected = 0;
        int subtotal = 0;
        DecimalFormat formatter = new DecimalFormat("#,###");
        for (Food food : foodList) {
            if (food.getQuantity() <= 0) continue;
            selected += food.getQuantity();
            subtotal += food.getPrice() * food.getQuantity();
            View row = getLayoutInflater().inflate(R.layout.item_basket, basketItems, false);
            ((ImageView) row.findViewById(R.id.basketImage)).setImageResource(food.getImageResId());
            ((TextView) row.findViewById(R.id.basketName)).setText(food.getName());
            ((TextView) row.findViewById(R.id.basketQuantity)).setText("Số lượng: " + food.getQuantity());
            ((TextView) row.findViewById(R.id.basketPrice)).setText(formatter.format((long) food.getPrice() * food.getQuantity()).replace(',', '.') + " ₫");
            basketItems.addView(row);
        }
        ((TextView) view.findViewById(R.id.tvSheetTotal)).setText(formatter.format(subtotal).replace(',', '.') + " ₫");
        if (selected == 0) {
            empty.setVisibility(View.VISIBLE);
            basketItems.setVisibility(View.GONE);
            view.findViewById(R.id.btnCheckout).setEnabled(false);
            view.findViewById(R.id.btnCheckout).setAlpha(0.45f);
        }
        view.findViewById(R.id.btnCheckout).setOnClickListener(v -> {
            java.util.ArrayList<java.util.Map<String, Object>> items = new java.util.ArrayList<>();
            int total = 0;
            for (Food food : foodList) if (food.getQuantity() > 0) {
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("storeId", store.id); item.put("storeName", store.name);
                item.put("name", food.getName()); item.put("price", food.getPrice()); item.put("quantity", food.getQuantity());
                items.add(item); total += food.getPrice() * food.getQuantity();
            }
            if (items.isEmpty()) { android.widget.Toast.makeText(this, "Giỏ hàng trống", android.widget.Toast.LENGTH_SHORT).show(); return; }
            View deliveryView = getLayoutInflater().inflate(R.layout.dialog_delivery, null);
            android.widget.EditText address = deliveryView.findViewById(R.id.deliveryAddress);
            android.widget.EditText phone = deliveryView.findViewById(R.id.deliveryPhone);
            androidx.appcompat.app.AlertDialog deliveryDialog = new androidx.appcompat.app.AlertDialog.Builder(this).setView(deliveryView).create();
            deliveryDialog.setOnShowListener(ignored -> {
                if (deliveryDialog.getWindow() != null) {
                    deliveryDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    deliveryDialog.getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                }
            });
            deliveryView.findViewById(R.id.cancelDelivery).setOnClickListener(ignored -> deliveryDialog.dismiss());
            final int orderTotal = total;
            View confirm = deliveryView.findViewById(R.id.confirmDelivery);
            confirm.setOnClickListener(ignored -> {
                if (address.getText().toString().trim().isEmpty()) { address.setError("Vui lòng nhập địa chỉ giao hàng"); address.requestFocus(); return; }
                if (phone.getText().toString().trim().isEmpty()) { phone.setError("Vui lòng nhập số điện thoại"); phone.requestFocus(); return; }
                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) { android.widget.Toast.makeText(this, "Vui lòng đăng nhập", android.widget.Toast.LENGTH_SHORT).show(); return; }
                java.util.Map<String, Object> order = new java.util.HashMap<>();
                order.put("customerId", user.getUid()); order.put("address", address.getText().toString().trim()); order.put("phone", phone.getText().toString().trim());
                order.put("items", items); order.put("total", orderTotal); order.put("status", "pending"); order.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                confirm.setEnabled(false);
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("orders").add(order).addOnSuccessListener(ref -> {
                    deliveryDialog.dismiss();
                    for (Food food : foodList) food.setQuantity(0); foodAdapter.notifyDataSetChanged(); updateBasketSummary(); bottomSheetDialog.dismiss();
                    showOrderSuccess();
                }).addOnFailureListener(e -> { confirm.setEnabled(true); android.widget.Toast.makeText(this, "Không thể đặt hàng: " + e.getLocalizedMessage(), android.widget.Toast.LENGTH_LONG).show(); });
            });
            deliveryDialog.show();
        });
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.setOnShowListener(dialog -> {
            View bottom = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottom != null) bottom.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        });
        bottomSheetDialog.show();
    }
}