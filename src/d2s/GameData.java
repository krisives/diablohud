package d2s;

import java.util.*;

public class GameData {
	private final Set<String> all;
	private final Set<String> misc;
	private final Set<String> weapons, armors;
	private final Map<Integer, Mod> mods;
	private final Map<String, String> trans;
	
	public GameData() {
		all = new HashSet<>();
		misc = new HashSet<>();
		weapons = new HashSet<>();
		armors = new HashSet<>();
		mods = new HashMap<>();
		trans = new HashMap<>();
	}
	
	/** Get an item modifier by (9-bit) identifier */
	public Mod getMod(int id) {
		return mods.get(id);
	}
	
	/** Ensure a string is a 3-char item code */
	public void checkCode(String code) {
		if (code.length() == 3) {
			return;
		}
		
		throw new IllegalArgumentException("Expected 3 digit code not '" + code + "'");
	}
	
	/** Check if an item code exists in the database */
	public boolean isItem(String code) {
		checkCode(code);
		return all.contains(code);
	}
	
	/** Check if a given item code (which must exist) is armor */
	public boolean isArmor(String code) {
		if (!isItem(code)) {
			throw new IllegalArgumentException("Unknown item code " + code);
		}
		
		return armors.contains(code);
	}
	
	/** Load data from Armor.txt */
	public void addArmorData(String[] lines) {
		addData(armors, lines);
	}
	
	/** Check if a given item code (which must exist) is weapon */
	public boolean isWeapon(String code) {
		if (!isItem(code)) {
			throw new IllegalArgumentException("Unknown item code " + code);
		}
		
		return weapons.contains(code);
	}
	
	/** Load data from Weapon.txt */
	public void addWeaponData(String[] lines) {
		addData(weapons, lines);
	}
	
	/** Add data from Misc.txt */
	public void addMiscData(String[] lines) {
		addData(misc, lines);
	}
	
	/** Parse a list looking for "code" to populate a set */
	private void addData(Set<String> set, String[] lines) {
		String[] header = lines[0].split("\t");
		int codeIndex = find(header, "code");
		
		for (int i=1; i < lines.length; i++) {
			String[] parts = lines[i].split("\t");
			
			if (codeIndex > parts.length) {
				//System.out.printf("Skipping %s", lines[i]);
				continue;
			}
			
			String code = parts[codeIndex];
			
			if (set.add(code) == false) {
				throw new IllegalArgumentException("Item with code " + code + " already exists");
			}
			
			all.add(code);
		}
	}
	
	/** Load data from ItemStatCost.txt */
	public void addModData(String[] lines) {
		String[] header = lines[0].split("\t");
		int nameHeader = find(header, "stat");
		int idHeader = find(header, "id");
		int saveBitsHeader = find(header, "save bits");
		int saveAddHeader = find(header, "save add");
		int saveParamHeader = find(header, "save param bits");
		int descHeader = find(header, "descstrpos");
		int priorityHeader = find(header, "descpriority");
		boolean collapse = false;
		Mod mod = null;
		String[] prev = null;
		
		for (int i=1; i < lines.length; i++) {
			String[] parts = lines[i].split("\t");
			String name = parts[nameHeader];
			int id = parseInt(parts[idHeader]);
			int saveBits = parseInt(parts[saveBitsHeader]);
			int saveAdd = parseInt(parts[saveAddHeader]);
			int saveParam = parseInt(parts[saveParamHeader]);
			int totalBits = saveBits + saveParam;
			
			if (collapse) {
				mod.bits += totalBits;
				collapse = false;
				
			}
			
			if (totalBits <= 0) {
				continue;
			}
			
			String pattern = translate(parts[descHeader]);
			
			mod = new Mod(id);
			mod.bits = totalBits;
			mod.add = saveAdd;
			mod.pattern = pattern;
			mod.priority = parseInt(parts[priorityHeader]);
			mods.put(id, mod);
			
			if (name.endsWith("mindam")) {
				collapse = true;
			}
			
			prev = parts;
		}
	}
	
	/** Convert a string to an int allowing empty strings */
	private static int parseInt(String s) {
		return s.isEmpty() ? 0 : Integer.parseInt(s);
	}
	
	/** Translate a string from the lookup table */
	public String translate(String str) {
		if (str.isEmpty()) {
			return str;
		}
		
		if (!trans.containsKey(str)) {
			System.out.printf("WARNING: unknown string '%s'\n", str);
			return str;
		}
		
		return trans.get(str);
	}
	
	/** Add data from strings.txt or expansionstrings.txt */
	public void addStringData(String[] lines) {
		for (String line : lines) {
			String[] parts = line.split("\t");
			
			if (parts.length != 2) {
				//System.out.printf("WARNING: skipping string line: %s\n", line);
				continue;
			}
			
			trans.put(parts[0], parts[1]);
		}
	}
	
	/** Find a header in an array (throw if not found) */
	private static int find(String[] headers, String label) {
		label = label.toLowerCase();
		
		for (int i=0; i < headers.length; i++) {
			if (headers[i].toLowerCase().equals(label)) {
				return i;
			}
		}
		
		throw new IllegalArgumentException("Cannot find header " + label);
	}
}
