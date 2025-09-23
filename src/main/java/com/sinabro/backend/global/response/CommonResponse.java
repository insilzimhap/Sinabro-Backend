package com.sinabro.backend.global.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "모든 응답에 공통으로 사용하는 응답 포맷")
public class CommonResponse<T> {

    @Schema(description = "응답 코드", example = "0")
    private int code;

    @Schema(description = "응답 메시지", example = "성공 또는 실패 메시지")
    private String message;

    @Schema(description = "응답 데이터", nullable = true)
    private T data;

    /**
     * ✅ 성공 응답을 생성할 때 사용
     * - T 타입에 원하는 응답 DTO나 List 등을 넣을 수 있음
     * - 예시: 회원가입 성공 시 UserRegisterDto 반환
     *
     * <pre>
     * return CommonResponse.success(userRegisterDto);
     * </pre>
     *
     * 또는 예시: 학습 목록 반환 시
     * <pre>
     * return CommonResponse.success(learningListDto);
     * </pre>
     */
    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(0, "성공", data);
    }

    /**
     * ✅ 실패 응답을 생성할 때 사용
     * - 실패 메시지만 전달하고 데이터는 null
     * - 예시: 중복된 회원가입 시
     *
     * <pre>
     * return CommonResponse.fail("이미 가입된 사용자입니다.");
     * </pre>
     *
     * 또는 예시: 로그인 실패 시
     * <pre>
     * return CommonResponse.fail("아이디 또는 비밀번호가 일치하지 않습니다.");
     * </pre>
     */
    public static <T> CommonResponse<T> fail(String message) {
        return new CommonResponse<>(-1, message, null);
    }
}
