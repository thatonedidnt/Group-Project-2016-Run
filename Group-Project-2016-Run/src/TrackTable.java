import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
	private TrackTableModel model;
	private ArrayList<ActionListener> listeners;
	private JScrollPane scrollpane;
	
	TrackTable(TrackList list) {
		super(new GridLayout(1,0));
		
		listeners = new ArrayList<ActionListener>();
		
		this.list = list;
		list.addActionListener(this);
		
		try { 
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"); 
		} catch (Exception ex) { 
			ex.printStackTrace(); 
		}
		
		model = new TrackTableModel(this.list);
		
		table = new JTable(model);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setRowHeight(32);
		table.setFillsViewportHeight(true);
		table.addMouseListener(this);

		popup = new JPopupMenu();
		JMenuItem edit = new JMenuItem("Edit...");
		edit.setIcon(new ImageIcon(this.getClass().getResource("assets/stock_edit_24.png")));
		edit.addActionListener(this);
		JMenuItem delete = new JMenuItem("Delete");
		delete.setIcon(new ImageIcon(this.getClass().getResource("assets/stock_trash_24.png")));
		delete.addActionListener(this);
		popup.add(edit);
		popup.add(delete);
		
		scrollpane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollpane.getVerticalScrollBar().setUnitIncrement(8);
		this.setLayout(new BorderLayout());
		
		add(scrollpane, BorderLayout.CENTER);
	}
	
	@Override
	public void mouseClicked(MouseEvent ev) {
		if (ev.getClickCount()==2 && !ev.isConsumed()) {
			if (ev.getButton() == MouseEvent.BUTTON1) {
				if (table.getSelectedRow() != -1) {
					new EditTrackDialog(list.get(table.getSelectedRow()), list);
				}
			}
			ev.consume();
		}
	}

	@Override
	public void mouseEntered(MouseEvent ev) {
		//do nothing
	}

	@Override
	public void mouseExited(MouseEvent ev) {
		//do nothing
	}

	@Override
	public void mousePressed(MouseEvent ev) {
		updateListeners();
		maybeShowPopup(ev);
	}

	@Override
	public void mouseReleased(MouseEvent ev) {
		maybeShowPopup(ev);
	}
	
	private void maybeShowPopup(MouseEvent ev) {
		if (ev.isPopupTrigger()) {
			Point p = ev.getPoint();
			int row = table.rowAtPoint(p);
			ListSelectionModel model = table.getSelectionModel();
			model.setSelectionInterval(row,row);
			updateListeners();
			if (table.getSelectedRow() != -1) {
				popup.show(ev.getComponent(), ev.getX(), ev.getY());
			}
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent ev) {
		switch (ev.getActionCommand()) {
		case "updateScript":
			model.fireTableDataChanged();
			this.updateListeners();
			break;
		case "Edit...":
			if (table.getSelectedRow() != -1) {
				new EditTrackDialog(list.get(table.getSelectedRow()), list);
			}
			break;
		case "Delete":
			if (table.getSelectedRow() != -1) {
				if (DeleteTrackConfirmation.showDialog()) {
					list.remove(table.getSelectedRow());
				}
			}
			break;
		}
	}
	
	public Track getSelected() {
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

	public void subscribeForSelectionUpdates(ActionListener listener) {
		listeners.add(listener);
	}
	
	private void updateListeners() {
		for (ActionListener listener : listeners) {
			listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "rowSelection"));
		}
	}
	
	public JScrollPane getScrollPane() {
		return scrollpane;
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
	
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return list.numTracks();
	}
	
	private String formatFileName(int row) {
		String filename = list.get(row).getShortFileName();
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
					return startend+track.getShortFileName();
				}
			}
			return "Track not found.";
		}
	}
	
	@Override
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
