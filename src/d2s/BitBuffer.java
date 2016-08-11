package d2s;

import java.nio.*;

public class BitBuffer {
	private final ByteBuffer data;
		
	public BitBuffer(ByteBuffer buffer) {
		data = buffer;
		data.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public BitBuffer(BitBuffer copy) {
		data = copy.data.duplicate();
		data.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public BitBuffer(BitBuffer copy, int byteOffset) {
		ByteBuffer b = copy.data.duplicate();
		b.position(byteOffset);
		data = b.slice();
		data.order(ByteOrder.LITTLE_ENDIAN);
	}
	
	public int getByteSize() {
		return data.capacity();
	}
	
	public int getBitSize() {
		return data.capacity() * 8;
	}
	
	/** Check if bit N is set */
	public boolean getBit(int absolute) {
		int index = absolute / 8;
		int relative = absolute - (index * 8);
		int mask = 1 << relative;
		return (getByte(index) & mask) == mask;
	}
	
	/** Read a N-bit integer at an arbitrary bit offset */
	public int getBitInt(int offset, int size) {
		int value = 0;
		
		for (int i=0; i < size; i++) {
			value |= getBit(offset + i) ? (1 << i) : 0;
		}
		
		return value;
	}
	
	/** Read a byte-aligned 8-bit integer */
	public int getByte(int byteOffset) {
		return data.get(byteOffset) & 0xFF;
	}
	
	/** Read a byte-aligned 16-bit integer */
	public int getShort(int byteOffset) {
		return data.getShort(byteOffset) & 0xFFFF;
	}
	
	/** Read a byte-aligned 32-bit integer */
	public int getInt(int byteOffset) {
		return data.getInt(byteOffset);
	}
	
	/** Calculate how many bytes are needed to store N bits */
	public static int align(int bit) {
		return ((bit % 8) == 0) ? (bit / 8) : (1 + (bit / 8));
	}
}
