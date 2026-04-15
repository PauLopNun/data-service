package com.example.dataservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@SpringBootTest
class DataServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully
    }

    @Test
    void main_invokesSpringApplicationRun() {
        try (MockedStatic<SpringApplication> sa = mockStatic(SpringApplication.class)) {
            sa.when(() -> SpringApplication.run(eq(DataServiceApplication.class), any(String[].class)))
              .thenReturn(mock(ConfigurableApplicationContext.class));

            DataServiceApplication.main(new String[]{});

            sa.verify(() -> SpringApplication.run(eq(DataServiceApplication.class), any(String[].class)));
        }
    }
}
