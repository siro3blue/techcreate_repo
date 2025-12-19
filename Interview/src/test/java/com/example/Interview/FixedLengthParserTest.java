package com.example.Interview;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

class FixedLengthParserTest {

	@Test
	void testParseValidLine() throws Exception {
		// GIVEN a schema with 3 fixed length fields - name, gender, age
		// AND a parser program to initialized the schema
		List<SchemaField> schema = List.of(
				new SchemaField("name", 1, 20),
				new SchemaField("gender", 21, 21),
				new SchemaField("age", 22, 25)
		);
		
		FixedLengthParser parser = new FixedLengthParser(schema);

		// WHEN a valid fixed length line is passed into the parser program
		Record record = parser.parseLine("John Doe            M 25 ");

		// THEN parser program should return record containing the line
		// AND all fields should be correctly extracted
		assertNotNull(record);
		assertEquals("John Doe", record.getValues().get("name"));
		assertEquals("M", record.getValues().get("gender"));
		assertEquals("25", record.getValues().get("age"));
	}

	@Test
	void testSkipBlankFields() throws Exception {
		// GIVEN a schema with 3 fixed length fields - name, gender, age
		// AND a parser program to initialized the schema
		List<SchemaField> schema = List.of(
				new SchemaField("name", 1, 20),
				new SchemaField("gender", 21, 21),
				new SchemaField("age", 22, 25)
		);
		
		FixedLengthParser parser = new FixedLengthParser(schema);

		// WHEN a valid fixed length line with certain blank field(s) is passed into the parser program
		Record record = parser.parseLine("Jane Smith               ");

		// THEN parser program should not return any record as the line is skipped
		assertNull(record);
	}

	@Test
	void testSkipShortLine() throws Exception {
		// GIVEN a schema with 3 fixed length fields - name, gender, age
		// AND a parser program to initialized the schema
		List<SchemaField> schema = List.of(
				new SchemaField("name", 1, 20),
				new SchemaField("gender", 21, 21),
				new SchemaField("age", 22, 25)
		);
		
		FixedLengthParser parser = new FixedLengthParser(schema);

		// WHEN a line shorter than the expected schema length is passed into the parser program
		Record record = parser.parseLine("John Doe     M 25");

		// THEN parser program should not return any record as the line is skipped
		assertNull(record);
	}

	@Test
	void testDynamicFieldsWithoutCodeChange() throws Exception {
		// GIVEN a extended schema with 4 fixed length fields - name, gender, age, phone
		// AND a parser program to initialized the schema
		List<SchemaField> schema = List.of(
				new SchemaField("name", 1, 20),
				new SchemaField("gender", 21, 21),
				new SchemaField("age", 22, 25),
				new SchemaField("phone", 26, 37)
		);
		
		FixedLengthParser parser = new FixedLengthParser(schema);

		// WHEN a valid fixed length line containing the additional field is passed into the parser program
		Record record = parser.parseLine("John Doe            M 25 012-3456789 ");

		// THEN parser program should return record containing the line
		// AND all fields should be correctly extracted
		assertNotNull(record);
		assertEquals("John Doe", record.getValues().get("name"));
		assertEquals("M", record.getValues().get("gender"));
		assertEquals("25", record.getValues().get("age"));
		assertEquals("012-3456789", record.getValues().get("phone"));
	}

	@Test
	void testLargeFilePerformance() throws Exception {
		// GIVEN a schema with fixed length fields loaded from a text file
		// AND a parser program to initialized the schema
		// AND a large data file containing many valid fixed length lines
		Path schemaFile = Files.createTempFile("schema", ".txt");
		Files.writeString(schemaFile, "name 1 20\n" + "gender 21 21\n" + "age 22 25\n");

		List<SchemaField> schema = SchemaLoader.loadFromTxt(schemaFile.toString());
		FixedLengthParser parser = new FixedLengthParser(schema);

		Path dataFile = Files.createTempFile("file", ".txt");
		final int LINE_COUNT = 200000;

		try (var writer = Files.newBufferedWriter(dataFile)) {
			for (int i = 0; i < LINE_COUNT; i++) {
				writer.write("John Doe            M 25 \n");
			}
		}

		// WHEN the large file is passed into the parser program
		// AND the start and elapsed time is recorded
		long startTime = System.currentTimeMillis();
		List<Record> records = parser.parseFile(dataFile.toString());
		long elapsedTime = System.currentTimeMillis() - startTime;

		// THEN parser program should return record containing all the lines
		// AND the parsing should complete within an acceptable time limit
		assertEquals(LINE_COUNT, records.size());
		assertTrue(elapsedTime < 5000, "Parser took too long");
	}
}
