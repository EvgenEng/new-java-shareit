package ru.practicum.shareit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John Doe").email("john.doe@example.com").build();
        userDto = UserDto.builder().id(1L).name("John Doe").email("john.doe@example.com").build();
    }

    @Test
    void shouldCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto createdUser = userService.createUser(userDto);

        assertNotNull(createdUser);
        assertEquals(userDto.getName(), createdUser.getName());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto foundUser = userService.getUserById(1L);

        assertNotNull(foundUser);
        assertEquals(user.getId(), foundUser.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void shouldUpdateUser() {
        UserDto updates = UserDto.builder().name("Jane Doe").email("jane.doe@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedUser = userService.updateUser(1L, updates);

        assertEquals("Jane Doe", updatedUser.getName());
        assertEquals("jane.doe@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals(user.getName(), users.get(0).getName());
    }

    @Test
    void shouldThrowConflictExceptionWhenCreatingUserWithDuplicateEmail() {
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(userDto));

        assertEquals("Email already exists: john.doe@example.com", exception.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentUser() {
        UserDto updates = UserDto.builder().name("Jane Doe").build();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(99L, updates));

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdatingUserWithDuplicateEmail() {
        UserDto updates = UserDto.builder().email("duplicate@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate email"));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(1L, updates));

        assertEquals("Email already exists: duplicate@example.com", exception.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldUpdateUserPartially() {
        // Тест обновления только имени
        UserDto updates = UserDto.builder().name("Jane Doe").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedUser = userService.updateUser(1L, updates);

        assertEquals("Jane Doe", updatedUser.getName());
        assertEquals("john.doe@example.com", updatedUser.getEmail()); // email остался прежним
    }

    @Test
    void shouldUpdateUserEmailOnly() {
        // Тест обновления только email
        UserDto updates = UserDto.builder().email("new.email@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedUser = userService.updateUser(1L, updates);

        assertEquals("John Doe", updatedUser.getName()); // имя осталось прежним
        assertEquals("new.email@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldGetAllUsersEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> users = userService.getAllUsers();

        assertEquals(0, users.size());
    }
}
