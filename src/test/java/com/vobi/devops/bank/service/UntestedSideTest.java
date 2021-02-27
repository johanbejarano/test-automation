package com.vobi.devops.bank.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UntestedSideTest {

	@Test
	void shouldFailWhenGivenFalse() {
		// Arrange
		boolean input = false;
		String result;
		String expected = "FAIL";
		UntestedSide untestedSide = new UntestedSide();
		
		// Act
		result = untestedSide.foo(input);

		// Assert
		
		assertEquals(expected, result);
	}

	@Test
	void shouldBeOkWhenGivenTrue() {
		// Arrange
		boolean input = true;
		String result;
		String expected = "OK";
		UntestedSide untestedSide = new UntestedSide();
		
		
		// Act
		result = untestedSide.foo(input);
		
		// Assert
		assertEquals(expected, result);
	}

}
