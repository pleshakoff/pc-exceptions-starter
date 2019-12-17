package com.parcom.exceptions.handler;

import com.parcom.exceptions.AccessDeniedParcomException;
import com.parcom.exceptions.ForbiddenParcomException;
import com.parcom.exceptions.NotFoundParcomException;
import com.parcom.exceptions.ParcomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.Optional;


@ControllerAdvice
public class GlobalDefaultExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);
    private final MessageSource messageSource;
    public GlobalDefaultExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }


    public static ExceptionResource getExceptionResource(HttpServletRequest request, Exception ex, String message) {
        String requestURI = Optional.ofNullable(request).map(HttpServletRequest::getRequestURI).orElse("");
        String method = Optional.ofNullable(request).map(HttpServletRequest::getMethod).orElse("");
        log.error("Method: \"{}\"; URI: \"%{}\" ", method, requestURI);
        log.error(ex.getMessage(), ex);
        ExceptionResource result = new ExceptionResource();
        result.setUrl(requestURI);
        result.setExceptionClass(ex.getClass().getName());
        result.setMethod(method);
        result.setMessage(message);
        return result;
    }

    public ExceptionResource getExceptionResourceLocalized(HttpServletRequest request, Exception ex) {
        return getExceptionResource(request,ex,localize(ex));
    }

    public ExceptionResource getExceptionResource(HttpServletRequest request, Exception ex) {
        return getExceptionResource(request,ex,ex.getMessage());
    }

    private String localize(Exception e){
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), LocaleContextHolder.getLocale());
    };

    @ExceptionHandler(value = NotFoundParcomException.class)
    public ResponseEntity<ExceptionResource> NotFoundParcomException(HttpServletRequest request, NotFoundParcomException ex) {
             return new ResponseEntity<>(getExceptionResourceLocalized(request, ex),HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(value = {AccessDeniedException.class, AccessDeniedParcomException.class})
    public ResponseEntity<ExceptionResource> handleAccessDeniedException(HttpServletRequest request, ParcomException ex) {
        return new ResponseEntity<>(getExceptionResourceLocalized(request, ex),HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(value = {ForbiddenParcomException.class})
    public ResponseEntity<ExceptionResource> handleForbiddenParcomException(HttpServletRequest request, ParcomException ex) {
        return new ResponseEntity<>(getExceptionResourceLocalized(request, ex),HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(value = ParcomException.class)
    public ResponseEntity<ExceptionResource> handleParcomException(HttpServletRequest request, ParcomException ex) {
        return new ResponseEntity<>(getExceptionResourceLocalized(request, ex),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResource> handleException(HttpServletRequest request, Exception ex) {
        return new ResponseEntity<>(getExceptionResource(request, ex),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        StringBuilder mess = new StringBuilder();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            mess.append(messageSource.getMessage(error, LocaleContextHolder.getLocale())).append("; ");
        }
        return new ResponseEntity<>(getExceptionResource(null, ex, mess.toString()),HttpStatus.BAD_REQUEST);
    }





}