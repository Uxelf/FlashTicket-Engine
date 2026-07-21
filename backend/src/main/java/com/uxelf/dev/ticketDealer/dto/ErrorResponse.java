package com.uxelf.dev.ticketDealer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ErrorResponse {
    private Map<String, String> errors;
}
