import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;


public class Track implements Runnable
{
	private String fileName;
	private double length;
	private double intensity;
	private long lengthInSamples;
	private int relativeTo;
	private boolean startEnd;
	private int ID;
	private AudioInputStream dataStream;
	private TrackList tracklist;
	private Clip soundClip;
	
	private boolean isGood;
	private volatile boolean terminateSound;
	
	public static final int RECORD = 1;
	public static final boolean START = true;
	public static final boolean END = false;
	public static final int TRACK_BEGINNING = 0;
	
	public Track(String fileName, TrackList tracklist)
	{
		this.fileName = fileName;
		intensity = 100;
		ID = TrackList.nextID();
		relativeTo = 0;
		startEnd = START;
		this.tracklist = tracklist;
		isGood = true;
		try 
		{
			soundClip = AudioSystem.getClip();
		} 
		catch(LineUnavailableException e) 
		{}
		try 
		{
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

			tracklist.updateActionListeners();
		}
	}
	
	Track(int mode, TrackList tracklist)
	{
		this.tracklist = tracklist;
		fileName = "";
		length = -1;
		lengthInSamples = -1;
		relativeTo = Track.TRACK_BEGINNING;
		startEnd = START;
		intensity = 100;
		isGood = true;
	}
	
	public void playNoBlock()
	{
		try 
		{
			loadStream();
		}
		catch (Exception e)
		{
			isGood = false;
			tracklist.updateActionListeners();
		}
		soundClip.start();
	}
	
	public void play()
	{
		playNoBlock();
		terminateSound = false;
		Thread t = new Thread(this);
		t.start();
		while(soundClip.isRunning() && !terminateSound);
		t.interrupt();
	}
	
	//	FOR PLAY METHOD
	public void run()
	{
		JOptionPane.showMessageDialog(null, "Preview...");
		terminateSound = true;
	}
	
	public Clip getSound()
	{
		reloadClip();
		return soundClip;
	}

	public void setFileName(String fileName)
	{
		this.fileName = fileName;
		try
		{
			loadStream();
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
		return length;
	}
	
	public void setIntensity(double intensity)
	{
		this.intensity = intensity;
		tracklist.updateActionListeners();
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
		Track relativeTrack = tracklist.get(relativeTo);
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
		return dataStream.getFormat();
	}
	
	//~~

	private void reloadClip()
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
		dataStream = getConvertedInputStream(AudioSystem.getAudioInputStream(new File(fileName)));
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
		double frameLength = s.getFrameLength();
		double frameRate = (double) s.getFormat().getFrameRate();
		double targetRate = (double) tracklist.getTrackListFormat().getFrameRate();
		double ret = (targetRate / frameRate) * frameLength;
		frameLength *= ((double)tracklist.getTrackListFormat().getChannels() / (double)s.getFormat().getChannels());
		return (long)ret;
	}

	
}
