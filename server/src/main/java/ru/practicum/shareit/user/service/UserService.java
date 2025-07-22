package ru.practicum.shareit.user.service;

import java.util.List;

import ru.practicum.shareit.user.dto.UserDto;

public interface UserService {

    List<UserDto> getAllUsers();

    UserDto getUserById(Long userId);

    UserDto createUser(UserDto userDto);

    UserDto updateUser(Long userId, UserDto userDto);

    void deleteUser(Long userId);
}
