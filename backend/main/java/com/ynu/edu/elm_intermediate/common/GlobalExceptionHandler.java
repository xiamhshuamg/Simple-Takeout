package com.ynu.edu.elm_intermediate.common;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<Result<Void>> handleApp(AppException e) {
        return ResponseEntity
                .status(e.getHttpStatus())
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    // 2. 处理 @Valid 参数校验失败异常（如 @NotNull、@Min 等）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().isEmpty()
                ? "参数错误"
                : e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Result.fail(40002, msg));
    }
    // 3. 处理 @RequestParam/@PathVariable 参数校验失败异常
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Result<Void>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(Result.fail(40002, e.getMessage()));
    }

    // 4. 兜底：处理所有未预期的异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleAny(Exception e) {
        e.printStackTrace();
        return ResponseEntity.internalServerError().body(Result.fail(50000, "服务器开小差了（500）"));
    }
}
