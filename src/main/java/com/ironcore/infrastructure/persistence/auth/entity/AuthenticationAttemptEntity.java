package com.ironcore.infrastructure.persistence.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "authentication_attempts")
@Getter
@Setter
@NoArgsConstructor
public class AuthenticationAttemptEntity {
    @Id
    @Column(length = 64, nullable = false)
    private String id;
    @Column(nullable = false)
    private Instant windowStartedAt;
    @Column(nullable = false)
    private int attempts;
    @Version
    @Column(nullable = false)
    private Long version;
}
