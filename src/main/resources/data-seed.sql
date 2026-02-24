-- Script insert dữ liệu mẫu chi tiết cho hệ thống ABMS
-- Đảm bảo Hibernate đã tạo bảng với cột UUID là VARCHAR(36)

SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------
-- Xóa toàn bộ dữ liệu cũ
-- --------------------------------------------------------
TRUNCATE TABLE maintenance_logs;
TRUNCATE TABLE maintenance_reviews;
TRUNCATE TABLE maintenance_resources;
TRUNCATE TABLE maintenance_progresses;
TRUNCATE TABLE maintenance_items;
TRUNCATE TABLE maintenance_quotations;
TRUNCATE TABLE maintenance_schedules;
TRUNCATE TABLE maintenance_requests;
TRUNCATE TABLE apartment_residents;
TRUNCATE TABLE apartments;
TRUNCATE TABLE buildings;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE roles;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

-- --------------------------------------------------------
-- 1. ROLES
-- --------------------------------------------------------
SET @role_admin_id = UUID();
SET @role_manager_id = UUID();
SET @role_resident_id = UUID();
SET @role_staff_id = UUID();

INSERT INTO roles (id, code, name, description, created_at, updated_at, is_deleted) VALUES
(@role_admin_id, 'ADMIN', 'System Administrator', 'Quản trị viên hệ thống', NOW(), NOW(), 0),
(@role_manager_id, 'BUILDING_MANAGER', 'Building Manager', 'Quản lý tòa nhà', NOW(), NOW(), 0),
(@role_resident_id, 'RESIDENT', 'Resident', 'Cư dân', NOW(), NOW(), 0),
(@role_staff_id, 'STAFF', 'Staff', 'Nhân viên kỹ thuật', NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 2. USERS (10 Users)
-- --------------------------------------------------------
-- PASS: 123456
SET @u_admin = UUID();
SET @u_mgr1  = UUID();
SET @u_mgr2  = UUID();
SET @u_tech1 = UUID();
SET @u_tech2 = UUID();
SET @u_tech3 = UUID();
SET @u_res1  = UUID();
SET @u_res2  = UUID();
SET @u_res3  = UUID();
SET @u_res4  = UUID();

INSERT INTO users (id, full_name, email, password, phone, status, created_at, updated_at, is_deleted) VALUES
(@u_admin, 'Admin Tổng', 'admin@bms.vn', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0900000001', 'ACTIVE', NOW(), NOW(), 0),
(@u_mgr1,  'Lê Quản Lý A', 'mgr1@bms.vn', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0910000001', 'ACTIVE', NOW(), NOW(), 0),
(@u_mgr2,  'Phạm Quản Lý B', 'mgr2@bms.vn', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0910000002', 'ACTIVE', NOW(), NOW(), 0),
(@u_tech1, 'Trần Thợ Điện', 'tech1@bms.vn', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0920000001', 'ACTIVE', NOW(), NOW(), 0),
(@u_tech2, 'Nguyễn Thợ Nước', 'tech2@bms.vn', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0920000002', 'ACTIVE', NOW(), NOW(), 0),
(@u_tech3, 'Vũ Thợ Mộc', 'tech3@bms.vn', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0920000003', 'ACTIVE', NOW(), NOW(), 0),
(@u_res1,  'Nguyễn Văn Cư Dân 1', 'res1@gmail.com', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0930000001', 'ACTIVE', NOW(), NOW(), 0),
(@u_res2,  'Trần Thị Cư Dân 2', 'res2@gmail.com', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0930000002', 'ACTIVE', NOW(), NOW(), 0),
(@u_res3,  'Lê Văn Cư Dân 3', 'res3@gmail.com', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0930000003', 'ACTIVE', NOW(), NOW(), 0),
(@u_res4,  'Hoàng Thị Cư Dân 4', 'res4@gmail.com', '$2a$10$tZ8qZ5.2.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g.g', '0930000004', 'ACTIVE', NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 3. BUILDINGS (2 Buildings)
-- --------------------------------------------------------
SET @b1 = UUID();
SET @b2 = UUID();

INSERT INTO buildings (id, code, name, address, num_floors, apartments_per_floor_1br, apartments_per_floor_2br, apartments_per_floor_3br, area_1br_sqm, area_2br_sqm, area_3br_sqm, apartments_generated, created_at, updated_at, is_deleted) VALUES
(@b1, 'TOWER-A', 'Toà Ruby (Tower A)', 'Số 1 Trần Duy Hưng', 30, 0, 0, 0, 0, 0, 0, 1, NOW(), NOW(), 0),
(@b2, 'TOWER-B', 'Toà Diamond (Tower B)', 'Số 2 Trần Duy Hưng', 25, 0, 0, 0, 0, 0, 0, 1, NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 4. APARTMENTS (10 Apartments)
-- --------------------------------------------------------
SET @a1 = UUID(); SET @a2 = UUID(); SET @a3 = UUID(); SET @a4 = UUID(); SET @a5 = UUID();
SET @a6 = UUID(); SET @a7 = UUID(); SET @a8 = UUID(); SET @a9 = UUID(); SET @a10 = UUID();

INSERT INTO apartments (id, building_id, code, floor_number, area_sqm, bedroom_count, status, created_at, updated_at, is_deleted) VALUES
(@a1, @b1, 'A-0501', 5, 75.5, 2, 'OCCUPIED', NOW(), NOW(), 0),
(@a2, @b1, 'A-0502', 5, 55.0, 1, 'OCCUPIED', NOW(), NOW(), 0),
(@a3, @b1, 'A-1005', 10, 120.0, 3, 'OCCUPIED', NOW(), NOW(), 0),
(@a4, @b1, 'A-1201', 12, 75.5, 2, 'AVAILABLE', NOW(), NOW(), 0),
(@a5, @b1, 'A-2008', 20, 90.0, 2, 'OCCUPIED', NOW(), NOW(), 0),
(@a6, @b2, 'B-0201', 2, 75.5, 2, 'OCCUPIED', NOW(), NOW(), 0),
(@a7, @b2, 'B-0505', 5, 55.0, 1, 'OCCUPIED', NOW(), NOW(), 0),
(@a8, @b2, 'B-0810', 8, 120.0, 3, 'AVAILABLE', NOW(), NOW(), 0),
(@a9, @b2, 'B-1502', 15, 75.5, 2, 'OCCUPIED', NOW(), NOW(), 0),
(@a10, @b2, 'B-2201', 22, 90.0, 2, 'OCCUPIED', NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 5. APARTMENT_RESIDENTS (Mapping)
-- --------------------------------------------------------
INSERT INTO apartment_residents (id, apartment_id, user_id, resident_type, assigned_at, created_at, updated_at, is_deleted) VALUES
(UUID(), @a1, @u_res1, 'OWNER', NOW(), NOW(), NOW(), 0),
(UUID(), @a2, @u_res2, 'TENANT', NOW(), NOW(), NOW(), 0),
(UUID(), @a3, @u_res3, 'OWNER', NOW(), NOW(), NOW(), 0),
(UUID(), @a6, @u_res4, 'OWNER', NOW(), NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 6. USER_ROLES
-- --------------------------------------------------------
INSERT INTO user_roles (id, user_id, role_id, created_at, updated_at, is_deleted) VALUES
(UUID(), @u_admin, @role_admin_id, NOW(), NOW(), 0),
(UUID(), @u_mgr1, @role_manager_id, NOW(), NOW(), 0),
(UUID(), @u_mgr2, @role_manager_id, NOW(), NOW(), 0),
(UUID(), @u_tech1, @role_staff_id, NOW(), NOW(), 0),
(UUID(), @u_tech2, @role_staff_id, NOW(), NOW(), 0),
(UUID(), @u_tech3, @role_staff_id, NOW(), NOW(), 0),
(UUID(), @u_res1, @role_resident_id, NOW(), NOW(), 0),
(UUID(), @u_res2, @role_resident_id, NOW(), NOW(), 0),
(UUID(), @u_res3, @role_resident_id, NOW(), NOW(), 0),
(UUID(), @u_res4, @role_resident_id, NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 7. MAINTENANCE_REQUESTS (8 Requests with diff status)
-- --------------------------------------------------------
SET @r1 = UUID(); SET @r2 = UUID(); SET @r3 = UUID(); SET @r4 = UUID();
SET @r5 = UUID(); SET @r6 = UUID(); SET @r7 = UUID(); SET @r8 = UUID();

INSERT INTO maintenance_requests (id, code, requester_id, apartment_id, building_id, category, priority, request_status, title, description, scope, created_at, updated_at, is_deleted) VALUES
(@r1, 'REQ-001', @u_res1, @a1, @b1, 'REPAIR', 'HIGH', 'IN_PROGRESS', 'Hỏng vòi nước', 'Vòi nước bồn rửa bát bị rò rỉ mạnh', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 0),
(@r2, 'REQ-002', @u_res2, @a2, @b1, 'REPAIR', 'NORMAL', 'COMPLETED', 'Cháy bóng đèn', 'Bóng đèn phòng khách bị cháy cần thay', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(@r3, 'REQ-003', @u_res3, @a3, @b1, 'CLEANING', 'LOW', 'RESIDENT_ACCEPTED', 'Vệ sinh điều hoà', 'Cần bảo dưỡng vệ sinh 2 máy điều hoà Casper', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 15 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(@r4, 'REQ-004', @u_res1, NULL, @b1, 'OTHER', 'NORMAL', 'PENDING', 'Kẹt cửa thoát hiểm tầng 5', 'Cửa thoát hiểm tầng 5 khó mở, cần kiểm tra', 'PUBLIC', NOW(), NOW(), 0),
(@r5, 'REQ-005', @u_res4, @a6, @b2, 'REPAIR', 'CRITICAL', 'VERIFYING', 'Thấm trần nhà', 'Nước thấm từ tầng trên xuống trần phòng ngủ', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 0),
(@r6, 'REQ-006', @u_mgr1, NULL, @b2, 'MAINTENANCE', 'NORMAL', 'QUOTING', 'Bảo trì thang máy số 2', 'Lịch bảo trì định kỳ thang máy', 'PUBLIC', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0),
(@r7, 'REQ-007', @u_res2, @a2, @b1, 'SERVICE', 'LOW', 'CANCELLED', 'Di chuyển nội thất', 'Muốn nhờ thợ hỗ trợ bê tủ quần áo', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 20 DAY), DATE_SUB(NOW(), INTERVAL 19 DAY), 0),
(@r8, 'REQ-008', @u_res3, @a3, @b1, 'REPAIR', 'NORMAL', 'WAITING_APPROVAL', 'Sửa ổ cắm điện', 'Ổ cắm điện bị lỏng, toé lửa khi cắm', 'PRIVATE', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 0);

-- --------------------------------------------------------
-- 8. MAINTENANCE_SCHEDULES (5 records)
-- --------------------------------------------------------
INSERT INTO maintenance_schedules (id, maintenance_request_id, proposed_time, estimated_duration, status, proposed_by_role, proposed_by_id, created_at, updated_at, is_deleted) VALUES
(UUID(), @r1, DATE_ADD(NOW(), INTERVAL 1 DAY), 60, 'CONFIRMED', 'STAFF', @u_tech1, NOW(), NOW(), 0),
(UUID(), @r5, DATE_ADD(NOW(), INTERVAL 2 DAY), 120, 'PROPOSED', 'MANAGER', @u_mgr1, NOW(), NOW(), 0),
(UUID(), @r2, DATE_SUB(NOW(), INTERVAL 3 DAY), 30, 'CONFIRMED', 'STAFF', @u_tech2, DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 0),
(UUID(), @r3, DATE_SUB(NOW(), INTERVAL 7 DAY), 90, 'CONFIRMED', 'RESIDENT', @u_res3, DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 8 DAY), 0),
(UUID(), @r8, DATE_ADD(NOW(), INTERVAL 1 DAY), 45, 'PROPOSED', 'STAFF', @u_tech3, NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 9. MAINTENANCE_QUOTATIONS (5 records)
-- --------------------------------------------------------
SET @q1 = UUID(); SET @q2 = UUID(); SET @q3 = UUID();

INSERT INTO maintenance_quotations (id, maintenance_request_id, code, title, status, total_amount, created_at, updated_at, is_deleted) VALUES
(@q1, @r6, 'QT-elevator', 'Báo giá linh kiện thang máy', 'SENT', 5500000.00, NOW(), NOW(), 0),
(@q2, @r8, 'QT-electric', 'Báo giá vật tư điện ổ cắm', 'DRAFT', 150000.00, NOW(), NOW(), 0),
(@q3, @r3, 'QT-clean', 'Chi phí vệ sinh điều hoà', 'APPROVED', 400000.00, DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), 0);

-- --------------------------------------------------------
-- 10. MAINTENANCE_ITEMS
-- --------------------------------------------------------
INSERT INTO maintenance_items (id, quotation_id, name, item_type, quantity, unit_price, created_at, updated_at, is_deleted) VALUES
(UUID(), @q1, 'Cáp tải thang máy', 'MATERIAL', 2, 2000000.00, NOW(), NOW(), 0),
(UUID(), @q1, 'Dầu bôi trơn', 'MATERIAL', 1, 500000.00, NOW(), NOW(), 0),
(UUID(), @q1, 'Công thay thế', 'LABOR', 1, 1000000.00, NOW(), NOW(), 0),
(UUID(), @q2, 'Hạt ổ cắm Panasonic', 'MATERIAL', 2, 45000.00, NOW(), NOW(), 0),
(UUID(), @q3, 'Gói vệ sinh máy lạnh', 'LABOR', 2, 200000.00, NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 11. MAINTENANCE_PROGRESSES
-- --------------------------------------------------------
INSERT INTO maintenance_progresses (id, maintenance_request_id, note, progress_percent, updated_by_id, created_at, updated_at, is_deleted) VALUES
(UUID(), @r1, 'Đang tháo vòi cũ để kiểm tra cỡ ren', 20, @u_tech1, NOW(), NOW(), 0),
(UUID(), @r2, 'Đã thay xong bóng đèn, đang kiểm tra công tắc', 90, @u_tech2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
(UUID(), @r2, 'Hoàn tất bàn giao cho chủ nhà', 100, @u_tech2, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0);

-- --------------------------------------------------------
-- 12. MAINTENANCE_REVIEWS
-- --------------------------------------------------------
INSERT INTO maintenance_reviews (id, maintenance_request_id, rating, comment, outcome, reviewed_by_id, created_at, updated_at, is_deleted) VALUES
(UUID(), @r3, 5, 'Làm việc nhanh, sạch sẽ. Máy lạnh chạy rất mát.', 'ACCEPTED', @u_res3, NOW(), NOW(), 0),
(UUID(), @r2, 4, 'Tốt, nhưng thợ đến hơi muộn 15p so với hẹn', 'ACCEPTED', @u_res2, NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 13. MAINTENANCE_LOGS
-- --------------------------------------------------------
INSERT INTO maintenance_logs (id, request_id, actor_id, action, created_at, updated_at, is_deleted) VALUES
(UUID(), @r1, @u_res1, 'CREATED_REQUEST', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
(UUID(), @r1, @u_mgr1, 'ASSIGNED_STAFF', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 0),
(UUID(), @r5, @u_res4, 'CREATED_REQUEST', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0);
