
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.tritonus.dsp.ais.AmplitudeAudioInputStream;


public class Track implements Runnable
{
	public static final AudioFormat RECORD_FMT = new AudioFormat(44100, 16, 2, true, false);
	
	private String fileName;
	private double length;
	private double intensity;
	private long lengthInSamples;
	private int relativeTo;
	private boolean startEnd;
	private int ID;
	private AmplitudeAudioInputStream dataStream;
	private TrackList tracklist;
	private SourceDataLine soundStream;
	private AudioInputStream soundStreamBuffer;
	
	private boolean isGood;
	private volatile boolean terminateSound;
	private JDialog playDialog;
	private JDialog recordDialog;

	public static final boolean START = true;
	public static final boolean END = false;
	private final AudioRecorder recorder;
	
	public Track(String fileName, TrackList tracklist)
	{
		recorder = null;
		this.fileName = fileName;
		intensity = 100;
		ID = tracklist.nextID();
		relativeTo = 0;
		startEnd = START;
		this.tracklist = tracklist;
		isGood = true;
		try 
		{
			loadStream();
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, TrackList.getTrackListFormat());
			soundStream = (SourceDataLine)AudioSystem.getLine(info);
			soundStream.open(TrackList.getTrackListFormat());
			loadClip();
		} 
		catch (Exception e)
		{
			isGood = false;
		}
		if (tracklist != null) {
			tracklist.updateActionListeners();
		}
	}
	
	public Track(String fileName, double intensity, int relativeTo, boolean startEnd, int ID, TrackList tracklist)
	{
		recorder = null;
		this.fileName = fileName;
		this.intensity = intensity;
		this.relativeTo = relativeTo;
		this.startEnd = startEnd;
		this.ID = ID;
		this.tracklist = tracklist;
		isGood = true;
		try 
		{
			DataLine.Info info = new DataLine.Info(SourceDataLine.class, TrackList.getTrackListFormat());
			soundStream = (SourceDataLine)AudioSystem.getLine(info);
			soundStream.open(TrackList.getTrackListFormat());
			loadClip();
		} 
		catch(LineUnavailableException e) 
		{
			e.printStackTrace();
		}
		try 
		{
			loadStream();
		} 
		catch (Exception e)
		{
			isGood = false;
		}
		if (tracklist != null) {
			tracklist.updateActionListeners();
		}
	}
	
	public void makeMeBad()
	{
		this.isGood = false;
	}
	
	public static void recordTrack(TrackList tracklist)
	{
		try 
		{
			Track temp = new Track(tracklist);
			temp.record();
		} 
		catch(LineUnavailableException e) 
		{
		}
		catch(Exception e) 
		{
		}

	}


	public Track(TrackList tracklist) throws LineUnavailableException
	{
		recorder = new AudioRecorder(this, tracklist);
		this.tracklist = tracklist;

	}
	
	public void playNoBlock()
	{
		Thread t = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				soundStream.start();
				int bytesRead = -1;
				byte b[] = new byte[4096];
				try {
					while((bytesRead = soundStreamBuffer.read(b)) != -1)
					{
						soundStream.write(b, 0, bytesRead);
					}
				}
				catch (IOException e) {}
				soundStream.flush();
				stop();
			}
			
		});
		t.start();
	}
	
	public void play()
	{
		if (!this.isGood()) {
			ArrayList<String> failedNames = new ArrayList<String>();
			for (Track t : tracklist.failedTracks()) {
				failedNames.add(t.getFileName());
			}
			new FileNotFound(failedNames);
			return;
		}
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JOptionPane pane = new JOptionPane("Previewing Track...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.CANCEL_OPTION, null, new String[]{"Cancel"});
				playDialog = new JDialog((JFrame)null, "Preview", false);
				playDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				playDialog.setModal(true);
				playDialog.setResizable(false);
				pane.addPropertyChangeListener(new PropertyChangeListener()
				{

					@Override
					public void propertyChange(PropertyChangeEvent arg0)
					{
						if(arg0.getPropertyName().equals("value"))
						{
							terminateSound = true;
						}
					}
				});
				playDialog.add(pane);
				playDialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent ev) {
						terminateSound = true;
					}
					
					@Override
					public void windowClosing(WindowEvent ev) {
						terminateSound = true;
					}
				});
				playDialog.pack();
				playDialog.setVisible(true);

			}
		});
		Thread t = new Thread(this);
		t.start();
	}
	
	//	FOR PLAY METHOD
	@Override
	public void run()
	{
		terminateSound = false;
		soundStream.start();
		int bytesRead = -1;
		byte b[] = new byte[4096];
		try {
			while((bytesRead = soundStreamBuffer.read(b)) != -1 && !terminateSound)
			{
				soundStream.write(b, 0, bytesRead);
				Thread.sleep(1);
			}
		}
		catch (IOException e) {}
		catch (InterruptedException ex) {}
		if(!terminateSound)
			soundStream.drain();
		else
			soundStream.flush();
		stop();
		if(playDialog != null && playDialog.isVisible())
			playDialog.dispose();
	}
	
	public void stop()
	{
		if(soundStream != null)
		{
			soundStream.stop();
			loadClip();
		}
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
		try
		{
			loadStream();
			loadClip();
			isGood = true;
			tracklist.updateActionListeners();
		}
		catch(Exception e) 
		{
			isGood = false;
			tracklist.updateActionListeners();
		}
	}
	
	public String getFileName()
	{
		return fileName;
	}
	
	public double getLength()
	{
		if (this.isGood()) 
		{
			return length;
		}
		return 0;
	}
	
	public void setIntensity(double intensity)
	{
		this.intensity = intensity;
		if (soundStream != null && dataStream!= null)
		{
			FloatControl volumeMod = (FloatControl)soundStream.getControl(FloatControl.Type.MASTER_GAIN);
			float range = volumeMod.getMinimum();
			range *= Math.pow(((100.0 - this.intensity) / 100.0), 2.2); //adjust curve of how much intensity affects volume
			volumeMod.setValue(range);
			tracklist.updateActionListeners();
			dataStream.setAmplitudeLog(range);
		}
	}
	
	public double getIntensity()
	{
		return intensity;
	}
	
	public void setRelativeTo(int ID) 
	{
		this.relativeTo = ID;
		tracklist.updateActionListeners();
	}
	
	public int getRelativeID() 
	{
		return relativeTo;
	}
	
	public void setStartEnd(boolean startEnd) 
	{
		this.startEnd = startEnd;
		tracklist.updateActionListeners();
	}
	
	public boolean getStartEnd()
	{
		if (this.getRelativeID() == 0) return Track.START;
		return startEnd;
	}
	
	public boolean isGood()
	{
		return isGood;
	}
	
	public double startTime()
	{
		if(relativeTo == 0)
			return 0;
		Track relativeTrack = tracklist.getByID(relativeTo);
		double relativeTime = relativeTrack.startTime();
		if(startEnd == START) //relative to beginning
			return relativeTime;
		else
			return relativeTime + relativeTrack.getLength();
	}
	
	public int getID()
	{
		return ID;
	}
	
	public AudioFormat getFormat()
	{
		if (dataStream != null) {
			return dataStream.getFormat();
		}
		else {
			return new AudioFormat(8000,8,1,false,false);
		}
	}
	
	public long getLengthInSamples()
	{
		return lengthInSamples;
	}
	
	public long startSample()
	{
		if(relativeTo == 0)
			return 0;
		Track relativeTrack = tracklist.getByID(relativeTo);
		long relativeSample = relativeTrack.startSample();
		if(startEnd == START) //relative to beginning
			return relativeSample;
		else
			return relativeSample + relativeTrack.getLengthInSamples();
	}

	public AudioInputStream getDataStream()
	{
		return dataStream;
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private void loadClip()
	{
		try 
		{
			if(soundStreamBuffer != null)
				soundStreamBuffer.close();
			soundStreamBuffer = AudioSystem.getAudioInputStream(TrackList.getTrackListFormat(), AudioSystem.getAudioInputStream(new File(fileName)));
			FloatControl volumeMod = (FloatControl)soundStream.getControl(FloatControl.Type.MASTER_GAIN);
			float range = volumeMod.getMinimum();
			range *= ((100.0 - this.intensity) / 100.0);
			volumeMod.setValue(range);
		} 
		catch (IOException | UnsupportedAudioFileException e) 
		{
			isGood = false;
			return;
		}
	}
	
	public void loadStream() throws IOException, UnsupportedAudioFileException, Exception
	{
		if(dataStream != null)
			dataStream.close();
		dataStream = new AmplitudeAudioInputStream(getConvertedInputStream(AudioSystem.getAudioInputStream(new File(fileName))));
		setIntensity(this.getIntensity());
	}
	
	private AudioInputStream getConvertedInputStream(AudioInputStream s) throws Exception
	{
		this.lengthInSamples = getSampleLength(s);
		this.length = getLength(s, this.lengthInSamples);
		if(s.getFormat().matches(TrackList.getTrackListFormat()))
			return s;
		if(AudioSystem.isConversionSupported(TrackList.getTrackListFormat(), s.getFormat()))
		{
			AudioInputStream ret = AudioSystem.getAudioInputStream(TrackList.getTrackListFormat(), s);
			return ret;
		}
		throw new Exception();
	}
	
	private double getLength(AudioInputStream s, long samples)
	{
		AudioFormat f = TrackList.getTrackListFormat();
		return ((double)samples / (double)f.getFrameRate());
	}
	
	private long getSampleLength(AudioInputStream s)
	{
		double frameLength = (double)s.getFrameLength();
		if(s.getFormat().getSampleSizeInBits() == 32)//why????
			frameLength /= 4;
		double frameRate = (double) s.getFormat().getFrameRate();
		double targetRate = (double) TrackList.getTrackListFormat().getFrameRate();
		double ret = (targetRate / frameRate) * frameLength;
		return (long)ret;
	}
	
	private void record() throws LineUnavailableException, Exception
	{
		Thread t = new Thread(recorder);
		t.start();
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JOptionPane pane = new JOptionPane("Recording...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
				recordDialog = new JDialog((JFrame)null, "Record", false);
				recordDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				pane.addPropertyChangeListener(new PropertyChangeListener()
				{

					@Override
					public void propertyChange(PropertyChangeEvent arg0)
					{
						if(arg0.getPropertyName().equals("value"))
						{
							recorder.stopRecord();
							recordDialog.dispose();
						}
					}

				});
				recordDialog.add(pane);
				recordDialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent ev) {
						recorder.stopRecord();
					}
					
					@Override
					public void windowClosing(WindowEvent ev) {
						recorder.stopRecord();
					}
				});
				recordDialog.pack();
				recordDialog.setVisible(true);

			}
		});
	}
	
	public String getShortFileName() {
		File file = new File(this.getFileName());
		return file.getName();
	}
	
	public boolean willBeCyclic(int relID) { //checks if switching to the provided ID results in a loop of relativeTo
		DirectedGraph<Track, DefaultEdge> relativeTos = new DefaultDirectedGraph<Track, DefaultEdge>(DefaultEdge.class);
		relativeTos.addVertex(tracklist.getByID(0));
		for (Track t : tracklist.getTracks()) {
			relativeTos.addVertex(t);
		}
		for (Track t : tracklist.getTracks()) {
			if (t == this) {
				relativeTos.addEdge(this, tracklist.getByID(relID));
			}
			else {
				relativeTos.addEdge(t, tracklist.getByID(t.getRelativeID()));
			}
		}
		
		CycleDetector<Track, DefaultEdge> cycledetector = new CycleDetector<Track, DefaultEdge>(relativeTos);
		
		return cycledetector.detectCycles();
	}
}

class AudioRecorder implements Runnable
{
	private Track track;
	private TrackList tracklist;
	private TargetDataLine line;

	public AudioRecorder(Track recorder, TrackList tl) throws LineUnavailableException
	{
		line = (TargetDataLine)AudioSystem.getLine(new DataLine.Info(TargetDataLine.class, Track.RECORD_FMT));
		line.open(Track.RECORD_FMT);
		track = recorder;
		this.tracklist = tl;
	}

	@Override
	public void run()
	{
		File temp = new File(System.getProperty("java.io.tmpdir")+"temp.wav");
		try {
			temp = File.createTempFile(System.getProperty("java.io.tmpdir")+"recording-temp", ".wav");
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "The path for saving the temporary recording isn't accessible.", "Temporary Recording Path Error", JOptionPane.ERROR_MESSAGE);
			track.makeMeBad();
			return;
		}
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, Track.RECORD_FMT);
		if(!AudioSystem.isLineSupported(info))
		{
			track.makeMeBad();
			return;
		}
		try
		{
			line.open(Track.RECORD_FMT);
		}
		catch(Exception e)
		{
			track.makeMeBad();
			return;
		}

		AudioInputStream recordStream = new AudioInputStream(line);
		line.start();
		try 
		{
			AudioSystem.write(recordStream, AudioFileFormat.Type.WAVE, temp);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, "The path for saving the temporary recording isn't accessible.", "Temporary Recording Path Error", JOptionPane.ERROR_MESSAGE);
			track.makeMeBad();
			return;
		}
		File save = tracklist.saveDialog("wav", "WAVE File (*.wav)");
		if(save == null)
		{
			temp.delete();
			track.makeMeBad();
			return;
		}
		if (save.exists()) {
			save.delete();
		}
		temp.renameTo(save);
		line.close();

		try 
		{
			recordStream.close();
		} 
		catch(IOException e){}

		track.setFileName(save.getAbsolutePath());
		File delTemp = new File("temp.wav");
		delTemp.delete();
		tracklist.add(new Track(save.getAbsolutePath(), tracklist));
		tracklist.updateActionListeners();
	}

	public void stopRecord()
	{
		if(line != null && line.isRunning())
		{
			line.stop();
		}
	}
}
