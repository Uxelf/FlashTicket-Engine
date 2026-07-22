package com.uxelf.dev.ticketDealer.dto.user;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UsersListResponse {
    private List<String> usernamesList = new ArrayList<>();
}
