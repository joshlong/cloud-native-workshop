package com.example.reservationservice;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
public class ReservationTest {

    @Test
    public void constructor() {
        Reservation reservation = new Reservation(1l, "Bob");
        Assert.assertEquals("id == 1", (long) reservation.getId(), 1L);
        Assert.assertThat(reservation.getId(), Matchers.is(1L));
        Assertions.assertThat(reservation.getId()).isEqualTo(1L);

    }
}