package com.example.project_foodapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderFragment extends Fragment {
    private ListenerRegistration listener;
    private LinearLayout orderList;

    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        ScrollView scroll = new ScrollView(requireContext());
        scroll.setBackgroundColor(Color.rgb(246, 248, 244));
        scroll.setFillViewport(true);
        LinearLayout content = new LinearLayout(requireContext());
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(18), dp(22), dp(24));
        scroll.addView(content);
        TextView eyebrow = text("ĐƠN HÀNG", 11, "#39825B", true);
        eyebrow.setLetterSpacing(0.12f);
        content.addView(eyebrow);
        TextView title = text("Bữa ngon của bạn", 29, "#193B2B", true);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(-1, -2);
        titleParams.topMargin = dp(5); content.addView(title, titleParams);
        TextView subtitle = text("Theo dõi hành trình từng đơn hàng.", 14, "#6F8075", false);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(-1, -2);
        subParams.topMargin = dp(4); content.addView(subtitle, subParams);
        orderList = new LinearLayout(requireContext()); orderList.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(-1, -2);
        listParams.topMargin = dp(22); content.addView(orderList, listParams);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            showMessage("Vui lòng đăng nhập để xem đơn hàng."); return scroll;
        }
        showMessage("Đang tải đơn hàng...");
        listener = FirebaseFirestore.getInstance().collection("orders")
            .whereEqualTo("customerId", FirebaseAuth.getInstance().getUid())
            .addSnapshotListener((snap, error) -> {
                if (!isAdded() || getView() == null) return;
                orderList.removeAllViews();
                if (error != null) { showMessage("Không tải được đơn hàng. Vui lòng thử lại sau."); return; }
                if (snap == null || snap.isEmpty()) { showMessage("Chưa có đơn hàng nào. Khám phá món ngon nhé!"); return; }
                for (DocumentSnapshot doc : snap) addOrder(doc);
            });
        return scroll;
    }

    private void addOrder(DocumentSnapshot doc) {
        LinearLayout card = new LinearLayout(requireContext()); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18)); card.setBackgroundResource(R.drawable.bg_surface);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(-1, -2); cardParams.bottomMargin = dp(14);
        orderList.addView(card, cardParams);
        LinearLayout row = new LinearLayout(requireContext()); row.setGravity(Gravity.CENTER_VERTICAL); card.addView(row);
        TextView id = text("Đơn #" + doc.getId().substring(0, Math.min(8, doc.getId().length())), 16, "#193B2B", true);
        row.addView(id, new LinearLayout.LayoutParams(0, -2, 1));
        String status = doc.getString("status");
        String label = "pending".equals(status) ? "Chờ nhận" : "accepted".equals(status) ? "Đang giao" : "delivered".equals(status) ? "Đã giao" : "Đang cập nhật";
        TextView badge = text(label, 12, "#166B46", true); badge.setBackgroundResource(R.drawable.bg_pill_green);
        badge.setPadding(dp(10), dp(6), dp(10), dp(6)); row.addView(badge);
        Long total = doc.getLong("total");
        TextView amount = text(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(total == null ? 0 : total) + " ₫", 20, "#166B46", true);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(-1, -2); amountParams.topMargin = dp(18); card.addView(amount, amountParams);
        String address = doc.getString("address");
        TextView destination = text("Giao đến  ·  " + (address == null ? "Chưa có địa chỉ" : address), 13, "#667B6D", false);
        destination.setLineSpacing(dp(3), 1); LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(-1, -2); addressParams.topMargin = dp(10); card.addView(destination, addressParams);
    }
    private void showMessage(String message) {
        TextView empty = text(message, 14, "#667B6D", false); empty.setBackgroundResource(R.drawable.bg_surface);
        empty.setPadding(dp(20), dp(22), dp(20), dp(22)); orderList.addView(empty);
    }
    private TextView text(String value, int size, String color, boolean bold) {
        TextView view = new TextView(requireContext()); view.setText(value); view.setTextSize(size); view.setTextColor(Color.parseColor(color));
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return view;
    }
    private int dp(int value) { return (int) (value * getResources().getDisplayMetrics().density + 0.5f); }
    @Override public void onDestroyView() { if (listener != null) listener.remove(); listener = null; orderList = null; super.onDestroyView(); }
}
