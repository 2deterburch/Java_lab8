
package org.example;

import org.junit.jupiter.api.Test;
import ua.vendor.limits.Transfer;
import ua.vendor.limits.TransferLimits;
import ua.vendor.limits.Vendor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransferLimitsTest {

    // Створення потрібної збірки з фіксованим часом
    private TransferLimits createLimits(Instant now) {

        int build = Integer.parseInt(
                System.getProperty("build", "1")
        );

        Clock clock = Clock.fixed(
                now,
                ZoneId.of("UTC")
        );

        return Vendor.build(build, clock);
    }

    // 1. Перевірка нульової суми
    @Test
    void shouldRejectZeroAmount() {

        TransferLimits limits = createLimits(
                Instant.parse("2025-01-02T10:00:00Z")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> limits.allowed(0, List.of())
        );
    }

    // 2. Перевірка максимальної суми одного переказу
    @Test
    void shouldAllowExactlyMaximumAmount() {

        TransferLimits limits = createLimits(
                Instant.parse("2025-01-02T10:00:00Z")
        );

        assertTrue(
                limits.allowed(20000, List.of())
        );
    }

    // 3. Перевірка перевищення максимальної суми
    @Test
    void shouldRejectAmountAboveLimit() {

        TransferLimits limits = createLimits(
                Instant.parse("2025-01-02T10:00:00Z")
        );

        assertFalse(
                limits.allowed(20000.01, List.of())
        );
    }

    // 4. Перевірка денного ліміту разом із новим переказом
    @Test
    void shouldIncludeNewTransferInDailyLimit() {

        TransferLimits limits = createLimits(
                Instant.parse("2025-01-02T10:00:00Z")
        );

        List<Transfer> history = List.of(
                new Transfer(
                        Instant.parse("2025-01-02T09:00:00Z"),
                        49500
                )
        );

        assertFalse(
                limits.allowed(1000, history)
        );
    }

    // 5. Перевірка часового поясу Києва
    @Test
    void shouldUseKyivTimezone() {

        TransferLimits limits = createLimits(
                Instant.parse("2025-01-01T22:30:00Z")
        );

        List<Transfer> history = List.of(
                new Transfer(
                        Instant.parse("2025-01-01T20:30:00Z"),
                        49500
                )
        );

        assertTrue(
                limits.allowed(1000, history)
        );
    }

    // 6. Перевірка, що старі перекази не враховуються
    @Test
    void shouldIgnoreTransfersFromPreviousDays() {

        TransferLimits limits = createLimits(
                Instant.parse("2025-01-02T10:00:00Z")
        );

        List<Transfer> history = List.of(
                new Transfer(
                        Instant.parse("2025-01-01T10:00:00Z"),
                        49500
                )
        );

        assertTrue(
                limits.allowed(1000, history)
        );
    }
}
