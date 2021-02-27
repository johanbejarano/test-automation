package com.vobi.devops.bank.service;

public class MissingBoundary {

	public static String foo(int i) {
		if (i >= 0) {
			return "foo";
		} else {
			return "bar";
		}
	}

}
