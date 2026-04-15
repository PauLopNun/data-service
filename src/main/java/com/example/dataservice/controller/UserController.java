package com.example.dataservice.controller;

import com.example.dataservice.domain.Allergy;
import com.example.dataservice.domain.User;
import com.example.dataservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<UserResponse> getUsers(@RequestParam(required = false) String nameContains) {
        List<User> users = nameContains != null
                ? userRepository.findByNameContainingIgnoreCase(nameContains)
                : userRepository.findAll();
        return users.stream().map(this::toResponse).toList();
    }

    @GetMapping("/count")
    public long count() {
        return userRepository.count();
    }

    @PostMapping
    public UserResponse create(@RequestBody NameRequest body) {
        User user = new User();
        user.setName(body.name());
        return toResponse(userRepository.save(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody NameRequest body) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setName(body.name());
                    return ResponseEntity.ok(toResponse(userRepository.save(user)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse toResponse(User user) {
        List<AllergyResponse> allergies = user.getAllergies() == null ? List.of() :
                user.getAllergies().stream()
                        .map(a -> new AllergyResponse(a.getId(), a.getName(), a.getSeverity()))
                        .toList();
        return new UserResponse(user.getId(), user.getName(), allergies);
    }

    record NameRequest(String name) {}
    record AllergyResponse(Long id, String name, String severity) {}
    record UserResponse(Long id, String name, List<AllergyResponse> allergies) {}
}
