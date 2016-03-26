import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainMenuBar extends JMenuBar implements ActionListener{
	private static final long serialVersionUID = -8709557995298923151L;
	
	private JMenu menuFile, menuRecording;
	private JMenuItem itemOpen, itemNew, itemSaveAs, itemQuit;
	private JMenuItem itemNewTrack, itemCreateRecording, itemPreview, itemExport;
	private TrackList tracklist;
	private String lastPathOpen;
	private String lastPathNew;
	private String lastPathSaveAs;
	private String lastPathNewTrack;
	private String lastPathExport;

	MainMenuBar(TrackList trackList){
		super();
		
		lastPathOpen = null;
		lastPathNew = null;
		lastPathSaveAs = null;
		lastPathNewTrack = null;
		lastPathExport = null;

		menuFile=new JMenu("File");

		itemOpen=new JMenuItem("Open");
		itemOpen.addActionListener(this);
		itemOpen.setIcon(new ImageIcon(this.getClass().getResource("stock_open_24.png")));
		menuFile.add(itemOpen);
		
		itemNew=new JMenuItem("New");
		itemNew.setIcon(new ImageIcon(this.getClass().getResource("stock_new_24.png")));
		itemNew.addActionListener(this);
		menuFile.add(itemNew);
		
		itemSaveAs=new JMenuItem("Save As");
		itemSaveAs.setIcon(new ImageIcon(this.getClass().getResource("stock_save_24.png")));
		itemSaveAs.addActionListener(this);
		menuFile.add(itemSaveAs); 
		
		itemQuit=new JMenuItem("Quit");
		itemQuit.setIcon(new ImageIcon(this.getClass().getResource("stock_exit_24.png")));
		itemQuit.addActionListener(this);
		menuFile.add(itemQuit);

		menuRecording=new JMenu("Recording");

		itemNewTrack=new JMenuItem("New Track");
		itemNewTrack.setIcon(new ImageIcon(this.getClass().getResource("stock_add_24.png")));
		itemNewTrack.addActionListener(this);
		menuRecording.add(itemNewTrack);
		
		itemCreateRecording=new JMenuItem("Create Recording");
		itemCreateRecording.setIcon(new ImageIcon(this.getClass().getResource("stock_media_record_24.png")));
		itemCreateRecording.addActionListener(this);
		menuRecording.add(itemCreateRecording);
		
		itemPreview=new JMenuItem("Preview");
		itemPreview.setIcon(new ImageIcon(this.getClass().getResource("stock_media_play_24.png")));
		itemPreview.addActionListener(this);
		menuRecording.add(itemPreview);
		
		itemExport=new JMenuItem("Export");
		itemExport.setIcon(new ImageIcon(this.getClass().getResource("stock_convert_24.png")));
		itemExport.addActionListener(this);
		menuRecording.add(itemExport);

		this.add(menuFile);
		this.add(menuRecording);
		
		this.disableButtons();
		
		this.tracklist = trackList;
	}
	public void disableButtons(){
		itemSaveAs.setEnabled(false);
		itemNewTrack.setEnabled(false);
		itemCreateRecording.setEnabled(false);
		itemPreview.setEnabled(false);
		itemExport.setEnabled(false);
	}
	public void enableButtons(){
		itemSaveAs.setEnabled(true);
		itemNewTrack.setEnabled(true);
		itemCreateRecording.setEnabled(true);
		itemPreview.setEnabled(true);
		itemExport.setEnabled(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == itemOpen){								//open
			JFileChooser fc = new JFileChooser(lastPathOpen);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Script files (*.dbts, *.xml)", "dbts", "xml");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			int returnVal = fc.showDialog(itemOpen, "Open...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastPathOpen = file.getParent();
				try {
					tracklist.setFileName(file.getAbsolutePath());
					this.enableButtons();
				}
				catch (BadFileException e1) {
					JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
				}
				catch (BadPathException e1) {
					JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
				}
			}
		}
		if(e.getSource() == itemNew){								//new
			JFileChooser fc = new JFileChooser(lastPathNew);
			FileNameExtensionFilter dbtsfilter = new FileNameExtensionFilter("Script files (*.dbts)", "dbts");
			fc.setFileFilter(dbtsfilter);
			int returnVal = fc.showDialog(itemOpen, "New...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastPathNew = file.getParent();
				if (!file.getAbsolutePath().matches(".*[dD][bB][tT][sS]")) {
					file = new File(file.getAbsolutePath()+".dbts");
				}

				String filename = file.getAbsolutePath();
				try {
					tracklist.clear();
					tracklist.save(filename);
					tracklist.setFileName(filename);
					this.enableButtons();
				}
				catch (BadFileException e1) {
					JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
				}
				catch (BadPathException e1) {
					JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
				}
			}
		}
		if(e.getSource() == itemSaveAs){							//save as
			JFileChooser fc = new JFileChooser(lastPathSaveAs);
			FileNameExtensionFilter dbtsfilter = new FileNameExtensionFilter("Script files (*.dbts)", "dbts");
			fc.setFileFilter(dbtsfilter);
			int returnVal = fc.showDialog(itemOpen, "Save As...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastPathSaveAs = file.getParent();
				if (!file.getAbsolutePath().matches(".*[dD][bB][tT][sS]")) {
					file = new File(file.getAbsolutePath()+".dbts");
				}
				try {
					tracklist.save(file.getAbsolutePath());
				}
				catch (BadPathException e1) {
					JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
				}
			}
		}
		if(e.getSource() == itemQuit){								//quit
			System.exit(0);
		}
		if(e.getSource() == itemNewTrack){							//new track
			JFileChooser fc = new JFileChooser(lastPathNewTrack);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV audio (*.wav)", "wav");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			int returnVal = fc.showDialog(itemOpen, "Select audio file...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastPathNewTrack = file.getParent();
				Track newtrack = new Track(file.getAbsolutePath(), tracklist);
				int latestRelID = 0;
				double latestEnd = 0;
				for (Track t : tracklist.getTracks()) {
					if (t.startTime()+t.getLength() > latestEnd) {
						latestEnd = t.startTime()+t.getLength();
						latestRelID = t.getID();
					}
				}
				tracklist.add(newtrack);
				if (latestRelID > 0) {
					tracklist.get(tracklist.numTracks()-1).setRelativeTo(latestRelID);
					tracklist.get(tracklist.numTracks()-1).setStartEnd(Track.END);
				}
				new EditTrackDialog(tracklist.get(tracklist.numTracks()-1),tracklist);
			}
		}
		if(e.getSource() == itemCreateRecording){					//create recording
			Track.recordTrack(tracklist);
		}
		if(e.getSource() == itemPreview){							//preview
			tracklist.play();
		}
		if(e.getSource() == itemExport){							//export
			JFileChooser fc = new JFileChooser(lastPathExport);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV audio", "wav");
			fc.setFileFilter(filter);
			int returnVal = fc.showDialog(itemOpen, "Export...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastPathExport = file.getParent();
				if (!file.getAbsolutePath().matches(".*[wW][aA][vV]")) {
					file = new File(file.getAbsolutePath()+".wav");
				}
				try
				{
					tracklist.export(file.getAbsolutePath());
				}
				catch(BadPathException ex)
				{
					JOptionPane.showMessageDialog(null, "The path for exporting the script isn't accessible.");
				}
			}
		}
	}
}