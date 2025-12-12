package com.example.Interview;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FixedLengthParserTest {

	@Test
	void testParseValidFile() throws Exception {
		Path schemaFile = Files.createTempFile("schema", ".txt");
		Files.writeString(schemaFile, "name 1 20\n" + "gender 21 21\n" + "age 22 25");

		Path dataFile = Files.createTempFile("file", ".txt");
		Files.writeString(dataFile, "John Doe            M 25 \n" + "Jane Smith          F 30 ");

		FixedLengthParser parser = new FixedLengthParser(schemaFile.toString());
		List<Record> records = parser.parseFile(dataFile.toString());

		Assertions.assertEquals(2, records.size()); //both line successfully recorded

		Record r1 = records.get(0);
		Assertions.assertEquals("John Doe", r1.getValues().get("name"));
		Assertions.assertEquals("M", r1.getValues().get("gender"));
		Assertions.assertEquals("25", r1.getValues().get("age"));
		
		Record r2 = records.get(1);
		Assertions.assertEquals("Jane Smith", r2.getValues().get("name"));
		Assertions.assertEquals("F", r2.getValues().get("gender"));
		Assertions.assertEquals("30", r2.getValues().get("age"));
	}

	@Test
	void testSkipBlankFields() throws Exception {
		Path schemaFile = Files.createTempFile("schema", ".txt");
		Files.writeString(schemaFile, "name 1 20\n" + "gender 21 21\n" + "age 22 25");

		Path dataFile = Files.createTempFile("file", ".txt");
		Files.writeString(dataFile, "John Doe            M 25 \n" + 
		                            "Jane Smith               " // having blank field and will be skipped
		);

		FixedLengthParser parser = new FixedLengthParser(schemaFile.toString());
		List<Record> records = parser.parseFile(dataFile.toString());

		Assertions.assertEquals(1, records.size()); // only 1 line successfully recorded

		Record r1 = records.get(0);
		Assertions.assertEquals("John Doe", r1.getValues().get("name"));
		Assertions.assertEquals("M", r1.getValues().get("gender"));
		Assertions.assertEquals("25", r1.getValues().get("age"));
	}
	
	@Test
	void testSkipShortLine() throws Exception {
		Path schemaFile = Files.createTempFile("schema", ".txt");
		Files.writeString(schemaFile, "name 1 20\n" + "gender 21 21\n" + "age 22 25");

		Path dataFile = Files.createTempFile("file", ".txt");
		Files.writeString(dataFile, "John Doe     M 25\n" + // short line will be skipped
		                            "Jane Smith          F 30 " 
		);

		FixedLengthParser parser = new FixedLengthParser(schemaFile.toString());
		List<Record> records = parser.parseFile(dataFile.toString());

		Assertions.assertEquals(1, records.size()); // only 1 line successfully recorded

		Record r1 = records.get(0);
		Assertions.assertEquals("Jane Smith", r1.getValues().get("name"));
		Assertions.assertEquals("F", r1.getValues().get("gender"));
		Assertions.assertEquals("30", r1.getValues().get("age"));
	}
	
	@Test
	void testDynamicFieldsWithoutCodeChange() throws Exception {
		Path schemaFile = Files.createTempFile("schema", ".txt");
		Files.writeString(schemaFile, "name 1 20\n" + 
		                              "gender 21 21\n" + 
				                      "age 22 25\n" + 
		                              "phone 26 37" //new field added into schema
		);

		Path dataFile = Files.createTempFile("file", ".txt");
		Files.writeString(dataFile, "John Doe            M 25 012-3456789 \n" + 
		                            "Jane Smith          F 30 011-2345678 "
		);

		FixedLengthParser parser = new FixedLengthParser(schemaFile.toString());
		List<Record> records = parser.parseFile(dataFile.toString());

		Assertions.assertEquals(2, records.size()); //both line successfully recorded

		Record r1 = records.get(0);
		Assertions.assertEquals("John Doe", r1.getValues().get("name"));
		Assertions.assertEquals("M", r1.getValues().get("gender"));
		Assertions.assertEquals("25", r1.getValues().get("age"));
		Assertions.assertEquals("012-3456789", r1.getValues().get("phone"));
		
		Record r2 = records.get(1);
		Assertions.assertEquals("Jane Smith", r2.getValues().get("name"));
		Assertions.assertEquals("F", r2.getValues().get("gender"));
		Assertions.assertEquals("30", r2.getValues().get("age"));
		Assertions.assertEquals("011-2345678", r2.getValues().get("phone"));
	}
	
	@Test
	void testLargeFilePerformance() throws Exception {
		Path schemaFile = Files.createTempFile("schema", ".txt");
		Files.writeString(schemaFile, "name 1 20\n" + "gender 21 21\n" + "age 22 25");

		Path dataFile = Files.createTempFile("file", ".txt");
		final int LINE_COUNT = 200000;
		try(var writer = Files.newBufferedWriter(dataFile)){
			for(int i = 0;i < LINE_COUNT;i++) {
				writer.write("John Doe            M 25 \n");	
			}
		}

		FixedLengthParser parser = new FixedLengthParser(schemaFile.toString());
		
		long startTime = System.currentTimeMillis();
		List<Record> records = parser.parseFile(dataFile.toString());
		long elapseTime = System.currentTimeMillis() - startTime;

		Assertions.assertEquals(LINE_COUNT, records.size()); //all line successfully recorded
		Assertions.assertTrue(elapseTime < 5000, "Parser took too long"); //check parser within 5 seconds
	}
}
