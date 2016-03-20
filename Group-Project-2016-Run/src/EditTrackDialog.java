import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.*;

public class EditTrackDialog extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1870545754361816765L;
	
	private Track currentTrack;
	private Track backUpTrack;
	private Track relativeTrack;
	private TrackList list;
	private int relativeID;
	private JButton chooser, preview, save, cancel;
	private JRadioButton beginning, end;
	private JComboBox<String> chooseTrack;
	private JTextField text;
	private JFileChooser fc;
	private JSlider ISlider;
	EditTrackDialog(Track track, TrackList list){
		//trackID = track.getID();
		backUpTrack = track;
		currentTrack = track;
		this.list = list;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	
		this.setResizable(false);
		try { 
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
		} catch (Exception ex) { }
		this.setTitle("Editing track "+track.getShortFileName());
		this.setIconImage(new ImageIcon(this.getClass().getResource("logo.png")).getImage());
		
		JPanel pane = new JPanel(new GridBagLayout());	
		Border margin = new EmptyBorder(10,10,10,10);
		pane.setBorder(margin);
		GridBagConstraints c = new GridBagConstraints();
		
		JLabel fileName = new JLabel ("File Name");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(10,10,7,7);
		pane.add(fileName, c);
		
		text = new JTextField();
		text.setEditable(false);
		text.setFont(new Font("Tahoma",Font.PLAIN,12));
		text.setText(track.getFileName());
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 0;
		pane.add(text, c);
		
		chooser = new JButton("Choose File");
		chooser.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 0;
		pane.add(chooser, c);
		
		fc = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV audio (*.wav)", "wav");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		
		JLabel relativeTrack = new JLabel ("Relative Track");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(relativeTrack, c);
		
		
		String[] tracks = getTrackNames(list, currentTrack);
		chooseTrack = new JComboBox<String>(tracks); 
		chooseTrack.setSelectedIndex(mapIndexToComboList(list.getIndexByID(currentTrack.getRelativeID())));
		chooseTrack.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		pane.add(chooseTrack, c);
		
		beginning = new JRadioButton("Beginning");
		beginning.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 1;
		pane.add(beginning, c);
		
		end = new JRadioButton("End");
		end.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 1;
		pane.add(end, c);
		
		if(track.getRelativeID() == 0)
			end.setEnabled(false);
		
		beginning.setSelected(track.getStartEnd() == Track.START);
		end.setSelected(!(track.getStartEnd() == Track.START));
		
	    ButtonGroup group = new ButtonGroup();
	    group.add(beginning);
	    group.add(end);
		
		JLabel label = new JLabel ("Intensity");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 2;
		pane.add(label, c);
		
		ISlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		ISlider.setMajorTickSpacing(25);
		ISlider.setPaintTicks(true);
		ISlider.setPaintLabels(true);
		ISlider.setValue((int)currentTrack.getIntensity());
		ISlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e){
				JSlider source = (JSlider)e.getSource();
				if(!source.getValueIsAdjusting()){
					double intensity = source.getValue();
					backUpTrack.setIntensity(intensity);
				}
			}
		});
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 2;
		pane.add(ISlider, c);
		
		preview = new JButton("Preview");
		preview.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 3;
		pane.add(preview, c);
		
		save = new JButton("Save");
		save.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 2;
		c.gridy = 3;
		pane.add(save, c);
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 3;
		c.gridy = 3;
		pane.add(cancel, c);
			
		this.add(pane);
		this.pack();
		SwingUtilities.getRootPane(save).setDefaultButton(save);
		this.setVisible(true);
		
		backUpTrack = new Track(
				currentTrack.getFileName(),
				currentTrack.getIntensity(),
				currentTrack.getRelativeID(),
				currentTrack.getStartEnd(),
				currentTrack.getID(),
				list
				);
	}
	
	private int mapIndexToComboList(int index) {
		if (index + 1 > list.getIndexByID(currentTrack.getID())) return index;
		return index + 1;
	}
	
	private String[] getTrackNames(TrackList list, Track excludedTrack){
		String[] trackNames = new String[list.numTracks()];
		boolean skippedTrackAlready = false;
		trackNames[0] = "Start of Script";
		for(int i = 1; i<list.numTracks()+1; i++){
			if (list.get(i-1) == excludedTrack) {
				skippedTrackAlready = true;
				continue;
			}
			trackNames[i + (skippedTrackAlready?-1:0)] = list.get(i-1).getFileName();
		}
		return trackNames;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == chooser){ 
			 int returnVal = fc.showOpenDialog(EditTrackDialog.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                text.setText(file.getAbsolutePath());
	                backUpTrack.setFileName(file.getAbsolutePath());
	            } else {
	                text.setText("");
	            }
		}
		if(e.getSource() == chooseTrack){ 
			int relInd = chooseTrack.getSelectedIndex();
			if(relInd == 0) {
				backUpTrack.setRelativeTo(0);
				end.setEnabled(false);
				beginning.setSelected(true);
				end.setSelected(false);
			}
			else{
				end.setEnabled(true);
				if (relInd>list.getIndexByID(currentTrack.getID())) {
					relInd = relInd + 1;
				}
				relativeTrack = list.get(relInd-1);
				relativeID = relativeTrack.getID();
				backUpTrack.setRelativeTo(relativeID);
			}
		}
		if(e.getSource() == beginning){
			backUpTrack.setStartEnd(Track.START);
		}
		if(e.getSource() == end){
			backUpTrack.setStartEnd(Track.END);
		}
		if(e.getSource() == preview){
			backUpTrack.play();
		}
		if(e.getSource() == save){
			currentTrack.setFileName(backUpTrack.getFileName());
			currentTrack.setIntensity(backUpTrack.getIntensity());
			currentTrack.setRelativeTo(backUpTrack.getRelativeID());
			currentTrack.setStartEnd(backUpTrack.getStartEnd());
			
			/*
			Track track = list.get(trackID);
			track.setIntensity(currentTrack.getIntensity());
			track.setRelativeTo(relativeID);
			track.setStartEnd(currentTrack.getStartEnd());
			*/
			this.setVisible(false);
			this.dispose();
			
		}
		if(e.getSource() == cancel){
			this.setVisible(false);
			this.dispose();
		}
	}
}
