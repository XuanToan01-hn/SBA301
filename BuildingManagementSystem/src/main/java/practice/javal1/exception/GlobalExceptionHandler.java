package practice.javal1.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import practice.javal1.dto.ApiResponse;
import practice.javal1.exception.auth.InvalidTokenException;
import practice.javal1.exception.auth.UnAuthorException;
import practice.javal1.exception.user.UserNotFound;
import practice.javal1.exception.user.UsernameExist;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    ResponseEntity<ApiResponse> handlingRuntimeException(RuntimeException exception) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(value = AppException.class)
    ResponseEntity<ApiResponse> handlingAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = UserNotFound.class)
    ResponseEntity<ApiResponse> handlingUserNotFoundException(UserNotFound exception) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.USER_NOT_EXISTED.getCode());
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.status(ErrorCode.USER_NOT_EXISTED.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = UsernameExist.class)
    ResponseEntity<ApiResponse> handlingUsernameExistException(UsernameExist exception) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.USER_EXISTED.getCode());
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.status(ErrorCode.USER_EXISTED.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = UnAuthorException.class)
    ResponseEntity<ApiResponse> handlingUnAuthorException(UnAuthorException exception) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.UNAUTHENTICATED.getCode());
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.status(ErrorCode.UNAUTHENTICATED.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = InvalidTokenException.class)
    ResponseEntity<ApiResponse> handlingInvalidTokenException(InvalidTokenException exception) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(ErrorCode.INVALID_TOKEN.getCode());
        apiResponse.setMessage(exception.getMessage());
        return ResponseEntity.status(ErrorCode.INVALID_TOKEN.getStatusCode()).body(apiResponse);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse> handlingValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            // Keep default INVALID_KEY
        }

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        return ResponseEntity.badRequest().body(apiResponse);
    }


}