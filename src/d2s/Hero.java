package d2s;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Hero extends BitBuffer {
	public Hero(ByteBuffer data) {
		super(data);
	}
	
	public boolean checkHeader() {
		return getInt(0) == 0xaa55aa55;
	}
	
	public int getVersion() {
		return getInt(4);
	}
	
	public String getName() {
		StringBuilder s = new StringBuilder();
		
		for (int i=0; i < 16; i++) {
			int c = getByte(20 + i);
			
			if (c == 0) {
				break;
			}
			
			s.append((char)c);
		}
		
		return s.toString();
	}
	
	public String getType() {
		switch (getByte(40)) {
		case 0: return "amazon";
		case 1: return "sorceress";
		case 2: return "necromancer";
		case 3: return "paladin";
		case 4: return "barbarian";
		case 5: return "druid";
		case 6: return "assassin";
		}
		
		throw new IllegalStateException("Unknown hero class");
	}
	
	public int getLevel() {
		return getByte(5);
	}
	
	public int measureStatsHeader() {
		if (getShort(765) !=  0x6667) {
			throw new IllegalStateException("Expected 'gf' header");
		}
		
		int bits = 0;
		int offset = 767 * 8;
		
		for (int i=0; i < 16; i++) {
			int id = getBitInt(offset + bits, 9);
			bits += 9;
			
			if (id == 0b111111111) {
				break;
			}
			
			bits += getStatSize(id);
		}
		
		System.out.printf("Stat bits: %d\n", bits);
		
		return isAligned(bits) ? (bits / 8) : (1 + (bits / 8));
	}
	
	
	public Iterable<Item> getItems(GameData gd) {
		int itemCount, offset;
		ArrayList<Item> items;
		
		offset = 767 + measureStatsHeader() + 32;
		items = new ArrayList<>();
		
		System.out.printf("offset=%d\n", offset);
		System.out.printf("header = %x %x\n", getByte(offset), getByte(offset+1));
		System.out.printf("short = %04x\n", getShort(offset));
		
		do {
			System.out.printf("offset = %d\n", offset);
			if (getShort(offset) != 0x4d4a) {
				throw new IllegalArgumentException("Expected 'JM' header for item list at offset " + offset);
			}
			
			offset += 2;
			itemCount = getShort(offset);
			offset += 2;
			
			System.out.printf("itemCount = %d\n", itemCount);
			
			for (int i=0; i < itemCount; i++) {
				Item item = new Item(gd, this, offset);
				offset += item.getItemBytes();
				items.add(item);
			}
		} while (itemCount > 0);
		
		return items;
	}

	private static boolean isAligned(int x) {
		return (x % 8) == 0;
	}
	
	public static String getStatLabel(int id) {
		switch (id) {
		case 0: return "strength";
		case 1: return "energy";
		case 2: return "dexterity";
		case 3: return "vitality";
		case 4: return "statpts";
		case 5: return "newskills";
		case 6: return "hitpoints";
		case 7: return "maxhp";
		case 8: return "mana";
		case 9: return "maxmana";
		case 10: return "stamina";
		case 11: return "maxstamina";
		case 12: return "level";
		case 13: return "experience";
		case 14: return "gold";
		case 15: return "goldbank";
		}
		
		throw new IllegalStateException("Unknown stat type");
	}
	
	public static int getStatSize(int id) {
		switch (id) {
		case 0: return 10;
		case 1: return 10;
		case 2: return 10;
		case 3: return 10;
		case 4: return 10;
		case 5: return 8;
		case 6: return 21;
		case 7: return 21;
		case 8: return 21;
		case 9: return 21;
		case 10: return 21;
		case 11: return 21;
		case 12: return 7;
		case 13: return 32;
		case 14: return 25;
		case 15: return 25;
		}
		
		throw new IllegalStateException("Unknown stat type " + id);
	}
}
