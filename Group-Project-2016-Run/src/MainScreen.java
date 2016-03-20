import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class MainScreen extends JFrame implements ActionListener {
	private static final long serialVersionUID = -5747885815904079529L;
	
	private JFrame mainFrame;
	private TrackList list;
	
	MainScreen(TrackList tracklist){
		list = tracklist;
		tracklist.addActionListener(this);
		
		mainFrame = new JFrame("[No file opened] - Dubbing Tool");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setIconImage(new ImageIcon(this.getClass().getResource("logo.png")).getImage());
		
		try { 
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
		} catch (Exception ex) { }
		
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
		new MainScreen(list);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("updateScript")) {
			mainFrame.setTitle(list.getFileName()+" - Dubbing Tool");
		}
	}
}