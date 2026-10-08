package com.example.wsdteaching.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.CodePointLength;

public record TaskRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @CodePointLength(max = 100, message = "제목은 100자 이하여야 합니다.") String title,
        @NotNull(message = "완료 여부는 필수입니다.") Boolean completed) {

    public TaskRequest {
        title = title == null ? null : title.strip();
    }
}
