package com.tech.aidocqna.security;

import com.tech.aidocqna.exception.AIDocException;
import com.tech.aidocqna.model.User;
import com.tech.aidocqna.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService  {

    private final UserRepository userRepository;

    public AppUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User loadUserByUsername(String username)  {
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new AIDocException("User not found"));
    }
}
