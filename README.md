# FCOAutoClick - Desktop Automation with Image Recognition & Window-Relative Coordinates

> **Developer**: TunEngDev2003  
> **Application Name**: FCOAutoClick  
> **Copyright**: © 2026 TunEngDev2003 • All rights reserved

Ứng dụng Java Desktop trên Windows tự động thao tác trên cửa sổ game FCO dựa trên nhận diện hình ảnh (OpenCV), quản lý cửa sổ bằng Win32 API (JNA), máy trạng thái điều kiện (Conditional State Machine Engine) và hỗ trợ đa ngôn ngữ quốc tế (English & Tiếng Việt).

---

## 1. Tính năng nổi bật

### 1.1. Đa ngôn ngữ (i18n) & Bản quyền tác giả
- **Đa ngôn ngữ (ENG / VIE)**: Hỗ trợ chuyển đổi nhanh chóng giữa **English** và **Tiếng Việt** qua dropdown `🌐` ngay trên thanh tiêu đề ứng dụng mà không cần khởi động lại.
- **Tên ứng dụng chính thức**: `FCOAutoClick`.
- **Dấu ấn bản quyền**: Đánh dấu thương hiệu và bản quyền của `TunEngDev2003` trên tiêu đề, badge tác giả, header, console log và thanh footer copyright.
- **Biểu tượng ứng dụng (App Icon)**: Icon chất lượng cao 128x128 (`src/main/resources/icons/app-icon.png`) hiển thị trên thanh tiêu đề Windows, Taskbar và góc ứng dụng.

### 1.2. Flow tự động hoá mới (Automation Flow)
```text
START
  ↓
[Initial Actions] (Luôn chạy trước trên mỗi chu kỳ)
  ↓
[Check Image] (So khớp OpenCV đúng 1 lần sau khi Initial Actions xong)
  ↓
  ├─ MATCH     → [Branch A] (Chạy toàn bộ action của Branch A)
  └─ NOT MATCH → [Branch B] (Chạy toàn bộ action của Branch B)
  ↓
Quay lại [Initial Actions] lặp tiếp
```

### 1.3. Hỗ trợ FCO Window qua JNA (Win32 API)
- Tự động liệt kê các cửa sổ đang mở trên Windows.
- Tự động nhận diện và chọn cửa sổ có tiêu đề chứa `FCO`.
- Theo dõi HWND và tọa độ Bounds thực tế của FCO window theo thời gian thực.
- Nếu cửa sổ FCO bị đóng hoặc không tìm thấy:
  ```text
  Target Window: NOT FOUND
  ```
  Ứng dụng tạm dừng thao tác click để đảm bảo an toàn tuyệt đối, không click nhầm vào các ứng dụng khác.

### 1.4. Window-Relative Coordinate System
- Hỗ trợ 2 chế độ tọa độ:
  - `WINDOW_RELATIVE` (Mặc định cho FCO)
  - `ABSOLUTE_SCREEN`
- Tự động bù trừ vị trí:
  ```text
  screenX = fcoWindow.x + relativeX
  screenY = fcoWindow.y + relativeY
  ```
- Khi người dùng di chuyển hoặc kéo cửa sổ FCO sang vị trí khác (kể cả giữa các màn hình), mọi action click và vùng capture vẫn tính toán chính xác tuyệt đối.

### 1.5. Pick Coordinate trực quan
- Khi thêm hoặc sửa action (`CLICK`, `DOUBLE_CLICK`, `RIGHT_CLICK`), không cần nhập X/Y thủ công.
- Bấm nút **[ Pick Coordinate ]**:
  1. Ứng dụng hiển thị lớp phủ mờ với tâm ngắm (crosshair) và bảng tọa độ real-time.
  2. Người dùng di chuột tới vị trí mong muốn trên FCO và click chuột.
  3. Tọa độ tương đối tự động được ghi nhận và điền vào form action.
  4. Nhấn phím `ESC` để hủy.
- Cú click chuột khi pick chỉ dùng để ghi nhận tọa độ, **hoàn toàn không click thật** vào game FCO bên dưới.

### 1.6. Pick Region & Image Preview
- Nút **[ Pick Region (Drag Mouse) ]** hỗ trợ kéo chuột khoanh vùng chụp ảnh theo `WINDOW_RELATIVE`.
- **Image Preview**: Hiển thị ảnh thu nhỏ (thumbnail) và kích thước `(WxH px)` của template ngay trên giao diện khi chọn file.
- OpenCV Template Matching (`TM_CCOEFF_NORMED`) tự động load thư viện C++ native qua `org.openpnp:opencv`.
- Nút **[ Test Image ]** chụp thử vùng hiện tại và hiển thị `Confidence (0.0 - 1.0)`.

### 1.7. 3 Nhóm Actions trên giao diện
Giao diện phân chia rõ ràng thành 3 Tabs:
1. **Initial Actions (Run First)**: Nhóm action luôn chạy đầu tiên trước khi quét ảnh.
2. **Branch A (Image MATCH)**: Nhóm action chạy khi ảnh khớp threshold.
3. **Branch B (Image NOT MATCH)**: Nhóm action chạy khi ảnh không khớp.

Mỗi danh sách đều hỗ trợ:
- `+ Add Action`
- `Pick Coord`
- `Edit`
- `Delete`
- `Move Up (↑)`
- `Move Down (↓)`

### 1.8. Global Hotkeys (Khẩn cấp)
- **F8**: START automation từ bất kỳ cửa sổ nào.
- **F9**: STOP automation tức thì (Emergency Stop).

### 1.9. JSON Profile Configuration
Toàn bộ cấu hình gồm: Target Window, Coordinate Mode, Initial Actions, Capture Region, Template Path, Threshold, Branch A, Branch B được lưu/đọc qua định dạng JSON chuẩn.

---

## 2. Hướng dẫn chạy ứng dụng

### Yêu cầu:
- JDK 21 LTS
- Maven 3.9+

### Chạy ứng dụng bằng Maven JavaFX:
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

mvn javafx:run
```

Hoặc đóng gói JAR:
```powershell
mvn clean package
java -jar target/autoclicker-1.0.0-SNAPSHOT.jar
```
