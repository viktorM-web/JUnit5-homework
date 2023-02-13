package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    @BeforeAll
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao, CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(), Clock.systemUTC()
                );
    }

    @Test
    void expire(){
        var subscription = subscriptionDao.insert(getSubscription(1, "Music"));

        subscriptionService.expire(subscription.getId());
        Subscription actualSubscription = subscriptionDao.findById(subscription.getId()).get();

        assertThat(actualSubscription.getStatus()).isNotEqualTo(subscription.getStatus());

    }

    private static Subscription getSubscription(Integer userId, String name) {
        return Subscription.builder()
                .userId(userId)
                .name(name)
                .provider(APPLE)
                .expirationDate(Instant.now().plus(Duration.ofDays(10)).truncatedTo(ChronoUnit.SECONDS))
                .status(ACTIVE)
                .build();
    }
}
