# FoodNow — Ứng dụng đặt món và quản lý giao hàng

FoodNow gồm ứng dụng Android cho khách hàng và shipper, cùng hai trang web riêng cho quản trị viên và shipper. Ứng dụng sử dụng Firebase Authentication và Cloud Firestore để lưu, theo dõi đơn hàng theo thời gian thực.

## 1. Chuẩn bị

- **Android Studio** để chạy ứng dụng Android; cài Android SDK Platform **37.0** và Build Tools **36.0.0** trong SDK Manager. Máy ảo Android hoặc điện thoại Android được bật USB debugging. iPhone không chạy trực tiếp ứng dụng Android; máy Mac có thể dùng máy ảo Android trong Android Studio.
- **JDK 25** cho Gradle. Dự án có cấu hình tự tải Gradle 9.5 và JDK 25 khi cần; nếu tự tải không thành công, cài JDK 25 rồi chọn JDK 25 trong Android Studio → Settings/Preferences → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK.
- Một máy chủ HTTP cho web: Python 3. Kiểm tra bằng `python3 --version` trên Mac hoặc `py --version` trên Windows. Nếu chưa có, cài Python từ <https://www.python.org/downloads/>; trên Windows chọn thêm Python vào PATH nếu trình cài đặt có tùy chọn.
- Hai tệp cấu hình Firebase của **cùng dự án FoodNow** do sinh viên cung cấp: `app/google-services.json` và `web/firebase-config.js` đã điền cấu hình web. Bản clone mặc định **không có** tệp Android và tệp web chỉ chứa `REPLACE_ME`, vì vậy không thể tải GitHub rồi đăng nhập ngay nếu chưa nhận cấu hình. Không cần service account private key để chạy ứng dụng.
- Dự án Firebase phải còn hoạt động, bật phương thức Email/Password, có Cloud Firestore và đã triển khai `firestore.rules` của phiên bản này. Tài khoản admin và shipper cần được cấp đúng custom claim. Nếu dự án đã được cấu hình, không cần thực hiện lại các bước này.

### Tài khoản kiểm thử

| Vai trò | Email | Nơi đăng nhập |
| --- | --- | --- |
| Quản trị viên | `lygialuan2006@gmail.com` | Trang web quản trị |
| Shipper | `shipper@gmail.com` | Trang web shipper hoặc ứng dụng Android |
| Khách hàng | `test@gmail.com` | Ứng dụng Android |


## 2. Tải dự án

1. Tải mã nguồn từ <https://github.com/zaluan136/Project_FoodApp> bằng nút **Code → Download ZIP**, giải nén, hoặc chạy `git clone https://github.com/zaluan136/Project_FoodApp.git` trong Terminal/PowerShell nếu có Git.
2. Mở thư mục `Project_FoodApp`. Sao chép tệp `google-services.json` vào thư mục `app`, sao cho đường dẫn là `Project_FoodApp/app/google-services.json`.
3. Sao chép tệp cấu hình web vào `Project_FoodApp/web/firebase-config.js`, thay tệp mẫu. Nếu đã có quyền truy cập Firebase Console, cũng có thể điền cấu hình ứng dụng web vào tệp mẫu. Không đưa service account private key vào thư mục web.
4. Xác nhận dự án Android và web trỏ tới cùng dự án Firebase. Nếu thiết lập Firebase mới, xem mục 6 bên dưới.

## 3. Chạy trang web admin và shipper

Mở Terminal (Mac) hoặc PowerShell (Windows) tại thư mục gốc `Project_FoodApp`.

**macOS:**

```bash
cd /duong-dan-den/Project_FoodApp
python3 -m http.server 8000 --directory web
```

**Windows PowerShell:**

```powershell
cd D:\duong-dan-den\Project_FoodApp
py -m http.server 8000 --directory web
```

Thay đường dẫn ví dụ bằng đường dẫn thực tế; nếu đã mở Terminal tại thư mục dự án thì chỉ cần chạy dòng thứ hai. Giữ cửa sổ Terminal/PowerShell đang mở và truy cập:

- **Quản trị:** <http://localhost:8000/> — đăng nhập bằng tài khoản admin để xem thống kê, danh sách và chi tiết đơn hàng.
- **Shipper:** <http://localhost:8000/shipper.html> — đăng nhập bằng tài khoản shipper để xem đơn chờ, nhận đơn và hoàn thành giao hàng.

Dừng server bằng `Ctrl+C`. Nếu cổng 8000 đã được dùng, đóng server cũ hoặc đổi `8000` thành `8001` trong lệnh và đường dẫn. Nếu dùng bản cũ trong trình duyệt, tải lại mạnh bằng `Cmd+Shift+R` trên Mac hoặc `Ctrl+F5` trên Windows. Khi kiểm thử Firebase Authentication tại localhost, hãy đảm bảo `localhost` được thêm trong Firebase Console → Authentication → Settings → Authorized domains.

## 4. Chạy ứng dụng Android trên macOS và Windows

1. Mở Android Studio → **Open** → chọn thư mục gốc `Project_FoodApp` (thư mục chứa `settings.gradle.kts`).
2. Kiểm tra đã đặt `app/google-services.json` đúng vị trí. Chờ Gradle Sync tải các phụ thuộc; nếu được hỏi, cho phép cài SDK còn thiếu. Đảm bảo Gradle dùng JDK 25 như mục 1.
3. Mở **Device Manager**, tạo và khởi động một máy ảo Android tương thích với máy; hoặc kết nối điện thoại Android đã bật USB debugging. Trên máy Mac Apple Silicon, chọn system image ARM64 nếu có.
4. Chọn cấu hình chạy `app` và thiết bị, rồi bấm **Run ▶**. Lần đầu tải SDK, Gradle và máy ảo có thể mất thời gian; các bước này cần Internet.
5. Đăng nhập bằng tài khoản khách để đặt món; đăng nhập tài khoản shipper để mở màn hình shipper Android.

Build dòng lệnh là tùy chọn sau khi đã cài JDK/SDK và cấu hình Firebase:

```bash
# macOS, từ thư mục Project_FoodApp
bash gradlew :app:assembleDebug
```

```powershell
# Windows PowerShell, từ thư mục Project_FoodApp
.\gradlew.bat :app:assembleDebug
```

Nếu Gradle không tìm thấy Android SDK, thiết lập SDK Location trong Android Studio hoặc cấu hình biến `ANDROID_HOME` trỏ tới SDK trên máy. Không cần tạo `local.properties` thủ công nếu Android Studio đã cấu hình SDK.

## 5. Kiểm thử

1. **Khách hàng — Android:** đăng nhập `test@gmail.com`; vào Khám phá, chọn cửa hàng và món, mở giỏ hàng, nhập tên người nhận, địa chỉ và số điện thoại rồi xác nhận. Kiểm tra thông báo thành công và mở mục Đơn hàng → Xem chi tiết. Dùng thông tin giao hàng minh họa khi kiểm thử.
2. **Shipper — web hoặc Android:** đăng nhập `shipper@gmail.com`, chọn một đơn đang chờ → **Nhận đơn** → **Hoàn thành giao hàng**. Shipper chỉ thấy đơn chờ và đơn được giao cho mình.
3. **Admin — web:** đăng nhập bằng tài khoản admin. Kiểm tra tổng số đơn, đơn chờ, đơn đang giao, giá trị các đơn đã giao, biểu đồ 7 ngày; tìm/lọc và mở chi tiết đơn. Giá trị đã giao là tổng giá trị đơn có trạng thái đã giao, không phải xác nhận đã thanh toán.
4. Quay lại mục Đơn hàng của khách để kiểm tra trạng thái thay đổi. Đơn cũ tạo trước khi có tên người nhận có thể hiển thị “Chưa có thông tin”.

Danh mục mẫu gồm 7 Eleven, Circle K, Ministop và VinMart; các món và giá là dữ liệu mẫu. Ứng dụng chưa hỗ trợ thanh toán, hủy đơn, chat, tìm shipper gần nhất, đăng nhập mạng xã hội hoặc quản lý thực đơn. Giá chưa được xác thực phía máy chủ; dự án không dành cho giao dịch thực tế trong trạng thái hiện tại.

## 6. Cấu hình Firebase cho người triển khai

Chỉ cần thực hiện phần này khi thiết lập dự án Firebase mới.

1. Tạo hoặc chọn dự án Firebase; bật Authentication Email/Password và tạo Cloud Firestore. Đăng ký ứng dụng Android package `com.example.project_foodapp`, tải `google-services.json` đặt trong `app/`; tạo ứng dụng web trong cùng dự án và cập nhật `web/firebase-config.js`.
2. Kiểm tra đúng dự án rồi triển khai rules: `firebase deploy --only firestore:rules --project <PROJECT_ID>`. Thao tác này cập nhật rules đang hoạt động, cần được người quản lý dự án thực hiện. Rules phiên bản hiện tại cho phép trường `recipientName`; nếu chưa triển khai, tạo đơn mới có thể báo `PERMISSION_DENIED`.
3. Tạo tài khoản email/mật khẩu. Từ môi trường máy chủ đáng tin cậy, dùng Firebase Admin SDK cấp custom claim `admin: true` cho quản trị viên hoặc `shipper: true` cho shipper. Sau khi đổi quyền, đăng xuất và đăng nhập lại. Không tự cấp quyền từ mã phía client.
4. Cho phép `localhost` trong Authentication → Settings → Authorized domains. Firebase Hosting có thể được triển khai riêng bằng Firebase CLI nếu người quản lý muốn công bố web; chạy localhost không yêu cầu bước này.

## 7. Xử lý lỗi thường gặp

- **`PERMISSION_DENIED` khi đặt hàng:** kiểm tra người dùng đã đăng nhập, ứng dụng đang trỏ đúng dự án Firebase và người quản lý đã triển khai rules mới. Xem lỗi đầy đủ nếu vẫn xảy ra.
- **Không thấy tên admin/shipper hoặc báo chưa có quyền:** kiểm tra đúng tài khoản, đúng Firebase project và custom claim; đăng xuất rồi đăng nhập lại. Tài khoản shipper không đăng nhập vào cổng admin trừ khi được cấp quyền admin riêng.
- **Gradle báo thiếu `google-services.json`:** kiểm tra tệp tại `app/google-services.json`, đồng bộ lại Gradle.
- **Web báo thiếu cấu hình Firebase:** kiểm tra `web/firebase-config.js` không còn `REPLACE_ME`, đang chạy qua HTTP và máy có Internet.
- **Ứng dụng không tải được đơn:** kiểm tra kết nối, cấu hình cùng dự án, trạng thái đăng nhập, quyền và Firestore Rules. Không cần gửi mật khẩu hoặc tệp khóa riêng để hỏi hỗ trợ.
