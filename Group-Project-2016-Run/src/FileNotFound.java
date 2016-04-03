import java.util.ArrayList;

import javax.swing.JOptionPane;

public class FileNotFound {

	FileNotFound(ArrayList<String> files) {
		String fileNotFoundList = "";
		int maxFiles = files.size();
		int overflowFiles = 0;
		if (maxFiles > 10) {
			maxFiles = 10;
			overflowFiles = files.size() - 10;
		}
		
		for (int i = 0; i < maxFiles; ++i) {
			fileNotFoundList += files.get(i);
			if (i<maxFiles-1) {
				fileNotFoundList += ",\n";
			}
		}
		if (overflowFiles > 0) {
			fileNotFoundList += "\nand "+overflowFiles+" more.";
		}
		JOptionPane.showMessageDialog(null,
			"Files not found, or are corrupt:\n" + fileNotFoundList, "Audio files not found, or are corrupt",
			JOptionPane.ERROR_MESSAGE);
	}
}
