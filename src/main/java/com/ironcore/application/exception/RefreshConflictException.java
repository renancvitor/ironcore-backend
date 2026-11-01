package com.ironcore.application.exception;

public class RefreshConflictException extends ApplicationException {
    public RefreshConflictException() {
        super("Renovação já realizada. Aguarde os cookies da requisição concorrente antes de tentar novamente.");
    }
}
