package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.revature.models.User;
import com.revature.repositories.UserRepository;

import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
	@Mock
	private UserRepository mockedUserRepository;

	@InjectMocks
	private UserService mockedUserService;

	@Test
	public void saveUserTest() {
		// given
		User user = new User(2, "Neffy", "Ros", "username", "pass", "neffy@gmail.com");
		when(mockedUserRepository.save(user)).thenReturn(user);
		User user2 = mockedUserService.save(user);
		System.out.println(user2);

		// then
		assertEquals(user2, user);
	}

	@Test
	public void findByIdTest() {
		User mockUser = new User("Neffy", "Ros", "username", "pass", "neffy@gmail.com");
		User mockUser1 = mockedUserService.save(mockUser);
		assertEquals(mockedUserService.getId(1), mockUser1);
	}

	@Test
	public void updateUserTest() {
		User user = new User("Neffy", "Ros", "Username", "pass", "nef@gmail.com");
		User userUpdated = new User("Ros", "Neffy", "UpdatedUsername", "pass", "neffy@gmail.com");
		when(mockedUserRepository.save(user)).thenReturn(userUpdated);

		User expected = mockedUserService.update(user);
		assertEquals(expected, userUpdated);
	}

}