import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class MainScreen extends JFrame implements ActionListener {
	private static final long serialVersionUID = -5747885815904079529L;
	
	private JFrame mainFrame;
	private TrackList list;
	private JScrollPane rightPane;
	private TrackTablePane trackTablePane;
	
	MainScreen(TrackList tracklist){
		list = tracklist;
		tracklist.addActionListener(this);
		
		mainFrame = new JFrame("[No file opened] - Dubbing Tool");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setPreferredSize(new Dimension(800,600));
		mainFrame.setMinimumSize(new Dimension(600,400));
		mainFrame.setIconImage(new ImageIcon(this.getClass().getResource("logo.png")).getImage());
		
		try { 
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
		} catch (Exception ex) { }
		
		MainMenuBar menuBar = new MainMenuBar(tracklist);
		mainFrame.setJMenuBar(menuBar);
		trackTablePane = new TrackTablePane(tracklist);
		RightHalfPane righthalfpane = new RightHalfPane(tracklist);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, trackTablePane, righthalfpane);
		mainFrame.add(splitPane);
		
		JScrollBar TrackTableVertBar = trackTablePane.getTrackTableScrollPane().getVerticalScrollBar();
		JScrollBar PicturePaneVertBar = righthalfpane.getVertScrollPane().getVerticalScrollBar();
		TrackTableVertBar.setModel(PicturePaneVertBar.getModel());
		//timescalescroller.getVerticalScrollBar().setModel(((MainScreen)this.getParent()).getTrackTableScrollPane().getVerticalScrollBar().getModel());
		
		
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
	
	public JScrollPane getTrackTableScrollPane() {
		return trackTablePane.getTrackTableScrollPane();
	}
}