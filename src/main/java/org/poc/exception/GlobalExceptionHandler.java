package org.poc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler
{

    @ResponseBody
    @ExceptionHandler({ConstraintsViolationException.class, CarAlreadyInUseException.class})
    protected ResponseEntity<String> handleConflict(RuntimeException exception)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }


    @ExceptionHandler({EntityNotFoundException.class})
    protected ResponseEntity<String> handleNotFound(RuntimeException exception)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }


    @ExceptionHandler({AssignedDriverNotOnlineException.class})
    protected ResponseEntity<String> handleAssignedDriverNotOnline(RuntimeException exception)
    {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> globalExceptionHandler(Exception ex, WebRequest request) throws Exception
    {
        return handleException(ex, request);
    }
}
