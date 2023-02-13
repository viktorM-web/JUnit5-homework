package com.dmdev.dao;

import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Provider.GOOGLE;
import static com.dmdev.entity.Status.ACTIVE;
import static com.dmdev.entity.Status.EXPIRED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SubscriptionDaoIT extends IntegrationTestBase {

    SubscriptionDao dao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        var subscription1 = dao.insert(getSubscription(1, "Music"));
        var subscription2 = dao.insert(getSubscription(2, "Map"));
        var subscription3 = dao.insert(getSubscription(3, "Move"));

        List<Subscription> subscriptionList = dao.findAll();

        assertThat(subscriptionList).hasSize(3);
        List<Integer> subscriptionId = subscriptionList.stream()
                .map(Subscription::getId)
                .toList();
        assertThat(subscriptionId).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());

    }

    @Test
    void findByIdIfEntityExist() {
        var subscription = dao.insert(getSubscription(1, "Music"));

        Optional<Subscription> actualResult = dao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(subscription);
    }

    @Test
    void findByIdIfEntityNotExist() {
        var subscription = dao.insert(getSubscription(1, "Music"));

        Optional<Subscription> actualResult = dao.findById(1000);

        assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteExistingEntity() {
        var subscription = dao.insert(getSubscription(1, "Music"));

        Boolean actualResult = dao.delete(subscription.getId());

        assertTrue(actualResult);
    }

    @Test
    void deleteNotExistingEntity() {
        var subscription = dao.insert(getSubscription(1, "Music"));

        boolean actualResult = dao.delete(1000);

        assertFalse(actualResult);
    }

    @Test
    void update() {
        Subscription subscription = getSubscription(1, "Music");
        dao.insert(subscription);
        subscription.setStatus(EXPIRED);
        subscription.setProvider(GOOGLE);

        dao.update(subscription);

        Subscription updatedSubscription = dao.findById(subscription.getId()).get();
        assertThat(updatedSubscription).isEqualTo(subscription);
    }

    @Test
    void insert() {
        Subscription subscription = getSubscription(1, "Music");

        Subscription insert = dao.insert(subscription);

        assertNotNull(insert.getId());
    }

    @Test
    void findByUserId() {
        var subscription1 = dao.insert(getSubscription(1, "Music"));
        var subscription2 = dao.insert(getSubscription(1, "Map"));
        var subscription3 = dao.insert(getSubscription(2, "Move"));

        List<Subscription> subscriptions = dao.findByUserId(subscription1.getUserId());

        assertThat(subscriptions).hasSize(2);
        List<Integer> subscriptionId = subscriptions.stream()
                .map(Subscription::getUserId)
                .toList();
        assertThat(subscriptionId).containsOnly(subscription1.getUserId());

    }

    @Test
    void upsertIfEntityNotExist() {
        Subscription subscription = getSubscription(1, "Music");

        Subscription upsert = dao.upsert(subscription);

        assertNotNull(upsert.getId());
    }

    @Test
    void upsertIfEntityExist() {
        Subscription subscription = getSubscription(1, "Music");
        dao.insert(subscription);
        subscription.setStatus(EXPIRED);
        subscription.setProvider(GOOGLE);

        dao.upsert(subscription);

        Subscription updatedSubscription = dao.findById(subscription.getId()).get();
        assertThat(updatedSubscription).isEqualTo(subscription);
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
