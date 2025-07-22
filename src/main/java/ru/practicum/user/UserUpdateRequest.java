package ru.practicum.user;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String name;
    private String email;
}
