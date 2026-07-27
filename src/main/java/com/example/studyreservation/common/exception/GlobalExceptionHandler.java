package com.example.studyreservation.common.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateSeatNumberException.class,
            SeatAlreadyReservedException.class,
            UserAlreadyReservedException.class,
            DailyReservationLimitExceededException.class
    })
    public ModelAndView handleConflict(RuntimeException e) {
        return errorView(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler({
            RoomNotFoundException.class,
            SeatNotFoundException.class,
            ReservationNotFoundException.class
    })
    public ModelAndView handleNotFound(RuntimeException e) {
        return errorView(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ReservationAccessDeniedException.class)
    public ModelAndView handleForbidden(ReservationAccessDeniedException e) {
        return errorView(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(InvalidReservationTimeException.class)
    public ModelAndView handleBadRequest(InvalidReservationTimeException e) {
        return errorView(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ModelAndView handleDataIntegrityViolation(DataIntegrityViolationException e) {
        return errorView(HttpStatus.CONFLICT, "요청이 다른 예약과 충돌했습니다. 다시 시도해 주세요.");
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleUnexpected(Exception e) {
        return errorView(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    private ModelAndView errorView(HttpStatus status, String message) {
        ModelAndView mav = new ModelAndView("error", status);
        mav.addObject("status", status.value());
        mav.addObject("message", message);
        return mav;
    }
}
