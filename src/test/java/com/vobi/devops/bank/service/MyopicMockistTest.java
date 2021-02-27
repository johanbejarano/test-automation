package com.vobi.devops.bank.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MyopicMockistTest {

	@Mock
	Collaborator mockCollaborator;

	@Test
	void shouldPerformActionWhenGivenTrue() {
		
		//Arrange
		boolean input = true;
		when(mockCollaborator.performAction()).thenReturn("BAR");
		
		//Act
		String result = MyopicMockist.foo(mockCollaborator, input);
		
		//Assert
		verify(mockCollaborator).performAction();
	}

	@Test
	void shouldNotPerformActionWhenGivenFalse() {
		//Arrange
		boolean input = false;
		lenient().when(mockCollaborator.performAction()).thenReturn("BAR");
		
		//Act
		String result = MyopicMockist.foo(mockCollaborator, input);
		
		//Assert
		verify(mockCollaborator, never()).performAction();
	}
	
}
