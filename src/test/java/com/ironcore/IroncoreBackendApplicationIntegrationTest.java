package com.ironcore;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        classes = IroncoreBackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "ironcore.bootstrap.single-user.enabled=false"
)
@ActiveProfiles("test")
class IroncoreBackendApplicationIntegrationTest {

    @Test
    void contextLoads() {
    }

}
