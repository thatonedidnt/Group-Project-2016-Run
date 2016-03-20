import java.awt.BorderLayout;
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
		trackTable = new TrackTable(tracklist);
		this.setLayout(new BorderLayout());
		this.add(trackTable, BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource() == edit){
			new EditTrackDialog(trackTable.getSelected(), tracklist);
		}
		
		if(e.getSource() == delete){
			tracklist.remove(trackTable.getSelectedIndex());
		}

	}
}
