package com.ironcore.domain.logging.error.valueobject;

import com.ironcore.domain.logging.error.enums.ErrorCodeType;

public record ErrorCode(ErrorCodeType type) {

    public ErrorCode {
        if (type == null) {
            throw new IllegalArgumentException("Código de erro não pode ser nulo");
        }
    }
}
