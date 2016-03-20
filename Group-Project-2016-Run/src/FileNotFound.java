import java.util.ArrayList;

import javax.swing.JOptionPane;

public class FileNotFound {

	FileNotFound(ArrayList<String> files) {

		for (int i = 0; i == files.size(); i++) {
			System.out.println("Error. Files not found: " + files.get(i));
		}
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
			"Files not found:\n" + fileNotFoundList, "Audio files not found",
			JOptionPane.ERROR_MESSAGE);
	}
}
