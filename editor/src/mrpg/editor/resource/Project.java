/*******************************************************************************
 * Rhythos Editor is a game editor and project management tool for making RPGs on top of the Rhythos Game system.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mrpg.editor.resource;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import mrpg.editor.MapEditor;
import mrpg.editor.TilesetViewer;
import mrpg.editor.WorkspaceBrowser;
import mrpg.script.HaxeCompiler;


public class Project extends Folder {
	private static final long serialVersionUID = -8656579697414666933L;
	public static JFileChooser folderChooser = new JFileChooser();
	static {
		folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		folderChooser.setDialogTitle("Choose Project Directory"); 
	}
	public static final String PROJECT = "project"; private static final short VERSION=1;
	private static final Icon icon = MapEditor.getIcon(PROJECT); public HaxeCompiler.Result lastCompile = null;
	private Properties properties; private String target; private ImageResource frame, font, bg; private ArrayList<String> options;
	public int tile_size;
	public ImageResource getFrame(){return frame;}
	public ImageResource getFont(){return font;}
	public ImageResource getBG(){return bg;}
	public String getTarget(){return target;}
	public static File selectProject(Component parent) throws Exception {
		if(folderChooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION){
			return folderChooser.getSelectedFile();
		} else throw new Exception();
	}
	public static Project createProject(MapEditor e) throws Exception {
		File f = selectProject(e); if(f.exists()){if(f.listFiles().length > 0) throw new Exception();}
		else if(!f.mkdirs()) throw new Exception();
		Project p = new Project(f, e); File project = new File("project");
		if(project.exists()){
			Resource.copyDir(project, f); p.read(f);
		} if(!new File(f, ".project").exists()) {
			p.target = HaxeCompiler.defaultTarget(); p.tile_size = TilesetViewer.TILE_SIZE;
			p.options = new ArrayList<String>(); p.save();
		} return p;
	}
	public static Project openProject(MapEditor e, Workspace w) throws Exception {
		File f; if(folderChooser.showOpenDialog(MapEditor.instance) == JFileChooser.APPROVE_OPTION){
			f = folderChooser.getSelectedFile(); if(!f.exists() && !f.mkdirs()) throw new Exception();
		} else throw new Exception(); return openProject(e,w,f);
	}
	public static Project openProject(MapEditor e, Workspace w, File f) throws Exception {
		File prop = new File(f.toString(),".project"); if(!prop.exists()) throw new Exception();
		for(int i=0; i<w.getProjectCount(); i++) if(w.getProject(i).getFile().equals(f)){
			JOptionPane.showMessageDialog(MapEditor.instance, "The selected project is already open!", "Cannot Open Project", JOptionPane.ERROR_MESSAGE);
			throw new Exception();
		} Project p = new Project(f, e); p.read(f); return p;
	}
	public Project(File f, MapEditor e) throws Exception {super(f, e);}
	public void init(int ts) throws Exception {
		target = HaxeCompiler.defaultTarget(); tile_size = ts;
		options = new ArrayList<String>(); save();
	}
	public boolean canDelete(){return false;}
	public void properties(){if(properties == null) properties = new Properties(this); properties.setVisible(true);}
	public void save() throws Exception {
		File f = new File(getFile().toString(),".project");
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
		try{out.writeShort(VERSION);
			out.writeUTF(target); out.writeShort(tile_size); ImageResource.write(out, frame);
			ImageResource.write(out, font); ImageResource.write(out, bg); int sz = options.size();
			out.write(sz); for(int i=0; i<sz; i++) out.writeUTF(options.get(i)); out.flush(); out.close();
		}catch(Exception e){out.close(); throw e;}
	}
	protected void read(File f) throws Exception {super.read(f); MapEditor.deferRead(this, MapEditor.DEF_PROJECT);}
	public void deferredRead(File _f) throws Exception {
		File f = new File(getFile().toString(),".project");
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
		try{if(in.readShort() != VERSION) throw new Exception();
			target = in.readUTF(); tile_size = in.readShort(); frame = ImageResource.read(in, this);
			font = ImageResource.read(in, this); bg = ImageResource.read(in, this); int sz = in.read();
			options = new ArrayList<String>(sz); for(int i=0; i<sz; i++) options.add(in.readUTF()); in.close();
		}catch(Exception e){in.close(); throw e;}
	}
	public boolean hasProperties(){return true;}
	public void contextMenu(JPopupMenu menu){
		WorkspaceBrowser browser = editor.getBrowser(); menu.add(browser.properties); menu.addSeparator();
		super.contextMenu(menu);
	}
	public Icon getIcon(){return icon;}
	
	private static Random random;
	private static <E extends Resource> long newId(HashMap<Long,E> table){
		if(random == null) random = new Random();
		long l; do{l = random.nextLong();}while(l != 0 && table.containsKey(l)); return l;
	}
	private static <E extends Resource> long setId(HashMap<Long,E> table, E r, long id) throws Exception {
		if(r == null) throw new Exception(); Resource old = table.get(id); if(r == old) return id;
		if(old != null) id = newId(table); table.put(id, r); return id;
	}
	private static <E extends Resource> void removeId(HashMap<Long,E> table, E r, long id) throws Exception {
		if(r == null) return; Resource old = table.get(id); if(r == old) table.remove(id);
	}
	private static <E extends Resource> E getById(HashMap<Long,E> table, long id) throws Exception {
		E r = table.get(id); if(r == null) throw new Exception(); else return r;
	}
	private HashMap<String,HashMap<Long,Resource>> assets = new HashMap<String,HashMap<Long,Resource>>();
	private HashMap<Long,Resource> get(String type){
		HashMap<Long,Resource> ret = assets.get(type); if(ret == null){
			ret = new HashMap<Long,Resource>(); assets.put(type, ret);
		} return ret;
	}
	public long newId(TypedResource r) {return newId(get(r.getType()));}
	public long setId(TypedResource r, long id) throws Exception {return setId(get(r.getType()), r, id);}
	public void removeId(TypedResource r, long id) throws Exception {removeId(get(r.getType()), r, id);}
	public Resource getById(String type, long id) throws Exception {return getById(get(type), id);}
	public Iterable<Resource> getResources(String type){return get(type).values();}
	public Iterable<String> getResourceTypes(){return assets.keySet();}
	public void removeType(String type) throws Exception {
		if(!assets.containsKey(type)) return;
		for(Resource r : getResources(type)){
			editor.getBrowser().removeResource(r, false);
		} assets.remove(type);
	}
	
	private static class Properties extends JDialog implements ActionListener {
		private static final long serialVersionUID = -4987880557990107307L;
		private final Project project; private final JTextField name; private final JComboBox target, tile_size;
		private final JLabel frame_thumb, font_thumb, bg_thumb; private ImageResource frame, font, bg;
		private final JTextArea options;
		private static final String SET_FRAME="set_frame", SET_FONT="set_font", SET_BG="set_bg";
		public Properties(Project p){
			//TODO: Break properties into tabs, including one custom one from the target?
			super(JOptionPane.getFrameForComponent(p.editor), "Project Properties", true); project = p;
			setResizable(false);
			Container c = getContentPane(); c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS)); JPanel settings = new JPanel();
			settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS)); settings.setBorder(BorderFactory.createRaisedBevelBorder());
			JPanel inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Name"));
			name = new JTextField(project.getName(), 20); name.setActionCommand(MapEditor.OK); name.addActionListener(this);
			inner.add(name);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Target"));
			target = new JComboBox(); inner.add(target);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Tile Size:"));
			tile_size = new JComboBox(new String[]{"16","32","64"}); tile_size.setEditable(true); inner.add(tile_size);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Frame"));
			frame_thumb = new JLabel(new ImageIcon());
			JScrollPane pane = new JScrollPane(frame_thumb); pane.setPreferredSize(new Dimension(150,32));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); JPanel inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			JButton set = new JButton("Set"); set.setActionCommand(SET_FRAME); set.addActionListener(this); inner2.add(set);
			inner.add(inner2);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Font"));
			font_thumb = new JLabel(new ImageIcon());
			pane = new JScrollPane(font_thumb); pane.setPreferredSize(new Dimension(150,32));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			set = new JButton("Set"); set.setActionCommand(SET_FONT); set.addActionListener(this); inner2.add(set);
			inner.add(inner2);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("BG"));
			bg_thumb = new JLabel(new ImageIcon());
			pane = new JScrollPane(bg_thumb); pane.setPreferredSize(new Dimension(150,115));
			pane.setBorder(BorderFactory.createLoweredBevelBorder()); inner.add(pane); inner2 = new JPanel();
			inner2.setLayout(new BoxLayout(inner2, BoxLayout.Y_AXIS));
			set = new JButton("Set"); set.setActionCommand(SET_BG); set.addActionListener(this); inner2.add(set);
			inner.add(inner2);
			settings.add(inner);
			inner = new JPanel(); inner.setBorder(BorderFactory.createTitledBorder("Options"));
			options = new JTextArea(5,20); inner.add(new JScrollPane(options));
			settings.add(inner);
			c.add(settings);
			inner = new JPanel();
			JButton b = new JButton("Ok"); b.setActionCommand(MapEditor.OK); b.addActionListener(this); inner.add(b);
			b = new JButton("Cancel"); b.setActionCommand(MapEditor.CANCEL); b.addActionListener(this); inner.add(b);
			c.add(inner);
			pack();
		}
		public void setVisible(boolean b){
			if(b == true){
				name.setText(project.getName()); name.requestFocus(); name.selectAll();
				target.setModel(new DefaultComboBoxModel(HaxeCompiler.TARGET_NAMES));
				target.setSelectedIndex(Arrays.asList(HaxeCompiler.TARGETS).indexOf(project.target));
				tile_size.setSelectedItem(Integer.toString(project.tile_size));
				frame = project.frame;
				if(frame == null) frame_thumb.setIcon(new ImageIcon()); else frame_thumb.setIcon(new ImageIcon(frame.getImage()));
				font = project.font;
				if(font == null) font_thumb.setIcon(new ImageIcon()); else font_thumb.setIcon(new ImageIcon(font.getImage()));
				bg = project.bg;
				if(bg == null) bg_thumb.setIcon(new ImageIcon()); else bg_thumb.setIcon(new ImageIcon(bg.getImage()));
				StringBuilder buf = new StringBuilder(); for(String o : project.options){buf.append(o); buf.append('\n');}
				options.setText(buf.toString());
			}
			super.setVisible(b);
		}
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			if(command == MapEditor.OK){
				project.target = HaxeCompiler.TARGETS[target.getSelectedIndex()];
				try{
					int ts = Integer.parseInt(tile_size.getSelectedItem().toString());
					if(ts != project.tile_size){
						if(project.getResources(Tileset.TYPE).iterator().hasNext() || project.getResources(Map.TYPE).iterator().hasNext()){
							JOptionPane.showMessageDialog(this, "You cannot change the tilesize of a project already containing maps and tilesets (as it would break them).\nIf you really wish to change the tile size, delete all maps and tilesets and try again.", "Unable to change Tile Size", JOptionPane.ERROR_MESSAGE);
						} else project.tile_size = ts;
					}
				}catch(Exception ex){}
				project.frame = frame; project.font = font; project.bg = bg;
				String[] o = options.getText().split("\n");
				project.options.clear(); for(int i=0; i<o.length; i++) project.options.add(o[i]);
				try{
					project.setName(name.getText());
				}catch(Exception ex){name.setText(project.getName()); return;}
				try{project.save();}catch(Exception ex){}
				setVisible(false);
			} else if(command == SET_FRAME){
				ImageResource im = ImageResource.choose(project, frame);
				if(im != null){
					BufferedImage b = im.getImage(); if(b.getWidth() != 12 || b.getHeight() != 12)
						JOptionPane.showMessageDialog(this, "Frame images must be 12x12!", "Bad Image Dimensions", JOptionPane.ERROR_MESSAGE);
					else {frame = im; frame_thumb.setIcon(new ImageIcon(frame.getImage()));}
				}
			} else if(command == SET_FONT){
				ImageResource im = ImageResource.choose(project, font);
				if(im != null){
					BufferedImage b = im.getImage(); if(b.getWidth() < 792 || Math.floor(b.getWidth()*0.125) != b.getWidth()*0.125 || b.getHeight() != 8)
						JOptionPane.showMessageDialog(this, "Font images must be 8 pixels high, and >= 792 pixels wide (divisible by 8)!", "Bad Image Dimensions", JOptionPane.ERROR_MESSAGE);
					else {font = im; font_thumb.setIcon(new ImageIcon(font.getImage()));}
				}
			} else if(command == SET_BG){
				ImageResource im = ImageResource.choose(project, bg);
				if(im != null){
					BufferedImage b = im.getImage(); if(b.getWidth() != 200 || b.getHeight() != 150)
						JOptionPane.showMessageDialog(this, "Background images must be 200x150!", "Bad Image Dimensions", JOptionPane.ERROR_MESSAGE);
					else {bg = im; bg_thumb.setIcon(new ImageIcon(bg.getImage()));}
				}
			} else setVisible(false);
		}
	}
}
