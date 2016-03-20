import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

public class TrackTablePane extends JPanel implements ActionListener {
	private static final long serialVersionUID = 1172621669112000413L;
	
	TrackTable trackTable;
	JButton edit;
	JButton delete;
	TrackList tracklist;

	public TrackTablePane(TrackList tracklist) {
		this.tracklist = tracklist;
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		
		trackTable = new TrackTable(tracklist);
		trackTable.subscribeForSelectionUpdates(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		this.add(trackTable, gbc);
		
		edit = new JButton();
		edit.setText("Edit...");
		edit.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridx = 0;
		gbc.gridy = 1;
		this.add(edit, gbc);
		
		delete = new JButton();
		delete.setText("Delete");
		delete.addActionListener(this);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 1;
		gbc.gridy = 1;
		this.add(delete, gbc);
		updateEnable();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == edit){
			new EditTrackDialog(trackTable.getSelected(), tracklist);
		}
		if(e.getSource() == delete){
			if (DeleteTrackConfirmation.showDialog()) {
				tracklist.remove(trackTable.getSelectedIndex());
			}
		}
		if (e.getSource() == trackTable) {
			updateEnable();
		}
	}
	
	private void updateEnable() {
		edit.setEnabled(trackTable.getSelectedIndex() != -1);
		delete.setEnabled(trackTable.getSelectedIndex() != -1);
	}
}
