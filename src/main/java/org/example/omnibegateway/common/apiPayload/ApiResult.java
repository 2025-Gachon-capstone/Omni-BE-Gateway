package org.example.omnibegateway.common.apiPayload;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.omnibegateway.common.apiPayload.code.BaseCode;
import org.example.omnibegateway.common.apiPayload.code.status.SuccessStatus;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess","code","message","result"})
public class ApiResult<T> {
    @JsonProperty("isSuccess")
    private final Boolean isSuccess;

    private final String code;
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T result;

    /*
    200번대 성공한 응답
     */

    // 단순 상태 응답
    public static <Void> ApiResult<Void> onSuccess(){
        return new ApiResult<>(true, SuccessStatus._OK.getCode(),SuccessStatus._OK.getMessage(),null);
    }

    // 단순 상태 응답 + 결과값
    public static <T> ApiResult<T> onSuccess(T result){
        return new ApiResult<>(true,SuccessStatus._OK.getCode(),SuccessStatus._OK.getMessage(),result);
    }


    // 커스텀 상태 응답(status에 정의)
    public static <T> ApiResult<T> onSuccess(BaseCode code){
        return new ApiResult<>(true,code.getReasonHttpStatus().getCode(),code.getReasonHttpStatus().getMessage(),null);
    }

    // 커스텀 상태 응답(status에 정의) + 결과값
    public static <T> ApiResult<T> onSuccess(BaseCode code, T result){
        return new ApiResult<>(true,code.getReasonHttpStatus().getCode(),code.getReasonHttpStatus().getMessage(),result);
    }

    /*
    실패한 응답
    ExceptionAdvice 응답
    커스텀 에러 응답
     */

    // 단순 실패 응답
    public static <T> ApiResult<T> onFailure(String code,String message,T data){
        return new ApiResult<>(false,code,message,data);
    }

    // http 상태 실패 응답
    public static <T> ApiResult<T> onFailure(BaseCode code,T result){
        return new ApiResult<>(false,code.getReasonHttpStatus().getCode(),code.getReasonHttpStatus().getMessage(),result);
    }



}
