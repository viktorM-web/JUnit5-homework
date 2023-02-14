package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Status.ACTIVE;
import static com.dmdev.entity.Status.CANCELED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao, CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(), Clock.systemUTC()
        );
    }

    @Test
    void upsert() {
        CreateSubscriptionDto dto = getSubscriptionDto();

        Subscription subscription = subscriptionService.upsert(dto);

        assertNotNull(subscription.getId());
    }

    @Test
    void expire() {
        var subscription = subscriptionDao.insert(getSubscription());

        subscriptionService.expire(subscription.getId());
        var actualSubscription = subscriptionDao.findById(subscription.getId());

        assertThat(actualSubscription).isPresent();
        assertThat(actualSubscription.get().getStatus()).isNotEqualTo(subscription.getStatus());
        assertThat(actualSubscription.get().getExpirationDate()).isNotEqualTo(subscription.getExpirationDate());
    }

    @Test
    void cansel() {
        var subscription = subscriptionDao.insert(getSubscription());

        subscriptionService.cancel(subscription.getId());
        var actualSubscription = subscriptionDao.findById(subscription.getId());

        assertThat(actualSubscription).isPresent();
        assertThat(actualSubscription.get().getStatus()).isEqualTo(CANCELED);
    }

    private static Subscription getSubscription() {
        return Subscription.builder()
                .userId(1)
                .name("Music")
                .provider(APPLE)
                .expirationDate(Instant.now().plus(Duration.ofDays(10)).truncatedTo(ChronoUnit.SECONDS))
                .status(ACTIVE)
                .build();
    }

    private static CreateSubscriptionDto getSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE.name())
                .expirationDate(Instant.now().plus(Duration.ofDays(10)).truncatedTo(ChronoUnit.SECONDS))
                .build();
    }
}
