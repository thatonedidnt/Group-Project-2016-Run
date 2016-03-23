import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LaunchJRE {

	private static boolean isWindows() {
		String os = System.getProperty("os.name");
		if (os == null) {
			throw new IllegalStateException("os.name");
		}
		os = os.toLowerCase();
		return os.startsWith("windows");
	}

	public static File getJreExecutable() throws FileNotFoundException {
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

	public static int launch(ArrayList<String> cmdarray) throws IOException,
	InterruptedException {
		byte[] buffer = new byte[1024];

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

	public static void main(String[] args) {
		try {
			String jarPath = new java.io.File(MainScreen.class.getProtectionDomain()
					  .getCodeSource()
					  .getLocation()
					  .getPath())
					.getAbsolutePath();
			System.out.println(jarPath);
			ArrayList<String> cmdarray = new ArrayList<String>();
			cmdarray.add(getJreExecutable().toString());
			cmdarray.add("-cp "+jarPath);
			cmdarray.add("MainScreen");
			int retValue = launch(cmdarray);
			if (retValue != 0) {
				System.err.println("Error code " + retValue);
			}
			System.out.println(getJreExecutable().toString());
			System.out.println("OK");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}