import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;


public class TrackTable extends JPanel implements ActionListener, MouseListener {
	private static final long serialVersionUID = -4739904494588317823L;
	
	private TrackList list;
	private JTable table;
	private JPopupMenu popup;
	
	TrackTable(TrackList list) {
		super(new GridLayout(1,0));
		
		this.list = list;
		
		try { 
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
		} catch (Exception ex) { 
			ex.printStackTrace(); 
		}

		table = new JTable(new TrackTableModel(this.list));
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setRowHeight(32);
		table.setFillsViewportHeight(true);
		table.addMouseListener(this);

		popup = new JPopupMenu();
		JMenuItem edit = new JMenuItem("Edit...");
		edit.setIcon(new ImageIcon(this.getClass().getResource("stock_edit_24.png")));
		edit.addActionListener(this);
		JMenuItem delete = new JMenuItem("Delete");
		delete.setIcon(new ImageIcon(this.getClass().getResource("stock_trash_24.png")));
		delete.addActionListener(this);
		popup.add(edit);
		popup.add(delete);
		
		JScrollPane scrollPane = new JScrollPane(table);
		
		this.setLayout(new BorderLayout());
		
		add(scrollPane, BorderLayout.CENTER);
	}
	/*Creates a JTable reflecting the provided TrackList, adding this TrackTable to the TrackList’s ActionListeners. The format of the table is as in the storyboard, except with no Actions column.*/
	
	//TODO: IMPLEMENT
	public void mouseClicked(MouseEvent ev) {
		/*
		Single click:
		Highlights track and activates edit and delete buttons
		Sets clicked track to selectedTrack
		Double click
		Constructs an EditTrackDialog([selected track]). If no track is selected, nothing happens. If more than one track is selected, nothing happens.
		Right click
		highlights clicked track
		opens right click JPopupMenu with the following JMenuItems: “Edit”, “Delete”
		*/
		if (ev.getClickCount()==2 && !ev.isConsumed()) {
			if (ev.getButton() == MouseEvent.BUTTON1) {
				if (table.getSelectedRow() != -1) {
					new EditTrackDialog(list.get(table.getSelectedRow()), list);
				}
			}
			ev.consume();
		}
	}

	public void mouseEntered(MouseEvent ev) {
		//do nothing
	}

	public void mouseExited(MouseEvent ev) {
		//do nothing
	}

	public void mousePressed(MouseEvent ev) {
		maybeShowPopup(ev);
	}

	public void mouseReleased(MouseEvent ev) {
		maybeShowPopup(ev);
	}
	
	private void maybeShowPopup(MouseEvent ev) {
		if (ev.isPopupTrigger()) {
			Point p = ev.getPoint();
			int row = table.rowAtPoint(p);
			ListSelectionModel model = table.getSelectionModel();
			model.setSelectionInterval(row,row);
			if (table.getSelectedRow() != -1) {
				popup.show(ev.getComponent(), ev.getX(), ev.getY());
			}
		}
	}
	
	public void actionPerformed(ActionEvent ev) {
		switch (ev.getActionCommand()) {
		case "updateScript":
			this.invalidate();
		case "Edit...":
			if (table.getSelectedRow() != -1) {
				new EditTrackDialog(list.get(table.getSelectedRow()), list);
			}
		case "Delete":
			if (table.getSelectedRow() != -1) {
				if (DeleteTrackConfirmation.showDialog()) {
					list.remove(table.getSelectedRow());
				}
			}
		}
	}
	
	public Track getSelected() {
		/*
		Track getSelected()
		Returns the selected Track.
		*/
		int row = table.getSelectedRow();
		if (row == -1) {
			return null;
		}
		else {
			return list.get(row);
		}
	}
	
	public int getSelectedIndex() {
		return table.getSelectedRow();
	}
}

class TrackTableModel extends AbstractTableModel {
	private static final long serialVersionUID = -6017075090147934985L;
	
	private static final String[] columnNames = {
		"File Name",
		"Start Time",
		"Attached To",
		"Intensity"
	};
	
	private TrackList list;
	
	TrackTableModel(TrackList list) {
		super();
		this.list = list;
	}
	
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	public int getColumnCount() {
		return 4;
	}

	public int getRowCount() {
		return list.numTracks();
	}
	
	private String formatFileName(int row) {
		String filename = list.get(row).getFileName();
		if (!list.get(row).isGood()) {
			filename = "[Error] "+filename;
		}
		return filename;
	}
	
	private static String formatStartTime(double time) {
		Date date = new Date((long)(time*1000));
		String formattedDate = new SimpleDateFormat("mm:ss").format(date);
		return formattedDate;
	}
	
	private String formatAttachedTo(int row) {
		if (list.get(row).getRelativeID()==0) {
			return "Start";
		}
		else {
			String startend = (list.get(row).getStartEnd()==Track.START)?"[Start] ":"[End] ";
			int ID = list.get(row).getRelativeID();
			for (Track track : list.getTracks()) {
				if (track.getID() == ID) {
					return startend+track.getFileName();
				}
			}
			return "Track not found.";
		}
	}
	
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return formatFileName(row);
		case 1:
			return formatStartTime(list.get(row).startTime());
		case 2:
			return formatAttachedTo(row);
		case 3:
			return Math.round(list.get(row).getIntensity())+"%";
		default:
			return "Error, column out of range";
		}
	}
}
