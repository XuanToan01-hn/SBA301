-- Script insert dữ liệu mẫu chi tiết cho hệ thống ABMS
-- Đảm bảo Hibernate đã tạo bảng với cột UUID là VARCHAR(36)

CREATE DATABASE IF NOT EXISTS building_management;
USE building_management;

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
TRUNCATE TABLE bill_details;
TRUNCATE TABLE monthly_bills;
TRUNCATE TABLE meter_readings;
TRUNCATE TABLE service_tariff_tiers;
TRUNCATE TABLE service_tariffs;
TRUNCATE TABLE services;
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
                                                                                                        (UUID(), @r1, @u_mgr1, 'ASSIGNED_STAFF', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY), 0);
                                                                                                        -- --------------------------------------------------------
-- 14. SERVICES
-- --------------------------------------------------------

SET @svc_electric = UUID();
SET @svc_water = UUID();
SET @svc_mgmt = UUID();
SET @svc_parking = UUID();

INSERT INTO services
(id, code, name, description, unit, is_recurring, billing_method,
 taxable, is_active, created_at, updated_at, is_deleted)
VALUES

    (@svc_electric,
     'ELECTRICITY',
     'Điện sinh hoạt',
     'Tiền điện theo chỉ số công tơ',
     'kWh',
     1,
     'TIER',
     1,
     1,
     NOW(),NOW(),0),

    (@svc_water,
     'WATER',
     'Nước sinh hoạt',
     'Tiền nước theo công tơ',
     'm3',
     1,
     'METER',
     1,
     1,
     NOW(),NOW(),0),

    (@svc_mgmt,
     'MGMT_FEE',
     'Phí quản lý',
     'Phí quản lý theo diện tích căn hộ',
     'm2',
     1,
     'AREA',
     1,
     1,
     NOW(),NOW(),0),

    (@svc_parking,
     'PARKING_FEE',
     'Phí gửi xe',
     'Phí gửi xe cố định hàng tháng',
     'xe/tháng',
     1,
     'FIXED',
     1,
     1,
     NOW(),NOW(),0);

-- --------------------------------------------------------
-- 15. SERVICE TARIFFS
-- --------------------------------------------------------

SET @tariff_electric = UUID();
SET @tariff_water = UUID();
SET @tariff_mgmt = UUID();
SET @tariff_parking = UUID();

INSERT INTO service_tariffs
(id, service_id, price, currency,
 effective_from, effective_to,
 vat_rate,
 created_at,updated_at,is_deleted)

VALUES

    (@tariff_electric,
     @svc_electric,
     0,
     'VND',
     '2025-01-01',
     NULL,
     10,
     NOW(),NOW(),0),

    (@tariff_water,
     @svc_water,
     15000,
     'VND',
     '2025-01-01',
     NULL,
     10,
     NOW(),NOW(),0),

    (@tariff_mgmt,
     @svc_mgmt,
     12000,
     'VND',
     '2025-01-01',
     NULL,
     10,
     NOW(),NOW(),0),

    (@tariff_parking,
     @svc_parking,
     100000,
     'VND',
     '2025-01-01',
     NULL,
     10,
     NOW(),NOW(),0);
-- --------------------------------------------------------
-- 16. ELECTRICITY TIERS
-- --------------------------------------------------------

INSERT INTO service_tariff_tiers
(id,tariff_id,min_val,max_val,price,
 created_at,updated_at,is_deleted)

VALUES

    (UUID(),@tariff_electric,0,50,1800,NOW(),NOW(),0),
    (UUID(),@tariff_electric,51,100,2000,NOW(),NOW(),0),
    (UUID(),@tariff_electric,101,200,2500,NOW(),NOW(),0),
    (UUID(),@tariff_electric,201,NULL,3000,NOW(),NOW(),0);

-- --------------------------------------------------------
-- 17. METER READINGS JAN-2026
-- --------------------------------------------------------

INSERT INTO meter_readings
(id,
 apartment_id,
 service_id,
 period,
 old_index,
 new_index,
 consumption,
 is_meter_reset,
 photo_url,
 taken_at,
 taken_by,
 status,
 note,
 created_at,
 updated_at,
 is_deleted)

VALUES

    (UUID(),@a1,@svc_electric,'2026-01',1200,1350,150,0,NULL,NOW(),@u_tech1,'CONFIRMED','Ghi điện tháng 1',NOW(),NOW(),0),

    (UUID(),@a1,@svc_water,'2026-01',350,365,15,0,NULL,NOW(),@u_tech1,'CONFIRMED','Ghi nước tháng 1',NOW(),NOW(),0),


    (UUID(),@a2,@svc_electric,'2026-01',800,900,100,0,NULL,NOW(),@u_tech2,'CONFIRMED','Điện tháng 1',NOW(),NOW(),0),

    (UUID(),@a2,@svc_water,'2026-01',210,218,8,0,NULL,NOW(),@u_tech2,'CONFIRMED','Nước tháng 1',NOW(),NOW(),0),


    (UUID(),@a3,@svc_electric,'2026-01',1500,1680,180,0,NULL,NOW(),@u_tech1,'CONFIRMED','Điện tháng 1',NOW(),NOW(),0),

    (UUID(),@a3,@svc_water,'2026-01',500,520,20,0,NULL,NOW(),@u_tech1,'CONFIRMED','Nước tháng 1',NOW(),NOW(),0),


    (UUID(),@a6,@svc_electric,'2026-01',900,980,80,0,NULL,NOW(),@u_tech3,'CONFIRMED','Điện tháng 1',NOW(),NOW(),0),

    (UUID(),@a6,@svc_water,'2026-01',300,312,12,0,NULL,NOW(),@u_tech3,'CONFIRMED','Nước tháng 1',NOW(),NOW(),0);



















-- =============================================
-- MONTHLY BILLS - tháng 3/2026
-- =============================================
SET @bill1 = UUID();
SET @bill2 = UUID();
SET @bill3 = UUID();

INSERT INTO monthly_bills (
    id, apartment_id,
    period_from, period_to, period_code,
    subtotal, tax_total, total_amount,
    status, issued_at, due_date, locked,
    created_at, updated_at, is_deleted
) VALUES
      (
          @bill1,
          @a1,
          '2026-03-01 00:00:00', '2026-03-31 23:59:59', '2026-03',
          500000, 50000, 550000,
          'UNPAID', NOW(), '2026-04-10 23:59:59', 0,
          NOW(), NOW(), 0
      ),
      (
          @bill2,
          @a2,
          '2026-03-01 00:00:00', '2026-03-31 23:59:59', '2026-03',
          700000, 70000, 770000,
          'UNPAID', NOW(), '2026-04-10 23:59:59', 0,
          NOW(), NOW(), 0
      ),
      (
          @bill3,
          @a3,
          '2026-03-01 00:00:00', '2026-03-31 23:59:59', '2026-03',
          900000, 90000, 990000,
          'UNPAID', NOW(), '2026-04-10 23:59:59', 0,
          NOW(), NOW(), 0
      );

-- =============================================
-- BILL DETAILS - 2 dòng cho mỗi bill
-- =============================================
INSERT INTO bill_details (
    id, bill_id,
    description, quantity, unit_price, amount,
    tax_rate, total_line,
    created_at, updated_at, is_deleted
) VALUES
-- Bill 1
('bd000001-0000-0000-0000-000000000001', @bill1,
 'Phí quản lý tháng 3/2026', 1, 300000, 300000, 10, 330000, NOW(), NOW(), 0),
('bd000002-0000-0000-0000-000000000002', @bill1,
 'Tiền điện tháng 3/2026', 150, 1500, 225000, 10, 247500, NOW(), NOW(), 0),

-- Bill 2
('bd000003-0000-0000-0000-000000000003', @bill2,
 'Phí quản lý tháng 3/2026', 1, 300000, 300000, 10, 330000, NOW(), NOW(), 0),
('bd000004-0000-0000-0000-000000000004', @bill2,
 'Tiền nước tháng 3/2026', 20, 20000, 400000, 10, 440000, NOW(), NOW(), 0),

-- Bill 3
('bd000005-0000-0000-0000-000000000005', @bill3,
 'Phí quản lý tháng 3/2026', 1, 300000, 300000, 10, 330000, NOW(), NOW(), 0),
('bd000006-0000-0000-0000-000000000006', @bill3,
 'Tiền điện tháng 3/2026', 400, 1800, 720000, 10, 792000, NOW(), NOW(), 0);

-- --------------------------------------------------------
-- 18. BULK MAINTENANCE REQUESTS FOR STAFF TESTING
-- --------------------------------------------------------
-- Muc tieu: tao nhieu ticket da assign de test trang /maintenance cua STAFF.

INSERT INTO maintenance_requests (
    id, code, requester_id, staff_id, apartment_id, building_id,
    category, priority, request_status, title, description, scope,
    created_at, updated_at, is_deleted
) VALUES
    (UUID(), 'REQ-1001', @u_res1, @u_tech1, @a1, @b1, 'REPAIR', 'HIGH', 'VERIFYING',
     'Dieu hoa phong ngu keu to', 'Dieu hoa phat ra tieng on, can kiem tra block.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 14 DAY), DATE_SUB(NOW(), INTERVAL 13 DAY), 0),
    (UUID(), 'REQ-1002', @u_res1, @u_tech1, @a1, @b1, 'REPAIR', 'NORMAL', 'IN_PROGRESS',
     'Bon rua bep thoat nuoc cham', 'Bon rua thoat nuoc cham va co mui.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 11 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
    (UUID(), 'REQ-1003', @u_res2, @u_tech1, @a2, @b1, 'REPAIR', 'LOW', 'WAITING_APPROVAL',
     'Thay khoa cua phong ngu', 'Khoa cua bi ket, can thay khoa moi.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 10 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
    (UUID(), 'REQ-1004', @u_res2, @u_tech1, @a2, @b1, 'SERVICE', 'NORMAL', 'COMPLETED',
     'Can ho tro lap rem cua', 'Lap rem cua phong khach theo kich thuoc co san.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 25 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY), 0),
    (UUID(), 'REQ-1005', @u_res3, @u_tech1, @a3, @b1, 'REPAIR', 'CRITICAL', 'IN_PROGRESS',
     'Ro dien tu tu dien', 'Tu dien co mui khe va aptomat nhay lien tuc.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 3 DAY), NOW(), 0),

    (UUID(), 'REQ-1006', @u_res4, @u_tech2, @a6, @b2, 'REPAIR', 'HIGH', 'VERIFYING',
     'Tran phong khach bi tham', 'Vet tham lan rong sau mua lon.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 12 DAY), DATE_SUB(NOW(), INTERVAL 9 DAY), 0),
    (UUID(), 'REQ-1007', @u_res4, @u_tech2, @a6, @b2, 'MAINTENANCE', 'NORMAL', 'QUOTING',
     'Bao tri nong lanh dinh ky', 'Nong lanh can ve sinh va thay thanh magie.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 8 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 0),
    (UUID(), 'REQ-1008', @u_res3, @u_tech2, @a3, @b1, 'REPAIR', 'HIGH', 'IN_PROGRESS',
     'Cong tac den bep bi cham chap', 'Bat den co tieng tach tach, nghi cham chap.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 5 DAY), NOW(), 0),
    (UUID(), 'REQ-1009', @u_res2, @u_tech2, @a2, @b1, 'OTHER', 'LOW', 'CANCELLED',
     'Yeu cau son lai cua go', 'Chu nha doi lich va huy yeu cau.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 18 DAY), DATE_SUB(NOW(), INTERVAL 17 DAY), 0),
    (UUID(), 'REQ-1010', @u_res1, @u_tech2, @a1, @b1, 'REPAIR', 'NORMAL', 'RESIDENT_ACCEPTED',
     'Thay day cap TV', 'Day cap cu hu hong, da thay moi.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 28 DAY), DATE_SUB(NOW(), INTERVAL 12 DAY), 0),

    (UUID(), 'REQ-1011', @u_res3, @u_tech3, @a3, @b1, 'SERVICE', 'LOW', 'PENDING',
     'Ho tro van chuyen may giat', 'Can 2 nguoi di doi may giat sang ban cong.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0),
    (UUID(), 'REQ-1012', @u_res4, @u_tech3, @a6, @b2, 'REPAIR', 'HIGH', 'VERIFYING',
     'Ro nuoc tu tran nha ve sinh', 'Tran nha ve sinh tang tren ro nuoc nho giot.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY), 0),
    (UUID(), 'REQ-1013', @u_res1, @u_tech3, @a1, @b1, 'CLEANING', 'NORMAL', 'COMPLETED',
     'Ve sinh ong thong gio', 'Ve sinh ong thong gio bep va nha ve sinh.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 22 DAY), DATE_SUB(NOW(), INTERVAL 10 DAY), 0),
    (UUID(), 'REQ-1014', @u_res2, @u_tech3, @a2, @b1, 'MAINTENANCE', 'NORMAL', 'WAITING_APPROVAL',
     'Bao tri khoa van nuoc tong', 'Van tong can bao tri de tranh ket cung.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 0),
    (UUID(), 'REQ-1015', @u_res4, @u_tech3, @a6, @b2, 'REPAIR', 'CRITICAL', 'IN_PROGRESS',
     'Mat dien cuc bo trong can ho', 'Mot phan can ho bi mat dien khong ro nguyen nhan.', 'PRIVATE',
     DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 0);

-- --------------------------------------------------------
-- 19. BULK SCHEDULES / QUOTATIONS / PROGRESS / LOGS
-- --------------------------------------------------------

INSERT INTO maintenance_schedules (
    id, maintenance_request_id, proposed_time, estimated_duration,
    status, proposed_by_role, proposed_by_id, created_at, updated_at, is_deleted
)
SELECT UUID(), r.id,
       DATE_ADD(NOW(), INTERVAL 1 + (ROW_NUMBER() OVER (ORDER BY r.created_at)) DAY),
       45 + (ROW_NUMBER() OVER (ORDER BY r.created_at) * 5),
       CASE WHEN r.request_status IN ('COMPLETED', 'RESIDENT_ACCEPTED') THEN 'CONFIRMED' ELSE 'PROPOSED' END,
       'STAFF',
       r.staff_id,
       DATE_SUB(NOW(), INTERVAL 1 DAY), NOW(), 0
FROM maintenance_requests r
WHERE r.code IN ('REQ-1001','REQ-1002','REQ-1003','REQ-1004','REQ-1005','REQ-1006','REQ-1007','REQ-1008','REQ-1010','REQ-1012','REQ-1013','REQ-1014','REQ-1015');

INSERT INTO maintenance_quotations (
    id, maintenance_request_id, code, title, status, total_amount, created_at, updated_at, is_deleted
)
SELECT UUID(), r.id,
       CONCAT('QT-', r.code),
       CONCAT('Bao gia cho ', r.code),
       CASE
           WHEN r.request_status IN ('WAITING_APPROVAL') THEN 'SENT'
           WHEN r.request_status IN ('COMPLETED', 'RESIDENT_ACCEPTED') THEN 'APPROVED'
           WHEN r.request_status IN ('QUOTING', 'VERIFYING') THEN 'DRAFT'
           ELSE 'SENT'
       END,
       CASE
           WHEN r.priority = 'CRITICAL' THEN 3200000
           WHEN r.priority = 'HIGH' THEN 1800000
           WHEN r.priority = 'NORMAL' THEN 900000
           ELSE 450000
       END,
       DATE_SUB(NOW(), INTERVAL 2 DAY), NOW(), 0
FROM maintenance_requests r
WHERE r.code IN ('REQ-1001','REQ-1003','REQ-1004','REQ-1005','REQ-1006','REQ-1007','REQ-1008','REQ-1010','REQ-1012','REQ-1013','REQ-1014','REQ-1015');

INSERT INTO maintenance_items (
    id, quotation_id, name, item_type, quantity, unit_price, created_at, updated_at, is_deleted
)
SELECT UUID(), q.id, 'Vat tu chinh', 'MATERIAL', 1,
       CASE WHEN q.total_amount >= 2000000 THEN 1400000 ELSE 500000 END,
       NOW(), NOW(), 0
FROM maintenance_quotations q
WHERE q.code LIKE 'QT-REQ-10%';

INSERT INTO maintenance_items (
    id, quotation_id, name, item_type, quantity, unit_price, created_at, updated_at, is_deleted
)
SELECT UUID(), q.id, 'Nhan cong', 'LABOR', 1,
       CASE WHEN q.total_amount >= 2000000 THEN 1000000 ELSE 300000 END,
       NOW(), NOW(), 0
FROM maintenance_quotations q
WHERE q.code LIKE 'QT-REQ-10%';

INSERT INTO maintenance_progresses (
    id, maintenance_request_id, note, progress_percent, updated_by_id, created_at, updated_at, is_deleted
)
SELECT UUID(), r.id,
       CONCAT('Cap nhat tien do ticket ', r.code),
       CASE
           WHEN r.request_status = 'VERIFYING' THEN 15
           WHEN r.request_status = 'QUOTING' THEN 25
           WHEN r.request_status = 'WAITING_APPROVAL' THEN 40
           WHEN r.request_status = 'IN_PROGRESS' THEN 70
           WHEN r.request_status IN ('COMPLETED', 'RESIDENT_ACCEPTED') THEN 100
           WHEN r.request_status = 'CANCELLED' THEN 0
           ELSE 10
       END,
       r.staff_id,
       NOW(), NOW(), 0
FROM maintenance_requests r
WHERE r.code IN ('REQ-1001','REQ-1002','REQ-1003','REQ-1004','REQ-1005','REQ-1006','REQ-1007','REQ-1008','REQ-1009','REQ-1010','REQ-1011','REQ-1012','REQ-1013','REQ-1014','REQ-1015');

INSERT INTO maintenance_logs (
    id, request_id, actor_id, action, created_at, updated_at, is_deleted
)
SELECT UUID(), r.id, r.requester_id, 'CREATED_REQUEST', DATE_SUB(r.created_at, INTERVAL 2 HOUR), DATE_SUB(r.created_at, INTERVAL 2 HOUR), 0
FROM maintenance_requests r
WHERE r.code IN ('REQ-1001','REQ-1002','REQ-1003','REQ-1004','REQ-1005','REQ-1006','REQ-1007','REQ-1008','REQ-1009','REQ-1010','REQ-1011','REQ-1012','REQ-1013','REQ-1014','REQ-1015');

INSERT INTO maintenance_logs (
    id, request_id, actor_id, action, created_at, updated_at, is_deleted
)
SELECT UUID(), r.id, r.staff_id, 'ASSIGNED_STAFF', DATE_SUB(r.created_at, INTERVAL 1 HOUR), DATE_SUB(r.created_at, INTERVAL 1 HOUR), 0
FROM maintenance_requests r
WHERE r.code IN ('REQ-1001','REQ-1002','REQ-1003','REQ-1004','REQ-1005','REQ-1006','REQ-1007','REQ-1008','REQ-1009','REQ-1010','REQ-1011','REQ-1012','REQ-1013','REQ-1014','REQ-1015');

-- --------------------------------------------------------
-- 20. BULK REVIEWS FOR COMPLETED REQUESTS
-- --------------------------------------------------------
INSERT INTO maintenance_reviews (
    id, maintenance_request_id, rating, comment, outcome, reviewed_by_id, created_at, updated_at, is_deleted
)
SELECT UUID(), r.id,
       CASE WHEN r.priority IN ('HIGH','CRITICAL') THEN 4 ELSE 5 END,
       CONCAT('Danh gia mau cho ', r.code),
       'ACCEPTED',
       r.requester_id,
       NOW(), NOW(), 0
FROM maintenance_requests r
WHERE r.request_status IN ('COMPLETED', 'RESIDENT_ACCEPTED')
  AND r.code IN ('REQ-1004','REQ-1010','REQ-1013');

-- --------------------------------------------------------
-- 21. EXTRA METER READINGS FOR 2026-02, 2026-03
-- --------------------------------------------------------
INSERT INTO meter_readings (
    id, apartment_id, service_id, period, old_index, new_index, consumption,
    is_meter_reset, photo_url, taken_at, taken_by, status, note,
    created_at, updated_at, is_deleted
) VALUES
    (UUID(), @a1, @svc_electric, '2026-02', 1350, 1510, 160, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Dien thang 2', NOW(), NOW(), 0),
    (UUID(), @a1, @svc_water,    '2026-02', 365, 381, 16, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Nuoc thang 2', NOW(), NOW(), 0),
    (UUID(), @a2, @svc_electric, '2026-02', 900, 1008, 108, 0, NULL, NOW(), @u_tech2, 'CONFIRMED', 'Dien thang 2', NOW(), NOW(), 0),
    (UUID(), @a2, @svc_water,    '2026-02', 218, 227, 9, 0, NULL, NOW(), @u_tech2, 'CONFIRMED', 'Nuoc thang 2', NOW(), NOW(), 0),
    (UUID(), @a3, @svc_electric, '2026-02', 1680, 1865, 185, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Dien thang 2', NOW(), NOW(), 0),
    (UUID(), @a3, @svc_water,    '2026-02', 520, 542, 22, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Nuoc thang 2', NOW(), NOW(), 0),
    (UUID(), @a6, @svc_electric, '2026-02', 980, 1068, 88, 0, NULL, NOW(), @u_tech3, 'CONFIRMED', 'Dien thang 2', NOW(), NOW(), 0),
    (UUID(), @a6, @svc_water,    '2026-02', 312, 325, 13, 0, NULL, NOW(), @u_tech3, 'CONFIRMED', 'Nuoc thang 2', NOW(), NOW(), 0),

    (UUID(), @a1, @svc_electric, '2026-03', 1510, 1672, 162, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Dien thang 3', NOW(), NOW(), 0),
    (UUID(), @a1, @svc_water,    '2026-03', 381, 397, 16, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Nuoc thang 3', NOW(), NOW(), 0),
    (UUID(), @a2, @svc_electric, '2026-03', 1008, 1122, 114, 0, NULL, NOW(), @u_tech2, 'CONFIRMED', 'Dien thang 3', NOW(), NOW(), 0),
    (UUID(), @a2, @svc_water,    '2026-03', 227, 237, 10, 0, NULL, NOW(), @u_tech2, 'CONFIRMED', 'Nuoc thang 3', NOW(), NOW(), 0),
    (UUID(), @a3, @svc_electric, '2026-03', 1865, 2055, 190, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Dien thang 3', NOW(), NOW(), 0),
    (UUID(), @a3, @svc_water,    '2026-03', 542, 565, 23, 0, NULL, NOW(), @u_tech1, 'CONFIRMED', 'Nuoc thang 3', NOW(), NOW(), 0),
    (UUID(), @a6, @svc_electric, '2026-03', 1068, 1158, 90, 0, NULL, NOW(), @u_tech3, 'CONFIRMED', 'Dien thang 3', NOW(), NOW(), 0),
    (UUID(), @a6, @svc_water,    '2026-03', 325, 339, 14, 0, NULL, NOW(), @u_tech3, 'CONFIRMED', 'Nuoc thang 3', NOW(), NOW(), 0);




-- ================================================
-- 1. ROLES (no FK)
-- ================================================
INSERT INTO roles (id, code, name, description, created_at, updated_at, is_deleted) VALUES
                                                                                        ('aa000001-0000-0000-0000-000000000001', 'ROLE_BUILDING_MANAGER', 'Building Manager', 'Quản lý toà nhà', NOW(), NOW(), 0),
                                                                                        ('aa000001-0000-0000-0000-000000000002', 'ROLE_RESIDENT',         'Resident',         'Cư dân',          NOW(), NOW(), 0),
                                                                                        ('aa000001-0000-0000-0000-000000000003', 'ROLE_STAFF',            'Staff',            'Nhân viên kỹ thuật', NOW(), NOW(), 0);

-- ================================================
-- 2. BUILDINGS (no FK)
-- ================================================
INSERT INTO buildings (id, name, code, address, num_floors,
                       apartments_per_floor1_br, apartments_per_floor2_br, apartments_per_floor3_br,
                       area1_br_sqm, area2_br_sqm, area3_br_sqm, apartments_generated,
                       created_at, updated_at, is_deleted) VALUES
    ('bb000001-0000-0000-0000-000000000001', 'Tòa A', 'BLOCK_A', '123 Nguyễn Văn Linh, Q7', 20,
     4, 4, 2,
     45.0, 70.0, 95.0, 1,
     NOW(), NOW(), 0);

-- ================================================
-- 3. USERS (no FK)
-- ================================================
INSERT INTO users (id, full_name, email, password, phone, status, created_at, updated_at, is_deleted) VALUES
                                                                                                          ('cc000001-0000-0000-0000-000000000001', 'Nguyễn Quản Lý',  'admin@building.vn',    '$2a$10$abcdefghijklmnopqrstuuVGZzH3p/NM3YQGRKfFcIJb9yGm6GKiG', '0901000001', 'ACTIVE', NOW(), NOW(), 0),
                                                                                                          ('cc000001-0000-0000-0000-000000000002', 'Trần Văn An',      'an.tran@gmail.com',    '$2a$10$abcdefghijklmnopqrstuuVGZzH3p/NM3YQGRKfFcIJb9yGm6GKiG', '0901000002', 'ACTIVE', NOW(), NOW(), 0),
                                                                                                          ('cc000001-0000-0000-0000-000000000003', 'Lê Thị Bình',      'binh.le@gmail.com',    '$2a$10$abcdefghijklmnopqrstuuVGZzH3p/NM3YQGRKfFcIJb9yGm6GKiG', '0901000003', 'ACTIVE', NOW(), NOW(), 0),
                                                                                                          ('cc000001-0000-0000-0000-000000000004', 'Phạm Kỹ Thuật',    'staff@building.vn',    '$2a$10$abcdefghijklmnopqrstuuVGZzH3p/NM3YQGRKfFcIJb9yGm6GKiG', '0901000004', 'ACTIVE', NOW(), NOW(), 0),
                                                                                                          ('cc000001-0000-0000-0000-000000000005', 'Hoàng Minh Cường', 'cuong.hoang@gmail.com','$2a$10$abcdefghijklmnopqrstuuVGZzH3p/NM3YQGRKfFcIJb9yGm6GKiG', '0901000005', 'ACTIVE', NOW(), NOW(), 0);

-- ================================================
-- 4. USER_ROLES (FK: users, roles, buildings)
-- ================================================
INSERT INTO user_roles (id, user_id, role_id, building_id, created_at, updated_at, is_deleted) VALUES
                                                                                                   ('dd000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000001', 'aa000001-0000-0000-0000-000000000001', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0),
                                                                                                   ('dd000001-0000-0000-0000-000000000002', 'cc000001-0000-0000-0000-000000000002', 'aa000001-0000-0000-0000-000000000002', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0),
                                                                                                   ('dd000001-0000-0000-0000-000000000003', 'cc000001-0000-0000-0000-000000000003', 'aa000001-0000-0000-0000-000000000002', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0),
                                                                                                   ('dd000001-0000-0000-0000-000000000004', 'cc000001-0000-0000-0000-000000000004', 'aa000001-0000-0000-0000-000000000003', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0),
                                                                                                   ('dd000001-0000-0000-0000-000000000005', 'cc000001-0000-0000-0000-000000000005', 'aa000001-0000-0000-0000-000000000002', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0);

-- ================================================
-- 5. APARTMENTS (FK: buildings)
-- ================================================
INSERT INTO apartments (id, building_id, code, floor_number, area_sqm, bedroom_count, status, notes, created_at, updated_at, is_deleted) VALUES
                                                                                                                                             ('60187487-1c9a-11f1-af4f-28c5d211c159', 'bb000001-0000-0000-0000-000000000001', 'A101', 1, 70.0, 2, 'OCCUPIED',  NULL, NOW(), NOW(), 0),
                                                                                                                                             ('ee000001-0000-0000-0000-000000000002', 'bb000001-0000-0000-0000-000000000001', 'A102', 1, 70.0, 2, 'OCCUPIED',  NULL, NOW(), NOW(), 0),
                                                                                                                                             ('ee000001-0000-0000-0000-000000000003', 'bb000001-0000-0000-0000-000000000001', 'A201', 2, 95.0, 3, 'OCCUPIED',  NULL, NOW(), NOW(), 0),
                                                                                                                                             ('ee000001-0000-0000-0000-000000000004', 'bb000001-0000-0000-0000-000000000001', 'A202', 2, 45.0, 1, 'AVAILABLE', NULL, NOW(), NOW(), 0);

-- ================================================
-- 6. APARTMENT_RESIDENTS (FK: apartments, users)
-- ================================================
INSERT INTO apartment_residents (id, apartment_id, user_id, resident_type, id_card_number, assigned_at, moved_out_at, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                              ('ff000001-0000-0000-0000-000000000001', '60187487-1c9a-11f1-af4f-28c5d211c159', 'cc000001-0000-0000-0000-000000000002', 'OWNER',  '079001000001', '2024-01-01 00:00:00', NULL, NOW(), NOW(), 0),
                                                                                                                                                              ('ff000001-0000-0000-0000-000000000002', 'ee000001-0000-0000-0000-000000000002', 'cc000001-0000-0000-0000-000000000003', 'OWNER',  '079001000002', '2024-01-01 00:00:00', NULL, NOW(), NOW(), 0),
                                                                                                                                                              ('ff000001-0000-0000-0000-000000000003', 'ee000001-0000-0000-0000-000000000003', 'cc000001-0000-0000-0000-000000000005', 'TENANT', '079001000003', '2024-06-01 00:00:00', NULL, NOW(), NOW(), 0);

-- ================================================
-- 7. SERVICES (no FK)
-- ================================================
INSERT INTO services (id, code, name, description, unit, is_recurring, billing_method, taxable, is_active, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                   ('gg000001-0000-0000-0000-000000000001', 'SVC_MGMT',  'Phí quản lý', 'Phí quản lý hàng tháng', 'm²',  1, 'AREA',  1, 1, NOW(), NOW(), 0),
                                                                                                                                                   ('gg000001-0000-0000-0000-000000000002', 'SVC_ELEC',  'Tiền điện',   'Chi phí điện theo chỉ số', 'kWh', 1, 'TIER',  1, 1, NOW(), NOW(), 0),
                                                                                                                                                   ('gg000001-0000-0000-0000-000000000003', 'SVC_WATER', 'Tiền nước',   'Chi phí nước theo chỉ số', 'm³',  1, 'TIER',  1, 1, NOW(), NOW(), 0),
                                                                                                                                                   ('gg000001-0000-0000-0000-000000000004', 'SVC_PARK',  'Phí gửi xe',  'Phí gửi xe cố định/tháng', 'xe',  1, 'FIXED', 0, 1, NOW(), NOW(), 0);

-- ================================================
-- 8. SERVICE_TARIFFS (FK: services)
-- ================================================
INSERT INTO service_tariffs (id, service_id, price, currency, effective_from, effective_to, vat_rate, created_at, updated_at, is_deleted) VALUES
                                                                                                                                              ('hh000001-0000-0000-0000-000000000001', 'gg000001-0000-0000-0000-000000000001', 5000.00,  'VND', '2025-01-01', NULL, 10.00, NOW(), NOW(), 0),
                                                                                                                                              ('hh000001-0000-0000-0000-000000000002', 'gg000001-0000-0000-0000-000000000002', 3500.00,  'VND', '2025-01-01', NULL, 10.00, NOW(), NOW(), 0),
                                                                                                                                              ('hh000001-0000-0000-0000-000000000003', 'gg000001-0000-0000-0000-000000000003', 15000.00, 'VND', '2025-01-01', NULL, 10.00, NOW(), NOW(), 0),
                                                                                                                                              ('hh000001-0000-0000-0000-000000000004', 'gg000001-0000-0000-0000-000000000004', 200000.00,'VND', '2025-01-01', NULL, 0.00,  NOW(), NOW(), 0);

-- ================================================
-- 9. SERVICE_TARIFF_TIERS (FK: service_tariffs)
-- Chỉ tạo tiers cho dịch vụ TIER (điện, nước)
-- ================================================
INSERT INTO service_tariff_tiers (id, tariff_id, min_val, max_val, price, created_at, updated_at, is_deleted) VALUES
-- Điện: bậc thang
('ii000001-0000-0000-0000-000000000001', 'hh000001-0000-0000-0000-000000000002',   0.00,  50.00, 1678.00, NOW(), NOW(), 0),
('ii000001-0000-0000-0000-000000000002', 'hh000001-0000-0000-0000-000000000002',  51.00, 100.00, 1734.00, NOW(), NOW(), 0),
('ii000001-0000-0000-0000-000000000003', 'hh000001-0000-0000-0000-000000000002', 101.00, 200.00, 2014.00, NOW(), NOW(), 0),
('ii000001-0000-0000-0000-000000000004', 'hh000001-0000-0000-0000-000000000002', 201.00,   NULL, 2536.00, NOW(), NOW(), 0),
-- Nước: bậc thang
('ii000001-0000-0000-0000-000000000005', 'hh000001-0000-0000-0000-000000000003',  0.00,  10.00, 5973.00, NOW(), NOW(), 0),
('ii000001-0000-0000-0000-000000000006', 'hh000001-0000-0000-0000-000000000003', 11.00,  20.00, 7052.00, NOW(), NOW(), 0),
('ii000001-0000-0000-0000-000000000007', 'hh000001-0000-0000-0000-000000000003', 21.00,   NULL, 8669.00, NOW(), NOW(), 0);

-- ================================================
-- 10. METER_READINGS (FK: apartments, services, users)
-- ================================================
INSERT INTO meter_readings (id, apartment_id, service_id, period, old_index, new_index, consumption, is_meter_reset, taken_at, taken_by, status, note, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                                                               ('jj000001-0000-0000-0000-000000000001', '60187487-1c9a-11f1-af4f-28c5d211c159', 'gg000001-0000-0000-0000-000000000002', '2026-03', 120.00, 270.00, 150.00, 0, '2026-03-28 09:00:00', 'cc000001-0000-0000-0000-000000000004', 'LOCKED', NULL, NOW(), NOW(), 0),
                                                                                                                                                                                               ('jj000001-0000-0000-0000-000000000002', '60187487-1c9a-11f1-af4f-28c5d211c159', 'gg000001-0000-0000-0000-000000000003', '2026-03',  10.00,  22.00,  12.00, 0, '2026-03-28 09:10:00', 'cc000001-0000-0000-0000-000000000004', 'LOCKED', NULL, NOW(), NOW(), 0),
                                                                                                                                                                                               ('jj000001-0000-0000-0000-000000000003', 'ee000001-0000-0000-0000-000000000002', 'gg000001-0000-0000-0000-000000000002', '2026-03', 200.00, 380.00, 180.00, 0, '2026-03-28 09:20:00', 'cc000001-0000-0000-0000-000000000004', 'LOCKED', NULL, NOW(), NOW(), 0),
                                                                                                                                                                                               ('jj000001-0000-0000-0000-000000000004', 'ee000001-0000-0000-0000-000000000002', 'gg000001-0000-0000-0000-000000000003', '2026-03',  20.00,  35.00,  15.00, 0, '2026-03-28 09:30:00', 'cc000001-0000-0000-0000-000000000004', 'LOCKED', NULL, NOW(), NOW(), 0);

-- ================================================
-- 11. MONTHLY_BILLS (FK: apartments)
-- ================================================
INSERT INTO monthly_bills (id, apartment_id, period_from, period_to, period_code, subtotal, tax_total, total_amount, status, issued_at, due_date, locked, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                                                                  ('kk000001-0000-0000-0000-000000000001', '60187487-1c9a-11f1-af4f-28c5d211c159', '2026-03-01 00:00:00', '2026-03-31 23:59:59', '2026-03', 682000.00, 63700.00,  745700.00,  'UNPAID', NOW(), '2026-04-10 23:59:59', 0, NOW(), NOW(), 0),
                                                                                                                                                                                                  ('kk000001-0000-0000-0000-000000000002', 'ee000001-0000-0000-0000-000000000002', '2026-03-01 00:00:00', '2026-03-31 23:59:59', '2026-03', 758000.00, 70800.00,  828800.00,  'UNPAID', NOW(), '2026-04-10 23:59:59', 0, NOW(), NOW(), 0),
                                                                                                                                                                                                  ('kk000001-0000-0000-0000-000000000003', 'ee000001-0000-0000-0000-000000000003', '2026-03-01 00:00:00', '2026-03-31 23:59:59', '2026-03', 950000.00, 95000.00, 1045000.00, 'PAID',   NOW(), '2026-04-10 23:59:59', 0, NOW(), NOW(), 0);

-- ================================================
-- 12. BILL_DETAILS (FK: monthly_bills)
-- ================================================
INSERT INTO bill_details (id, bill_id, description, quantity, unit_price, amount, tax_rate, total_line, created_at, updated_at, is_deleted) VALUES
-- Bill 1 - A101
('ll000001-0000-0000-0000-000000000001', 'kk000001-0000-0000-0000-000000000001', 'Phí quản lý tháng 3/2026', 70.0,  5000.00,  350000.00, 10.0,  385000.00, NOW(), NOW(), 0),
('ll000001-0000-0000-0000-000000000002', 'kk000001-0000-0000-0000-000000000001', 'Tiền điện tháng 3/2026',  150.0, 1734.00,  260100.00, 10.0,  286110.00, NOW(), NOW(), 0),
('ll000001-0000-0000-0000-000000000003', 'kk000001-0000-0000-0000-000000000001', 'Tiền nước tháng 3/2026',   12.0, 6000.00,   72000.00, 10.0,   79200.00, NOW(), NOW(), 0),
-- Bill 2 - A102
('ll000001-0000-0000-0000-000000000004', 'kk000001-0000-0000-0000-000000000002', 'Phí quản lý tháng 3/2026', 70.0,  5000.00,  350000.00, 10.0,  385000.00, NOW(), NOW(), 0),
('ll000001-0000-0000-0000-000000000005', 'kk000001-0000-0000-0000-000000000002', 'Tiền điện tháng 3/2026',  180.0, 2014.00,  362520.00, 10.0,  398772.00, NOW(), NOW(), 0),
('ll000001-0000-0000-0000-000000000006', 'kk000001-0000-0000-0000-000000000002', 'Tiền nước tháng 3/2026',   15.0, 7052.00,  105780.00, 10.0,  116358.00, NOW(), NOW(), 0),
-- Bill 3 - A201 (đã PAID)
('ll000001-0000-0000-0000-000000000007', 'kk000001-0000-0000-0000-000000000003', 'Phí quản lý tháng 3/2026', 95.0,  5000.00,  475000.00, 10.0,  522500.00, NOW(), NOW(), 0),
('ll000001-0000-0000-0000-000000000008', 'kk000001-0000-0000-0000-000000000003', 'Phí gửi xe tháng 3/2026',   1.0, 200000.00, 200000.00,  0.0,  200000.00, NOW(), NOW(), 0),
('ll000001-0000-0000-0000-000000000009', 'kk000001-0000-0000-0000-000000000003', 'Tiền nước tháng 3/2026',   18.0, 7052.00,  126936.00, 10.0,  139630.00, NOW(), NOW(), 0);

-- ================================================
-- 13. PAYMENT_TRANSACTIONS (FK: monthly_bills, users)
-- ================================================
INSERT INTO payment_transactions (id, bill_id, posted_by, amount, currency, proof_url, reference_no, status, paid_at, rejected_reason, verified_at, order_code, checkout_url, qr_code, created_at, updated_at, is_deleted) VALUES
-- Bill 3 đã SUCCESS
('mm000001-0000-0000-0000-000000000001', 'kk000001-0000-0000-0000-000000000003', NULL, 1045000.00, 'VND', NULL, NULL, 'SUCCESS', '2026-03-10 10:30:00', NULL, NULL, 1741600200000, 'https://pay.payos.vn/web/abc123', NULL, NOW(), NOW(), 0),
-- Bill 1 đang PENDING (có proof, chờ duyệt)
('mm000001-0000-0000-0000-000000000002', 'kk000001-0000-0000-0000-000000000001', NULL, 745700.00,  'VND', 'http://localhost:9000/building-management/payment-proofs/proof_a101.png', NULL, 'PENDING', NULL, NULL, NULL, 1741600300000, 'https://pay.payos.vn/web/def456', NULL, NOW(), NOW(), 0);

-- ================================================
-- 14. MAINTENANCE_REQUESTS (FK: users, apartments, buildings)
-- ================================================
INSERT INTO maintenance_requests (id, code, title, description, is_billable, preferred_time, started_at, finished_at, closed_at, scope, category, request_status, priority, payment_status, requester_id, staff_id, apartment_id, building_id, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                                                                                                                                                       ('nn000001-0000-0000-0000-000000000001', 'MR-2026-001', 'Sửa vòi nước bị rò rỉ', 'Vòi nước trong nhà vệ sinh bị rò rỉ, cần sửa gấp', 0, '2026-03-15 09:00:00', '2026-03-16 08:00:00', '2026-03-16 10:00:00', NULL, 'PRIVATE', 'REPAIR', 'COMPLETED', 'HIGH', 'PAID', 'cc000001-0000-0000-0000-000000000002', 'cc000001-0000-0000-0000-000000000004', '60187487-1c9a-11f1-af4f-28c5d211c159', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0),
                                                                                                                                                                                                                                                                                       ('nn000001-0000-0000-0000-000000000002', 'MR-2026-002', 'Thay bóng đèn hành lang', 'Bóng đèn tầng 2 hành lang bị cháy', 0, '2026-03-20 14:00:00', NULL, NULL, NULL, 'PUBLIC', 'MAINTENANCE', 'IN_PROGRESS', 'NORMAL', 'UNPAID', 'cc000001-0000-0000-0000-000000000003', 'cc000001-0000-0000-0000-000000000004', 'ee000001-0000-0000-0000-000000000002', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0),
                                                                                                                                                                                                                                                                                       ('nn000001-0000-0000-0000-000000000003', 'MR-2026-003', 'Kiểm tra điều hoà', 'Điều hoà phòng ngủ chạy yếu, cần kiểm tra gas', 1, '2026-03-25 10:00:00', NULL, NULL, NULL, 'PRIVATE', 'SERVICE', 'QUOTING', 'NORMAL', 'UNPAID', 'cc000001-0000-0000-0000-000000000005', NULL, 'ee000001-0000-0000-0000-000000000003', 'bb000001-0000-0000-0000-000000000001', NOW(), NOW(), 0);

-- ================================================
-- 15. MAINTENANCE_QUOTATIONS (FK: maintenance_requests)
-- ================================================
INSERT INTO maintenance_quotations (id, maintenance_request_id, code, title, status, description, note, total_amount, valid_until, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                                           ('oo000001-0000-0000-0000-000000000001', 'nn000001-0000-0000-0000-000000000001', 'QUO-2026-001', 'Báo giá sửa vòi nước', 'APPROVED', 'Thay ron và siết lại đầu nối', NULL, 150000.00, '2026-03-20 23:59:59', NOW(), NOW(), 0),
                                                                                                                                                                           ('oo000001-0000-0000-0000-000000000002', 'nn000001-0000-0000-0000-000000000003', 'QUO-2026-002', 'Báo giá kiểm tra điều hoà', 'SENT', 'Kiểm tra gas, vệ sinh phin lọc', 'Nếu thiếu gas sẽ phát sinh thêm chi phí', 500000.00, '2026-04-01 23:59:59', NOW(), NOW(), 0);

-- ================================================
-- 16. MAINTENANCE_ITEMS (FK: maintenance_quotations)
-- ================================================
INSERT INTO maintenance_items (id, quotation_id, name, description, item_type, quantity, unit_price, created_at, updated_at, is_deleted) VALUES
                                                                                                                                             ('pp000001-0000-0000-0000-000000000001', 'oo000001-0000-0000-0000-000000000001', 'Ron vòi nước', 'Ron cao su thay thế', 'MATERIAL', 2, 15000.00,  NOW(), NOW(), 0),
                                                                                                                                             ('pp000001-0000-0000-0000-000000000002', 'oo000001-0000-0000-0000-000000000001', 'Nhân công sửa', 'Phí kỹ thuật viên', 'LABOR',    1, 120000.00, NOW(), NOW(), 0),
                                                                                                                                             ('pp000001-0000-0000-0000-000000000003', 'oo000001-0000-0000-0000-000000000002', 'Kiểm tra gas', 'Phí kiểm tra áp suất gas', 'LABOR',    1, 200000.00, NOW(), NOW(), 0),
                                                                                                                                             ('pp000001-0000-0000-0000-000000000004', 'oo000001-0000-0000-0000-000000000002', 'Vệ sinh phin lọc', 'Phí vệ sinh', 'LABOR',    1, 150000.00, NOW(), NOW(), 0),
                                                                                                                                             ('pp000001-0000-0000-0000-000000000005', 'oo000001-0000-0000-0000-000000000002', 'Dung dịch vệ sinh', 'Hoá chất làm sạch', 'MATERIAL', 1, 150000.00, NOW(), NOW(), 0);

-- ================================================
-- 17. MAINTENANCE_SCHEDULES (FK: maintenance_requests, users)
-- ================================================
INSERT INTO maintenance_schedules (id, maintenance_request_id, proposed_by_id, parent_schedule_id, proposed_time, estimated_duration, note, status, proposed_by_role, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                                                                              ('qq000001-0000-0000-0000-000000000001', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000004', NULL, '2026-03-16 08:00:00', 120, 'Đến sửa buổi sáng', 'CONFIRMED', 'STAFF', NOW(), NOW(), 0),
                                                                                                                                                                                                              ('qq000001-0000-0000-0000-000000000002', 'nn000001-0000-0000-0000-000000000002', 'cc000001-0000-0000-0000-000000000004', NULL, '2026-03-20 14:00:00', 60,  'Thay bóng buổi chiều', 'PROPOSED', 'STAFF', NOW(), NOW(), 0);

-- ================================================
-- 18. MAINTENANCE_PROGRESSES (FK: maintenance_requests, users)
-- ================================================
INSERT INTO maintenance_progresses (id, maintenance_request_id, updated_by_id, note, progress_percent, created_at, updated_at, is_deleted) VALUES
                                                                                                                                               ('rr000001-0000-0000-0000-000000000001', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000004', 'Đã đến kiểm tra, chuẩn bị dụng cụ', 30,  NOW(), NOW(), 0),
                                                                                                                                               ('rr000001-0000-0000-0000-000000000002', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000004', 'Đã thay ron và siết đầu nối xong',   100, NOW(), NOW(), 0),
                                                                                                                                               ('rr000001-0000-0000-0000-000000000003', 'nn000001-0000-0000-0000-000000000002', 'cc000001-0000-0000-0000-000000000004', 'Đang trên đường đến',                 10,  NOW(), NOW(), 0);

-- ================================================
-- 19. MAINTENANCE_REVIEWS (FK: maintenance_requests, users)
-- OneToOne với maintenance_request
-- ================================================
INSERT INTO maintenance_reviews (id, maintenance_request_id, reviewed_by_id, rating, comment, outcome, created_at, updated_at, is_deleted) VALUES
    ('ss000001-0000-0000-0000-000000000001', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000002', 5, 'Nhân viên nhiệt tình, sửa nhanh và sạch sẽ', 'ACCEPTED', NOW(), NOW(), 0);

-- ================================================
-- 20. MAINTENANCE_RESOURCES (FK: maintenance_requests, users, items)
-- ================================================
INSERT INTO maintenance_resources (id, maintenance_request_id, user_id, item_id, name, url, resource_type, created_at, updated_at, is_deleted) VALUES
                                                                                                                                                   ('tt000001-0000-0000-0000-000000000001', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000002', NULL, 'Ảnh vòi nước bị rò', 'http://localhost:9000/building-management/maintenance/mr001_before.jpg', 'IMAGE', NOW(), NOW(), 0),
                                                                                                                                                   ('tt000001-0000-0000-0000-000000000002', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000004', NULL, 'Ảnh sau khi sửa xong', 'http://localhost:9000/building-management/maintenance/mr001_after.jpg',  'IMAGE', NOW(), NOW(), 0);

-- ================================================
-- 21. MAINTENANCE_LOGS (no FK - stores UUID as VARCHAR)
-- ================================================
INSERT INTO maintenance_logs (id, request_id, actor_id, action, note, created_at, updated_at, is_deleted) VALUES
                                                                                                              ('uu000001-0000-0000-0000-000000000001', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000002', 'CREATE',   'Tạo yêu cầu bảo trì',       NOW(), NOW(), 0),
                                                                                                              ('uu000001-0000-0000-0000-000000000002', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000001', 'ASSIGN',   'Phân công cho kỹ thuật viên', NOW(), NOW(), 0),
                                                                                                              ('uu000001-0000-0000-0000-000000000003', 'nn000001-0000-0000-0000-000000000001', 'cc000001-0000-0000-0000-000000000004', 'COMPLETE', 'Hoàn thành sửa chữa',        NOW(), NOW(), 0),
                                                                                                              ('uu000001-0000-0000-0000-000000000004', 'nn000001-0000-0000-0000-000000000002', 'cc000001-0000-0000-0000-000000000003', 'CREATE',   'Tạo yêu cầu bảo trì',       NOW(), NOW(), 0),
                                                                                                              ('uu000001-0000-0000-0000-000000000005', 'nn000001-0000-0000-0000-000000000003', 'cc000001-0000-0000-0000-000000000005', 'CREATE',   'Tạo yêu cầu bảo trì',       NOW(), NOW(), 0);
