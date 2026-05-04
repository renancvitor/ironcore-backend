package com.ironcore.domain.logging.error.valueobject;

import com.ironcore.domain.logging.error.enums.ErrorCodeType;

import java.util.Objects;

public record ErrorCode(ErrorCodeType type) {

    public ErrorCode {
        Objects.requireNonNull(type, "Código de erro não pode ser nulo");
    }
}
