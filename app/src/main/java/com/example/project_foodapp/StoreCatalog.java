package com.example.project_foodapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Local sample catalogue. Each call creates a fresh basket. */
public final class StoreCatalog {
    public static final class Store {
        public final String id, name, address, description;
        public final int logo;
        Store(String id, String name, String address, String description, int logo) {
            this.id = id; this.name = name; this.address = address; this.description = description; this.logo = logo;
        }
        public List<Food> menu() {
            List<Food> items = new ArrayList<>();
            switch (id) {
                case "circlek":
                    items.add(new Food("Bánh mì xúc xích", 29000, R.drawable.ic_banh_mi));
                    items.add(new Food("Cà phê sữa đá", 25000, R.drawable.ic_black_coffee));
                    items.add(new Food("Trà sữa trân châu", 35000, R.drawable.ic_milk_tea));
                    items.add(new Food("Bánh mì thịt", 32000, R.drawable.ic_banh_mi));
                    break;
                case "ministop":
                    items.add(new Food("Bánh mì gà", 35000, R.drawable.ic_banh_mi));
                    items.add(new Food("Cà phê đen đá", 22000, R.drawable.ic_black_coffee));
                    items.add(new Food("Trà sữa truyền thống", 34000, R.drawable.ic_milk_tea));
                    items.add(new Food("Bánh mì trứng", 27000, R.drawable.ic_banh_mi));
                    break;
                case "vinmart":
                    items.add(new Food("Bánh mì chà bông", 26000, R.drawable.ic_banh_mi));
                    items.add(new Food("Cà phê sữa", 28000, R.drawable.ic_black_coffee));
                    items.add(new Food("Trà sữa", 32000, R.drawable.ic_milk_tea));
                    items.add(new Food("Bánh mì thịt nguội", 39000, R.drawable.ic_banh_mi));
                    break;
                default:
                    items.add(new Food("Cà phê đen đá", 29000, R.drawable.ic_black_coffee));
                    items.add(new Food("Bánh mì thịt", 39000, R.drawable.ic_banh_mi));
                    items.add(new Food("Trà sữa trân châu", 45000, R.drawable.ic_milk_tea));
                    items.add(new Food("Bánh mì trứng", 28000, R.drawable.ic_banh_mi));
            }
            return items;
        }
    }
    public static List<Store> stores() {
        return Arrays.asList(
            new Store("seven", "7 Eleven", "82 Nguyễn Thị Minh Khai, Quận 3", "Bánh mì · Cà phê · Trà sữa", R.drawable.ic_seveneleven),
            new Store("circlek", "Circle K", "Chi nhánh mẫu", "Đồ ăn nhanh · Cà phê · Trà sữa", R.drawable.ic_circlek),
            new Store("ministop", "Ministop", "Chi nhánh mẫu", "Bánh mì · Đồ uống", R.drawable.ic_ministop),
            new Store("vinmart", "VinMart", "Chi nhánh mẫu", "Bánh mì · Đồ uống", R.drawable.ic_vinmart)
        );
    }
    public static Store find(String id) {
        for (Store store : stores()) if (store.id.equals(id)) return store;
        return stores().get(0);
    }
    private StoreCatalog() { }
}
