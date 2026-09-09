package com.freddieapp.loginfrontend.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(LoginViewController.class)
public class LoginViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testIndexRoute() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    public void testServiceInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/service-info")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service", is("login-frontend-service")))
                .andExpect(jsonPath("$.port", is(8088)))
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.database", is("PostgreSQL Database 1 (freddie_customer)")));
    }
}
