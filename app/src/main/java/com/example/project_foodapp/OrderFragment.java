package com.example.project_foodapp;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class OrderFragment extends Fragment {
    private ListenerRegistration listener;
    @Override public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        ScrollView scroll = new ScrollView(requireContext()); LinearLayout list = new LinearLayout(requireContext());
        list.setOrientation(LinearLayout.VERTICAL); list.setPadding(32, 32, 32, 32); scroll.addView(list);
        TextView title = new TextView(requireContext()); title.setText("Đơn hàng của bạn"); title.setTextSize(24); list.addView(title);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return scroll;
        listener = FirebaseFirestore.getInstance().collection("orders").whereEqualTo("customerId", FirebaseAuth.getInstance().getUid())
            .addSnapshotListener((snap, error) -> {
                if (!isAdded()) return; list.removeViews(1, list.getChildCount()-1);
                if (error != null) { title.setText("Không tải được đơn hàng"); return; }
                if (snap == null || snap.isEmpty()) { TextView empty = new TextView(requireContext()); empty.setText("Bạn chưa có đơn hàng nào."); list.addView(empty); return; }
                for (DocumentSnapshot doc : snap) { TextView text = new TextView(requireContext());
                    text.setPadding(0, 24, 0, 24); text.setText("Đơn #" + doc.getId().substring(0, 8) + "\nTrạng thái: " + doc.getString("status") + "\nTổng: " + doc.getLong("total") + " ₫\n" + doc.getString("address")); list.addView(text); }
            });
        return scroll;
    }
    @Override public void onDestroyView() { if (listener != null) listener.remove(); super.onDestroyView(); }
}
