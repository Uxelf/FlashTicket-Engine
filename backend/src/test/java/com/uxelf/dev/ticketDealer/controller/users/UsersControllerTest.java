package com.uxelf.dev.ticketDealer.controller.users;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uxelf.dev.ticketDealer.dto.user.UserRequest;
import com.uxelf.dev.ticketDealer.dto.user.UsersListResponse;
import com.uxelf.dev.ticketDealer.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
public class UsersControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    final private static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void cleanDb(){
        userRepository.deleteAll();
    }

    @Test
    void whenUsernameFree_thenReturnOk() throws Exception {
        UserRequest request1 = new UserRequest("Test1");
        UserRequest request2 = new UserRequest("Test2");
        UserRequest request3 = new UserRequest("Test3");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3)))
                .andExpect(status().isOk());
    }

    @Test
    void whenUsernameTaken_thenReturnBadRequest() throws Exception {
        UserRequest request = new UserRequest("Test");

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenNoUsers_thenReturnEmptyList() throws Exception{
        MvcResult result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        UsersListResponse response = objectMapper.readValue(json, UsersListResponse.class);

        Assertions.assertTrue(response.getUsernamesList().isEmpty());
    }

    @Test
    void whenUsers_thenReturnUsernamesList() throws Exception{

        String[] usernames = {"Test1", "Test2", "Test3"};

        for (String username : usernames) {
            UserRequest request = new UserRequest(username);

            mockMvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        MvcResult result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        UsersListResponse response = objectMapper.readValue(json, UsersListResponse.class);

        Assertions.assertEquals(usernames.length, response.getUsernamesList().size());

        for (String username : usernames) {
            Assertions.assertTrue(response.getUsernamesList().contains(username));
        }
    }
}
