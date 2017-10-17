package com.example.reservationservice;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */

@SpringBootTest(classes = ReservationServiceApplication.class)
@RunWith(SpringRunner.class)
public class BaseClass {

    @Autowired
    private ReservationRestController controller;

    @MockBean
    private ReservationRepository reservationRepository;

    @Before
    public void before() throws Exception {
        Mockito.when(this.reservationRepository.findAll()).thenReturn(Arrays.asList(new Reservation(1L, "Jane"),
                new Reservation(2L, "Joe")));

        RestAssuredMockMvc.standaloneSetup(this.controller);
    }
}
