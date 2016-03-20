import javax.swing.*;

public class DeleteTrackConfirmation {
	
	public static boolean showDialog() {
		int prompt = 0;
		prompt = JOptionPane.showConfirmDialog(null, "Are you sure?", "Delete Track", JOptionPane.YES_NO_OPTION);
		if (prompt == JOptionPane.YES_OPTION) {
			return true;
		}
		if (prompt == JOptionPane.NO_OPTION) {
			return false;
		}
		return false;
	}
	
}
