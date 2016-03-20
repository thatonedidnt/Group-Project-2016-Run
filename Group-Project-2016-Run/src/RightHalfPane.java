import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;


public class RightHalfPane extends JPanel {
	private static final long serialVersionUID = -136513124581425117L;
	
	RightHalfPane(TrackList tracklist) {
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		
		PicturePane picpane = new PicturePane(tracklist);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		this.add(picpane, gbc);
		
		PictureTimeScalePane timescale = new PictureTimeScalePane(tracklist);
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridx = 0;
		gbc.gridy = 1;
		this.add(timescale, gbc);
	}
}
