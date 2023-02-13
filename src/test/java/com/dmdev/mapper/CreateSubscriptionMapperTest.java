package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        var now = Instant.now().plus(Duration.ofDays(10));

        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE.name())
                .expirationDate(now)
                .build();

        Subscription actualResult = mapper.map(dto);

        Subscription expectedResult = Subscription.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE)
                .expirationDate(now)
                .status(ACTIVE)
                .build();

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
