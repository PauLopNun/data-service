package com.example.dataservice;

import com.example.dataservice.repository.AllergyRepository;
import com.example.dataservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataSeederTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AllergyRepository allergyRepository;

    @InjectMocks
    private DataSeeder dataSeeder;

    @Test
    void run_skipsSeeding_whenDataAlreadyExists() throws Exception {
        when(userRepository.count()).thenReturn(5L);

        dataSeeder.run();

        verify(userRepository, never()).save(any());
        verify(allergyRepository, never()).save(any());
    }

    @Test
    void run_skipsSeeding_whenAllergyDataAlreadyExists() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(allergyRepository.count()).thenReturn(3L);

        dataSeeder.run();

        verify(userRepository, never()).save(any());
        verify(allergyRepository, never()).save(any());
    }

    @Test
    void run_seedsData_whenRepositoriesAreEmpty() throws Exception {
        when(userRepository.count()).thenReturn(0L);
        when(allergyRepository.count()).thenReturn(0L);

        com.example.dataservice.domain.Allergy savedAllergy = new com.example.dataservice.domain.Allergy();
        savedAllergy.setId(1L);
        savedAllergy.setName("Allergy 1");
        when(allergyRepository.save(any())).thenReturn(savedAllergy);

        com.example.dataservice.domain.User savedUser = new com.example.dataservice.domain.User();
        savedUser.setId(1L);
        when(userRepository.save(any())).thenReturn(savedUser);

        dataSeeder.run();

        // 5 allergies should be saved
        verify(allergyRepository, times(5)).save(any());
        // 30 users should be saved
        verify(userRepository, times(30)).save(any());
    }
}
