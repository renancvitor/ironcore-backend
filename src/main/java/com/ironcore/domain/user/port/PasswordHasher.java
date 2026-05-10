package com.ironcore.domain.user.port;

import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.RawPassword;

public interface PasswordHasher {

    PasswordHash hash(RawPassword rawPassword);

    boolean matches(RawPassword rawPassword, PasswordHash passwordHash);
}
