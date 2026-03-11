# Hướng dẫn sử dụng API Maintenance theo Role

> **Base URL:** `http://localhost:8080/building-management/api/v1`  
> Mỗi feature: điều kiện cho phép + API + **tác dụng** (thay đổi trạng thái / dữ liệu).  
> Xem chi tiết request/response body: [maintenance-api.md](./maintenance-api.md)

---

## Mục lục

1. [maintenance-request](#1-maintenance-request)
2. [maintenance-quotation](#2-maintenance-quotation)
3. [maintenance-schedule](#3-maintenance-schedule)
4. [maintenance-progress](#4-maintenance-progress)
5. [maintenance-review](#5-maintenance-review)
6. [maintenance-resource](#6-maintenance-resource)
7. [maintenance-log](#7-maintenance-log)
8. [maintenance-statistics](#8-maintenance-statistics)
9. [Ma trận tổng hợp](#9-ma-trận-tổng-hợp)
10. [Nghiệp vụ từng role trong luồng](#10-nghiệp-vụ-từng-role-trong-luồng)

---

## 1. maintenance-request

| Feature | API | Role | Điều kiện | Tác dụng |
|---------|-----|------|-----------|---------|
| **request/create** | `POST /maintenance-requests` | RESIDENT | Không có | Tạo yêu cầu mới, `requestStatus = PENDING`, sinh `code = "REQ-{timestamp}"` |
| **request/list** | `GET /maintenance-requests` | ALL | — | Trả về danh sách; FE tự filter theo role (resident → `requesterId`, staff → `staffId`) |
| **request/get** | `GET /maintenance-requests/{id}` | ALL | — | Trả về toàn bộ thông tin yêu cầu |
| **request/update** | `PUT /maintenance-requests/{id}` | RESIDENT | `status = PENDING` | Cập nhật tiêu đề, mô tả, ưu tiên, thời gian mong muốn |
| **request/cancel** | `PATCH /maintenance-requests/{id}/cancel` | RESIDENT, ADMIN | `status ∈ {PENDING, VERIFYING}` | `requestStatus → CANCELLED`, `closedAt = now()` |
| **request/assign** | `PATCH /maintenance-requests/{id}/assign` | ADMIN | `status ∈ {PENDING, VERIFYING}` | Gán `staffId` cho yêu cầu, `requestStatus → VERIFYING` |

---

## 2. maintenance-quotation

| Feature | API | Role | Điều kiện | Tác dụng |
|---------|-----|------|-----------|---------|
| **quotation/create** | `POST /maintenance-requests/{id}/quotations` | STAFF | `requestStatus ∈ {VERIFYING, QUOTING}` | Tạo báo giá bản nháp, `quotationStatus = DRAFT`, `requestStatus → QUOTING` |
| **quotation/list** | `GET /maintenance-requests/{id}/quotations` | ALL | — | Trả về tất cả báo giá của yêu cầu (kể cả DRAFT, REJECTED) |
| **quotation/get** | `GET /maintenance-requests/quotations/{qId}` | ALL | — | Chi tiết một báo giá kèm danh sách items |
| **quotation/update** | `PUT /maintenance-requests/quotations/{qId}` | STAFF | `quotationStatus = DRAFT` | Thay toàn bộ items cũ bằng items mới; chỉ được sửa khi còn DRAFT |
| **quotation/send** | `PATCH /quotations/{qId}/status?status=SENT` | STAFF | `quotationStatus = DRAFT` | Gửi báo giá cho cư dân: `quotationStatus → SENT`, `requestStatus → WAITING_APPROVAL` |
| **quotation/approve** | `PATCH /quotations/{qId}/status?status=APPROVED` | RESIDENT | `quotationStatus = SENT` | Cư dân đồng ý: `quotationStatus → APPROVED`, `requestStatus → APPROVED` |
| **quotation/reject** | `PATCH /quotations/{qId}/status?status=REJECTED` | RESIDENT | `quotationStatus = SENT` | Cư dân từ chối: `quotationStatus → REJECTED`, `requestStatus → QUOTING` (staff phải lập lại) |
| **quotation/cancel** | `PATCH /quotations/{qId}/status?status=CANCELLED` | ADMIN | — | Hủy báo giá, `quotationStatus → CANCELLED` |

> **FE lưu ý:** `totalAmount` luôn trả về `null` — tự tính: `items.reduce((s,i) => s + i.quantity * i.unitPrice, 0)`

---

## 3. maintenance-schedule

| Feature | API | Role | Điều kiện | Tác dụng |
|---------|-----|------|-----------|---------|
| **schedule/propose** | `POST /maintenance-requests/{id}/schedules` | RESIDENT, STAFF, ADMIN | `requestStatus ∈ {APPROVED, IN_PROGRESS}` | Tạo đề xuất lịch mới, `scheduleStatus = PROPOSED`, `proposedByRole` tự gán theo người gọi |
| **schedule/list** | `GET /maintenance-requests/{id}/schedules` | ALL | — | Toàn bộ lịch sử đề xuất; dùng `parentScheduleId` để hiển thị cây đàm phán |
| **schedule/accept** | `PATCH /{id}/schedules/{sId}/respond` `{action:ACCEPT}` | Bên kia đề xuất | `scheduleStatus = PROPOSED` | `scheduleStatus → CONFIRMED`, `requestStatus → IN_PROGRESS`, `startedAt = proposedTime` |
| **schedule/reject** | `PATCH /{id}/schedules/{sId}/respond` `{action:REJECT}` | Bên kia đề xuất | `scheduleStatus = PROPOSED` | `scheduleStatus → REJECTED`; bên đề xuất phải propose lại |
| **schedule/counter** | `PATCH /{id}/schedules/{sId}/respond` `{action:COUNTER_PROPOSE}` | Bên kia đề xuất | `scheduleStatus = PROPOSED` | Schedule gốc → `COUNTER_PROPOSED`; tạo record mới với `parentScheduleId = sId`, `proposedByRole` = người phản hồi |

> **Quy tắc "bên kia":** `proposedByRole = RESIDENT` → STAFF phản hồi · `proposedByRole = STAFF/MANAGER` → RESIDENT phản hồi

---

## 4. maintenance-progress

| Feature | API | Role | Điều kiện | Tác dụng |
|---------|-----|------|-----------|---------|
| **progress/add** | `POST /maintenance-requests/{id}/progress` | STAFF | `requestStatus = IN_PROGRESS` | Thêm mốc tiến độ mới. Nếu `progressPercent >= 100`: tự động `requestStatus → COMPLETED`, `finishedAt = now()` |
| **progress/list** | `GET /maintenance-requests/{id}/progress` | ALL | — | Toàn bộ lịch sử tiến độ; phần tử `[0]` là mới nhất (dùng cho progress bar) |

---

## 5. maintenance-review

| Feature | API | Role | Điều kiện | Tác dụng |
|---------|-----|------|-----------|---------|
| **review/submit** | `POST /maintenance-requests/{id}/review` | RESIDENT | `requestStatus = COMPLETED` | Gửi đánh giá (1 lần duy nhất). `ACCEPTED/PARTIAL_ACCEPT` → `RESIDENT_ACCEPTED`, `closedAt = now()`. `REDO` → `IN_PROGRESS` |
| **review/get** | `GET /maintenance-requests/{id}/review` | ALL | — | Lấy đánh giá; trả về `404` nếu chưa có |

---

## 6. maintenance-resource

| Feature | API | Role | Điều kiện | Tác dụng |
|---------|-----|------|-----------|---------|
| **resource/add** | `POST /maintenance-requests/{id}/resources` | RESIDENT, STAFF | Bất kỳ | Lưu URL file đính kèm; FE phải upload lên cloud storage trước, sau đó gửi URL |
| **resource/list** | `GET /maintenance-requests/{id}/resources` | ALL | — | Toàn bộ file đính kèm của yêu cầu |

---

## 7. maintenance-log

| Feature | API | Role | Tác dụng |
|---------|-----|------|---------|
| **log/list** | `GET /maintenance-requests/{id}/logs` | ALL | Lịch sử mọi thay đổi; log được ghi tự động — không thể tạo thủ công |

---

## 8. maintenance-statistics

| Feature | API | Role | Tác dụng |
|---------|-----|------|---------|
| **statistics/overview** | `GET /maintenance-requests/statistics` | ADMIN | Thống kê: tổng số, theo `status/category/priority`, rating TB, thời gian xử lý TB |
| **statistics/staff-workload** | `GET /maintenance-requests/staff-workload` | ADMIN | Khối lượng từng nhân viên: tổng YC, đang làm, hoàn thành, rating TB |
| **statistics/overdue** | `GET /maintenance-requests/overdue` | ADMIN | Danh sách YC `IN_PROGRESS` quá 7 ngày chưa xong |

---

## 9. Ma trận tổng hợp

| Resource/Feature | RESIDENT | STAFF | ADMIN |
|-----------------|----------|-------|-------|
| request/create | ✅ | ✗ | ✗ |
| request/list | ✅ | ✅ | ✅ |
| request/get | ✅ | ✅ | ✅ |
| request/update | ✅ PENDING | ✗ | ✗ |
| request/cancel | ✅ PENDING/VERIFYING | ✗ | ✅ |
| request/assign | ✗ | ✗ | ✅ |
| quotation/create | ✗ | ✅ VERIFYING/QUOTING | ✗ |
| quotation/update | ✗ | ✅ DRAFT | ✗ |
| quotation/send | ✗ | ✅ DRAFT | ✗ |
| quotation/approve | ✅ SENT | ✗ | ✗ |
| quotation/reject | ✅ SENT | ✗ | ✗ |
| quotation/cancel | ✗ | ✗ | ✅ |
| schedule/propose | ✅ APPROVED | ✅ APPROVED/IN_PROGRESS | ✅ |
| schedule/accept | ✅ (khi STAFF propose) | ✅ (khi RESIDENT propose) | ✅ |
| schedule/reject | ✅ | ✅ | ✅ |
| schedule/counter | ✅ | ✅ | ✅ |
| progress/add | ✗ | ✅ IN_PROGRESS | ✗ |
| progress/list | ✅ | ✅ | ✅ |
| review/submit | ✅ COMPLETED | ✗ | ✗ |
| review/get | ✅ | ✅ | ✅ |
| resource/add | ✅ | ✅ | ✗ |
| resource/list | ✅ | ✅ | ✅ |
| log/list | ✅ | ✅ | ✅ |
| statistics/overview | ✗ | ✗ | ✅ |
| statistics/staff-workload | ✗ | ✗ | ✅ |
| statistics/overdue | ✗ | ✗ | ✅ |

---

## 10. Nghiệp vụ từng role trong luồng

### 🏠 RESIDENT — Cư dân

Cư dân là người **khởi tạo** và **nghiệm thu** yêu cầu bảo trì. Trong suốt luồng, cư dân đóng vai trò quyết định ở 2 điểm quan trọng: **duyệt báo giá** và **nghiệm thu kết quả**.

```
1. Phát hiện vấn đề → Tạo yêu cầu (request/create)
      Điền title, mô tả, category, ưu tiên, thời gian mong muốn
      Kèm ảnh mô tả nếu cần (resource/add)

2. Chờ admin giao việc → Nhận thông báo (log/list để theo dõi)

3. Nhận báo giá từ staff → Xem (quotation/list)
      Đồng ý  → quotation/approve → APPROVED
      Từ chối → quotation/reject  → QUOTING (chờ báo giá mới)

4. Đề xuất lịch sửa chữa (schedule/propose)
      Hoặc nhận lịch từ staff → Phản hồi (schedule/accept / schedule/counter)

5. Theo dõi tiến độ (progress/list) trong quá trình IN_PROGRESS

6. Nhận thông báo hoàn thành (status = COMPLETED)
      Nghiệm thu (review/submit):
        ACCEPTED      → Đóng ticket ✅
        PARTIAL_ACCEPT → Đóng ticket, ghi nhận tồn tại
        REDO          → Staff tiếp tục làm lại
```

**Màn hình cần xây dựng (FE_Resident):**
- Danh sách yêu cầu của tôi
- Form tạo yêu cầu
- Trang chi tiết: tab Thông tin | Báo giá | Lịch sửa | Tiến độ | Nhật ký
- Modal nghiệm thu (review)

---

### 🔧 STAFF — Nhân viên kỹ thuật

Staff là người **thực hiện** yêu cầu: lập báo giá, thỏa thuận lịch, cập nhật tiến độ. Vai trò chính bắt đầu sau khi được admin giao việc.

```
1. Nhận yêu cầu được giao (request/list → filter theo staffId)
      Đọc mô tả, xem ảnh cư dân đính kèm (resource/list)

2. Lập báo giá (quotation/create → DRAFT)
      Thêm hạng mục: vật tư (MATERIAL), nhân công (LABOR), thuê ngoài (OUTSOURCE)
      Chỉnh sửa lại nếu cần (quotation/update)
      Gửi cho cư dân (quotation/send → WAITING_APPROVAL)

3. Chờ cư dân phê duyệt
      Nếu bị từ chối → Tạo báo giá mới (quotation/create lại)

4. Thỏa thuận lịch
      Cư dân propose → Xem xét → Chấp nhận (schedule/accept) hoặc đề xuất lại (schedule/counter)
      Chủ động propose nếu cần (schedule/propose)

5. Thực hiện sửa chữa (requestStatus = IN_PROGRESS)
      Cập nhật tiến độ nhiều lần (progress/add)
      Đính kèm ảnh trước/sau (resource/add)

6. Hoàn thành (progress/add với progressPercent = 100)
      → requestStatus tự động → COMPLETED

7. Xem đánh giá cư dân (review/get) để cải thiện
```

**Màn hình cần xây dựng (FE_Staff):**
- Danh sách việc được giao (filter theo staffId)
- Trang chi tiết: tab Thông tin | Báo giá | Lịch | Tiến độ | Nhật ký
- Modal tạo/sửa báo giá (với danh sách hạng mục)
- Modal cập nhật tiến độ (slider %)
- Modal đề xuất lịch

---

### 👑 ADMIN / MANAGER — Quản lý tòa nhà

Admin là người **điều phối** và **giám sát** toàn bộ luồng. Không trực tiếp thực hiện sửa chữa hay tương tác kỹ thuật, nhưng có quyền can thiệp ở bất kỳ bước nào.

```
1. Tiếp nhận yêu cầu mới (request/list → filter PENDING)
      Xem xét thông tin, đánh giá ưu tiên

2. Phân công nhân viên phù hợp (request/assign)
      Căn cứ vào: staff-workload (ai đang rảnh), loại hỏng hóc, vị trí

3. Giám sát tiến độ
      Theo dõi tất cả YC đang IN_PROGRESS
      Cảnh báo YC quá hạn (statistics/overdue)

4. Xử lý ngoại lệ
      Hủy YC không hợp lệ (request/cancel)
      Hủy báo giá sai (quotation/cancel)
      Giao lại nhân viên nếu cần (request/assign)

5. Báo cáo định kỳ (statistics/overview)
      Thống kê theo thời gian, tòa nhà
      Đánh giá hiệu suất từng nhân viên (statistics/staff-workload)
```

**Màn hình cần xây dựng (FE_Admin):**
- Dashboard: thống kê tổng quan + chart theo status/category
- Danh sách tất cả yêu cầu + bộ lọc nâng cao
- Trang chi tiết: toàn quyền xem, nút giao việc / hủy
- Bảng khối lượng nhân viên (workload)
- Danh sách yêu cầu quá hạn (overdue alert)
