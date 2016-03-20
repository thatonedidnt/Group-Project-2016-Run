import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

public class PictureTimeScalePane extends JPanel implements ActionListener {
	private static final long serialVersionUID = 9145548790046344469L;
	
	private TrackList tracklist;
	
	PictureTimeScalePane(TrackList tracklist) {
		this.tracklist = tracklist;
		tracklist.addActionListener(this);
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		int totalLength = (int)(tracklist.totalLength()*PicturePane.PIXELS_PER_SECOND);
		this.setSize(new Dimension(totalLength, 20));
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, totalLength, 2);
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("updateScript")) {
			repaint();
		}
	}
}
