package com.jmix.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidReportRequestException.class)
    ProblemDetail invalidReportRequest(InvalidReportRequestException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid report request", exception.getMessage(),
                "urn:problem:invalid-report-request");
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    ProblemDetail invalidRequestParameters(Exception exception) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid report request", "The report parameters are invalid.",
                "urn:problem:invalid-report-request");
    }

    @ExceptionHandler(ReportGenerationException.class)
    ProblemDetail reportGenerationFailure() {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Report generation failed",
                "The report could not be generated.", "urn:problem:report-generation-failed");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, String type) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create(type));
        return problem;
    }
}
