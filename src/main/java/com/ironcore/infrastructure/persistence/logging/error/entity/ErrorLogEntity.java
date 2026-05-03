package com.ironcore.infrastructure.persistence.logging.error.entity;

import com.ironcore.domain.logging.error.enums.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_code", length = 30, nullable = false)
    private ErrorCode errorCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "exception_class", nullable = false)
    private String exceptionClass;

    @Column(name = "request_path", nullable = false)
    private String requestPath;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "correlation_id", nullable = false)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
