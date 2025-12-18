package com.example.Interview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixedLengthParser {

	private final Logger logger;
	private final List<SchemaField> fields;

	public FixedLengthParser(List<SchemaField> fields) {
		this.logger = LoggerFactory.getLogger(getClass());
		this.fields = fields;
	}

	private String extractField(String line, int start, int end) {
		return line.substring(start - 1, end).trim();
	}

	public Record parseLine(String line) {
		try {
			Map<String, String> recordValues = new LinkedHashMap<>();

			for (SchemaField field : fields) {
				String value = "";

				if (line.length() < field.end) {
					// log file format issues
					logger.error("Skipping line (too short): {}", line);
					return null;
				} else {
					value = extractField(line, field.start, field.end);
				}
				recordValues.put(field.name, value);
			}

			for (Map.Entry<String, String> entry : recordValues.entrySet()) {
				if (entry.getValue() == null || entry.getValue().isBlank()) {
					// log file format issues
					logger.error("Skipping line (blank field) [{}] : {}", entry.getKey(), line);
					return null;
				}
			}

			return new Record(recordValues);
		} catch (Exception e) {
			logger.error("Error parsing line: {}", line, e);
			return null;
		}
	}

	public List<Record> parseByLines(java.util.stream.Stream<String> lines) {
		return lines.map(this::parseLine).filter(e -> e != null).toList();
	}

	public List<Record> parseFile(String filePath) throws IOException {
		Path pathFileToRead = Paths.get(filePath);

		try (var lines = Files.lines(pathFileToRead)) {
			return parseByLines(lines);
		}
	}
}
