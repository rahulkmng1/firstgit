package com.cts.ecotrack.service;

import com.cts.ecotrack.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User saveUser(User user);
    User getUserById(Integer id);
    List<User> getAllUsers();
    User updateUser(User user);
    void deleteUser(Integer id);
    Optional<User> getUserByEmail(String email);
}
