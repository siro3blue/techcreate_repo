package com.example.Interview;

import java.io.IOException;
import java.util.List;

public class ParserRunner {

	public static void main(String[] args) {
		String schemaPath = "C:/test/schema.txt";
		String filePath = "C:/test/file.txt";

		List<SchemaField> fields = SchemaLoader.loadFromTxt(schemaPath);

		FixedLengthParser parser = new FixedLengthParser(fields);

		try {
			List<Record> records = parser.parseFile(filePath);
			for (Record record : records) {
				System.out.println(record);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
