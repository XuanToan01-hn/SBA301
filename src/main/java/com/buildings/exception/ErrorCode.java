package com.buildings.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Key không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Người dùng đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Tên đăng nhập phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1008, "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    RESOURCE_NOT_FOUND(1009, "Không tìm thấy dữ liệu", HttpStatus.NOT_FOUND),
    INVALID_UUID(1010, "UUID không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_PASSWORD(1010, "Email hoặc mật khẩu không đúng", HttpStatus.BAD_REQUEST),
    BUILDING_CODE_ARE_EXIST(1011, "Mã tòa nhà đã tồn tại", HttpStatus.NOT_FOUND),
    BUILDING_NAME_ARE_EXIST(1011, "Tên tòa nhà đã tồn tại", HttpStatus.NOT_FOUND),
    BUILDING_NOT_FOUND(1012, "Không tìm thấy tòa nhà", HttpStatus.NOT_FOUND),
    CANNOT_MODIFY_LAYOUT(1013, "Không thể thay đổi cấu trúc", HttpStatus.BAD_REQUEST),
    BUILD_HAS_APARTMENT(1014, "Tòa nhà đã có căn hộ", HttpStatus.BAD_REQUEST),
    APARTMENT_ALREADY_GENERATED(1015, "Căn hộ đã được tạo trước đó", HttpStatus.BAD_REQUEST),
    APARTMENT_NOT_FOUND(1016, "Không tìm thấy căn hộ", HttpStatus.NOT_FOUND),
    ROLE_NOT_EXISTED(1017, "Vai trò không tồn tại", HttpStatus.NOT_FOUND),
    APARTMENT_FULL(1018, "Căn hộ đã đầy. Số người tối đa: {max}", HttpStatus.BAD_REQUEST),
    RESIDENT_NOT_FOUND(1019, "Không tìm thấy cư dân", HttpStatus.NOT_FOUND),
    DEACTIVE(1020, "Tài khoản đã bị vô hiệu hóa", HttpStatus.BAD_REQUEST),
    INVALID_AREA_SIZE(1021,"Diện tích không hợp lệ", HttpStatus.BAD_REQUEST),
    AREA_ORDER_INVALID(1022, "Thứ tự diện tích không hợp lệ", HttpStatus.BAD_REQUEST),
    Name_b(1023,"Tên tòa nhà không được để trống",HttpStatus.BAD_REQUEST),
    SERVICE_NOT_FOUND(2001, "Không tìm thấy dịch vụ", HttpStatus.NOT_FOUND),
    SERVICE_CODE_EXISTED(2002, "Mã dịch vụ đã tồn tại", HttpStatus.BAD_REQUEST),
    TARIFF_NOT_FOUND(2003, "Không tìm thấy bảng giá", HttpStatus.NOT_FOUND),
    TARIFF_OVERLAPPING(2004, "Khoảng thời gian bảng giá bị trùng", HttpStatus.BAD_REQUEST),
    SERVICE_HAS_ACTIVE_READINGS(2005, "Không thể vô hiệu hóa dịch vụ vì đang có chỉ số sử dụng",
            HttpStatus.BAD_REQUEST),
    SERVICE_ALREADY_INACTIVE(2006, "Dịch vụ đã bị vô hiệu hóa", HttpStatus.BAD_REQUEST),
    SERVICE_ALREADY_ACTIVE(2007, "Dịch vụ đã đang hoạt động", HttpStatus.BAD_REQUEST),
    SERVICE_NAME_INVALID(2008, "Tên dịch vụ không được chứa ký tự đặc biệt.", HttpStatus.BAD_REQUEST),
    SERVICE_UNIT_INVALID(2009, "Đơn vị dịch vụ không được chứa các ký tự đặc biệt.", HttpStatus.BAD_REQUEST),

    // Meter Reading errors (3xxx)
    METER_READING_NOT_FOUND(3001, "Không tìm thấy chỉ số công tơ", HttpStatus.NOT_FOUND),
    METER_READING_LOCKED(3002, "Chỉ số công tơ đã bị khóa, không thể chỉnh sửa", HttpStatus.BAD_REQUEST),
    INVALID_METER_INDEX(3003, "Chỉ số mới phải lớn hơn hoặc bằng chỉ số cũ", HttpStatus.BAD_REQUEST),
    DUPLICATE_METER_READING(3004, "Chỉ số đã tồn tại cho căn hộ, dịch vụ và kỳ",
            HttpStatus.BAD_REQUEST),

    // Payment errors (4xxx)
    BILL_NOT_FOUND(4001, "Không tìm thấy hóa đơn", HttpStatus.NOT_FOUND),
    PAYMENT_TRANSACTION_NOT_FOUND(4002, "Không tìm thấy giao dịch thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_CREATION_FAILED(4003, "Tạo link thanh toán thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_SYNC_FAILED(4004, "Đồng bộ trạng thái thanh toán thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_ALREADY_PROCESSED(4005, "Giao dịch đã được xử lý", HttpStatus.BAD_REQUEST),
    PROOF_UPLOAD_NOT_AUTHORIZED(4006, "Chỉ cư dân của căn hộ mới được tải minh chứng", HttpStatus.FORBIDDEN),
    INVALID_TRANSACTION_FOR_PROOF(4007, "Chỉ có thể tải minh chứng khi giao dịch đang chờ", HttpStatus.BAD_REQUEST),
    TRANSACTION_NOT_AWAITING_PROOF(4008, "Giao dịch chưa có minh chứng", HttpStatus.BAD_REQUEST),

    // File errors (5xxx)
    FILE_UPLOAD_FAILED(5001, "Tải file thất bại", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND(5002, "Không tìm thấy file", HttpStatus.NOT_FOUND),
    FILE_TYPE_NOT_ALLOWED(5003, "Loại file không được phép", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(5004, "Kích thước file vượt quá giới hạn", HttpStatus.BAD_REQUEST),

    ;

    private final int code;
    private final String message;
    private final HttpStatus statusCode;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}