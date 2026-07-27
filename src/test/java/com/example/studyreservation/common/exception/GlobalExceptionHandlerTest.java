package com.example.studyreservation.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    static Stream<Arguments> conflictExceptions() {
        return Stream.of(
                Arguments.of(new DuplicateEmailException("dup@example.com")),
                Arguments.of(new DuplicateSeatNumberException("A1")),
                Arguments.of(new SeatAlreadyReservedException()),
                Arguments.of(new UserAlreadyReservedException()),
                Arguments.of(new DailyReservationLimitExceededException(8))
        );
    }

    @ParameterizedTest
    @MethodSource("conflictExceptions")
    void 충돌_예외는_409로_매핑된다(RuntimeException exception) {
        assertMapping(handler.handleConflict(exception), HttpStatus.CONFLICT, exception.getMessage());
    }

    static Stream<Arguments> notFoundExceptions() {
        return Stream.of(
                Arguments.of(new RoomNotFoundException()),
                Arguments.of(new SeatNotFoundException()),
                Arguments.of(new ReservationNotFoundException())
        );
    }

    @ParameterizedTest
    @MethodSource("notFoundExceptions")
    void 찾을_수_없음_예외는_404로_매핑된다(RuntimeException exception) {
        assertMapping(handler.handleNotFound(exception), HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @Test
    void 권한없음_예외는_403으로_매핑된다() {
        ReservationAccessDeniedException exception = new ReservationAccessDeniedException();
        assertMapping(handler.handleForbidden(exception), HttpStatus.FORBIDDEN, exception.getMessage());
    }

    @Test
    void 잘못된_시간_예외는_400으로_매핑된다() {
        InvalidReservationTimeException exception = new InvalidReservationTimeException("경계값 오류");
        assertMapping(handler.handleBadRequest(exception), HttpStatus.BAD_REQUEST, "경계값 오류");
    }

    @Test
    void DB_제약_위반은_409로_매핑되고_내부_상세정보는_노출하지_않는다() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("내부 SQL 상세 정보");

        ModelAndView mav = handler.handleDataIntegrityViolation(exception);

        assertThat(mav.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(mav.getModel()).doesNotContainValue("내부 SQL 상세 정보");
    }

    @Test
    void 예상하지_못한_예외는_500으로_매핑되고_내부_메시지는_노출하지_않는다() {
        ModelAndView mav = handler.handleUnexpected(new IllegalStateException("스택트레이스에만 있어야 할 정보"));

        assertThat(mav.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(mav.getModel()).doesNotContainValue("스택트레이스에만 있어야 할 정보");
    }

    private void assertMapping(ModelAndView mav, HttpStatus expectedStatus, String expectedMessage) {
        assertThat(mav.getViewName()).isEqualTo("error");
        assertThat(mav.getStatus()).isEqualTo(expectedStatus);
        assertThat(mav.getModel()).containsEntry("status", expectedStatus.value());
        assertThat(mav.getModel()).containsEntry("message", expectedMessage);
    }
}
