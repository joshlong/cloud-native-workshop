package com.example.reservationclient;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@SpringBootTest(classes = ReservationClientApplication.class)
@RunWith(SpringRunner.class)
//@AutoConfigureWireMock(port = 8000)
@AutoConfigureStubRunner(ids = "com.example:reservation-service:+:8000", workOffline = true)
@AutoConfigureJsonTesters
public class ReservationReaderTest {


//    @Autowired
//    private ObjectMapper objectMapper;

    @Autowired
    private ReservationReader reservationReader;

    @Test
    public void read() throws Exception {

      /*  String json = this.objectMapper.writeValueAsString(Arrays.asList(new Reservation(1L, "Jane"),
                new Reservation(2L, "John")));

        WireMock.stubFor(
                WireMock.get(WireMock.urlMatching("/reservations"))
                        .willReturn(WireMock.aResponse()
                                .withBody( json )
                                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE)
                                .withStatus(HttpStatus.OK.value())));*/

        Collection<Reservation> reservations = this.reservationReader.read();
        Assertions.assertThat(reservations.size()).isEqualTo(2);
        Assertions.assertThat(reservations.iterator().next().getReservationName()).isEqualTo("Jane");

    }

}