package diablohud;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

import javax.swing.UIManager;

import d2s.GameData;

/** Program entry point */
public class Main {
	public static void main(String[] args) throws IOException {
		try {
			String theme = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(theme);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Loading data...");
		GameData gd = new GameData();
		
		gd.addStringData(readLines("data/string.txt"));
		gd.addStringData(readLines("data/expansionstring.txt"));
		gd.addStringData(readLines("data/patchstring.txt"));
		
		gd.addMiscData(readLines("data/Misc.txt"));
		gd.addArmorData(readLines("data/Armor.txt"));
		gd.addWeaponData(readLines("data/Weapons.txt"));
		gd.addModData(readLines("data/ItemStatCost.txt"));
		
		System.out.println("Starting screen");
		
		Screen screen = new Screen(gd);
		screen.setVisible(true);
	}
	
	
	private static String[] readLines(String name) throws IOException {
		File file = new File(name);
		List<String> lines = Files.readAllLines(file.toPath());
		return lines.toArray(new String[lines.size()]);
	}
	
	
	/**
	public static void main(String[] args) throws IOException {
		if (args.length <= 0) {
			System.err.println("USAGE: d2info <file>");
			System.exit(1);
			return;
		}
		
		File file = new File(args[0]);
		byte[] data = Files.readAllBytes(file.toPath());
		ByteBuffer buffer = ByteBuffer.wrap(data);
		GameData gd = new GameData();
		
		gd.addStringData(readLines("data/string.txt"));
		gd.addStringData(readLines("data/expansionstring.txt"));
		gd.addStringData(readLines("data/patchstring.txt"));
		
		gd.addMiscData(readLines("data/Misc.txt"));
		gd.addArmorData(readLines("data/Armor.txt"));
		gd.addWeaponData(readLines("data/Weapons.txt"));
		gd.addModData(readLines("data/ItemStatCost.txt"));
		
		Hero hero = new Hero(buffer);
		
		if (!hero.checkHeader()) {
			error("Unknown header probably not a .d2s file");
			return;
		}
		
		System.out.printf("Version: %d\n", hero.getVersion());
		System.out.printf("Name: %s\n", hero.getName());
		System.out.printf("Level: %d\n", hero.getLevel());
		System.out.printf("Class: %s\n", hero.getType());
		
		List<Item> items = hero.getItems(gd);
		
		System.out.printf("Item Count: %d\n", items.size());
	}
	
	private static String[] readLines(String name) throws IOException {
		File file = new File(name);
		List<String> lines = Files.readAllLines(file.toPath());
		return lines.toArray(new String[lines.size()]);
	}
	
	private static void error(String msg) {
		System.err.printf("ERROR: %s\n", msg);
		System.exit(1);
	}
	*/
}
