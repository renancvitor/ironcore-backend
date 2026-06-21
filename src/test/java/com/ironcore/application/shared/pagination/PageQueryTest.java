package com.ironcore.application.shared.pagination;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class PageQueryTest {

    @Nested
    class Successful {

        @Test
        void shouldCreatePageQueryWithValidBoundaries() {
            PageQuery minimum = assertDoesNotThrow(
                    () -> new PageQuery(0, 1)
            );

            PageQuery maximum = assertDoesNotThrow(
                    () -> new PageQuery(5, 100)
            );

            assertAll(
                    () -> assertEquals(0, minimum.page()),
                    () -> assertEquals(1, minimum.size()),
                    () -> assertEquals(5, maximum.page()),
                    () -> assertEquals(100, maximum.size())
            );
        }
    }
    
    @Nested
    class Failing {
        
        @Test
        void shouldFailWhenPageIsNegative() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PageQuery(-1, 100)
            );

            assertEquals("Página não pode ser negativa.",
                    exception.getMessage()
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 101})
        void shouldFailWhenSizeIsOutsideAllowedRange(int invalidSize) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new PageQuery(0, invalidSize)
            );

            assertEquals("Tamanho da página deve estar entre 1 e 100.",
                    exception.getMessage()
            );
        }
    }
}
