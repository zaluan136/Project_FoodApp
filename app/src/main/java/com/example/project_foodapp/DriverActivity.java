package com.example.project_foodapp;

import android.os.Bundle;
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
        ScrollView scroll = new ScrollView(this); LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,32,32,32); scroll.addView(root); setContentView(scroll);
        TextView title = new TextView(this); title.setText("FoodNow · Shipper"); title.setTextSize(24); root.addView(title);
        Button logout = new Button(this); logout.setText("Đăng xuất"); root.addView(logout); logout.setOnClickListener(v -> { FirebaseAuth.getInstance().signOut(); startActivity(new android.content.Intent(this, SignInActivity.class)); finish(); });
        TextView available = new TextView(this); available.setText("Đơn chờ nhận"); available.setTextSize(20); root.addView(available);
        openList = new LinearLayout(this); openList.setOrientation(LinearLayout.VERTICAL); root.addView(openList);
        TextView yours = new TextView(this); yours.setText("Đơn của bạn"); yours.setTextSize(20); root.addView(yours);
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
            TextView info = new TextView(this); info.setPadding(0,20,0,4); info.setText("#" + doc.getId().substring(0,8) + " · " + doc.getString("status") + store + "\n" + doc.getString("address") + "\n" + doc.getString("phone") + " · " + doc.getLong("total") + " ₫"); list.addView(info);
            String status = doc.getString("status");
            if (available || "accepted".equals(status)) { Button action = new Button(this); action.setText(available ? "Nhận đơn" : "Hoàn thành giao hàng"); list.addView(action);
                action.setOnClickListener(v -> update(doc.getReference(), available)); }
        }
    }
    private void update(DocumentReference ref, boolean accept) {
        FirebaseFirestore.getInstance().runTransaction(tx -> { DocumentSnapshot current = tx.get(ref); String status = current.getString("status");
            if (accept && !"pending".equals(status) || !accept && (!"accepted".equals(status) || !FirebaseAuth.getInstance().getUid().equals(current.getString("driverId")))) throw new IllegalStateException("Đơn hàng đã thay đổi");
            Map<String,Object> changes = new HashMap<>(); changes.put("status", accept ? "accepted" : "delivered"); if (accept) changes.put("driverId", FirebaseAuth.getInstance().getUid()); tx.update(ref, changes); return null;
        }).addOnFailureListener(e -> Toast.makeText(this, "Không cập nhật được đơn: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show());
    }
    @Override protected void onDestroy() { if (pending != null) pending.remove(); if (assigned != null) assigned.remove(); super.onDestroy(); }
}
