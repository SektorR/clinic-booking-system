package com.groundandgrow.security;

import com.groundandgrow.model.Psychologist;
import com.groundandgrow.repository.PsychologistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PsychologistRepository psychologistRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Psychologist psychologist = psychologistRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Psychologist not found with email: " + email
                ));

        if (!psychologist.getIsActive()) {
            throw new UsernameNotFoundException(
                    "Psychologist account is inactive: " + email
            );
        }

        return UserPrincipal.create(psychologist);
    }

    @Transactional
    public UserDetails loadUserById(String id) {
        Psychologist psychologist = psychologistRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Psychologist not found with id: " + id
                ));

        return UserPrincipal.create(psychologist);
    }
}
