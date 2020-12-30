package com.yinqian.order.exception;

import lombok.Getter;
@Getter
public class OrderException extends RuntimeException{
    private ExceptionEnum exceptionEnum;
    private String message;

    public OrderException(ExceptionEnum exceptionEnum) {
        super(exceptionEnum.getMsg());
        this.exceptionEnum = exceptionEnum;
    }

    public OrderException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String toString() {
        return "BlogException{" +
                "exceptionEnum=" + exceptionEnum.getMsg() +
                '}';
    }
}
