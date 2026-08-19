package com.ironcore.infrastructure.persistence.workoutplanning.workoutday.converter;

import com.ironcore.domain.workoutplanning.workoutday.enums.WeekDay;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class WeekDayConverter implements AttributeConverter<WeekDay, Integer> {

    @Override
    public Integer convertToDatabaseColumn(WeekDay weekDay) {
        return weekDay == null ? null : weekDay.getValue();
    }

    @Override
    public WeekDay convertToEntityAttribute(Integer value) {
        return value == null ? null : WeekDay.fromValue(value);
    }
}
