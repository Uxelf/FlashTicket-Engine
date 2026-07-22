package com.uxelf.dev.ticketDealer.service;

import com.uxelf.dev.ticketDealer.entity.User;
import com.uxelf.dev.ticketDealer.exception.AppBadRequestException;
import com.uxelf.dev.ticketDealer.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class UsersService {

    private UserRepository userRepository;

    public User createUser(String username){
        if (userRepository.existsByUsername(username))
            throw new AppBadRequestException("Username already taken");

        User newUser = new User();
        newUser.setUsername(username);
        userRepository.save(newUser);

        return newUser;
    }

    public List<String> getAllUsernames(){
        return userRepository.findAll().stream().map(User::getUsername).toList();
    }
}
