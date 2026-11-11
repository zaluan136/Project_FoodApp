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
        Object rawItems = doc.get("items");
        if (rawItems instanceof java.util.List && !((java.util.List<?>) rawItems).isEmpty()) {
            Object first = ((java.util.List<?>) rawItems).get(0);
            if (first instanceof java.util.Map) {
                Object storeName = ((java.util.Map<?, ?>) first).get("storeName");
                if (storeName instanceof String) {
                    TextView storeLabel = text((String) storeName, 14, "#405D4C", true);
                    LinearLayout.LayoutParams storeParams = new LinearLayout.LayoutParams(-1, -2);
                    storeParams.topMargin = dp(14); card.addView(storeLabel, storeParams);
                }
            }
        }
        Long total = doc.getLong("total");
        TextView amount = text(NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(total == null ? 0 : total) + " ₫", 20, "#166B46", true);
        LinearLayout.LayoutParams amountParams = new LinearLayout.LayoutParams(-1, -2); amountParams.topMargin = dp(18); card.addView(amount, amountParams);
        String address = doc.getString("address");
        TextView destination = text("Giao đến  ·  " + (address == null ? "Chưa có địa chỉ" : address), 13, "#667B6D", false);
        destination.setLineSpacing(dp(3), 1); LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(-1, -2); addressParams.topMargin = dp(10); card.addView(destination, addressParams);
        TextView more = text("Xem chi tiết  →", 13, "#168755", true);
        LinearLayout.LayoutParams moreParams = new LinearLayout.LayoutParams(-1, -2); moreParams.topMargin = dp(14); card.addView(more, moreParams);
        card.setFocusable(true); card.setClickable(true); card.setContentDescription("Xem chi tiết đơn " + doc.getId());
        card.setOnClickListener(v -> showOrderDetails(doc));
    }
    private void showOrderDetails(DocumentSnapshot doc) {
        ScrollView scroll = new ScrollView(requireContext()); scroll.setBackgroundResource(R.drawable.bg_delivery_dialog);
        LinearLayout content = new LinearLayout(requireContext()); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(24), dp(24), dp(24), dp(24)); scroll.addView(content);
        content.addView(text("CHI TIẾT ĐƠN HÀNG", 12, "#168755", true));
        addDetail(content, "Đơn #" + doc.getId().substring(0, Math.min(8, doc.getId().length())), 23, true);
        String status = doc.getString("status");
        addDetail(content, "Trạng thái: " + ("pending".equals(status) ? "Chờ nhận" : "accepted".equals(status) ? "Đang giao" : "delivered".equals(status) ? "Đã giao" : "Đang cập nhật"), 14, false);
        com.google.firebase.Timestamp created = doc.getTimestamp("createdAt");
        if (created != null) addDetail(content, "Ngày đặt: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN")).format(created.toDate()), 14, false);
        addDetail(content, "Thông tin giao hàng", 17, true);
        addDetail(content, "Người nhận: " + valueOrFallback(doc.getString("recipientName"), "Chưa có thông tin"), 14, false);
        addDetail(content, "Số điện thoại: " + valueOrFallback(doc.getString("phone"), "Chưa có thông tin"), 14, false);
        addDetail(content, "Địa chỉ: " + valueOrFallback(doc.getString("address"), "Chưa có thông tin"), 14, false);
        addDetail(content, "Chi tiết món", 17, true);
        Object raw = doc.get("items");
        if (raw instanceof java.util.List && !((java.util.List<?>) raw).isEmpty()) {
            for (Object item : (java.util.List<?>) raw) {
                if (!(item instanceof java.util.Map)) continue;
                java.util.Map<?, ?> values = (java.util.Map<?, ?>) item;
                Object name = values.get("name"), quantity = values.get("quantity"), price = values.get("price");
                long count = quantity instanceof Number ? ((Number) quantity).longValue() : 0;
                long unit = price instanceof Number ? ((Number) price).longValue() : 0;
                addDetail(content, (name == null ? "Món ăn" : name.toString()) + " × " + count + "\n" + money(unit) + " / món · " + money(unit * count), 14, false);
            }
        } else addDetail(content, "Không có thông tin món.", 14, false);
        Long total = doc.getLong("total"); addDetail(content, "Tổng cộng: " + money(total == null ? 0 : total), 19, true);
        androidx.appcompat.widget.AppCompatButton close = new androidx.appcompat.widget.AppCompatButton(requireContext());
        close.setText("Đóng"); close.setAllCaps(false); close.setTextColor(Color.WHITE); close.setBackgroundResource(R.drawable.bg_button_green);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(-1, dp(52)); buttonParams.topMargin = dp(24); content.addView(close, buttonParams);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setView(scroll).create();
        dialog.setOnShowListener(ignored -> { if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); });
        close.setOnClickListener(v -> dialog.dismiss()); dialog.show();
    }
    private String valueOrFallback(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
    private String money(long amount) { return NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(amount) + " ₫"; }
    private void addDetail(LinearLayout parent, String value, int size, boolean bold) {
        TextView line = text(value, size, bold ? "#193B2B" : "#667B6D", bold); line.setLineSpacing(dp(3), 1);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2); params.topMargin = dp(bold ? 18 : 8); parent.addView(line, params);
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
