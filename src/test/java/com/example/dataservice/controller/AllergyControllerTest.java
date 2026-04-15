package com.example.dataservice.controller;

import com.example.dataservice.domain.Allergy;
import com.example.dataservice.repository.AllergyRepository;
import com.example.dataservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AllergyControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private AllergyRepository allergyRepository;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private Allergy buildAllergy(Long id, String name, String severity) {
        Allergy allergy = new Allergy();
        allergy.setId(id);
        allergy.setName(name);
        allergy.setSeverity(severity);
        return allergy;
    }

    @Test
    void getAll_returnsAllAllergies() throws Exception {
        when(allergyRepository.findAll()).thenReturn(List.of(
                buildAllergy(1L, "Peanuts", "HIGH"),
                buildAllergy(2L, "Dust", "MEDIUM")
        ));

        mockMvc.perform(get("/api/allergies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Peanuts"))
                .andExpect(jsonPath("$[0].severity").value("HIGH"))
                .andExpect(jsonPath("$[1].name").value("Dust"));
    }

    @Test
    void getAll_returnsEmptyList_whenNoneExist() throws Exception {
        when(allergyRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/allergies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void count_returnsAllergyCount() throws Exception {
        when(allergyRepository.count()).thenReturn(5L);

        mockMvc.perform(get("/api/allergies/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void create_savesAndReturnsAllergy() throws Exception {
        Allergy saved = buildAllergy(3L, "Gluten", "LOW");
        when(allergyRepository.save(any(Allergy.class))).thenReturn(saved);

        mockMvc.perform(post("/api/allergies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Gluten\",\"severity\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("Gluten"))
                .andExpect(jsonPath("$.severity").value("LOW"));

        verify(allergyRepository).save(any(Allergy.class));
    }
}
