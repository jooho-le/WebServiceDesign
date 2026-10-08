package com.example.wsdteaching.note;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.CodePointLength;

public record NoteRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @CodePointLength(max = 100, message = "제목은 100자 이하여야 합니다.") String title,
        @NotBlank(message = "내용은 필수입니다.")
        @CodePointLength(max = 10000, message = "내용은 10000자 이하여야 합니다.") String content) {

    public NoteRequest {
        title = title == null ? null : title.strip();
        content = content == null ? null : content.strip();
    }
}
