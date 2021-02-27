package com.vobi.devops.bank.service;

public class MyopicMockist {

	public static String foo(Collaborator c, boolean b) {
		if (b) {
			return c.performAction(); //Retorna "BAR"
		}

		return "FOO";
	}

}
