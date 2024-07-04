package com.newy.algotrade.web_flux.common.error

import com.newy.algotrade.domain.common.exception.DuplicateDataException
import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleException(exception: ConstraintViolationException) =
        HttpStatus.BAD_REQUEST.let { httpStatus ->
            ResponseEntity
                .status(httpStatus)
                .body(ErrorResponse(
                    message = httpStatus.reasonPhrase,
                    errors = exception.constraintViolations.map {
                        FieldError(
                            field = it.propertyPath.toString(),
                            value = it.invalidValue.toString(),
                            reason = it.message
                        )
                    }
                ))
        }

    @ExceptionHandler(DuplicateDataException::class)
    fun handleException(exception: DuplicateDataException) =
        HttpStatus.BAD_REQUEST.let { httpStatus ->
            ResponseEntity
                .status(httpStatus)
                .body(
                    ErrorResponse(
                        message = exception.message ?: httpStatus.reasonPhrase,
                    )
                )
        }
}