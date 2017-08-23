package com.example.reservationclient;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.BDDAssertions;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

import static org.junit.Assert.*;

@SpringBootTest
@AutoConfigureStubRunner(ids = "com.example:reservation-service:+:stubs:8000", workOffline = true)
@RunWith(SpringRunner.class)
public class ReservationApiAdapterRestControllerTest {

    @Autowired
    private ReservationReader reservationReader;

    @Test
    public void should_return_reservations() {

        Collection<Reservation> reservations = this.reservationReader.read();

        BDDAssertions.then(reservations)
                .is(new Condition<Iterable<? extends Reservation>>() {
                    @Override
                    public boolean matches(Iterable<? extends Reservation> reservations) {
                        for (Reservation r : reservations)
                            if (r.getReservationName().equalsIgnoreCase("Jane"))
                                return true;
                        return false;
                    }
                });


    }

}