package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Status.ACTIVE;
import static com.dmdev.entity.Status.EXPIRED;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private Clock clock;
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void expire() {
        Subscription subscription = getSubscription();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

        var exception = assertThrows(SubscriptionException.class,
                () -> subscriptionService.expire(subscription.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(String.format("Subscription %d has already expired", subscription.getId()));
    }

    @Test
    void cancelIfSubscriptionNotActive() {
        Subscription subscription = getSubscription();

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

        var exception = assertThrows(SubscriptionException.class,
                () -> subscriptionService.cancel(subscription.getId()));
        assertThat(exception.getMessage())
                .isEqualTo(String.format("Only active subscription %d can be canceled", subscription.getId()));
    }

    @Test
    void cancelIfSubscriptionActive() {
        Subscription subscription = getSubscription();
        subscription.setStatus(ACTIVE);

        doReturn(Optional.of(subscription)).when(subscriptionDao).findById(subscription.getId());

        subscriptionService.cancel(subscription.getId());

        assertThat(subscription.getStatus()).isNotEqualTo(ACTIVE);
    }

    @Test
    void upsertIfInvalidDto() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("")
                .provider(APPLE.name())
                .expirationDate(Instant.now().plus(Duration.ofDays(10).truncatedTo(SECONDS)))
                .build();
        var validationResult = new ValidationResult();
        validationResult.add(Error.of(101, "name is invalid"));
        doReturn(validationResult).when(createSubscriptionValidator).validate(dto);

        var exception = assertThrows(ValidationException.class, () -> subscriptionService.upsert(dto));
    }

    @Test
    void upsertIfValidDtoAndNotExistInDataBase() {
        Instant instant = Instant.now().plus(Duration.ofDays(10).truncatedTo(SECONDS));
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE.name())
                .expirationDate(instant)
                .build();
        Subscription subscription = Subscription.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE)
                .expirationDate(instant)
                .status(EXPIRED)
                .build();

        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(dto);
        doReturn(List.of()).when(subscriptionDao).findByUserId(dto.getUserId());
        doReturn(subscription).when(createSubscriptionMapper).map(dto);
        doReturn(subscription).when(subscriptionDao).upsert(subscription);

        Subscription actualSubscription = subscriptionService.upsert(dto);

        assertThat(actualSubscription).isEqualTo(subscription);
    }

    @Test
    void upsertIfValidDtoAndExistInDataBase() {
        Instant instant = Instant.now().plus(Duration.ofDays(10).truncatedTo(SECONDS));
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE.name())
                .expirationDate(instant)
                .build();
        Subscription subscription = Subscription.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE)
                .expirationDate(instant)
                .status(EXPIRED)
                .build();

        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(dto);
        doReturn(List.of(subscription)).when(subscriptionDao).findByUserId(dto.getUserId());
        doReturn(subscription).when(subscriptionDao).upsert(subscription);

        Subscription actualSubscription = subscriptionService.upsert(dto);

        assertThat(actualSubscription).isEqualTo(subscription.setStatus(ACTIVE));
    }

    private static Subscription getSubscription() {
        return Subscription.builder()
                .userId(11)
                .name("Music")
                .provider(APPLE)
                .expirationDate(Instant.now().plus(Duration.ofDays(10).truncatedTo(SECONDS)))
                .status(EXPIRED)
                .build();
    }
}
