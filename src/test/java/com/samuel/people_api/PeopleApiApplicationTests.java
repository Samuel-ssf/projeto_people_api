package com.samuel.people_api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.samuel.people_api.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class PeopleApiApplicationTests {
    private static final String USER = "testuser";
    private static final String PASSWORD = "testpassword";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository repository;

    @BeforeEach
    void cleanDatabase() {
        repository.deleteAll();
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registersFindsAndDeletesPerson() throws Exception {
        MvcResult created = mockMvc.perform(post("/registrarName")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andReturn();

        String id = com.jayway.jsonpath.JsonPath.read(created.getResponse().getContentAsString(), "$.id").toString();
        mockMvc.perform(get("/list/{id}", id).with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@example.com"));

        mockMvc.perform(delete("/list/{id}", id).with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/list/{id}", id).with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidPersonAndInvalidId() throws Exception {
        mockMvc.perform(post("/registrarName")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"document\":\"\",\"name\":\"\",\"lastName\":\"Silva\",\"email\":\"invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.document").exists())
                .andExpect(jsonPath("$.errors.email").exists());

        mockMvc.perform(post("/registrarName")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"document\":\"123\",\"name\":\"Ana\",\"lastName\":\"Silva\",\"email\":\"ana@example.com\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.document").value("Documento deve conter entre 7 e 12 dígitos numéricos"));

        mockMvc.perform(get("/list/0").with(httpBasic(USER, PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    private String validPersonJson() {
        return "{\"document\":\"123456789\",\"name\":\"Ana\",\"lastName\":\"Silva\",\"email\":\"ana@example.com\"}";
    }
}
