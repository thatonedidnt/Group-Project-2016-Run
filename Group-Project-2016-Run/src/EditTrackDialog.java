import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.*;

public class EditTrackDialog extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1870545754361816765L;
	
	private Track currentTrack;
	private Track backUpTrack;
	private JButton chooser, preview, save, cancel;
	private JRadioButton beginning, end;
	private ArrayList<TrackForwarder> comboItems;
	private JComboBox<TrackForwarder> chooseTrack;
	private JTextField text;
	private JFileChooser fc;
	private JSlider ISlider;
	private String lastPathChooseFile;
	
	EditTrackDialog(Track track, TrackList list){
		super();
		
		backUpTrack = track;
		currentTrack = track;
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
		
		JLabel relativeTrack = new JLabel ("Relative Track");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		pane.add(relativeTrack, c);
		
		comboItems = new ArrayList<TrackForwarder>();
		comboItems.add(new TrackForwarder(list.getByID(0), "Start"));
		for (Track t : list.getTracks()) {
			if ((t == currentTrack) || currentTrack.willBeCyclic(t.getID())) continue;
			comboItems.add(new TrackForwarder(t, t.getShortFileName()));
		}
		chooseTrack = new JComboBox<TrackForwarder>(comboItems.toArray(new TrackForwarder[0])); 
		chooseTrack.setSelectedIndex(mapIDToComboList(currentTrack.getRelativeID()));
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
		this.setLocationRelativeTo(null);
	}
	
	private int mapIDToComboList(int id) {
		for (int i = 0; i < comboItems.size(); ++i) {
			if (comboItems.get(i).getTrack().getID() == id) return i;
		}
		return -1;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == chooser){ 
			fc = new JFileChooser(lastPathChooseFile);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("WAV audio (*.wav)", "wav");
			fc.addChoosableFileFilter(filter);
			fc.setFileFilter(filter);
			
			int returnVal = fc.showOpenDialog(EditTrackDialog.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				lastPathChooseFile = file.getParent();
				text.setText(file.getAbsolutePath());
				backUpTrack.setFileName(file.getAbsolutePath());
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
				Track relativeTrack = chooseTrack.getItemAt(relInd).getTrack();
				int relativeID = relativeTrack.getID();
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
			
			this.setVisible(false);
			this.dispose();
			
		}
		if(e.getSource() == cancel){
			this.setVisible(false);
			this.dispose();
		}
	}
}

class TrackForwarder {
	Track track;
	String string;
	
	TrackForwarder(Track track, String string) {
		this.track = track;
		this.string = string;
	}
	
	public Track getTrack() {
		return track;
	}
	
	@Override
	public String toString() {
		return string;
	}
}
