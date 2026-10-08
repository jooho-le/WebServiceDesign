package com.example.wsdteaching.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class FailureSimulator {

    private final boolean enabled;

    public FailureSimulator(@Value("${practice.failure-simulation-enabled:false}") boolean enabled) {
        this.enabled = enabled;
    }

    public void check(String simulate) {
        if ("none".equals(simulate)) {
            return;
        }
        if (!"500".equals(simulate) && !"503".equals(simulate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "simulate는 none, 500, 503 중 하나여야 합니다.");
        }
        if (!enabled) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "실습용 오류 재현 기능이 비활성화되어 있습니다.");
        }
        if ("503".equals(simulate)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "실습용 서비스 점검 상황입니다. 잠시 후 다시 시도해주세요.");
        }
        // Deliberately trigger the global unexpected-exception handler for the 500 example.
        throw new IllegalStateException("실습용 500 오류 재현");
    }
}
