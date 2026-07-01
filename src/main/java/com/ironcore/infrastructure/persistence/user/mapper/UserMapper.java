package com.ironcore.infrastructure.persistence.user.mapper;

import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.user.model.User;
import com.ironcore.domain.user.valueobject.Email;
import com.ironcore.domain.user.valueobject.PasswordHash;
import com.ironcore.domain.user.valueobject.UserId;
import com.ironcore.infrastructure.exception.DataMappingException;
import com.ironcore.infrastructure.persistence.person.entity.PersonEntity;
import com.ironcore.infrastructure.persistence.user.entity.UserEntity;

public class UserMapper {

    public static UserEntity toEntity(User user, PersonEntity person) {
        try {
            return new UserEntity(
                    user.getId() == null ? null : user.getId().value(),
                    user.getNickname(),
                    person,
                    user.getEmail().value(),
                    user.getPasswordHash().value(),
                    user.getMustChangePassword(),
                    user.getActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user de domínio para entidade.", exception);
        }
    }

    public static User toDomain(UserEntity entity) {
        try {
            return User.restore(
                    new UserId(entity.getId()),
                    entity.getNickname(),
                    new PersonId(entity.getPerson().getId()),
                    new Email(entity.getEmail()),
                    new PasswordHash(entity.getPasswordHash()),
                    entity.getMustChangePassword(),
                    entity.getActive(),
                    entity.getCreatedAt(),
                    entity.getUpdatedAt()
            );
        } catch (RuntimeException exception) {
            throw new DataMappingException("Falha ao converter user de entidade para domínio.", exception);
        }
    }
}
