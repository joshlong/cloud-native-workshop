package com.example.reservationservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
@SpringBootTest(classes = ReservationServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class ReservationRestControllerTest {

    @MockBean
    private ReservationRepository reservationRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void reservations() throws Exception {

        Mockito.when(this.reservationRepository.findAll()).thenReturn(Arrays.asList(new Reservation(1L, "Jane"),
                new Reservation(2L, "Joe")));

        this.mockMvc.perform(MockMvcRequestBuilders.get("/reservations"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(MockMvcResultMatchers.jsonPath("@.[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("@.[0].reservationName").value("Jane"));
    }

}