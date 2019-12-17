package com.parcom.exceptions.handler;

import com.parcom.exceptions.AccessDeniedParcomException;
import com.parcom.exceptions.ForbiddenParcomException;
import com.parcom.exceptions.NotFoundParcomException;
import com.parcom.exceptions.ParcomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.AccessDeniedException;
import java.util.List;
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

    @ExceptionHandler(value = BindException.class)
    public ResponseEntity<Object> handleBindException(HttpServletRequest request, BindException ex)
    {
        String message = getMessageFromErrorList(ex.getBindingResult());
        return new ResponseEntity<>(getExceptionResource(request, ex, message),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleBindException(HttpServletRequest request,MethodArgumentNotValidException ex)
    {
        String message = getMessageFromErrorList(ex.getBindingResult());
        return new ResponseEntity<>(getExceptionResource(request, ex, message),HttpStatus.BAD_REQUEST);
    }


    private String getMessageFromErrorList(BindingResult bindingResult) {
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        StringBuilder mess = new StringBuilder();
        for (ObjectError error : allErrors) {
            mess.append(messageSource.getMessage(error, LocaleContextHolder.getLocale())).append("; ");
        }
        return mess.toString();
    }




    @ExceptionHandler({
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            MissingServletRequestPartException.class,
            NoHandlerFoundException.class,
            AsyncRequestTimeoutException.class
    })
    @Nullable
    public final ResponseEntity<Object> handleExcluded(Exception ex, WebRequest request) throws Exception {
        throw ex;
    }

        @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ExceptionResource> handleException(HttpServletRequest request, Exception ex) {
        return new ResponseEntity<>(getExceptionResource(request, ex),HttpStatus.INTERNAL_SERVER_ERROR);
    }







}