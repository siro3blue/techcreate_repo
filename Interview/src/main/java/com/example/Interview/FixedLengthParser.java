package com.example.Interview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedLengthParser {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private List<SchemaField> fields;

	public FixedLengthParser(String schemaPath) {
		loadSchema(schemaPath);
	}

	private void loadSchema(String schemaPath) {
		try {
			fields = new ArrayList<>();

			List<String> lines = Files.readAllLines(Paths.get(schemaPath));

			for (String line : lines) {
				String[] parts = line.split(" ");

				String name = parts[0];
				int start = Integer.parseInt(parts[1]);
				int end = Integer.parseInt(parts[2]);

				fields.add(new SchemaField(name, start, end));
			}
		} catch (Exception e) {
			throw new RuntimeException("Fail to load schema : {}", e);
		}
	}

	private Record parseLine(String line) {
		try {
			Map<String, String> recordValues = new LinkedHashMap<>();

			for (SchemaField field : fields) {
				String value = "";

				if (line.length() < field.end) {
					// log file format issues
					logger.error("Skipping line (too short): {}", line);
					return null;
				} else {
					value = line.substring(field.start - 1, field.end).trim();
				}
				recordValues.put(field.name, value);
			}

			if (recordValues.values().stream().anyMatch(String::isBlank)) {
				// log file format issues
				logger.error("Skipping line (blank field): {}", line);
				return null;
			}

			return new Record(recordValues);
		} catch (Exception e) {
			logger.error("Error parsing line: {}", line, e);
			return null;
		}
	}

	public List<Record> parseFile(String filePath) throws IOException {
		Path pathFileToRead = Paths.get(filePath);

		try (var lines = Files.lines(pathFileToRead)) {
			return lines.map(this::parseLine).filter(e -> e != null).toList();
		}
	}

	public static void main(String[] args) {
		String schemaPath = "C:/test/schema.txt";
		String filePath = "C:/test/file.txt";

		FixedLengthParser parser = new FixedLengthParser(schemaPath);

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
