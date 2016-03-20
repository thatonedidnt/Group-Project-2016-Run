import java.util.List;

import javax.swing.JOptionPane;

public class FileNotFound {

	FileNotFound(List<String> files) {

		for (int i = 0; i == files.size(); i++) {
			System.out.println("Error. Files not found: " + files.get(i));

			if (i == files.size()) {
				JOptionPane.showMessageDialog(null,
						"Files not found: " + files, "Error",
						JOptionPane.INFORMATION_MESSAGE);

			}
		}
	}
}
