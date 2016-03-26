import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PicturePane extends JPanel implements ActionListener, MouseListener, Scrollable {

	private static final long serialVersionUID = -562341342997159884L;
	
	private TrackList list;
	private ImageIcon trackImage = new ImageIcon(this.getClass().getResource("TrackImage.png"));
	private static final int SCALE_SPACING = 15;
	private static final int IMAGE_HEIGHT = 32;
	public static final int PIXELS_PER_SECOND = 8;
	public static final int FIRST_ROW_OFFSET = 23;
	
	PicturePane (TrackList List) {
		list = List;
		list.addActionListener(this);
		this.addMouseListener(this);
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		((Graphics2D)g).setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		this.setSize(new Dimension((int)(list.totalLength()*PIXELS_PER_SECOND)+200, list.numTracks()*IMAGE_HEIGHT + SCALE_SPACING));
		this.setPreferredSize(new Dimension((int)(list.totalLength()*PIXELS_PER_SECOND)+200, list.numTracks()*IMAGE_HEIGHT + SCALE_SPACING));
		g.setColor(Color.BLACK);
		
		for (int i=0; i< list.numTracks(); i++) {
			g.drawImage(trackImage.getImage(), (int)(list.get(i).startTime()*PIXELS_PER_SECOND), i*IMAGE_HEIGHT, (int)(list.get(i).getLength()*PIXELS_PER_SECOND), IMAGE_HEIGHT-1, null);
			g.setColor(Color.BLACK);
			g.drawString(list.get(i).getShortFileName(),(int)(list.get(i).startTime()*PIXELS_PER_SECOND),i*IMAGE_HEIGHT+19);
		}
	
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2){
			int y = e.getY();
			int x = e.getX();
			int tracknum = (y)/IMAGE_HEIGHT;
			int start = (int)(list.get(tracknum).startTime() * PIXELS_PER_SECOND);
			int end = (int)(start + list.get(tracknum).getLength() * PIXELS_PER_SECOND);
			if (x>=start && x<=end) {
				new EditTrackDialog(list.get(tracknum), list); 
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		//do nothing
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		//do nothing
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		//do nothing
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		//do nothing
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("updateScript")) {
			revalidate();
			this.getParent().getParent().repaint();
		}
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle arg0, int arg1, int arg2) {
		return 15;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle arg0, int arg1, int arg2) {
		return 5;
	}

}
