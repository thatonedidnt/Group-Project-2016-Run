import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PicturePane extends JPanel implements ActionListener, MouseListener, Scrollable {

	private static final long serialVersionUID = -562341342997159884L;
	
	private TrackList list;
	//private ImageIcon leftTrack = new ImageIcon(this.getClass().getResource("GraphicsElementLeft.png"));
	//private ImageIcon rightTrack = new ImageIcon(this.getClass().getResource("GraphicsElementRight.png"));
	//private ImageIcon centerTrack = new ImageIcon(this.getClass().getResource("GraphicsElementCenter.png"));
	private static final int SCALE_SPACING = 15;
	//private static final int SIDE_WIDTH = 10;
	private static final int IMAGE_HEIGHT = 32;
	public static final int PIXELS_PER_SECOND = 8;
	private static final int SCALE_INTERVAL = 5;
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
		//g.drawLine(0,SCALE_SPACING,this.getWidth(), SCALE_SPACING);
		int mark = 0;
		while(mark<list.totalLength()) {
			//g.drawLine(Integer.toString(mark), mark*PIXELS_PER_SECOND, 0, mark*PIXELS_PER_SECOND, SCALE_SPACING);
			//g.drawString(mark,mark*PIXELS_PER_SECOND+23,SCALE_SPACING);
			mark+=SCALE_INTERVAL;
		}
		
		for (int i=0; i< list.numTracks(); i++) {
			g.setColor(new Color(150,170,255));
			//g.fillRoundRect(arg0, arg1, arg2, arg3, arg4, arg5)
			g.fillRoundRect((int)(list.get(i).startTime()*PIXELS_PER_SECOND),
					i*IMAGE_HEIGHT,
					(int)(list.get(i).getLength()*PIXELS_PER_SECOND),
					IMAGE_HEIGHT-1, 4, 4);
			/*
			g.fillRect((int)(list.get(i).startTime()*PIXELS_PER_SECOND),
					i*IMAGE_HEIGHT + FIRST_ROW_OFFSET,
					(int)(list.get(i).getLength()*PIXELS_PER_SECOND),
					IMAGE_HEIGHT-1);
			*/
			g.setColor(Color.BLACK);
			g.drawString(list.get(i).getShortFileName(),(int)(list.get(i).startTime()*PIXELS_PER_SECOND),i*IMAGE_HEIGHT+19);
			
			/*
			//Draws the left section of the track
			g.drawImage(leftTrack.getImage(), 
					(int)(list.get(i).startTime()*PIXELS_PER_SECOND),
					i*IMAGE_HEIGHT + SCALE_SPACING, 
					SIDE_WIDTH, 
					IMAGE_HEIGHT,
					null);
			//Draws the center section of the track
			g.drawImage(centerTrack.getImage(), 
					(int)(list.get(i).startTime()*PIXELS_PER_SECOND)+ SIDE_WIDTH,
					i*IMAGE_HEIGHT + SCALE_SPACING, 
					(list.get(i).getLength()*PIXELS_PER_SECOND) - SIDE_WIDTH*2, 
					IMAGE_HEIGHT, 
					null);
			//Draws the right section of the track
			g.drawImage(rightTrack.getImage(),
					(int)((list.get(i).startTime()+list.getLength())*PIXELS_PER_SECOND) - SIDE_WIDTH,
					i*IMAGE_HEIGHT + SCALE_SPACING,
					SIDE_WIDTH,
					IMAGE_HEIGHT,
					null);
			*/
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
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
