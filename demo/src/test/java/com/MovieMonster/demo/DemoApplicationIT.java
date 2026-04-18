package com.MovieMonster.demo;

import com.MovieMonster.demo.Repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DemoApplicationIT {

	@Autowired
	private RoleRepository roleRepository;

	@Test
	void contextLoadsAndSeedsDefaultRoles() {
		assertTrue(roleRepository.findByName("USER").isPresent());
		assertTrue(roleRepository.findByName("ADMIN").isPresent());
	}
}
