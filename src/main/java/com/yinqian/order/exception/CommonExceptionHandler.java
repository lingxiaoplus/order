package com.yinqian.order.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author admin
 */
@ControllerAdvice
@Slf4j
public class CommonExceptionHandler {


    /*@Autowired
    private OperationLogService logService;
    @Autowired
    private HttpServletRequest request;*/

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<ExceptionResult> handleException(OrderException e) {
        ExceptionEnum exceptionEnum = e.getExceptionEnum();
        if (exceptionEnum == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ExceptionResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage()));
        }
        return ResponseEntity.status(exceptionEnum.getCode()).body(new ExceptionResult(exceptionEnum));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResult> handleValidException(MethodArgumentNotValidException e) {
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        StringBuilder stringBuilder = new StringBuilder();
        allErrors.forEach(error -> stringBuilder.append(error.getDefaultMessage()).append(", "));
        ExceptionResult exceptionResult = new ExceptionResult(ExceptionEnum.ILLEGA_ARGUMENT.getCode(),
                stringBuilder.toString());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResult);
    }

    /**
     * 处理所有不可知异常
     *
     * @param throwable
     * @return json
     */
    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<ExceptionResult> handlerException(Throwable throwable) {
        ExceptionResult apiResult = new ExceptionResult(HttpStatus.INTERNAL_SERVER_ERROR.value(), throwable.getMessage());
        throwable.printStackTrace();
        //insertOperationLog(throwable);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResult);
    }

    /*private void insertOperationLog(Throwable throwable) {
        OperationLog operationLog = new OperationLog();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            User user = (User) authentication.getPrincipal();
            operationLog.setUsername(user.getUsername());
            operationLog.setNickname(user.getNickname());
        }
        operationLog.setOperationType(OperationType.EXCEPTION.getCode());
        operationLog.setOperationContent(throwable.getClass().getSimpleName());
        operationLog.setUserIp(IPUtils.ipToNum(IPUtils.getIpAddress(request)));
        operationLog.setCreateAt(new Date());
        operationLog.setBrowser(IPUtils.getBrowserName(request));
        try (Writer writer = new StringWriter();
             PrintWriter printWriter = new PrintWriter(writer)) {
            throwable.printStackTrace(printWriter);
            Throwable cause = throwable.getCause();
            while (cause != null) {
                cause.printStackTrace(printWriter);
                cause = cause.getCause();
            }
            String result = writer.toString();
            operationLog.setExceptionInfo(result);
            logService.setOperationLog(operationLog);
        } catch (IOException e) {
            log.error("插入异常日志失败：{}", e.getMessage());
        }
    }*/
}
