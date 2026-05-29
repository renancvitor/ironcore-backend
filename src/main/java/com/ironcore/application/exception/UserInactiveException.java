package com.ironcore.application.exception;

public class UserInactiveException extends OperationNotAllowedException {

    public UserInactiveException(String message) {
        super(message);
    }
}
