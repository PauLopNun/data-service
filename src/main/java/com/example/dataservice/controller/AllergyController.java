package com.example.dataservice.controller;

import com.example.dataservice.domain.Allergy;
import com.example.dataservice.repository.AllergyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/allergies")
@RequiredArgsConstructor
public class AllergyController {

    private final AllergyRepository allergyRepository;

    @GetMapping
    public List<AllergyResponse> getAll() {
        return allergyRepository.findAll().stream()
                .map(a -> new AllergyResponse(a.getId(), a.getName(), a.getSeverity()))
                .toList();
    }

    @GetMapping("/count")
    public long count() {
        return allergyRepository.count();
    }

    @PostMapping
    public AllergyResponse create(@RequestBody AllergyRequest body) {
        Allergy allergy = new Allergy();
        allergy.setName(body.name());
        allergy.setSeverity(body.severity());
        Allergy saved = allergyRepository.save(allergy);
        return new AllergyResponse(saved.getId(), saved.getName(), saved.getSeverity());
    }

    record AllergyRequest(String name, String severity) {}
    record AllergyResponse(Long id, String name, String severity) {}
}
