package io.kentra.openlineage;

import java.time.Clock;

/**
 * ClockProvider is used to facilitate deterministic testing.
 */
public class ClockProvider {

    private static final Clock CLOCK = Clock.systemDefaultZone();

    public Clock getClock() {
        return CLOCK;
    }
}
