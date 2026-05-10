package com.ironcore.domain.user.repository;

import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UserId id);

    Optional<User> findByEmail(Email email);

    boolean existsById(UserId id);

    boolean existsByEmail(Email email);
}
