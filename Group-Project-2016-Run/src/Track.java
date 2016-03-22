
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.tritonus.dsp.ais.AmplitudeAudioInputStream;


public class Track implements Runnable
{
	private static final AudioFormat RECORD_FMT = new AudioFormat(44100, 16, 2, true, false);
	
	private String fileName;
	private double length;
	private double intensity;
	private long lengthInSamples;
	private int relativeTo;
	private boolean startEnd;
	private int ID;
	private AmplitudeAudioInputStream dataStream;
	private TrackList tracklist;
	private Clip soundClip;
	
	private boolean isGood;
	private volatile boolean terminateSound;
	private TargetDataLine line;
	
	public static final int RECORD = 1;
	public static final boolean START = true;
	public static final boolean END = false;
	public static final int TRACK_BEGINNING = 0;
	
	public Track(String fileName, TrackList tracklist)
	{
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
			soundClip = AudioSystem.getClip();
			loadClip();
			loadStream();
		} 
		catch (Exception e)
		{
			isGood = false;
			tracklist.updateActionListeners();
		}
	}
	
	public Track(String fileName, double intensity, int relativeTo, boolean startEnd, int ID, TrackList tracklist)
	{
		this.fileName = fileName;
		this.intensity = intensity;
		this.relativeTo = relativeTo;
		this.startEnd = startEnd;
		this.ID = ID;
		this.tracklist = tracklist;
		isGood = true;
		try 
		{
			soundClip = AudioSystem.getClip();
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

			if (tracklist != null) {
				tracklist.updateActionListeners();
			}
		}
	}
	
	public Track(int mode, TrackList tracklist)
	{
		if(mode == Track.RECORD)
		{
			this.tracklist = tracklist;
			this.ID = tracklist.nextID();
			fileName = "";
			length = -1;
			lengthInSamples = -1;
			relativeTo = Track.TRACK_BEGINNING;
			startEnd = START;
			intensity = 100;
			isGood = true;
			try
			{
				record();
				loadStream();
				soundClip = AudioSystem.getClip();
				loadClip();
				loadStream();
			}
			catch(Exception e)
			{
				isGood = false;
				tracklist.updateActionListeners();
			}
			
		}
	}
	
	public void playNoBlock()
	{
		if(!soundClip.isOpen())
		{
			loadClip();
		}
		soundClip.stop();
		while(soundClip.getFramePosition() != 0) soundClip.setFramePosition(0);
		soundClip.start();
		
	}
	
	public void play()
	{
		playNoBlock();
		terminateSound = false;
		
		Thread t = new Thread(this);
		t.start();
		
		while(soundClip.getFramePosition() == 0);
		while(soundClip.isRunning() && !terminateSound);
		
		t.interrupt();
		stop();
			
		try 
		{
			t.join();
		} 
		catch (InterruptedException e){}
	}
	
	//	FOR PLAY METHOD
	public void run()
	{
		JOptionPane.showMessageDialog(null, "Preview...");
		terminateSound = true;
	}
	
	public void stop()
	{
		if(soundClip != null)
		{
			if(soundClip.isRunning())
				soundClip.stop();
		}
	}
	
	public Clip getSound()
	{
		if(!soundClip.isOpen())
			loadClip();
		return soundClip;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
		try
		{
			loadStream();
			loadClip();
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
		if (this.isGood()) {
			return length;
		}
		return 0;
	}
	
	public void setIntensity(double intensity)
	{
		this.intensity = intensity;
		try {
			if (!soundClip.isOpen()) {
				soundClip.open(dataStream);
			}
			FloatControl volumeMod = (FloatControl)soundClip.getControl(FloatControl.Type.MASTER_GAIN);
			float range = volumeMod.getMinimum();
			range *= ((100.0 - this.intensity) / 100.0);
			volumeMod.setValue(range);
			tracklist.updateActionListeners();
			dataStream.setAmplitudeLog(range);
		}
		catch (LineUnavailableException e) { }
		catch (IOException e) { }
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
		if(soundClip.isOpen())
			soundClip.close();
		try 
		{
			soundClip.open(dataStream);
			FloatControl volumeMod = (FloatControl)soundClip.getControl(FloatControl.Type.MASTER_GAIN);
			float range = volumeMod.getMinimum();
			range *= ((100.0 - this.intensity) / 100.0);
			volumeMod.setValue(range);
		} 
		catch (LineUnavailableException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		soundClip.setFramePosition(0);
	}
	
	private void loadStream() throws IOException, UnsupportedAudioFileException, Exception
	{
		if(dataStream != null)
			dataStream.close();
		dataStream = new AmplitudeAudioInputStream(getConvertedInputStream(AudioSystem.getAudioInputStream(new File(fileName))));
	}
	
	private AudioInputStream getConvertedInputStream(AudioInputStream s) throws Exception
	{
		this.lengthInSamples = getSampleLength(s);
		this.length = getLength(s, this.lengthInSamples);
		if(s.getFormat().matches(tracklist.getTrackListFormat()))
			return s;
		if(AudioSystem.isConversionSupported(tracklist.getTrackListFormat(), s.getFormat()))
		{
			AudioInputStream ret = AudioSystem.getAudioInputStream(tracklist.getTrackListFormat(), s);
			return ret;
		}
		throw new Exception();
	}
	
	private double getLength(AudioInputStream s, long samples)
	{
		AudioFormat f = tracklist.getTrackListFormat();
		return ((double)samples / (double)f.getFrameRate());
	}
	
	private long getSampleLength(AudioInputStream s)
	{
		double frameLength = (double)s.getFrameLength() / (double)s.getFormat().getFrameSize();
		if(s.getFormat().getFrameSize() == 4)
			frameLength /= 2.0;
		double frameRate = (double) s.getFormat().getFrameRate();
		double targetRate = (double) tracklist.getTrackListFormat().getFrameRate();
		double ret = (targetRate / frameRate) * frameLength;
		ret *= ((double)tracklist.getTrackListFormat().getChannels() / (double)s.getFormat().getChannels());
		return (long)ret;
	}
	
	
	public void stopRecord()
	{
		if(line != null)
			line.stop();
	}

	public void record() throws LineUnavailableException, Exception
	{
		File temp = new File("temp.wav");
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, RECORD_FMT);
		if(!AudioSystem.isLineSupported(info))
			throw new LineUnavailableException();
		
		line = (TargetDataLine)AudioSystem.getLine(info);
		line.open(RECORD_FMT);
		
		AudioInputStream s = new AudioInputStream(line);
		line.start();
		Thread t = new Thread(new AudioWaiter(this));
		t.start();
		try 
		{
			AudioSystem.write(s, AudioFileFormat.Type.WAVE, temp);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		File save = tracklist.saveDialog("wav", "WAVE File");
		if(save == null)
		{
			temp.delete();
			throw new Exception();	
		}
		temp.renameTo(save);
		line.close();
		try 
		{
			s.close();
		} 
		catch(IOException e){}
		this.fileName = save.getAbsolutePath();
		File delTemp = new File("temp.wav");
		delTemp.delete();
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

class AudioWaiter implements Runnable
{
	private Track t;
	public AudioWaiter(Track recorder)
	{
		t = recorder;
	}

	@Override
	public void run()
	{
		JOptionPane.showMessageDialog(null, "Recording....");
		t.stopRecord();
	}
	
}
