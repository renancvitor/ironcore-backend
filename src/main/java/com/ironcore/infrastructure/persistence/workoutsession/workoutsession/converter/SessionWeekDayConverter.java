package com.ironcore.infrastructure.persistence.workoutsession.workoutsession.converter;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SessionWeekDayConverter implements AttributeConverter<WeekDay, Short> {
    @Override
    public Short convertToDatabaseColumn(WeekDay day) {
        return day == null ? null : (short) day.getValue();
    }

    @Override
    public WeekDay convertToEntityAttribute(Short value) {
        return value == null ? null : WeekDay.fromValue(value);
    }
}
