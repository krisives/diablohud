package d2s;

import java.util.*;

public class Item extends BitBuffer {
	/** Game data used for mods and item types */
	public final GameData gd;

	/** Location of this item in the save file */
	public final int byteOffset;

	/** Size of this item in bits */
	public final int size;

	// Cache
	private String type;
	private int quality = -1;

	// Read while parsing variable length structure
	protected boolean trinket;
	protected int customGfx;
	protected boolean classSpecific;
	protected int classBits;
	protected int qualityBits;
	protected int specialBits;
	protected int durMin, durMax;
	protected int stackBits;
	protected int socketCount;
	protected List<Mod.Value> mods;

	/** Create an item from save file data */
	public Item(GameData gd, BitBuffer saveFile, int byteOffset) {
		super(saveFile, byteOffset);

		this.gd = gd;
		this.byteOffset = byteOffset;
		this.size = parse();

		System.out.printf("Item: %s\n", getType());
	}
	
	public List<Mod.Value> getMods() {
		if (mods == null) {
			return Collections.emptyList();
		}
		
		return mods;
	}

	/** Get the number of bits used to store the item in the save file */
	public int getItemBits() {
		return size;
	}

	/** Get the number of bytes used to store the item in the save file */
	public int getItemBytes() {
		return align(size);
	}

	/** Check if the item is a stack */
	public boolean isStack() {
		// TODO
		return false;
	}

	/** Check if the item has a defense rating */
	public boolean hasDefense() {
		return gd.isArmor(getType());
	}

	/** Check if the item has a durability rating */
	public boolean hasDurability() {
		return gd.isArmor(getType()) || gd.isWeapon(getType());
	}
	
	/** Get the minimum durability of this item */
	public int getDurabilityMin() {
		return durMin;
	}
	
	/** Get the maximum durability of this item */
	public int getDurabilityMax() {
		return durMax;
	}

	/** Check if an item is socketed */
	public boolean isSocketed() {
		return getBit(27);
	}

	/** Get the number of sockets on the item */
	public int getSocketCount() {
		return socketCount;
	}

	/** Check if the item is an player ear dropped in PvP */
	public boolean isEar() {
		return getBit(32);
	}

	/** Get the name of the player for this ear item */
	public String getEarName() {
		throw new IllegalArgumentException("TODO");
	}

	/** Check if this item is a simple 14-byte item with no extra bits */
	public boolean isSimple() {
		return getBit(37);
	}

	/** Check if this item has a runeword inserted into it */
	public boolean isRuneword() {
		return getBit(42);
	}

	/** Check if this item is equipped to the character currently */
	public boolean isEquipped() {
		return getBit(59);
	}

	public boolean isPersonalized() {
		return getBit(40);
	}

	/** Get the quality of the item (magic, rare, etc.) */
	public int getQuality() {
		if (quality == -1) {
			quality = getBitInt(150, 4);
		}

		return quality;
	}

	/** Get the extra bits for low/high quality items */
	public int getQualityBits() {
		return qualityBits;
	}

	/** Check if an item is low quality (cracked, damaged, etc.) */
	public boolean isLowQuality() {
		return getQuality() == 1;
	}

	/** Check if an item is normal quality (white) */
	public boolean isNormalQuality() {
		return getQuality() == 2;
	}

	/** Check if item is superior */
	public boolean isHighQuality() {
		return getQuality() == 3;
	}

	/** Check if item is magical (blue) */
	public boolean isMagic() {
		return getQuality() == 4;
	}

	/** Check if item is part of a set (green) */
	public boolean isSet() {
		return getQuality() == 5;
	}

	/** Check if item is rare (yellow) */
	public boolean isRare() {
		return getQuality() == 6;
	}

	/** Check if item is crafted from a recipe (orange) */
	public boolean isCrafted() {
		return getQuality() == 7;
	}

	/** Check if item is unique (brown) */
	public boolean isUnique() {
		return getQuality() == 8;
	}

	/** Get bits for magic, rare, etc. items */
	public int getSpecialBits() {
		return specialBits;
	}

	/** Get the 3 character code for this item type */
	public String getType() {
		if (type != null) {
			return type;
		}

		return type = codeToString(getInt(9) >> 4);
	}

	/** Check if the item has custom graphics */
	public boolean hasCustomGfx() {
		return trinket;
	}

	/** Get alternate graphic artwork ID */
	public int getCustomGfx() {
		return customGfx;
	}

	/** Check if item is class specific (barb helm, etc.) */
	public boolean isClassSpecific() {
		return classSpecific;
	}
	
	/** Get bits for describing class specific properties? */
	public int getClassSpecificBits() {
		return classBits;
	}

	public int getStackBits() {
		return stackBits;
	}

	/** Parse the item to calculate it's size and save key bits */
	protected int parse() {
		int bits = 0;

		if ((getShort(0)) != 0x4d4a) {
			throw new IllegalArgumentException("Item without 'JM' header at offset " + byteOffset);
		}

		if (isSimple()) {
			return 108;
		}

		bits = parseHeader(bits);
		bits = parseCustomGraphics(bits);
		bits = parseClassSpecific(bits);
		bits = parseLowQuality(bits);
		bits = parseHighQuality(bits);
		bits = parseMagic(bits);
		bits = parseSet(bits);
		bits = parseRare(bits);
		bits = parseUnique(bits);
		bits = parseCrafted(bits);
		bits = parseRuneword(bits);
		bits = parsePersonalization(bits);
		bits = parseGuid(bits);
		bits = parseDefense(bits);
		bits = parseDurability(bits);
		bits = parseSocketCount(bits);
		bits = parseStack(bits);
		bits = parseMods(bits, 511);

		System.out.printf("Item is %d bits\n", bits);
		return bits;
	}

	protected int parseHeader(int n) {
		n = 108; // start after fixed header
		n += 3; // skip socket item count
		n += 32 + 7 + 4; // skip extended header
		return n;
	}

	protected int parseCustomGraphics(int n) {
		trinket = getBit(n++);

		if (trinket) {
			System.out.println("item is trinket");
			customGfx = getBitInt(n, 3);
			n += 3;
		}

		return n;
	}

	protected int parseClassSpecific(int n) {
		classSpecific = getBit(n++);

		if (classSpecific) {
			System.out.println("Item is class specific");
			classBits = getBitInt(n, 11);
			n += 11;
		}

		return n;
	}

	protected int parseLowQuality(int n) {
		if (isLowQuality()) {
			System.out.println("Item is low quality");
			qualityBits = getBitInt(n, 3);
			n += 3;
		}

		return n;
	}

	protected int parseHighQuality(int n) {
		if (isHighQuality()) {
			System.out.println("Item is high quality");
			qualityBits = getBitInt(n, 3);
			n += 3;
		}

		return n;
	}

	protected int parseMagic(int n) {
		if (isMagic()) {
			System.out.println("Item is magical");
			specialBits = getBitInt(n, 22);
			n += 22;
		}

		return n;
	}

	protected int parseSet(int n) {
		if (isSet()) {
			System.out.println("Item is part of a set");
			specialBits = getBitInt(n, 12);
			n += 12;
		}

		return n;
	}

	/** Parse rare item bits */
	protected int parseRare(int n) {
		if (isRare() == false) {
			return n;
		}

		System.out.println("Item is rare");
		specialBits = getBitInt(n, 16);
		n += 16;

		for (int i = 0; i < 6; i++) {
			if (getBit(n++)) {
				n += 11;
			}
		}

		return n;
	}

	/** Parse unique item bits */
	protected int parseUnique(int n) {
		if (isUnique()) {
			System.out.println("Item is unique");
			specialBits = getBitInt(n, 12);
			n += 12;
		}

		return n;
	}

	/** Parse crafted item bits */
	protected int parseCrafted(int n) {
		if (isCrafted()) {
			System.out.println("Item is crafted");
			specialBits = getBitInt(n, 16);
			n += 16;

			for (int i = 0; i < 6; i++) {
				if (getBit(n++)) {
					n += 11;
				}
			}
		}

		return n;
	}

	/** Parse runword item bits */
	protected int parseRuneword(int n) {
		if (isRuneword()) {
			System.out.println("Item is runeword");
			specialBits = getBitInt(n, 16);
			n += 16;
		}

		return n;
	}

	/** Parse personalized items from Act 5 */
	protected int parsePersonalization(int n) {
		if (isPersonalized() == false) {
			return n;
		}

		throw new IllegalStateException("TODO");
	}

	/** ? */
	protected int parseGuid(int n) {
		boolean guid = getBit(n++);

		if (guid) {
			System.out.println("Item has GUID");
			n += 3;
		}

		return n;
	}

	/** Parse defense rating */
	protected int parseDefense(int n) {
		if (hasDefense()) {
			System.out.println("Item has defense");
			n += 11;
		}

		return n;
	}

	/** Parse min/max durability */
	protected int parseDurability(int n) {
		if (hasDurability() == false) {
			return n;
		}

		System.out.println("Item has durability");

		durMax = getBitInt(n, 8);
		n += 8;

		if (durMax > 0) {
			durMin = getBitInt(n, 9);
			n += 9;
		}

		return n;
	}

	/** Parse bits for how many sockets item supports */
	protected int parseSocketCount(int n) {
		if (isSocketed()) {
			socketCount = getBitInt(n, 4);
			n += 4;
		}

		return n;
	}

	/** Parse bits related to item stacking */
	protected int parseStack(int n) {
		if (isStack()) {
			stackBits = getBitInt(n, 9);
			n += 9;
		}

		return n;
	}

	/** Parse mods until a 9-bit value is seen */
	protected int parseMods(int n, int exitValue) {
		mods = new ArrayList<>();
		
		for (int i = 0; i < 255; i++) {
			int id = getBitInt(n, 9);
			n += 9;

			if (id == exitValue) {
				break;
			}

			Mod mod = gd.getMod(id);

			if (mod == null) {
				throw new IllegalStateException("Unknown mod " + id + " at bit " + n);
			}
			
			Mod.Value value = new Mod.Value(mod);
			value.x = getBitInt(n, mod.bits);
			mods.add(value);

			System.out.printf("mod %d %s\n", mod.id, mod.pattern);
			n += mod.bits;
		}

		return n;
	}

	/** Convert a 24-bit integer to a 3 char String */
	public static final String codeToString(int i) {
		byte a = (byte) ((i >> 0) & 0xFF);
		byte b = (byte) ((i >> 8) & 0xFF);
		byte c = (byte) ((i >> 16) & 0x7F);
		return new String(new byte[] { a, b, c });
	}
}
