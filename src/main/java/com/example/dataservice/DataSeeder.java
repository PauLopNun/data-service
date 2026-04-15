package com.example.dataservice;

import com.example.dataservice.domain.Allergy;
import com.example.dataservice.domain.User;
import com.example.dataservice.repository.AllergyRepository;
import com.example.dataservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AllergyRepository allergyRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0 || allergyRepository.count() > 0) {
            return;
        }

        List<Allergy> allergies = new ArrayList<>();
        for (int j = 1; j <= 5; j++) {
            Allergy allergy = new Allergy();
            allergy.setName("Allergy " + j);
            allergyRepository.save(allergy);
            allergies.add(allergy);
        }

        for (int i = 1; i <= 30; i++) {
            User user = new User();
            user.setName("User " + i);
            user.setAllergies(allergies);
            userRepository.save(user);
        }
    }
}
