import java.awt.Dimension;

import javax.swing.*;
public class MainScreen extends JFrame{
	private TrackList tracklist;
	MainScreen(TrackList tracklist){
		
		JFrame mainFrame = new JFrame("[No file opened] - Dubbing Tool");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		try { 
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
		} catch (Exception ex) { }
		
		this.tracklist = tracklist;
		MainMenuBar menuBar = new MainMenuBar(tracklist);
		mainFrame.setJMenuBar(menuBar.getMenuBar());
		TrackTablePane trackTablePane = new TrackTablePane(tracklist);
		PicturePane picturePane = new PicturePane(tracklist);
		JScrollPane rightPane = new JScrollPane(picturePane);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trackTablePane, rightPane);
		mainFrame.add(splitPane);
		
		mainFrame.pack();
		mainFrame.setVisible(true);
		mainFrame.setExtendedState(mainFrame.getExtendedState()|JFrame.MAXIMIZED_BOTH);
	}
	public static void main(String[] a){
		TrackList list = new TrackList();
		MainScreen screen = new MainScreen(list);
	}
}