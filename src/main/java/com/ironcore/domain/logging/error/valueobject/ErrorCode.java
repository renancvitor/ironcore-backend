package com.ironcore.domain.logging.error.valueobject;

import com.ironcore.domain.logging.error.enums.ErrorCodeType;
import com.ironcore.domain.logging.error.exception.InvalidErrorLogException;

public record ErrorCode(ErrorCodeType type) {

    public ErrorCode {
        if (type == null) {
            throw new InvalidErrorLogException("Código de erro não pode ser nulo");
        }
    }
}
