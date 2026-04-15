package com.example.dataservice.controller;

import com.example.dataservice.domain.Product;
import com.example.dataservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class ProductControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        productRepository.deleteAll();
    }

    private Product buildAndSave(String name, String category, BigDecimal price, Integer stock, Boolean active) {
        Product p = new Product();
        p.setName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setStock(stock);
        p.setActive(active);
        return productRepository.save(p);
    }

    @Test
    void getById_found_returnsProduct() throws Exception {
        Product saved = buildAndSave("TestProd", "Electronics", new BigDecimal("99.99"), 10, true);

        mockMvc.perform(get("/api/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("TestProd"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_allFields_updatesProduct() throws Exception {
        Product saved = buildAndSave("OldName", "OldCat", new BigDecimal("10.00"), 5, true);

        mockMvc.perform(put("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewName\",\"category\":\"NewCat\",\"price\":25.00,\"stock\":20,\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.category").value("NewCat"))
                .andExpect(jsonPath("$.price").value(25.0))
                .andExpect(jsonPath("$.stock").value(20))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void update_partialFields_updatesOnlyProvidedFields() throws Exception {
        Product saved = buildAndSave("OriginalName", "OriginalCat", new BigDecimal("50.00"), 3, true);

        mockMvc.perform(put("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"OnlyNameChanged\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("OnlyNameChanged"))
                .andExpect(jsonPath("$.category").value("OriginalCat"))
                .andExpect(jsonPath("$.stock").value(3))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void update_withoutName_doesNotChangeName() throws Exception {
        Product saved = buildAndSave("Unchanged", "Cat", new BigDecimal("10.00"), 1, true);

        mockMvc.perform(put("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"category\":\"NewCat\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Unchanged"))
                .andExpect(jsonPath("$.category").value("NewCat"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        mockMvc.perform(put("/api/products/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Ghost\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRevisions_returnsRevisionList() throws Exception {
        Product saved = buildAndSave("AuditProd", "Cat", new BigDecimal("1.00"), 1, true);

        mockMvc.perform(put("/api/products/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"AuditProdUpdated\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/" + saved.getId() + "/audit/revisions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAtRevision_found_returnsProductAtRevision() throws Exception {
        Product saved = buildAndSave("HistoryProd", "HistCat", new BigDecimal("5.00"), 2, false);

        MvcResult revResult = mockMvc.perform(get("/api/products/" + saved.getId() + "/audit/revisions"))
                .andExpect(status().isOk())
                .andReturn();

        int firstRevision = objectMapper.readTree(revResult.getResponse().getContentAsString())
                .get(0).asInt();

        mockMvc.perform(get("/api/products/" + saved.getId() + "/audit/" + firstRevision))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HistoryProd"))
                .andExpect(jsonPath("$.category").value("HistCat"));
    }

    @Test
    void getAtRevision_notFound_returns404() throws Exception {
        Product saved = buildAndSave("ThrowawayProd", "Cat", new BigDecimal("1.00"), 1, true);

        MvcResult revResult = mockMvc.perform(get("/api/products/" + saved.getId() + "/audit/revisions"))
                .andExpect(status().isOk())
                .andReturn();

        int existingRevision = objectMapper.readTree(revResult.getResponse().getContentAsString())
                .get(0).asInt();

        // Product ID 99999 was never saved — Envers returns null → 404
        mockMvc.perform(get("/api/products/99999/audit/" + existingRevision))
                .andExpect(status().isNotFound());
    }
}
