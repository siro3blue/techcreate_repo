package com.example.Interview;

import java.util.Map;

public class Record {
	private Map<String, String> values;

	public Record(Map<String, String> values) {
		this.values = values;
	}

	public Map<String, String> getValues() {
		return values;
	}

	@Override
	public String toString() {
		return values.toString();
	}
}
