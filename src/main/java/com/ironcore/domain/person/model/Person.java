package com.ironcore.domain.person.model;

import com.ironcore.domain.person.exception.InvalidPersonException;
import com.ironcore.domain.person.valueobject.BirthDate;
import com.ironcore.domain.person.valueobject.PersonId;
import com.ironcore.domain.person.valueobject.Sex;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Person {

    private final PersonId id;
    private String name;
    private Sex sex;
    private BirthDate birthDate;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Person(PersonId id, String name, Sex sex, BirthDate birthDate, LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.id = id;
        this.name = requireNonBlank(name, "Nome não pode ser nulo ou vazio.");
        this.sex = requireNonNull(sex, "Sexo não pode ser nulo.");
        this.birthDate = requireNonNull(birthDate, "Data de nascimento não pode ser nulo.");
        this.createdAt = requireNonNull(createdAt, "Data de criação não pode ser nulo.");
        this.updatedAt = updatedAt;
    }

    public static Person register(String name, Sex sex, BirthDate birthDate, LocalDateTime createdAt) {
        return new Person(null, name, sex,  birthDate, createdAt, null);
    }

    public static Person restore(PersonId id, String name, Sex sex, BirthDate birthDate, LocalDateTime createdAt,
                                 LocalDateTime updatedAt) {
        return new Person(id, name, sex,  birthDate, createdAt, updatedAt);
    }

    public void changeBirthDate(BirthDate birthDate,  LocalDateTime updatedAt) {
        this.birthDate = requireNonNull(birthDate, "Data de nascimento é obrigatória.");
        markUpdatedAt(updatedAt);
    }

    public void changeName(String newName, LocalDateTime updatedAt) {
        this.name = requireNonBlank(newName, "Nome é obrigatório.");
        markUpdatedAt(updatedAt);
    }

    public void changeSex(Sex sex,  LocalDateTime updatedAt) {
        this.sex = requireNonNull(sex, "Sexo é obrigatório.");
        markUpdatedAt(updatedAt);
    }

    private void markUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = requireNonNull(updatedAt, "Data de alteração é obrigatória.");
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new InvalidPersonException(message);
        }

        return value.trim();
    }

    private <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new InvalidPersonException(message);
        }

        return value;
    }
}
