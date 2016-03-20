import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;


public class RightHalfPane extends JPanel implements ActionListener {
	private static final long serialVersionUID = -136513124581425117L;
	
	private JScrollPane vertScroll;
	private JScrollPane horizScroll;
	
	RightHalfPane(TrackList tracklist) {
		GridBagLayout layout = new GridBagLayout();
		this.setLayout(layout);
		
		tracklist.addActionListener(this);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 2;
		this.add(Box.createRigidArea(new Dimension(0,PicturePane.FIRST_ROW_OFFSET)), gbc);
		
		PicturePane picpane = new PicturePane(tracklist);
		vertScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		vertScroll.setViewportView(picpane);
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		this.add(vertScroll, gbc);
		
		PictureTimeScalePane timescale = new PictureTimeScalePane(tracklist);
		horizScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		horizScroll.setViewportView(timescale);
		//horizScroll.setPreferredSize(new Dimension(99999,60));
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 1;
		this.add(horizScroll, gbc);
		
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 2;
		this.add(Box.createRigidArea(new Dimension(16,0)), gbc);
		
		JScrollBar PicturePaneScrollBar = vertScroll.getHorizontalScrollBar();
		JScrollBar TimeScaleScrollBar = horizScroll.getHorizontalScrollBar();
		TimeScaleScrollBar.setModel(PicturePaneScrollBar.getModel());
		
		JLabel units = new JLabel("Seconds");
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		gbc.gridy = 3;
		this.add(units, gbc);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("updateScript")) {
			this.revalidate();
			this.getParent().getParent().revalidate();
		}
	}
	
	public JScrollPane getVertScrollPane() {
		return vertScroll;
	}
}
