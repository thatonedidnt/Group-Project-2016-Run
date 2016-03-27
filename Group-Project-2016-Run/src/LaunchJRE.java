import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.jgrapht.DirectedGraph;
import org.tritonus.dsp.ais.AmplitudeAudioInputStream;
import org.tritonus.share.sampled.convert.TSynchronousFilteredAudioInputStream;

public class LaunchJRE {

	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			throw new IllegalStateException("os.name");
		}
		os = os.toLowerCase();
		return os.startsWith("windows");
	}

	private static File getJreExecutable() throws FileNotFoundException {
		String jreDirectory = System.getProperty("java.home");
		if (jreDirectory == null) {
			throw new IllegalStateException("java.home");
		}
		File exe;
		if (isWindows()) {
			exe = new File(jreDirectory, "bin/java.exe");
		} else {
			exe = new File(jreDirectory, "bin/java");
		}
		if (!exe.isFile()) {
			throw new FileNotFoundException(exe.toString());
		}
		return exe;
	}

	private static int launch(ArrayList<String> cmdarray) throws IOException,
	InterruptedException {
		byte[] buffer = new byte[65536];

		ProcessBuilder processBuilder = new ProcessBuilder(cmdarray);
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		InputStream in = process.getInputStream();
		while (true) {
			int r = in.read(buffer);
			if (r <= 0) {
				break;
			}
			System.out.write(buffer, 0, r);
		}
		return process.waitFor();
	}

	private static String getLibPath(Class<?> c) {
		try {
			return new java.io.File(c.getProtectionDomain()
					  .getCodeSource()
					  .getLocation()
					  .toURI()).getAbsolutePath();
		} catch (URISyntaxException e) {
			JOptionPane.showMessageDialog(null, "An internal program file couldn't be located.", "Load error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	public static void main(String[] args) {
		try {
			String jarPath = getLibPath(MainScreen.class);
			ArrayList<String> cmdarray = new ArrayList<String>();
			cmdarray.add(getJreExecutable().toString());
			if (LaunchJRE.class.getResource("LaunchJRE.class").toString().substring(0,3).equals("jar")) {
				cmdarray.add("-cp");
				cmdarray.add(jarPath);
			}
			else {
				String libPath = getLibPath(AmplitudeAudioInputStream.class);
				libPath += ";"+getLibPath(TSynchronousFilteredAudioInputStream.class);
				libPath += ";"+getLibPath(DirectedGraph.class);
				cmdarray.add("-cp");
				cmdarray.add(jarPath+";"+libPath);
			}
			cmdarray.add("-Xmx1g");
			cmdarray.add("MainScreen");
			launch(cmdarray);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}