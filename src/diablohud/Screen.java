package diablohud;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import d2s.*;

public class Screen extends JFrame {
	public final GameData gd;
	private final Canvas canvas;
	private final Menu menu;
	private Color screenColor;
	private Color textColor;
	private Path heroPath;
	private Hero hero;
	private Config config;
	private ArrayList<Mod.Value> mods = new ArrayList<>();
	//private ArrayList<Item> items = new ArrayList<>();
	private Font font;
	
	public Screen(GameData gd) {
		super("DiabloHud");
		this.gd = gd;
		this.canvas = new Canvas();
		this.menu = new Menu();
		this.config = new Config();
		
		setIconImage(new ImageIcon("icon.png").getImage());
		
		try {
			File configFile = new File("config.txt");
			
			if (configFile.exists()) {
				config.load(configFile);
			}
			
			font = Font.createFont(Font.TRUETYPE_FONT, new File("mitr.ttf"));
			font = font.deriveFont(Font.PLAIN, 13);
		} catch (Exception e) {
			e.printStackTrace();
		}

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(config.getInt("width", 200), config.getInt("height", 200));
		setContentPane(canvas);
		setJMenuBar(menu);
		
		loadColors();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				saveConfig();
			}
		});
	}
	
	private void loadColors() {
		screenColor = config.getColor("screenColor", Color.BLACK);
		textColor = config.getColor("textColor", new Color(155, 155, 200));
	}
	
	public void saveConfig() {
		File configFile = new File("config.txt");
		
		config.set("width", getWidth());
		config.set("height", getHeight());
		config.set("screenColor", screenColor);
		config.set("textColor", textColor);
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void prepareGraphics(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private class Canvas extends JComponent {
		@Override
		protected void paintComponent(Graphics g) {
			if (g instanceof Graphics2D) {
				prepareGraphics((Graphics2D)g);
			}
			
			g.setColor(screenColor);
			g.fillRect(0, 0, getWidth(), getHeight());
			
			g.setFont(font);
			g.setColor(textColor);
			
			int x = 3;
			int y = 15;
			
			mods.sort(new Comparator<Mod.Value>() {
				@Override
				public int compare(Mod.Value a, Mod.Value b) {
					return a.mod.priority - b.mod.priority;
				}
			});
			
			for (Mod.Value value : mods) {
				String text;
				
				text = String.valueOf(value.x - value.mod.add);
				text += " " + value.mod.pattern;
				
				g.drawString(text, x, y);
				y += 20;
			}
		}
	}

	public void openFile() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileNameExtensionFilter("Diablo II Save Files", "d2s"));
		String last = config.get("last");
		
		if (last.isEmpty() == false) {
			chooser.setCurrentDirectory(new File(last).getParentFile());
		}
		
		switch (chooser.showOpenDialog(this)) {
		case JFileChooser.APPROVE_OPTION:
			break;
		default:
			return;
		}
		
		File file = chooser.getSelectedFile();
		heroPath = file.toPath();
		config.set("last", heroPath.toAbsolutePath().toString());
		
		readHero();
	}
	
	private void readHero() {
		mods = new ArrayList<>();
		
		try {
			hero = new Hero(readFile(heroPath));
			
			for (Item item : hero.getItems(gd)) {
				mods.addAll(item.getMods());
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
		}
		
		repaint();
	}
	
	private ByteBuffer readFile(Path path) throws IOException {
		FileChannel channel = FileChannel.open(path);
		ByteBuffer buffer = ByteBuffer.allocate((int)channel.size());
		int remaining = buffer.capacity();
		
		while (remaining > 0) {
			int count = channel.read(buffer);
			
			if (count == -1) {
				throw new IOException("File ended before expected size");
			}
			
			remaining -= count;
		}
		
		buffer.clear();
		return buffer;
	}
	
	public void quit() {
		setVisible(false);
		dispose();
	}
	
	public void selectBg() {
		screenColor = JColorChooser.showDialog(this, "Screen Color", screenColor);
		repaint();
	}
	
	public void selectFg() {
		textColor = JColorChooser.showDialog(this, "TextColor", textColor);
		repaint();
	}
	
	public void addTextWidget() {
		
	}
	
	public void addMods() {
		
	}
	
	public void addArmor() {
		
	}
	
	public void addWeapon() {
		
	}
	
	private void resetColors() {
		config.remove("screenColor");
		config.remove("textColor");
		loadColors();
		repaint();
	}

	private class Menu extends JMenuBar {
		private JMenu file, view;

		public Menu() {
			file = new JMenu("File");
			add(file, "Open", (e) -> { openFile(); });
			file.addSeparator();
			add(file, "Quit", (e) -> { quit(); });
			add(file);
			
			view = new JMenu("View");
			add(view, "Set Background", (e) -> { selectBg(); });
			add(view, "Set Text", (e) -> { selectFg(); });
			view.addSeparator();
			add(view, "Reset Colors", (e) -> { resetColors(); });
			add(view);
		}

		private JMenuItem add(JMenu menu, String name, ActionListener a) {
			return menu.add(new AbstractAction(name) {
				@Override
				public void actionPerformed(ActionEvent e) {
					a.actionPerformed(e);
				}
			});
		}
	}
}
