package com.example.project_foodapp;

import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.HashMap;
import java.util.Map;

public class DriverActivity extends AppCompatActivity {
    private ListenerRegistration pending, assigned;
    private LinearLayout openList, myList;
    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(243, 247, 242));
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(30), dp(20), dp(30)); scroll.addView(root); setContentView(scroll);
        TextView brand = text("FOODNOW  /  SHIPPER", 12, "#168755", true); root.addView(brand);
        TextView title = text("Xin chào, đối tác!", 28, "#173B30", true); title.setPadding(0, dp(8), 0, 0); root.addView(title);
        TextView subtitle = text("Sẵn sàng cho những chuyến giao hàng hôm nay?", 14, "#71867A", false); subtitle.setPadding(0, dp(6), 0, dp(18)); root.addView(subtitle);
        Button logout = new Button(this); logout.setText("Đăng xuất"); styleButton(logout, false); root.addView(logout);
        logout.setOnClickListener(v -> { FirebaseAuth.getInstance().signOut(); startActivity(new android.content.Intent(this, SignInActivity.class)); finish(); });
        section(root, "Đơn chờ nhận", "Chọn đơn phù hợp để bắt đầu giao hàng");
        openList = new LinearLayout(this); openList.setOrientation(LinearLayout.VERTICAL); root.addView(openList);
        section(root, "Đơn của bạn", "Theo dõi các đơn đã nhận");
        myList = new LinearLayout(this); myList.setOrientation(LinearLayout.VERTICAL); root.addView(myList);
        FirebaseFirestore db = FirebaseFirestore.getInstance(); String uid = FirebaseAuth.getInstance().getUid();
        pending = db.collection("orders").whereEqualTo("status", "pending").addSnapshotListener((s,e) -> render(openList,s,e,true));
        assigned = db.collection("orders").whereEqualTo("driverId", uid).addSnapshotListener((s,e) -> render(myList,s,e,false));
    }
    private void render(LinearLayout list, QuerySnapshot snap, FirebaseFirestoreException error, boolean available) {
        list.removeAllViews();
        if (error != null) { TextView t = new TextView(this); t.setText("Không tải được đơn hàng: " + error.getMessage()); list.addView(t); return; }
        if (snap == null || snap.isEmpty()) { TextView t = new TextView(this); t.setText("Không có đơn hàng."); list.addView(t); return; }
        for (DocumentSnapshot doc : snap) {
            String store = "";
            Object rawItems = doc.get("items");
            if (rawItems instanceof java.util.List && !((java.util.List<?>) rawItems).isEmpty()) {
                Object first = ((java.util.List<?>) rawItems).get(0);
                if (first instanceof java.util.Map) {
                    Object name = ((java.util.Map<?, ?>) first).get("storeName");
                    if (name instanceof String) store = "\nCửa hàng: " + name;
                }
            }
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(18),dp(18),dp(18),dp(18)); card.setBackground(background("#FFFFFF", 18));
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1,-2); cardParams.setMargins(0,dp(10),0,0); list.addView(card,cardParams);
            String status = doc.getString("status");
            card.addView(text("ĐƠN #" + doc.getId().substring(0, Math.min(8, doc.getId().length())), 12, "#168755", true));
            TextView info = text((store.isEmpty() ? "Đơn hàng" : store.trim()) + "\n" + doc.getString("address") + "\n" + doc.getString("phone") + "  ·  " + doc.getLong("total") + " ₫", 15, "#173B30", false);
            info.setLineSpacing(dp(6), 1); info.setPadding(0, dp(10), 0, dp(10)); card.addView(info);
            card.addView(text("pending".equals(status) ? "●  Chờ nhận" : "accepted".equals(status) ? "●  Đang giao" : "●  Đã giao", 13, "#168755", true));
            if (available || "accepted".equals(status)) { Button action = new Button(this); action.setText(available ? "Nhận đơn" : "Hoàn thành giao hàng"); styleButton(action, true); LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-2); params.topMargin=dp(14); card.addView(action,params);
                action.setOnClickListener(v -> update(doc.getReference(), available)); }

        }
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable background(String color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(Color.parseColor(color)); d.setCornerRadius(dp(radius)); return d; }
    private TextView text(String value, int size, String color, boolean bold) { TextView t = new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(Color.parseColor(color)); if (bold) t.setTypeface(null, Typeface.BOLD); return t; }
    private void styleButton(Button button, boolean primary) { button.setAllCaps(false); button.setTextColor(Color.parseColor(primary ? "#FFFFFF" : "#176B47")); button.setBackground(background(primary ? "#168755" : "#E0F0E4", 12)); button.setPadding(dp(16),dp(12),dp(16),dp(12)); }
    private void section(LinearLayout root, String title, String subtitle) { TextView heading = text(title, 21, "#173B30", true); heading.setPadding(0, dp(28), 0, dp(4)); root.addView(heading); root.addView(text(subtitle, 13, "#71867A", false)); }
    private void update(DocumentReference ref, boolean accept) {
        FirebaseFirestore.getInstance().runTransaction(tx -> { DocumentSnapshot current = tx.get(ref); String status = current.getString("status");
            if (accept && !"pending".equals(status) || !accept && (!"accepted".equals(status) || !FirebaseAuth.getInstance().getUid().equals(current.getString("driverId")))) throw new IllegalStateException("Đơn hàng đã thay đổi");
            Map<String,Object> changes = new HashMap<>(); changes.put("status", accept ? "accepted" : "delivered"); if (accept) changes.put("driverId", FirebaseAuth.getInstance().getUid()); tx.update(ref, changes); return null;
        }).addOnFailureListener(e -> Toast.makeText(this, "Không cập nhật được đơn: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }
    @Override protected void onDestroy() { if (pending != null) pending.remove(); if (assigned != null) assigned.remove(); super.onDestroy(); }
}
