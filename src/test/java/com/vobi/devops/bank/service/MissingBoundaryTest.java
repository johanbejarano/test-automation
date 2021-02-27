package com.vobi.devops.bank.service;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MissingBoundaryTest {

	@Test
	void shouldReturnFooWhenGiven1() {
		
	  assertEquals("foo", MissingBoundary.foo(1));
	}

	@Test
	void shouldReturnBarWhenGivenMinus1() {
	  assertEquals("bar", MissingBoundary.foo(-1));
	}
	
}
