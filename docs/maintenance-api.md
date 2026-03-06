# Tài liệu API - Module Bảo trì (Maintenance)

> **Phiên bản:** 3.0 | **Cập nhật:** 2026-03-06  
> **Base URL:** `http://localhost:8080/building-management/api/v1`  
> **Swagger UI:** `http://localhost:8080/building-management/swagger-ui/index.html`  
> **Content-Type:** `application/json`

---

## Mục lục

1. [Tổng quan & Enums](#1-tổng-quan--enums)
2. [maintenance-request](#2-maintenance-request)
3. [maintenance-quotation](#3-maintenance-quotation)
4. [maintenance-schedule](#4-maintenance-schedule)
5. [maintenance-progress](#5-maintenance-progress)
6. [maintenance-review](#6-maintenance-review)
7. [maintenance-resource](#7-maintenance-resource)
8. [maintenance-log](#8-maintenance-log)
9. [maintenance-statistics](#9-maintenance-statistics)
10. [Data Models](#10-data-models)
11. [Luồng nghiệp vụ đầy đủ](#11-luồng-nghiệp-vụ-đầy-đủ)

---

## 1. Tổng quan & Enums

### Cấu trúc Response chung

```json
{ "code": 200, "message": null, "result": { } }
```

Phân trang (`pagination=true`):
```json
{ "code": 200, "result": { "currentPage": 1, "pageSize": 10, "totalPages": 5, "totalElements": 47, "data": [] } }
```

### Enums

#### RequestStatus — Trạng thái yêu cầu
```
PENDING → VERIFYING → QUOTING → WAITING_APPROVAL → APPROVED
                                                        ↓
                                                   IN_PROGRESS → COMPLETED → RESIDENT_ACCEPTED
                                                        ↑ (REDO)
                       ↓ (bất kỳ lúc nào)
                    CANCELLED / REJECTED
```

| Value | Ý nghĩa | Badge |
|-------|---------|-------|
| `PENDING` | Mới tạo, chờ xử lý | 🟡 |
| `VERIFYING` | Đã giao staff, đang xác minh | 🔵 |
| `QUOTING` | Staff đang lập báo giá | 🟠 |
| `WAITING_APPROVAL` | Chờ cư dân duyệt báo giá | 🟣 |
| `APPROVED` | Báo giá được duyệt | 🟢 |
| `IN_PROGRESS` | Đang thực hiện | 🔵 |
| `COMPLETED` | Hoàn thành, chờ nghiệm thu | 🟢 |
| `RESIDENT_ACCEPTED` | Cư dân nghiệm thu xong | ✅ |
| `CANCELLED` | Đã hủy | ⚫ |

#### QuotationStatus
| Value | Ý nghĩa |
|-------|---------|
| `DRAFT` | Bản nháp |
| `SENT` | Đã gửi cư dân |
| `APPROVED` | Cư dân đồng ý |
| `REJECTED` | Cư dân từ chối |
| `CANCELLED` | Đã hủy |
| `EXPIRED` | Hết hạn (`validUntil`) |

#### ScheduleStatus
| Value | Ý nghĩa |
|-------|---------|
| `PROPOSED` | Đã đề xuất, chờ phản hồi |
| `CONFIRMED` | Đã xác nhận |
| `REJECTED` | Bị từ chối |
| `CANCELLED` | Đã hủy |
| `COUNTER_PROPOSED` | Đã bị đề xuất lại |

#### ReviewOutcome
| Value | Tác động |
|-------|---------|
| `ACCEPTED` | `requestStatus → RESIDENT_ACCEPTED` |
| `PARTIAL_ACCEPT` | `requestStatus → RESIDENT_ACCEPTED` |
| `REDO` | `requestStatus → IN_PROGRESS` |

#### Các enum khác
- **RequestPriority:** `LOW` · `NORMAL` · `HIGH` · `CRITICAL`
- **RequestScope:** `PRIVATE` (trong căn hộ) · `PUBLIC` (khu vực chung)
- **MaintenanceCategory:** `MAINTENANCE` · `REPAIR` · `SERVICE` · `CLEANING` · `OTHER`
- **ItemType:** `MATERIAL` · `LABOR` · `OUTSOURCE`
- **ResourceType:** `IMAGE` · `VIDEO` · `DOCUMENT` · `OTHER`
- **ScheduleProposedBy:** `RESIDENT` · `STAFF` · `MANAGER`

---

## 2. maintenance-request

### 2.1 maintenance-request/create

```
POST /maintenance-requests
```

**Role được phép:** RESIDENT

**Request Body:**
```json
{
  "title": "Rò rỉ nước phòng bếp",
  "description": "Ống nước dưới bồn rửa bị rò, nước chảy ra sàn",
  "scope": "PRIVATE",
  "category": "REPAIR",
  "priority": "HIGH",
  "preferredTime": "2026-03-10T09:00:00",
  "isBillable": false,
  "apartmentId": "uuid-apartment",
  "buildingId": "uuid-building"
}
```

| Field | Bắt buộc | Ghi chú |
|-------|:--------:|---------|
| `title` | ✅ | |
| `description` | ✅ | |
| `scope` | | Mặc định `PRIVATE` |
| `category`, `priority` | | |
| `preferredTime` | | ISO 8601 |
| `isBillable` | | `false` = miễn phí |
| `apartmentId`, `buildingId` | | |

**Kết quả:** `requestStatus = PENDING`, `code = "REQ-{timestamp}"`

---

### 2.2 maintenance-request/list

```
GET /maintenance-requests?keyword=&page=1&size=10&pagination=true
```

**Role được phép:** ALL

| Param | Mặc định | Ghi chú |
|-------|---------|---------|
| `keyword` | `""` | Tìm theo `title`, `code` |
| `page` | `1` | |
| `size` | `10` | |
| `pagination` | `true` | `false` = lấy toàn bộ |

> **FE lưu ý:** Backend chưa filter theo role. FE tự filter: resident → `requesterId`, staff → `staffId`.

---

### 2.3 maintenance-request/get

```
GET /maintenance-requests/{id}
```

**Role được phép:** ALL

---

### 2.4 maintenance-request/update

```
PUT /maintenance-requests/{id}
```

**Role được phép:** RESIDENT  
**Điều kiện:** `requestStatus = PENDING`

**Request Body** (tất cả optional):
```json
{
  "title": "Tiêu đề mới",
  "description": "Mô tả mới",
  "scope": "PRIVATE",
  "category": "REPAIR",
  "priority": "CRITICAL",
  "preferredTime": "2026-03-11T08:00:00"
}
```

---

### 2.5 maintenance-request/cancel

```
PATCH /maintenance-requests/{id}/cancel
```

**Role được phép:** RESIDENT, ADMIN  
**Điều kiện:** `requestStatus ∈ {PENDING, VERIFYING}`

**Request Body** (optional):
```json
{ "reason": "Tôi tự sửa được rồi" }
```

**Kết quả:** `requestStatus → CANCELLED`, `closedAt = now()`

---

### 2.6 maintenance-request/assign

```
PATCH /maintenance-requests/{id}/assign
```

**Role được phép:** ADMIN  
**Điều kiện:** `requestStatus ∈ {PENDING, VERIFYING}`

**Request Body:**
```json
{ "staffId": "uuid-staff" }
```

**Kết quả:** Gán `staffId`, `requestStatus → VERIFYING`

---

## 3. maintenance-quotation

### 3.1 maintenance-quotation/create

```
POST /maintenance-requests/{id}/quotations
```

**Role được phép:** STAFF  
**Điều kiện:** `requestStatus ∈ {VERIFYING, QUOTING}`

**Request Body:**
```json
{
  "title": "Báo giá thay ống nước",
  "description": "Ghi chú nội bộ của staff",
  "note": "Lưu ý dành cho cư dân",
  "validUntil": "2026-03-15T17:00:00",
  "items": [
    { "name": "Ống nhựa PVC 21mm", "itemType": "MATERIAL", "quantity": 5, "unitPrice": 35000 },
    { "name": "Công thợ lắp đặt",   "itemType": "LABOR",    "quantity": 2, "unitPrice": 150000 }
  ]
}
```

**Kết quả:** `quotationStatus = DRAFT`, `requestStatus → QUOTING`

> `totalAmount` BE không tự tính — FE tính: `items.reduce((s,i) => s + i.quantity * i.unitPrice, 0)`

---

### 3.2 maintenance-quotation/list

```
GET /maintenance-requests/{id}/quotations
```

**Role được phép:** ALL  
**Ghi chú:** Trả về tất cả báo giá. FE hiển thị nổi bật báo giá có `status = SENT`.

---

### 3.3 maintenance-quotation/get

```
GET /maintenance-requests/quotations/{quotationId}
```

**Role được phép:** ALL

---

### 3.4 maintenance-quotation/update

```
PUT /maintenance-requests/quotations/{quotationId}
```

**Role được phép:** STAFF  
**Điều kiện:** `quotationStatus = DRAFT`

**Request Body** (giống create, tất cả optional):
```json
{
  "title": "Báo giá cập nhật",
  "items": [
    { "name": "Ống PVC 27mm", "itemType": "MATERIAL", "quantity": 5, "unitPrice": 45000 }
  ]
}
```

> Khi gửi `items` → toàn bộ items cũ bị **xóa và thay thế**.

---

### 3.5 maintenance-quotation/send

```
PATCH /maintenance-requests/quotations/{quotationId}/status?status=SENT
```

**Role được phép:** STAFF  
**Điều kiện:** `quotationStatus = DRAFT`  
**Kết quả:** `quotationStatus → SENT`, `requestStatus → WAITING_APPROVAL`

---

### 3.6 maintenance-quotation/approve

```
PATCH /maintenance-requests/quotations/{quotationId}/status?status=APPROVED
```

**Role được phép:** RESIDENT  
**Điều kiện:** `quotationStatus = SENT`  
**Kết quả:** `quotationStatus → APPROVED`, `requestStatus → APPROVED`

---

### 3.7 maintenance-quotation/reject

```
PATCH /maintenance-requests/quotations/{quotationId}/status?status=REJECTED
```

**Role được phép:** RESIDENT  
**Điều kiện:** `quotationStatus = SENT`  
**Kết quả:** `quotationStatus → REJECTED`, `requestStatus → QUOTING` (staff lập báo giá mới)

---

### 3.8 maintenance-quotation/cancel

```
PATCH /maintenance-requests/quotations/{quotationId}/status?status=CANCELLED
```

**Role được phép:** ADMIN

---

## 4. maintenance-schedule

### 4.1 maintenance-schedule/propose

```
POST /maintenance-requests/{id}/schedules
```

**Role được phép:** RESIDENT, STAFF, ADMIN  
**Điều kiện:** `requestStatus ∈ {APPROVED, IN_PROGRESS}`

**Request Body:**
```json
{
  "proposedTime": "2026-03-12T08:00:00",
  "estimatedDuration": 120,
  "note": "Buổi sáng trước 11h"
}
```

**Kết quả:** `scheduleStatus = PROPOSED`, `proposedByRole` tự động theo người gọi

---

### 4.2 maintenance-schedule/list

```
GET /maintenance-requests/{id}/schedules
```

**Role được phép:** ALL  
**Ghi chú:** Trả về toàn bộ lịch sử đề xuất (kể cả counter-proposals). Dùng `parentScheduleId` để build cây.

---

### 4.3 maintenance-schedule/accept

```
PATCH /maintenance-requests/{id}/schedules/{scheduleId}/respond
```

**Role được phép:** Bên **không** đề xuất schedule đó  
**Điều kiện:** `scheduleStatus = PROPOSED`

**Body:**
```json
{ "action": "ACCEPT", "note": "OK, tôi sẽ có mặt" }
```

**Kết quả:** `scheduleStatus → CONFIRMED`, `requestStatus → IN_PROGRESS`, `startedAt = proposedTime`

---

### 4.4 maintenance-schedule/reject

```
PATCH /maintenance-requests/{id}/schedules/{scheduleId}/respond
```

**Body:**
```json
{ "action": "REJECT", "note": "Hôm đó tôi bận" }
```

**Kết quả:** `scheduleStatus → REJECTED`

---

### 4.5 maintenance-schedule/counter

```
PATCH /maintenance-requests/{id}/schedules/{scheduleId}/respond
```

**Body:**
```json
{
  "action": "COUNTER_PROPOSE",
  "counterProposedTime": "2026-03-12T14:00:00",
  "counterEstimatedDuration": 90,
  "note": "Chiều 2h được không?"
}
```

**Kết quả:** Schedule gốc → `COUNTER_PROPOSED`. Tạo record mới với `parentScheduleId = scheduleId`.

> **Quy tắc respond:**  
> - `proposedByRole = RESIDENT` → STAFF phải phản hồi  
> - `proposedByRole = STAFF` hoặc `MANAGER` → RESIDENT phải phản hồi

---

## 5. maintenance-progress

### 5.1 maintenance-progress/add

```
POST /maintenance-requests/{id}/progress
```

**Role được phép:** STAFF  
**Điều kiện:** `requestStatus = IN_PROGRESS`

**Request Body:**
```json
{
  "note": "Đã tháo ống cũ, đang lắp ống mới",
  "progressPercent": 60
}
```

> ⚠️ **Tự động:** `progressPercent >= 100` → `requestStatus → COMPLETED`, `finishedAt = now()`

---

### 5.2 maintenance-progress/list

```
GET /maintenance-requests/{id}/progress
```

**Role được phép:** ALL  
**Ghi chú:** Sắp xếp `createdAt` giảm dần — phần tử `[0]` là mới nhất → dùng cho progress bar.

---

## 6. maintenance-review

### 6.1 maintenance-review/submit

```
POST /maintenance-requests/{id}/review
```

**Role được phép:** RESIDENT  
**Điều kiện:** `requestStatus = COMPLETED`  
**Giới hạn:** Mỗi YC chỉ được đánh giá **1 lần**.

**Request Body:**
```json
{
  "rating": 4,
  "outcome": "ACCEPTED",
  "comment": "Làm nhanh, sạch sẽ, nhưng còn vết bẩn trên tường"
}
```

| Field | Bắt buộc | Ghi chú |
|-------|:--------:|---------|
| `rating` | ✅ | 1–5 sao |
| `outcome` | ✅ | `ACCEPTED` / `PARTIAL_ACCEPT` / `REDO` |
| `comment` | | |

**Kết quả:**
| outcome | requestStatus |
|---------|--------------|
| `ACCEPTED` | `→ RESIDENT_ACCEPTED`, `closedAt = now()` |
| `PARTIAL_ACCEPT` | `→ RESIDENT_ACCEPTED`, `closedAt = now()` |
| `REDO` | `→ IN_PROGRESS` (quay lại thực hiện) |

---

### 6.2 maintenance-review/get

```
GET /maintenance-requests/{id}/review
```

**Role được phép:** ALL  
**Lưu ý:** Trả về `404` nếu chưa có đánh giá.

---

## 7. maintenance-resource

### 7.1 maintenance-resource/add

```
POST /maintenance-requests/{id}/resources
```

**Role được phép:** RESIDENT, STAFF

| Role | Thời điểm | Mục đích |
|------|-----------|---------|
| RESIDENT | Khi `PENDING` | Ảnh mô tả vấn đề |
| STAFF | Khi `IN_PROGRESS` | Ảnh tiến độ / kết quả |

**Request Body:**
```json
{
  "name": "Ảnh rò nước dưới bồn",
  "url": "https://storage.example.com/images/leak.jpg",
  "resourceType": "IMAGE"
}
```

> **Lưu ý:** API nhận `url` không nhận file trực tiếp. FE upload lên cloud storage (S3/Firebase...) trước → lấy URL → mới gọi API.

---

### 7.2 maintenance-resource/list

```
GET /maintenance-requests/{id}/resources
```

**Role được phép:** ALL

---

## 8. maintenance-log

### 8.1 maintenance-log/list

```
GET /maintenance-requests/{id}/logs
```

**Role được phép:** ALL (chỉ đọc — log được ghi tự động bởi hệ thống)

**Response mẫu:**
```json
{
  "code": 200,
  "result": [
    { "id": "uuid-1", "action": "CREATE_REQUEST",  "note": "Tạo YC: Rò nước bếp", "actorId": null, "createdAt": "2026-03-06T10:00:00" },
    { "id": "uuid-2", "action": "ASSIGN_REQUEST",  "note": "Giao cho: Nguyễn Văn A", "actorId": null, "createdAt": "2026-03-06T10:15:00" },
    { "id": "uuid-3", "action": "UPDATE_PROGRESS", "note": "Tiến độ 60%", "actorId": null, "createdAt": "2026-03-06T14:00:00" }
  ]
}
```

> `actorId = null` — Authentication Context chưa được implement.

**Các giá trị `action`:**

| Action | Khi nào |
|--------|---------|
| `CREATE_REQUEST` | Tạo YC |
| `UPDATE_REQUEST` | Sửa YC |
| `CANCEL_REQUEST` | Hủy YC |
| `ASSIGN_REQUEST` | Giao nhân viên |
| `CREATE_QUOTATION` | Tạo báo giá |
| `UPDATE_QUOTATION` | Sửa báo giá |
| `UPDATE_QUOTATION_STATUS` | Thay đổi trạng thái BG |
| `ADD_RESOURCE` | Thêm file đính kèm |
| `PROPOSE_SCHEDULE` | Đề xuất lịch |
| `CONFIRM_SCHEDULE` | Xác nhận lịch |
| `REJECT_SCHEDULE` | Từ chối lịch |
| `COUNTER_PROPOSE_SCHEDULE` | Đề xuất lại lịch |
| `UPDATE_PROGRESS` | Cập nhật tiến độ |
| `COMPLETE_REQUEST` | Hoàn thành (progress 100%) |
| `RESIDENT_ACCEPTED` | Cư dân nghiệm thu |
| `REDO_REQUESTED` | YC làm lại |

---

## 9. maintenance-statistics

> **Role được phép:** ADMIN

### 9.1 maintenance-statistics/overview

```
GET /maintenance-requests/statistics?from=2026-01-01&to=2026-03-06&buildingId=uuid
```

**Response:**
```json
{
  "totalRequests": 120,
  "byStatus": { "PENDING": 5, "IN_PROGRESS": 12, "RESIDENT_ACCEPTED": 90, "CANCELLED": 13 },
  "byCategory": { "REPAIR": 60, "MAINTENANCE": 30, "CLEANING": 30 },
  "byPriority": { "HIGH": 45, "NORMAL": 60, "LOW": 15 },
  "avgRating": 4.2,
  "avgResolutionDays": 3.5,
  "overdueCount": 3
}
```

---

### 9.2 maintenance-statistics/staff-workload

```
GET /maintenance-requests/staff-workload
```

**Response:**
```json
[
  { "staffId": "uuid", "staffName": "Nguyễn Văn A", "totalAssigned": 15, "inProgress": 3, "completed": 11, "cancelled": 1, "avgRating": 4.5, "overdueCount": 0 }
]
```

---

### 9.3 maintenance-statistics/overdue

```
GET /maintenance-requests/overdue
```

Trả về `MaintenanceRequest[]` đang `IN_PROGRESS` mà `startedAt` > 7 ngày trước.

---

## 10. Data Models

### MaintenanceRequest
```typescript
interface MaintenanceRequest {
  id: string; code: string; title: string; description: string;
  isBillable: boolean;
  preferredTime: string | null; startedAt: string | null;
  finishedAt: string | null; closedAt: string | null;
  scope: 'PRIVATE' | 'PUBLIC';
  category: 'MAINTENANCE' | 'REPAIR' | 'SERVICE' | 'CLEANING' | 'OTHER';
  requestStatus: string; priority: string; paymentStatus: string | null;
  requesterId: string | null; requesterName: string | null;
  staffId: string | null; staffName: string | null;
  apartmentId: string | null; apartmentCode: string | null;
  buildingId: string | null; buildingName: string | null;
  createdAt: string; updatedAt: string;
}
```

### MaintenanceQuotation + Item
```typescript
interface MaintenanceQuotation {
  id: string; code: string; title: string; status: string;
  description: string | null; // ghi chú staff
  note: string | null;        // ghi chú cư dân
  totalAmount: number | null; // null — FE tự tính
  validUntil: string | null;
  items: MaintenanceItem[];
  createdAt: string; updatedAt: string;
}
interface MaintenanceItem {
  id: string; name: string; description: string | null;
  itemType: 'MATERIAL' | 'LABOR' | 'OUTSOURCE';
  quantity: number; unitPrice: number;
}
```

### MaintenanceSchedule
```typescript
interface MaintenanceSchedule {
  id: string; proposedTime: string; estimatedDuration: number | null;
  note: string | null; status: string;
  proposedByRole: 'RESIDENT' | 'STAFF' | 'MANAGER';
  proposedById: string | null; proposedByName: string | null;
  parentScheduleId: string | null; // null nếu không phải counter-proposal
  createdAt: string; updatedAt: string;
}
```

### MaintenanceProgress
```typescript
interface MaintenanceProgress {
  id: string; note: string; progressPercent: number; // 0-100
  updatedById: string | null; updatedByName: string | null;
  createdAt: string;
}
```

### MaintenanceReview
```typescript
interface MaintenanceReview {
  id: string; rating: number; // 1-5
  comment: string | null;
  outcome: 'ACCEPTED' | 'REDO' | 'PARTIAL_ACCEPT';
  reviewedById: string | null; reviewedByName: string | null;
  createdAt: string;
}
```

### MaintenanceResource
```typescript
interface MaintenanceResource {
  id: string; name: string; url: string;
  resourceType: 'IMAGE' | 'VIDEO' | 'DOCUMENT' | 'OTHER';
}
```

---

## 11. Luồng nghiệp vụ đầy đủ

### Happy Path

```
[RESIDENT] maintenance-request/create          → PENDING
[ADMIN]    maintenance-request/assign           → VERIFYING
[STAFF]    maintenance-quotation/create         → QUOTING (DRAFT)
[STAFF]    maintenance-quotation/send           → WAITING_APPROVAL
[RESIDENT] maintenance-quotation/approve        → APPROVED
[RESIDENT] maintenance-schedule/propose         → PROPOSED
[STAFF]    maintenance-schedule/accept          → IN_PROGRESS
[STAFF]    maintenance-progress/add (×n)        → cập nhật tiến độ
[STAFF]    maintenance-resource/add (×n)        → ảnh tiến độ
[STAFF]    maintenance-progress/add (100%)      → COMPLETED (tự động)
[RESIDENT] maintenance-review/submit (ACCEPTED) → RESIDENT_ACCEPTED ✅
```

### Luồng phụ — Cư dân từ chối báo giá
```
[RESIDENT] maintenance-quotation/reject → QUOTING
[STAFF]    maintenance-quotation/create (lại)
```

### Luồng phụ — Đàm phán lịch
```
[RESIDENT] maintenance-schedule/propose → PROPOSED
[STAFF]    maintenance-schedule/counter → COUNTER_PROPOSED + record mới
[RESIDENT] maintenance-schedule/accept  → IN_PROGRESS ✅
```

### Luồng phụ — Yêu cầu làm lại
```
[RESIDENT] maintenance-review/submit (REDO) → IN_PROGRESS
[STAFF]    maintenance-progress/add (×n)    → COMPLETED
[RESIDENT] maintenance-review/submit (ACCEPTED) → RESIDENT_ACCEPTED ✅
```

---

## Phụ lục — Checklist triển khai

| API (resource/feature) | Status | Ghi chú |
|------------------------|--------|---------|
| maintenance-request/create | ✅ | `requesterId` chưa set từ auth |
| maintenance-request/list | ✅ | Chưa filter theo role |
| maintenance-request/get | ✅ | |
| maintenance-request/update | ✅ | |
| maintenance-request/cancel | ✅ | |
| maintenance-request/assign | ✅ | |
| maintenance-quotation/create | ✅ | |
| maintenance-quotation/list | ✅ | |
| maintenance-quotation/get | ✅ | |
| maintenance-quotation/update | ✅ | |
| maintenance-quotation/send | ✅ | |
| maintenance-quotation/approve | ✅ | |
| maintenance-quotation/reject | ✅ | |
| maintenance-schedule/propose | ✅ | `proposedByRole` cứng RESIDENT |
| maintenance-schedule/list | ✅ | |
| maintenance-schedule/accept | ✅ | |
| maintenance-schedule/reject | ✅ | |
| maintenance-schedule/counter | ✅ | |
| maintenance-progress/add | ✅ | `updatedBy` chưa set từ auth |
| maintenance-progress/list | ✅ | |
| maintenance-review/submit | ✅ | `reviewedBy` chưa set từ auth |
| maintenance-review/get | ✅ | |
| maintenance-resource/add | ✅ | |
| maintenance-resource/list | ✅ | |
| maintenance-log/list | ✅ | `actorId` chưa set từ auth |
| maintenance-statistics/overview | ✅ | |
| maintenance-statistics/staff-workload | ✅ | |
| maintenance-statistics/overdue | ✅ | Ngưỡng cứng 7 ngày |
