package com.uxelf.dev.ticketDealer.controller;

import com.uxelf.dev.ticketDealer.dto.user.UserRequest;
import com.uxelf.dev.ticketDealer.entity.User;
import com.uxelf.dev.ticketDealer.service.UsersService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/users")
@AllArgsConstructor
public class UsersController {

    private UsersService usersService;

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid UserRequest request){
        User newUser = usersService.createUser(request.getUsername());
        return ResponseEntity.ok(newUser);
    }
}
