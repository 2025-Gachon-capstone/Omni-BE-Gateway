package org.example.omnibegateway.common.apiPayload.code.status;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.omnibegateway.common.apiPayload.code.BaseCode;
import org.example.omnibegateway.common.apiPayload.code.ReasonDto;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {

    _OK(HttpStatus.OK,"COMMON200","성공입니다."),
    _CREATED(HttpStatus.OK,"COMMON201","생성 성공입니다"),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ReasonDto getReason() {
        return ReasonDto.builder()
                .isSuccess(true)
                .code(code)
                .message(message)
                .build();
    }

    @Override
    public ReasonDto getReasonHttpStatus() {
        return ReasonDto.builder()
                .isSuccess(true)
                .code(code)
                .message(message)
                .httpStatus(httpStatus)
                .build();
    }
}
