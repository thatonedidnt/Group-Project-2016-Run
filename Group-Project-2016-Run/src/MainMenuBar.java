import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.*;

public class MainMenuBar implements ActionListener{
	JMenuBar MenuBar;
	JMenu menuFile, menuRecording;
	JMenuItem itemOpen, itemNew, itemSaveAs, itemQuit;
	JMenuItem itemNewTrack, itemCreateRecording, itemPreview, itemExport;
	TrackList tracklist;

	MainMenuBar(TrackList trackList){
		MenuBar=new JMenuBar();
		//MenuBar.add(Box.createRigidArea(new Dimension(100,25)));

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

		MenuBar.add(menuFile);
		MenuBar.add(menuRecording);
		
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

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == itemOpen){								//open
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showDialog(itemOpen, "Open");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
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
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showDialog(itemOpen, "New...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
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
			if(e.getSource() == itemNew){
				JFileChooser fc = new JFileChooser();
				int returnVal = fc.showDialog(itemOpen, "Open");
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					try {
						tracklist.save(file.getName());
					}
					catch (BadPathException e1) {
						JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
					}
				}
			}
		}
		if(e.getSource() == itemQuit){								//quit
			System.exit(0);
		}
		if(e.getSource() == itemNewTrack){							//new track
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showDialog(itemOpen, "Select audio file...");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				Track newtrack = new Track(file.getAbsolutePath(), tracklist);
				tracklist.add(newtrack);
			}
		}
		if(e.getSource() == itemCreateRecording){					//create recording
			Track newtrack = new Track(Track.RECORD, tracklist);
			tracklist.add(newtrack);
		}
		if(e.getSource() == itemPreview){							//preview
			tracklist.play();
		}
		if(e.getSource() == itemExport){							//export
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showDialog(itemOpen, "Open");
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				tracklist.export(file.getName());
			}
		}
	}
		
	public JMenuBar getMenuBar() {
		return MenuBar;
	}
}
