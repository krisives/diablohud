package diablohud;

import java.awt.Color;
import java.io.*;
import java.util.*;

public class Config {
	private final Map<String, String> values;
	
	public Config() {
		values = new HashMap<>();
	}
	
	public void remove(String key) {
		values.remove(key);
	}
	
	public Iterable<String> getKeys() {
		return values.keySet();
	}
	
	public void set(String key, String value) {
		if (value == null) {
			value = "";
		} else {
			value = value.trim();
		}
		
		if (value.isEmpty()) {
			values.remove(key);
		} else {
			values.put(key, value);
		}
	}
	
	public void set(String key, int value) {
		set(key, String.valueOf(value));
	}
	
	public void set(String key, Color c) {
		set(key, c.getRed() + " " + c.getGreen() + " " + c.getBlue());
	}
	
	public String get(String key) {
		String value = values.get(key);
		return (value == null) ? "" : value;
	}
	
	public int getInt(String key, int def) {
		String str = get(key);
		if (str.isEmpty()) { return def; }
		return Integer.parseInt(str);
	}
	
	public Color getColor(String key, Color def) {
		String str = get(key);
		
		if (str.isEmpty()) {
			return def;
		}
		
		String[] parts = str.split(" ");
		int r = Integer.parseInt(parts[0]);
		int g = Integer.parseInt(parts[1]);
		int b = Integer.parseInt(parts[2]);
		return new Color(r, g, b);
	}
	
	public void save(File file) throws IOException {
		FileWriter fileWriter = new FileWriter(file);
		BufferedWriter writer = new BufferedWriter(fileWriter);
		
		for (String key : getKeys()) {
			String line = String.format("%s %s\n", key, get(key));
			writer.write(line);
		}
		
		writer.write("\n");
		writer.flush();
		fileWriter.close();
	}
	
	public void load(File file) throws IOException {
		FileReader fileReader = new FileReader(file);
		BufferedReader reader = new BufferedReader(fileReader);
		String line;
		
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			
			if (line.isEmpty()) {
				continue;
			}
			
			String[] parts = line.split(" ", 2);
			
			if (parts.length < 2) {
				continue;
			}
			
			set(parts[0], parts[1]);
		}
		
		fileReader.close();
	}
}
