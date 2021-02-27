package com.vobi.devops.bank.service;

import org.springframework.stereotype.Component;

@Component
public class UntestedSide {

	public String foo(boolean b) {
		if (b) {
			performAction();
			return "OK";
		}

		return "FAIL";
	}
	
	public void performAction() {
		System.out.println("SOMENTHING VERY IMPORTANT");
	}

}
