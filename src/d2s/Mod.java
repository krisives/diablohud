package d2s;

public class Mod implements Comparable<Mod> {
	public final int id;
	public int bits;
	public int add;
	public String pattern;
	public int priority;
	
	public Mod(int id) {
		this.id = id;
	}
	
	@Override
	public int compareTo(Mod o) {
		return o.id - id;
	}
	
	public static class Value {
		public final Mod mod;
		public int x, y, z;
		
		public Value(Mod mod) {
			this.mod = mod;
		}
	}
}