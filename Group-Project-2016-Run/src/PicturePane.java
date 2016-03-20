import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class PicturePane extends JPanel implements ActionListener, MouseListener {

	private static final long serialVersionUID = -562341342997159884L;
	
	private TrackList list;
	//private ImageIcon leftTrack = new ImageIcon(this.getClass().getResource("GraphicsElementLeft.png"));
	//private ImageIcon rightTrack = new ImageIcon(this.getClass().getResource("GraphicsElementRight.png"));
	//private ImageIcon centerTrack = new ImageIcon(this.getClass().getResource("GraphicsElementCenter.png"));
	private static final int SCALE_SPACING = 15;
	//private static final int SIDE_WIDTH = 10;
	private static final int IMAGE_HEIGHT = 32;
	private static final int PIXELS_PER_SECOND = 5;
	private static final int SCALE_INTERVAL = 5;
	
	PicturePane (TrackList List) {
		list = List;
	}
	 
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		this.setSize(new Dimension((int)(list.totalLength()*PIXELS_PER_SECOND), list.numTracks()*IMAGE_HEIGHT + SCALE_SPACING));
		g.setColor(Color.BLACK);
		//g.drawLine(0,SCALE_SPACING,this.getWidth(), SCALE_SPACING);
		int mark = 0;
		while(mark<list.totalLength()) {
			//g.drawLine(Integer.toString(mark), mark*PIXELS_PER_SECOND, 0, mark*PIXELS_PER_SECOND, SCALE_SPACING);
			//g.drawString(mark,mark*PIXELS_PER_SECOND+23,SCALE_SPACING);
			mark+=SCALE_INTERVAL;
		}
		
		for (int i=0; i< list.numTracks(); i++) {
			g.setColor(new Color(100,100,255));
			g.fillRect((int)(list.get(i).startTime()*PIXELS_PER_SECOND),
					i*IMAGE_HEIGHT+23,
					(int)(list.get(i).getLength()*PIXELS_PER_SECOND),
					IMAGE_HEIGHT);
			g.setColor(Color.BLACK);
			g.drawString(list.get(i).getFileName(),(int)(list.get(i).startTime()*PIXELS_PER_SECOND),i*IMAGE_HEIGHT+42);
			
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
			int tracknum = y/IMAGE_HEIGHT;
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
			repaint();
		}
	}

}
