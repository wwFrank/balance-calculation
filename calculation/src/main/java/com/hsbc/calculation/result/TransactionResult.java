package com.hsbc.calculation.result;

import lombok.Getter;
import lombok.Setter;

public class TransactionResult<T> {
    @Getter
    @Setter
    private boolean success;
    @Getter
    @Setter
    private int code;
    @Getter
    @Setter
    private String message;
    @Getter
    @Setter
    private T data;

    public TransactionResult() {}

    public TransactionResult(int code, String message, T data, boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }

    public static <T> TransactionResult<T> success(T data) {
        return new TransactionResult<>(200, "操作成功", data, true);
    }

    public static <T> TransactionResult<T> error(String message) {
        return new TransactionResult<>(500, message, null, false);
    }

    public static <T> TransactionResult<T> error(int code, String message) {
        return new TransactionResult<>(code, message, null, false);
    }

    @Override
    public String toString() {
        return "Result{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ",success=" + success +
                '}';
    }
}
