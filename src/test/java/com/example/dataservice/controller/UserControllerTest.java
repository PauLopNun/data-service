package com.example.dataservice.controller;

import com.example.dataservice.domain.Allergy;
import com.example.dataservice.domain.User;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private AllergyRepository allergyRepository;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private User buildUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAllergies(List.of());
        return user;
    }

    private User buildUserWithAllergy(Long id, String name) {
        Allergy allergy = new Allergy();
        allergy.setId(1L);
        allergy.setName("Peanuts");
        allergy.setSeverity("HIGH");

        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setAllergies(List.of(allergy));
        return user;
    }

    @Test
    void getUsers_noParam_returnsAll() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(buildUser(1L, "Alice"), buildUser(2L, "Bob")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getUsers_withNameContains_returnsFiltered() throws Exception {
        when(userRepository.findByNameContainingIgnoreCase("Ali")).thenReturn(List.of(buildUser(1L, "Alice")));

        mockMvc.perform(get("/api/users").param("nameContains", "Ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Alice"));

        verify(userRepository).findByNameContainingIgnoreCase("Ali");
    }

    @Test
    void getUsers_withAllergies_returnsAllergyData() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(buildUserWithAllergy(1L, "Alice")));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].allergies.length()").value(1))
                .andExpect(jsonPath("$[0].allergies[0].name").value("Peanuts"))
                .andExpect(jsonPath("$[0].allergies[0].severity").value("HIGH"));
    }

    @Test
    void getUsers_withNullAllergies_returnsEmptyAllergyList() throws Exception {
        User user = new User();
        user.setId(3L);
        user.setName("NoAllergyUser");
        user.setAllergies(null);

        when(userRepository.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("NoAllergyUser"))
                .andExpect(jsonPath("$[0].allergies.length()").value(0));
    }

    @Test
    void count_returnsUserCount() throws Exception {
        when(userRepository.count()).thenReturn(15L);

        mockMvc.perform(get("/api/users/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));
    }

    @Test
    void create_savesAndReturnsUser() throws Exception {
        User saved = buildUser(10L, "Charlie");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Charlie\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Charlie"));

        verify(userRepository).save(any(User.class));
    }

    @Test
    void update_found_returnsUpdatedUser() throws Exception {
        User existing = buildUser(1L, "Alice");
        User updated = buildUser(1L, "Alice Updated");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ghost\"}"))
                .andExpect(status().isNotFound());
    }
}
