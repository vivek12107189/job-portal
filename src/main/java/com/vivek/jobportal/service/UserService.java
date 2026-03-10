package com.vivek.jobportal.service;

import com.vivek.jobportal.entity.User;
import com.vivek.jobportal.exception.NotFoundException;
import com.vivek.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}
