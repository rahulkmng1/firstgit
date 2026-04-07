package com.cts.ecotrack.service;

import com.cts.ecotrack.dto.RegisterRequest;
import com.cts.ecotrack.dto.UserResponse;
import com.cts.ecotrack.entity.User;
import com.cts.ecotrack.enums.UserRole;
import com.cts.ecotrack.enums.UserStatus;
import com.cts.ecotrack.dao.UserRepository;
import com.cts.ecotrack.exception.EmailAlreadyExistsException;
import com.cts.ecotrack.exception.ResourceNotFoundException;
import com.cts.ecotrack.exception.UserIdAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // POST
    public UserResponse registerNewUser(RegisterRequest request) {
        if (userRepository.existsById(request.userId())) {
            throw new UserIdAlreadyExistsException("UserID " + request.userId() + " is already taken.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with this email already exists.");
        }

        if (request.role() != UserRole.CITIZEN) {
            // Check if that role already exists in the database
            if (userRepository.existsByRole(request.role())) {
                throw new IllegalArgumentException("Registration failed: A user with the role " + request.role() + " already exists. Only one is allowed.");
            }
        }

        User user = new User();
        user.setUserId(request.userId());
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhone(request.phone());
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setPasswordHash(passwordEncoder.encode(request.password())); // Saving without encryption for now

        User saveUser = userRepository.save(user);
        return new UserResponse(saveUser.getUserId(),
                saveUser.getName(),
                saveUser.getEmail(),
                saveUser.getPhone(),
                saveUser.getRole(),
                saveUser.getStatus());
    }

    //GET
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(user.getUserId(),
                        user.getName(),
                        user.getEmail(),
                        user.getPhone(),
                        user.getRole(),
                        user.getStatus())).toList();
    }

    //get by id
    public UserResponse getUserById(int id) {

        User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("user not found"));

        return new UserResponse(user.getUserId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole(), user.getStatus());
    }

    //put
    public UserResponse updateUser(Integer id, RegisterRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EmailAlreadyExistsException("User with ID " + id + " not found."));

        if (!existingUser.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already taken by another user.");
        }

        // Update the fields
        existingUser.setName(request.name());
        existingUser.setEmail(request.email());
        existingUser.setPhone(request.phone());
        existingUser.setRole(request.role());
        existingUser.setPasswordHash(passwordEncoder.encode(request.password()));

        // Save to database
        User updatedUser = userRepository.save(existingUser);

        // Return the safe DTO
        return new UserResponse(
                updatedUser.getUserId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getPhone(),
                updatedUser.getRole(),
                updatedUser.getStatus()
        );
    }

    // DELETE
    public UserResponse deleteUserById(int id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot delete: User with ID " + id + " not found."));

        userRepository.deleteById(id);
        return new UserResponse(
                existingUser.getUserId(),
                existingUser.getName(),
                existingUser.getEmail(),
                existingUser.getPhone(),
                existingUser.getRole(),
                existingUser.getStatus()
        );
    }


}