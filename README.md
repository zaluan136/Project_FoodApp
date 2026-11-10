# FoodNow

Ứng dụng Android cho khách đặt món và shipper, cùng trang web quản trị theo dõi đơn hàng. Cả ba sử dụng Firebase Authentication và Cloud Firestore.

## Kết nối Firebase

1. Tạo một dự án Firebase, bật Authentication bằng email/mật khẩu và tạo Cloud Firestore. Thêm ứng dụng Android với package `com.example.project_foodapp`, tải `google-services.json` từ Firebase Console và đặt vào `app/`. Tệp này được Git bỏ qua.
2. Thêm ứng dụng web trong cùng dự án và điền cấu hình công khai của ứng dụng vào `web/firebase-config.js`.
3. Triển khai `firestore.rules` bằng Firebase CLI: `firebase deploy --only firestore:rules`. Đọc và kiểm thử quy tắc trước khi dùng dữ liệu thật.
4. Tạo tài khoản email/mật khẩu cho quản trị viên và shipper trong Firebase Authentication. Từ một môi trường máy chủ tin cậy dùng Firebase Admin SDK, cấp custom claim `admin: true` cho quản trị viên hoặc `shipper: true` cho shipper. Không cấp quyền bằng mã phía client. Người dùng cần đăng nhập lại sau khi cấp quyền. Tài khoản tự đăng ký là khách hàng.
5. Build Android bằng Android Studio hoặc `bash gradlew :app:assembleDebug`, sử dụng JDK 25 và Android SDK 37 như cấu hình project. Đăng nhập bằng tài khoản shipper để mở màn hình shipper; tài khoản khác vào màn hình khách.
6. Để chạy web cục bộ, phục vụ thư mục `web` qua HTTP, ví dụ `python3 -m http.server 8000 --directory web`. Thêm miền triển khai vào Authorized domains của Firebase Authentication. Có thể triển khai lên Firebase Hosting bằng `firebase deploy --only hosting` sau khi chọn dự án bằng Firebase CLI.

## Phạm vi hiện tại

Khách đăng ký/đăng nhập, chọn món mẫu, nhập địa chỉ và số điện thoại để đặt hàng, xem trạng thái. Shipper xem đơn chờ, nhận đơn và xác nhận giao. Quản trị viên xem đơn theo thời gian thực. Đây là bản nền tảng: menu, giá và một số chi nhánh là dữ liệu mẫu nằm trong ứng dụng, máy chủ chưa xác thực giá; chưa có thanh toán, hủy đơn, chat, tìm tài xế gần nhất, xác thực xã hội hay quản lý thực đơn. Không dùng cho giao dịch thật trước khi bổ sung xác thực giá ở máy chủ, kiểm thử và hoàn thiện các luồng còn thiếu.

Danh mục mẫu gồm 7 Eleven, Circle K, Ministop và VinMart; mỗi cửa hàng có thực đơn riêng. Tên cửa hàng được lưu trong từng món của đơn để vẫn tương thích với quy tắc Firestore và đơn cũ. Khi quyền shipper thay đổi, đăng xuất và đăng nhập lại để nhận token mới.
