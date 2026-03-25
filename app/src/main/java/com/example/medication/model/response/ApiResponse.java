package com.example.medication.model.response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("status")
    private int status;

    @SerializedName("code")
    private String code;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("timestamp")
    private String timestamp; // Gson 파싱을 위해 String 사용 (ISO-8601 포맷)

    // Getter 메서드
    public boolean isSuccess() {
        return success;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // 비즈니스 로직 성공 여부 헬퍼 메서드
    public boolean isBusinessSuccess() {
        return success && "SUCCESS".equals(code);
    }
}
