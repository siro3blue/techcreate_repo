package com.example.Interview;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SchemaLoader {
	public static List<SchemaField> loadFromTxt(String schemaPath) {
		try {
			Path path = Paths.get(schemaPath);

			List<String> lines = Files.readAllLines(path);
			return parseLines(lines);
		} catch (IOException e) {
			throw new RuntimeException("Failed to load schema from: " + schemaPath, e);
		}
	}

	static List<SchemaField> parseLines(Iterable<String> lines) {
		List<SchemaField> fields = new ArrayList<>();

		for (String line : lines) {
			String[] parts = line.split(" ");

			String name = parts[0];
			int start = Integer.parseInt(parts[1]);
			int end = Integer.parseInt(parts[2]);

			fields.add(new SchemaField(name, start, end));
		}

		return fields;
	}
}
