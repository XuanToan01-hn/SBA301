# Tài liệu API - Module Bảo trì (Maintenance)

> **Phiên bản:** 2.0
> **Cập nhật:** 2026-02-23
> **Base URL:** `http://localhost:8080`
> **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
> **Content-Type:** `application/json`

---

## Mục lục

1. [Tổng quan hệ thống](#1-tổng-quan-hệ-thống)
2. [Cấu trúc Response chung](#2-cấu-trúc-response-chung)
3. [Enums & Giá trị hợp lệ](#3-enums--giá-trị-hợp-lệ)
4. [Data Model (TypeScript Interface)](#4-data-model-typescript-interface)
5. [API - Yêu cầu bảo trì](#5-api---yêu-cầu-bảo-trì)
6. [API - Báo giá](#6-api---báo-giá)
7. [API - Lịch sửa chữa](#7-api---lịch-sửa-chữa)
8. [API - Tiến độ](#8-api---tiến-độ)
9. [API - Tài nguyên đính kèm](#9-api---tài-nguyên-đính-kèm)
10. [API - Đánh giá kết quả](#10-api---đánh-giá-kết-quả)
11. [API - Lịch sử hoạt động](#11-api---lịch-sử-hoạt-động)
12. [API - Thống kê](#12-api---thống-kê)
13. [Luồng nghiệp vụ đầy đủ](#13-luồng-nghiệp-vụ-đầy-đủ)
14. [Hướng dẫn FE theo role](#14-hướng-dẫn-fe-theo-role)

---

## 1. Tổng quan hệ thống

### Vai trò (Role)

| Role | Mô tả |
|---|---|
| `RESIDENT` | Cư dân — người gửi yêu cầu bảo trì |
| `STAFF` | Nhân viên kỹ thuật — người thực hiện sửa chữa |
| `MANAGER` | Quản lý tòa nhà — có toàn quyền |

> ⚠️ **Lưu ý hiện tại:** Phân quyền chưa được implement — mọi API đang cho phép gọi tự do. Phân quyền sẽ được bổ sung sau.

### Controller Structure

| Controller | Base URL | Chức năng |
|---|---|---|
| `MaintenanceRequestController` | `/maintenance-requests` | CRUD yêu cầu, hủy, giao việc |
| `MaintenanceWorkflowController` | `/maintenance-requests` | Sub-resources: báo giá (nested), lịch, tiến độ, đánh giá, tài nguyên, log |
| `MaintenanceQuotationController` | `/maintenance-requests/quotations` | Thao tác trực tiếp theo quotationId |
| `MaintenanceStatisticsController` | `/maintenance-requests` | Thống kê, workload, overdue |

### Luồng trạng thái yêu cầu (RequestStatus)

```
PENDING ──────────────────────────────────────────────────► CANCELLED
   │                                                         ▲
   ▼                                                         │
VERIFYING ──────────────────────────────────────────────────┤
   │                                                         │
   ▼                                                         │
QUOTING (staff lập báo giá)                                  │
   │                                                         │
   ▼                                                         │
WAITING_APPROVAL (đã gửi báo giá cho cư dân)               │
   │                    │                                    │
   ▼                    ▼                                    │
APPROVED         QUOTING (cư dân từ chối → lập lại)        │
   │                                                         │
   ▼                                                         │
IN_PROGRESS (đang thực hiện, sau khi xác nhận lịch)         │
   │                    │                                    │
   ▼                    ▼                                    │
COMPLETED        IN_PROGRESS (cư dân yêu cầu làm lại)      │
   │
   ▼
RESIDENT_ACCEPTED (nghiệm thu xong, đóng ticket)
```

---

## 2. Cấu trúc Response chung

Tất cả API đều trả về cùng 1 wrapper `ApiResponse<T>`:

### Thành công

```json
{
  "code": 200,
  "message": null,
  "result": { }
}
```

### Lỗi

```json
{
  "code": 404,
  "message": "Maintenance request not found",
  "result": null
}
```

### Có phân trang (`pagination=true`)

```json
{
  "code": 200,
  "message": null,
  "result": {
    "currentPage": 1,
    "pageSize": 10,
    "totalPages": 5,
    "totalElements": 47,
    "data": [ ]
  }
}
```

### Danh sách không phân trang (`pagination=false`)

```json
{
  "code": 200,
  "message": null,
  "result": [ ]
}
```

---

## 3. Enums & Giá trị hợp lệ

### RequestStatus — Trạng thái yêu cầu

| Giá trị | Ý nghĩa | Badge màu gợi ý |
|---|---|---|
| `PENDING` | Chờ xử lý (mới tạo) | 🟡 Vàng |
| `VERIFYING` | Đang xác minh (đã giao cho staff) | 🔵 Xanh nhạt |
| `QUOTING` | Staff đang lập báo giá | 🟠 Cam |
| `WAITING_APPROVAL` | Chờ cư dân duyệt báo giá | 🟣 Tím |
| `APPROVED` | Báo giá được duyệt, chờ xác nhận lịch | 🟢 Xanh nhạt |
| `IN_PROGRESS` | Đang thực hiện sửa chữa | 🔵 Xanh đậm |
| `COMPLETED` | Hoàn thành, chờ cư dân nghiệm thu | 🟢 Xanh |
| `RESIDENT_ACCEPTED` | Cư dân nghiệm thu, đóng ticket | ✅ Xanh lá |
| `CANCELLED` | Đã hủy | ⚫ Xám |

### QuotationStatus — Trạng thái báo giá

| Giá trị | Ý nghĩa |
|---|---|
| `DRAFT` | Bản nháp (staff đang soạn) |
| `SENT` | Đã gửi cho cư dân, chờ phản hồi |
| `APPROVED` | Cư dân đồng ý |
| `REJECTED` | Cư dân từ chối |
| `CANCELLED` | Bị hủy |
| `EXPIRED` | Hết hạn (vượt quá `validUntil`) |

### ScheduleStatus — Trạng thái lịch sửa chữa

| Giá trị | Ý nghĩa |
|---|---|
| `PROPOSED` | Đã đề xuất, chờ phản hồi |
| `CONFIRMED` | Đã được chấp nhận |
| `REJECTED` | Bị từ chối |
| `CANCELLED` | Bị hủy |
| `COUNTER_PROPOSED` | Đã bị đề xuất lại (bản gốc) |

### ReviewOutcome — Kết quả nghiệm thu

| Giá trị | Ý nghĩa | Tác động |
|---|---|---|
| `ACCEPTED` | Đồng ý nghiệm thu | Status → `RESIDENT_ACCEPTED` |
| `PARTIAL_ACCEPT` | Chấp nhận một phần | Status → `RESIDENT_ACCEPTED` |
| `REDO` | Yêu cầu làm lại | Status → `IN_PROGRESS` |

### RequestPriority — Độ ưu tiên

| Giá trị | Badge màu gợi ý |
|---|---|
| `LOW` | 🟢 Xanh |
| `NORMAL` | 🔵 Xanh dương |
| `HIGH` | 🟠 Cam |
| `CRITICAL` | 🔴 Đỏ |

### RequestScope — Phạm vi

| Giá trị | Ý nghĩa |
|---|---|
| `PRIVATE` | Trong căn hộ riêng |
| `PUBLIC` | Khu vực chung tòa nhà |

### MaintenanceCategory — Danh mục

| Giá trị | Ý nghĩa |
|---|---|
| `MAINTENANCE` | Bảo trì định kỳ |
| `REPAIR` | Sửa chữa |
| `SERVICE` | Dịch vụ |
| `CLEANING` | Vệ sinh |
| `OTHER` | Khác |

### ItemType — Loại hạng mục báo giá

| Giá trị | Ý nghĩa |
|---|---|
| `MATERIAL` | Vật tư, nguyên liệu |
| `LABOR` | Nhân công |
| `OUTSOURCE` | Thuê ngoài |

### ResourceType — Loại tài nguyên đính kèm

| Giá trị | Ý nghĩa |
|---|---|
| `IMAGE` | Hình ảnh |
| `VIDEO` | Video |
| `DOCUMENT` | Tài liệu |
| `OTHER` | Khác |

---

## 4. Data Model (TypeScript Interface)

### MaintenanceRequest

```typescript
interface MaintenanceRequest {
  id: string;                 // UUID
  code: string;               // "REQ-1740268800000" — mã hiển thị cho user
  title: string;
  description: string;
  isBillable: boolean;        // true = tính phí cư dân

  preferredTime: string | null;  // ISO 8601 — thời gian cư dân mong muốn
  startedAt: string | null;      // thời gian bắt đầu thực tế (sau ACCEPT schedule)
  finishedAt: string | null;     // thời gian hoàn thành (sau progress 100%)
  closedAt: string | null;       // thời gian đóng (sau nghiệm thu)

  scope: 'PRIVATE' | 'PUBLIC';
  category: 'MAINTENANCE' | 'REPAIR' | 'SERVICE' | 'CLEANING' | 'OTHER';
  requestStatus: string;         // RequestStatus enum
  priority: 'LOW' | 'NORMAL' | 'HIGH' | 'CRITICAL';
  paymentStatus: 'UNPAID' | 'PAID' | 'PARTIAL' | 'REFUNDED' | null;

  requesterId: string | null;    // ID cư dân (null khi chưa có auth)
  requesterName: string | null;

  staffId: string | null;        // null = chưa được giao
  staffName: string | null;

  apartmentId: string | null;
  apartmentCode: string | null;  // VD: "A1201"
  buildingId: string | null;
  buildingName: string | null;

  createdAt: string;
  updatedAt: string;
}
```

**Lưu ý FE:**
- `code` là mã hiển thị cho user (như mã ticket), dùng thay `id` trong UI.
- `staffId = null` → hiển thị badge "Chưa có nhân viên".
- `isBillable = false` → ẩn phần thanh toán.
- `requesterId` và `staffId` hiện đang trả về `null` vì chưa có Authentication Context.

---

### MaintenanceQuotation

```typescript
interface MaintenanceQuotation {
  id: string;
  code: string;               // "Q-1740269000000"
  title: string;
  status: string;             // QuotationStatus enum
  description: string | null; // ghi chú của staff
  note: string | null;        // ghi chú cho cư dân
  totalAmount: number | null; // tổng tiền (hiện BE chưa tự tính)
  validUntil: string | null;  // hạn báo giá
  items: MaintenanceItem[];
  createdAt: string;
  updatedAt: string;
}

interface MaintenanceItem {
  id: string;
  name: string;
  description: string | null;
  itemType: 'MATERIAL' | 'LABOR' | 'OUTSOURCE';
  quantity: number;
  unitPrice: number;          // đơn giá (VNĐ)
  // totalPrice = quantity * unitPrice — FE tự tính
}
```

**Lưu ý FE:**
- `totalAmount` trả về `null` — FE tự tính: `items.reduce((sum, i) => sum + i.quantity * i.unitPrice, 0)`.
- Một yêu cầu có thể có **nhiều báo giá** (khi cư dân từ chối). Hiển thị báo giá mới nhất có status `SENT` để cư dân phản hồi.

---

### MaintenanceSchedule

```typescript
interface MaintenanceSchedule {
  id: string;
  maintenanceRequestId: string;
  proposedTime: string;         // ISO 8601
  estimatedDuration: number | null; // phút
  note: string | null;
  status: string;               // ScheduleStatus enum
  proposedByRole: 'RESIDENT' | 'STAFF' | 'MANAGER';
  proposedById: string | null;
  proposedByName: string | null;
  parentScheduleId: string | null; // ID lịch gốc nếu đây là counter-proposal
  createdAt: string;
  updatedAt: string;
}
```

**Lưu ý FE:**
- `parentScheduleId != null` → đây là lịch đề xuất lại (counter-proposal).
- Khi hiển thị timeline đàm phán lịch: filter theo `parentScheduleId` để build cây phân cấp.

---

### MaintenanceProgress

```typescript
interface MaintenanceProgress {
  id: string;
  maintenanceRequestId: string;
  note: string;
  progressPercent: number;    // 0 - 100
  updatedById: string | null;
  updatedByName: string | null;
  createdAt: string;
  updatedAt: string;
}
```

**Lưu ý FE:**
- Khi `progressPercent >= 100`, BE tự động chuyển request status → `COMPLETED`.
- Hiển thị progress bar theo giá trị của entry mới nhất.

---

### MaintenanceReview

```typescript
interface MaintenanceReview {
  id: string;
  maintenanceRequestId: string;
  rating: number;              // 1 - 5 sao
  comment: string | null;
  outcome: 'ACCEPTED' | 'REDO' | 'PARTIAL_ACCEPT';
  reviewedById: string | null;
  reviewedByName: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

### MaintenanceResource

```typescript
interface MaintenanceResource {
  id: string;
  name: string;
  url: string;                 // URL ảnh/video/tài liệu
  resourceType: 'IMAGE' | 'VIDEO' | 'DOCUMENT' | 'OTHER';
}
```

---

### MaintenanceLog

```typescript
interface MaintenanceLog {
  id: string;
  action: string;
  note: string;
  actorId: string | null;     // null vì chưa có Authentication Context
  createdAt: string;
}
```

**Các giá trị `action`:**

| Action | Khi nào |
|---|---|
| `CREATE_REQUEST` | Tạo yêu cầu |
| `UPDATE_REQUEST` | Cập nhật yêu cầu |
| `CANCEL_REQUEST` | Hủy yêu cầu |
| `ASSIGN_REQUEST` | Giao cho nhân viên |
| `CREATE_QUOTATION` | Tạo báo giá |
| `UPDATE_QUOTATION` | Cập nhật báo giá |
| `UPDATE_QUOTATION_STATUS` | Thay đổi trạng thái báo giá |
| `ADD_RESOURCE` | Thêm tài nguyên đính kèm |
| `PROPOSE_SCHEDULE` | Đề xuất lịch sửa chữa |
| `CONFIRM_SCHEDULE` | Xác nhận lịch |
| `REJECT_SCHEDULE` | Từ chối lịch |
| `COUNTER_PROPOSE_SCHEDULE` | Đề xuất lại lịch |
| `UPDATE_PROGRESS` | Cập nhật tiến độ |
| `COMPLETE_REQUEST` | Hoàn thành sửa chữa (progress 100%) |
| `RESIDENT_ACCEPTED` | Cư dân nghiệm thu |
| `REDO_REQUESTED` | Cư dân yêu cầu làm lại |

---

## 5. API - Yêu cầu bảo trì

### 5.1 Tạo yêu cầu bảo trì

```
POST /maintenance-requests
```

**Request Body:**

```json
{
  "title": "Rò rỉ nước phòng bếp",
  "description": "Ống nước dưới bồn rửa bị rò, nước chảy ra sàn",
  "scope": "PRIVATE",
  "category": "REPAIR",
  "priority": "HIGH",
  "preferredTime": "2026-02-25T09:00:00",
  "isBillable": false,
  "apartmentId": "uuid-apartment",
  "buildingId": "uuid-building"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `title` | string | ✅ | Tiêu đề ngắn gọn |
| `description` | string | ✅ | Mô tả chi tiết vấn đề |
| `scope` | enum | | `PRIVATE` hoặc `PUBLIC` |
| `category` | enum | | Loại bảo trì |
| `priority` | enum | | Độ ưu tiên |
| `preferredTime` | datetime | | Thời gian cư dân mong muốn |
| `isBillable` | boolean | | `true` = tính phí cư dân |
| `apartmentId` | UUID | | ID căn hộ |
| `buildingId` | UUID | | ID tòa nhà |

**Tác động:** Tự sinh `code = "REQ-{timestamp}"`, `requestStatus = PENDING`.

**Response `200 OK`:** Trả về `MaintenanceRequest`.

---

### 5.2 Danh sách yêu cầu

```
GET /maintenance-requests
```

**Query Parameters:**

| Param | Kiểu | Mặc định | Mô tả |
|---|---|---|---|
| `keyword` | string | `""` | Tìm theo `title` hoặc `code` |
| `page` | number | `1` | Trang hiện tại (bắt đầu từ 1) |
| `size` | number | `10` | Số item mỗi trang |
| `pagination` | boolean | `true` | `false` = lấy toàn bộ |

**Ví dụ:**

```
GET /maintenance-requests?keyword=rò rỉ&page=1&size=10
GET /maintenance-requests?pagination=false
```

---

### 5.3 Chi tiết yêu cầu

```
GET /maintenance-requests/{id}
```

**Response `200 OK`:** Trả về `MaintenanceRequest`.

**Response `404`:**
```json
{ "code": 404, "message": "Maintenance request not found", "result": null }
```

---

### 5.4 Cập nhật yêu cầu

```
PUT /maintenance-requests/{id}
```

**Request Body** (tất cả field đều optional):

```json
{
  "title": "Tiêu đề mới",
  "description": "Mô tả mới",
  "scope": "PRIVATE",
  "category": "REPAIR",
  "priority": "CRITICAL",
  "preferredTime": "2026-02-26T08:00:00",
  "isBillable": true,
  "status": "IN_PROGRESS",
  "paymentStatus": "UNPAID",
  "staffId": "uuid-staff"
}
```

> **Lưu ý:** `staffId` ở đây cập nhật trực tiếp — để **giao việc đúng chuẩn** nên dùng endpoint `PATCH /{id}/assign` thay thế.

**Response `200 OK`:** Trả về `MaintenanceRequest` đã cập nhật.

---

### 5.5 Hủy yêu cầu

```
PATCH /maintenance-requests/{id}/cancel
```

**Request Body** (không bắt buộc):

```json
{
  "reason": "Tôi tự sửa được rồi"
}
```

**Tác động:** `requestStatus → CANCELLED`, `closedAt = now()`.

**Response `200 OK`:** Trả về `MaintenanceRequest`.

---

### 5.6 Giao yêu cầu cho nhân viên

```
PATCH /maintenance-requests/{id}/assign
```

**Request Body:**

```json
{
  "staffId": "uuid-staff"
}
```

**Tác động:** Gán staff, `requestStatus → VERIFYING`.

**Response `200 OK`:** Trả về `MaintenanceRequest`.

---

## 6. API - Báo giá

### 6.1 Tạo báo giá

```
POST /maintenance-requests/{id}/quotations
```

**Request Body:**

```json
{
  "title": "Báo giá thay ống nước",
  "description": "Cần thay toàn bộ đường ống PVC",
  "note": "Lưu ý dành cho cư dân",
  "validUntil": "2026-03-01T17:00:00",
  "items": [
    {
      "name": "Ống nhựa PVC 21mm",
      "description": "Loại A, nhập khẩu",
      "itemType": "MATERIAL",
      "quantity": 5,
      "unitPrice": 35000
    },
    {
      "name": "Công thợ lắp đặt",
      "itemType": "LABOR",
      "quantity": 2,
      "unitPrice": 150000
    }
  ]
}
```

**Tác động:** Tạo báo giá với `status = DRAFT`, `requestStatus → QUOTING`.

**Response `200 OK`:** Trả về `MaintenanceQuotation` (với `items`).

---

### 6.2 Danh sách báo giá của yêu cầu

```
GET /maintenance-requests/{id}/quotations
```

**Response `200 OK`:** Trả về `MaintenanceQuotation[]`.

---

### 6.3 Chi tiết báo giá

```
GET /maintenance-requests/quotations/{quotationId}
```

**Response `200 OK`:** Trả về `MaintenanceQuotation`.

---

### 6.4 Cập nhật báo giá

```
PUT /maintenance-requests/quotations/{quotationId}
```

> Chỉ cập nhật được khi `status = DRAFT`.

**Request Body** (tương tự tạo, tất cả optional):

```json
{
  "title": "Báo giá thay ống nước (cập nhật)",
  "validUntil": "2026-03-05T17:00:00",
  "items": [
    {
      "name": "Ống nhựa PVC 27mm",
      "itemType": "MATERIAL",
      "quantity": 5,
      "unitPrice": 45000
    }
  ]
}
```

> Khi `items` được gửi lên, toàn bộ items cũ sẽ bị **xóa và thay thế** bằng items mới.

**Response `200 OK`:** Trả về `MaintenanceQuotation`.

---

### 6.5 Cập nhật trạng thái báo giá

```
PATCH /maintenance-requests/quotations/{quotationId}/status?status={QuotationStatus}
```

**Luồng trạng thái:**

```
DRAFT ──[STAFF]──► SENT ──[RESIDENT]──► APPROVED
                      │
                      └──[RESIDENT]──► REJECTED
```

**Tác động kèm theo:**

| Chuyển | Tác động lên Request |
|---|---|
| `DRAFT → SENT` | `requestStatus → WAITING_APPROVAL` |
| `SENT → APPROVED` | `requestStatus → APPROVED` |
| `SENT → REJECTED` | `requestStatus → QUOTING` (staff lập báo giá mới) |

**Response `200 OK`:** Trả về `MaintenanceQuotation`.

---

## 7. API - Lịch sửa chữa

### 7.1 Đề xuất lịch sửa chữa

```
POST /maintenance-requests/{id}/schedules
```

**Request Body:**

```json
{
  "proposedTime": "2026-02-28T08:00:00",
  "estimatedDuration": 120,
  "note": "Buổi sáng trước 11h, tôi ở nhà"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `proposedTime` | datetime | ✅ | Thời gian đề xuất |
| `estimatedDuration` | number | | Thời lượng ước tính (phút) |
| `note` | string | | Ghi chú thêm |

**Tác động:** Tạo schedule với `status = PROPOSED`, `proposedByRole = RESIDENT`.

**Response `200 OK`:** Trả về `MaintenanceSchedule`.

---

### 7.2 Danh sách lịch của yêu cầu

```
GET /maintenance-requests/{id}/schedules
```

**Response `200 OK`:** Trả về `MaintenanceSchedule[]` (sắp xếp theo `createdAt` tăng dần).

---

### 7.3 Phản hồi lịch đề xuất

```
PATCH /maintenance-requests/{id}/schedules/{scheduleId}/respond
```

**Request Body — Chấp nhận:**

```json
{
  "action": "ACCEPT",
  "note": "OK, tôi sẽ đến đúng giờ"
}
```

**Request Body — Từ chối:**

```json
{
  "action": "REJECT",
  "note": "Ngày này tôi bận, vui lòng đổi ngày khác"
}
```

**Request Body — Đề xuất lại (Counter-propose):**

```json
{
  "action": "COUNTER_PROPOSE",
  "counterProposedTime": "2026-02-28T14:00:00",
  "counterEstimatedDuration": 90,
  "note": "Buổi sáng tôi có lịch khác, chiều 2h được không?"
}
```

| Field | Bắt buộc khi | Mô tả |
|---|---|---|
| `action` | ✅ | `ACCEPT`, `REJECT`, hoặc `COUNTER_PROPOSE` |
| `note` | | Ghi chú |
| `counterProposedTime` | `COUNTER_PROPOSE` | Thời gian đề xuất lại |
| `counterEstimatedDuration` | | Thời lượng (phút) |

**Tác động:**

| Action | Tác động |
|---|---|
| `ACCEPT` | Schedule → `CONFIRMED`, `requestStatus → IN_PROGRESS`, `startedAt = proposedTime` |
| `REJECT` | Schedule → `REJECTED` |
| `COUNTER_PROPOSE` | Schedule gốc → `COUNTER_PROPOSED`, tạo schedule mới với `proposedByRole = STAFF`, `parentScheduleId` trỏ về schedule gốc |

**Response `200 OK`:** Trả về `MaintenanceSchedule` (schedule mới nếu là COUNTER_PROPOSE).

---

## 8. API - Tiến độ

### 8.1 Cập nhật tiến độ

```
POST /maintenance-requests/{id}/progress
```

**Request Body:**

```json
{
  "note": "Đã tháo xong ống cũ, đang chờ vật tư về",
  "progressPercent": 40
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `note` | string | ✅ | Mô tả tiến độ |
| `progressPercent` | number (0-100) | | % hoàn thành |

**Tác động:** Nếu `progressPercent >= 100` → `requestStatus → COMPLETED`, `finishedAt = now()`.

**Response `200 OK`:** Trả về `MaintenanceProgress`.

---

### 8.2 Lịch sử tiến độ

```
GET /maintenance-requests/{id}/progress
```

**Response `200 OK`:** Trả về `MaintenanceProgress[]` (sắp xếp theo `createdAt` tăng dần).

---

## 9. API - Tài nguyên đính kèm

### 9.1 Thêm tài nguyên

```
POST /maintenance-requests/{id}/resources
```

**Request Body:**

```json
{
  "name": "Ảnh chỗ bị rò nước",
  "url": "https://storage.example.com/images/leak-photo.jpg",
  "resourceType": "IMAGE"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `name` | string | ✅ | Tên mô tả file |
| `url` | string | ✅ | URL đầy đủ (upload lên storage trước) |
| `resourceType` | enum | | `IMAGE` / `VIDEO` / `DOCUMENT` / `OTHER` |

> **Lưu ý FE:** API nhận `url`, không nhận file trực tiếp. FE cần upload lên cloud storage (S3, Firebase, Cloudinary,...) trước, lấy URL rồi mới gọi API này.

**Response `200 OK`:** Trả về `MaintenanceResource`.

---

### 9.2 Danh sách tài nguyên

```
GET /maintenance-requests/{id}/resources
```

**Response `200 OK`:** Trả về `MaintenanceResource[]`.

---

## 10. API - Đánh giá kết quả

> Chỉ có thể gửi đánh giá khi `requestStatus = COMPLETED`. Mỗi yêu cầu chỉ được đánh giá **một lần**.

### 10.1 Gửi đánh giá

```
POST /maintenance-requests/{id}/review
```

**Request Body:**

```json
{
  "rating": 4,
  "comment": "Làm nhanh, gọn, nhưng còn vết bẩn trên tường",
  "outcome": "ACCEPTED"
}
```

| Field | Kiểu | Bắt buộc | Mô tả |
|---|---|:---:|---|
| `rating` | number (1-5) | ✅ | Số sao |
| `comment` | string | | Nhận xét |
| `outcome` | enum | ✅ | `ACCEPTED`, `REDO`, `PARTIAL_ACCEPT` |

**Tác động theo `outcome`:**

| Outcome | Tác động |
|---|---|
| `ACCEPTED` | `requestStatus → RESIDENT_ACCEPTED`, `finishedAt = now()`, `closedAt = now()` |
| `PARTIAL_ACCEPT` | `requestStatus → RESIDENT_ACCEPTED`, `finishedAt = now()`, `closedAt = now()` |
| `REDO` | `requestStatus → IN_PROGRESS` (quay lại thực hiện) |

**Response `200 OK`:** Trả về `MaintenanceReview`.

---

### 10.2 Xem đánh giá

```
GET /maintenance-requests/{id}/review
```

**Response `200 OK`:** Trả về `MaintenanceReview`.

**Response `404`:** Nếu chưa có đánh giá.

---

## 11. API - Lịch sử hoạt động

```
GET /maintenance-requests/{id}/logs
```

**Response `200 OK`:** Trả về `MaintenanceLog[]` (sắp xếp theo `createdAt` tăng dần).

```json
{
  "code": 200,
  "result": [
    {
      "id": "uuid-1",
      "action": "CREATE_REQUEST",
      "note": "Tao yeu cau bao tri: Rò rỉ nước phòng bếp",
      "actorId": null,
      "createdAt": "2026-02-23T10:00:00"
    },
    {
      "id": "uuid-2",
      "action": "ASSIGN_REQUEST",
      "note": "Giao cho nhan vien: Nguyễn Văn A",
      "actorId": null,
      "createdAt": "2026-02-23T10:15:00"
    }
  ]
}
```

> **Lưu ý FE:** `actorId = null` vì chưa có Authentication Context. Hiển thị timeline từ cũ → mới theo `createdAt`.

---

## 12. API - Thống kê

### 12.1 Thống kê tổng quan

```
GET /maintenance-requests/statistics?from=2026-01-01&to=2026-02-28&buildingId=uuid
```

**Query Parameters:**

| Param | Bắt buộc | Mô tả |
|---|---|---|
| `from` | | Ngày bắt đầu lọc (format: `yyyy-MM-dd`) |
| `to` | | Ngày kết thúc lọc (format: `yyyy-MM-dd`) |
| `buildingId` | | Lọc theo tòa nhà |

**Response `200 OK`:**

```json
{
  "code": 200,
  "result": {
    "totalRequests": 120,
    "pendingCount": 5,
    "inProgressCount": 12,
    "completedCount": 90,
    "cancelledCount": 13,
    "overdueCount": 3,
    "avgResolutionDays": 3.5,
    "avgRating": 4.2,
    "byStatus": {
      "PENDING": 5,
      "IN_PROGRESS": 12,
      "RESIDENT_ACCEPTED": 90,
      "CANCELLED": 13
    },
    "byCategory": {
      "REPAIR": 60,
      "MAINTENANCE": 30,
      "CLEANING": 30
    },
    "byPriority": {
      "HIGH": 45,
      "NORMAL": 60,
      "LOW": 15
    }
  }
}
```

---

### 12.2 Khối lượng công việc của nhân viên

```
GET /maintenance-requests/staff-workload
```

**Response `200 OK`:** Trả về array `StaffWorkload[]`.

```json
{
  "code": 200,
  "result": [
    {
      "staffId": "uuid-staff-1",
      "staffName": "Nguyễn Văn A",
      "totalAssigned": 15,
      "inProgress": 3,
      "completed": 11,
      "cancelled": 1,
      "avgRating": 4.5,
      "overdueCount": 0
    }
  ]
}
```

---

### 12.3 Yêu cầu quá hạn

```
GET /maintenance-requests/overdue
```

Trả về danh sách yêu cầu `IN_PROGRESS` đã bắt đầu hơn **7 ngày** mà chưa hoàn thành.

**Response `200 OK`:** Trả về `MaintenanceRequest[]`.

---

## 13. Luồng nghiệp vụ đầy đủ

### Happy Path

```
Bước 1: RESIDENT tạo yêu cầu
  POST /maintenance-requests
  → requestStatus: PENDING

Bước 2: MANAGER giao cho STAFF
  PATCH /maintenance-requests/{id}/assign  { staffId }
  → requestStatus: VERIFYING

Bước 3: STAFF lập báo giá
  POST /maintenance-requests/{id}/quotations  { title, items... }
  → quotationStatus: DRAFT, requestStatus: QUOTING

Bước 4: STAFF gửi báo giá cho cư dân
  PATCH /maintenance-requests/quotations/{qId}/status?status=SENT
  → quotationStatus: SENT, requestStatus: WAITING_APPROVAL

Bước 5: RESIDENT duyệt báo giá
  PATCH /maintenance-requests/quotations/{qId}/status?status=APPROVED
  → quotationStatus: APPROVED, requestStatus: APPROVED

Bước 6: RESIDENT đề xuất lịch sửa chữa
  POST /maintenance-requests/{id}/schedules  { proposedTime, estimatedDuration }
  → scheduleStatus: PROPOSED

Bước 7: STAFF xác nhận lịch
  PATCH /maintenance-requests/{id}/schedules/{sId}/respond  { action: "ACCEPT" }
  → scheduleStatus: CONFIRMED, requestStatus: IN_PROGRESS, startedAt được set

Bước 8: STAFF cập nhật tiến độ (có thể nhiều lần)
  POST /maintenance-requests/{id}/progress  { note, progressPercent: 50 }
  POST /maintenance-requests/{id}/resources  { url, resourceType: "IMAGE" }

Bước 9: STAFF hoàn thành sửa chữa
  POST /maintenance-requests/{id}/progress  { note: "Xong", progressPercent: 100 }
  → requestStatus: COMPLETED (tự động), finishedAt được set
  POST /maintenance-requests/{id}/resources  { url ảnh kết quả }

Bước 10: RESIDENT nghiệm thu
  POST /maintenance-requests/{id}/review  { rating: 5, outcome: "ACCEPTED" }
  → requestStatus: RESIDENT_ACCEPTED, closedAt được set
```

---

### Luồng phụ — Cư dân từ chối báo giá

```
Bước 5 (thay thế): RESIDENT từ chối
  PATCH /maintenance-requests/quotations/{qId}/status?status=REJECTED
  → quotationStatus: REJECTED, requestStatus: QUOTING

→ Quay lại Bước 3: STAFF lập báo giá mới
```

---

### Luồng phụ — Đàm phán lịch sửa chữa

```
Bước 7 (thay thế): STAFF đề xuất lại lịch
  PATCH /maintenance-requests/{id}/schedules/{sId}/respond
  { action: "COUNTER_PROPOSE", counterProposedTime: "...", note: "..." }
  → Tạo schedule mới (proposedByRole=STAFF, parentScheduleId=sId)

RESIDENT phản hồi schedule mới:
  PATCH /maintenance-requests/{id}/schedules/{newSId}/respond
  { action: "ACCEPT" }  hoặc  { action: "COUNTER_PROPOSE", ... }
```

---

### Luồng phụ — Cư dân yêu cầu làm lại

```
Bước 10 (thay thế): RESIDENT yêu cầu làm lại
  POST /maintenance-requests/{id}/review  { outcome: "REDO", comment: "..." }
  → requestStatus: IN_PROGRESS (quay lại Bước 8)
```

---

### Luồng phụ — Hủy yêu cầu

```
Bất kỳ lúc nào (PENDING hoặc VERIFYING):
  PATCH /maintenance-requests/{id}/cancel  { reason: "..." }
  → requestStatus: CANCELLED, closedAt được set
```

---

## 14. Hướng dẫn FE theo role

### RESIDENT

| Màn hình | API sử dụng |
|---|---|
| Danh sách yêu cầu của tôi | `GET /maintenance-requests` |
| Tạo yêu cầu mới | `POST /maintenance-requests` |
| Chi tiết yêu cầu | `GET /maintenance-requests/{id}` |
| Hủy yêu cầu | `PATCH /{id}/cancel` |
| Xem báo giá | `GET /{id}/quotations` |
| Phản hồi báo giá | `PATCH /quotations/{qId}/status?status=APPROVED/REJECTED` |
| Đề xuất lịch sửa chữa | `POST /{id}/schedules` |
| Xem lịch sửa chữa | `GET /{id}/schedules` |
| Phản hồi counter-propose | `PATCH /{id}/schedules/{sId}/respond` |
| Xem tiến độ | `GET /{id}/progress` |
| Nghiệm thu kết quả | `POST /{id}/review` |
| Upload ảnh vấn đề | `POST /{id}/resources` |
| Xem timeline | `GET /{id}/logs` |

**Hiển thị nút hành động theo `requestStatus`:**

| Status | Nút hành động |
|---|---|
| `PENDING` | Chỉnh sửa (`PUT`), Hủy (`PATCH /cancel`) |
| `WAITING_APPROVAL` | Duyệt (`APPROVED`) / Từ chối (`REJECTED`) báo giá |
| `APPROVED` | Đề xuất lịch sửa chữa |
| `IN_PROGRESS` | Xem tiến độ |
| `COMPLETED` | Nghiệm thu: Đồng ý / Làm lại |
| `RESIDENT_ACCEPTED` + `isBillable=true` | Thanh toán |

---

### STAFF

| Màn hình | API sử dụng |
|---|---|
| Danh sách việc được giao | `GET /maintenance-requests` |
| Chi tiết yêu cầu | `GET /maintenance-requests/{id}` |
| Lập báo giá | `POST /{id}/quotations` |
| Chỉnh sửa báo giá (DRAFT) | `PUT /quotations/{qId}` |
| Gửi báo giá cho cư dân | `PATCH /quotations/{qId}/status?status=SENT` |
| Phản hồi lịch sửa chữa | `PATCH /{id}/schedules/{sId}/respond` |
| Cập nhật tiến độ | `POST /{id}/progress` |
| Upload ảnh tiến độ / kết quả | `POST /{id}/resources` |
| Xem timeline | `GET /{id}/logs` |

---

### MANAGER

| Màn hình | API sử dụng |
|---|---|
| Dashboard thống kê | `GET /maintenance-requests/statistics` |
| Yêu cầu quá hạn | `GET /maintenance-requests/overdue` |
| Workload nhân viên | `GET /maintenance-requests/staff-workload` |
| Danh sách tất cả yêu cầu | `GET /maintenance-requests` |
| Giao việc cho nhân viên | `PATCH /{id}/assign` |
| Hủy bất kỳ yêu cầu nào | `PATCH /{id}/cancel` |
| Xem toàn bộ thông tin | Tất cả GET endpoints |

---

## Phụ lục — Checklist implement

| API | Status | Ghi chú |
|---|---|---|
| `POST /maintenance-requests` | ✅ Có | `requesterId` chưa set từ auth |
| `GET /maintenance-requests` | ✅ Có | Chưa filter theo role |
| `GET /maintenance-requests/{id}` | ✅ Có | |
| `PUT /maintenance-requests/{id}` | ✅ Có | |
| `PATCH /{id}/cancel` | ✅ Có | |
| `PATCH /{id}/assign` | ✅ Có | |
| `POST /{id}/quotations` | ✅ Có | |
| `GET /{id}/quotations` | ✅ Có | |
| `GET /quotations/{quotationId}` | ✅ Có | |
| `PUT /quotations/{quotationId}` | ✅ Có | |
| `PATCH /quotations/{quotationId}/status` | ✅ Có | |
| `POST /{id}/resources` | ✅ Có | |
| `GET /{id}/resources` | ✅ Có | |
| `GET /{id}/logs` | ✅ Có | `actorId` chưa set từ auth |
| `POST /{id}/schedules` | ✅ Có | `proposedByRole` cứng là RESIDENT |
| `GET /{id}/schedules` | ✅ Có | |
| `PATCH /{id}/schedules/{sId}/respond` | ✅ Có | |
| `POST /{id}/progress` | ✅ Có | `updatedBy` chưa set từ auth |
| `GET /{id}/progress` | ✅ Có | |
| `POST /{id}/review` | ✅ Có | `reviewedBy` chưa set từ auth |
| `GET /{id}/review` | ✅ Có | |
| `GET /statistics` | ✅ Có | |
| `GET /staff-workload` | ✅ Có | |
| `GET /overdue` | ✅ Có | Ngưỡng cứng 7 ngày |
