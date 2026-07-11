package com.ironcore.domain.equipmenttype.exception;

import com.ironcore.domain.exception.DomainException;

public class InvalidEquipmentTypeException extends DomainException {

    public InvalidEquipmentTypeException(String message) {
        super(message);
    }
}
