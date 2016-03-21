import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.Scrollable;

public class PictureTimeScalePane extends JPanel implements ActionListener, Scrollable {
	private static final long serialVersionUID = 9145548790046344469L;
	
	private TrackList tracklist;
	private JPanel picpane;
	
	PictureTimeScalePane(TrackList tracklist, JPanel picpane) {
		this.tracklist = tracklist;
		this.picpane = picpane;
		tracklist.addActionListener(this);
		this.repaint();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		((Graphics2D)g).setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		int totalLength = (int)(tracklist.totalLength()*PicturePane.PIXELS_PER_SECOND);
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, totalLength, 2);
		for (int i = 5; i < totalLength/PicturePane.PIXELS_PER_SECOND; i += 5) {
			g.fillRect(i*PicturePane.PIXELS_PER_SECOND, 2, 2, 5);
			g.drawString(Integer.toString(i), i*PicturePane.PIXELS_PER_SECOND+3, 12);
		}
	}

	@Override
	public void actionPerformed(ActionEvent ev) {
		if (ev.getActionCommand().equals("updateScript")) {
			this.revalidate();
			this.getParent().getParent().repaint();
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		//int totalLength = (int)(tracklist.totalLength()*PicturePane.PIXELS_PER_SECOND);
		return new Dimension(picpane.getWidth(), 60);
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return 15;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return true;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return 5;
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(picpane.getWidth(), 60);
	}
}
