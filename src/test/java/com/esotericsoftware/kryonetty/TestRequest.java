package com.esotericsoftware.kryonetty;

import java.util.List;

public class TestRequest {

	public String someText;
	public long someLong;
	public boolean someBoolean;
	public List<String> someList;

	public TestRequest () {
	}

	public TestRequest(String someText, long someLong, boolean someBoolean, List<String> someList) {
		this.someText = someText;
		this.someLong = someLong;
		this.someBoolean = someBoolean;
		this.someList = someList;
	}
}