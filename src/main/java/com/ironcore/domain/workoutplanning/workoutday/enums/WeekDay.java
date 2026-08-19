package com.ironcore.domain.workoutplanning.workoutday.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum WeekDay {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);

    private final int value;

    WeekDay(int value) {
        this.value = value;
    }

    public static WeekDay fromValue(int value) {
        return Arrays.stream(values())
                .filter(day -> day.value == value)
                .findFirst()
                .orElseThrow();

    }
}
