
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

class ClippingInputStream extends AudioInputStream
{	
	private long sampleLength;
	private List<Long> frameStartList;
	private List<Track> trackList;
	private long curSample;
	
	public ClippingInputStream(TrackList t) 
	{
		super(null, t.getTrackListFormat(), 0);
		sampleLength = t.totalLengthInSamples();
		frameStartList = new ArrayList<Long>();
		trackList = new ArrayList<Track>();
		
		for(int a=0;a<t.numTracks();a++)
		{
			trackList.add(t.get(a));
			frameStartList.add(t.get(a).startSample());
		}
	}
	
	
	@Override
	public long getFrameLength()
	{
		return sampleLength;
	}
	
	public int read(byte[] buffer, int off, int len) throws IOException
	{
		int[] byteSums = new int[len];
		byte[] singleRead = new byte[len];
		int totalReads = 0;
		int maxLen = 0;
		for(int a=0;a<trackList.size();a++)
		{
			Track t = trackList.get(a);
			int frameSize = t.getFormat().getFrameSize();
			if(t.startSample() * frameSize < (curSample + len) && curSample < (t.startSample() + t.getLengthInSamples()) * frameSize)
			{
				totalReads++;
				long samplesToRead;
				int offset = 0;
				if(curSample < t.startSample() * frameSize)
				{
					samplesToRead = (curSample + len) - t.startSample() * frameSize;
					offset = (int)((t.startSample() * frameSize) - curSample);
				}
				else if((curSample + len) > (t.startSample() + t.getLengthInSamples()) * frameSize)
				{
					samplesToRead = (t.startSample() + t.getLengthInSamples()) * frameSize - (curSample);
				}
				else
				{
					samplesToRead = len;
				}
				t.getDataStream().read(singleRead, offset, (int)samplesToRead);
				for(int b=0;b<samplesToRead;b++)
				{
					byteSums[b + offset] += singleRead[b + offset];
				}
				maxLen = Math.max((int)samplesToRead, maxLen);
			}
		}
		for(int a=0;a<len;a++)
		{
			if(byteSums[a] > Byte.MAX_VALUE)
				byteSums[a] = Byte.MAX_VALUE;
			if(byteSums[a] < Byte.MIN_VALUE)
				byteSums[a] = Byte.MIN_VALUE;
			buffer[a] = (byte)(byteSums[a]);
		}
		curSample += (len);
		if(totalReads == 0)
			return -1;
		else
			return maxLen;
	}
}






public class TrackList implements Runnable
{
	private class StopDialog extends JDialog implements ActionListener {
		private static final long serialVersionUID = 6107977679622241260L;
		
		private TrackList list;
		
		StopDialog(JFrame frame, String text, boolean iDontKnowWhatThisIs, TrackList list) {
			super(frame, text, iDontKnowWhatThisIs);
			this.list = list;
		}

		@Override
		public void actionPerformed(ActionEvent ev) {
			if (ev.getActionCommand().equals("stopPlay")) {
				list.terminateSound = true;
				this.dispose();
			}
		}
	}
	
	private static final Track START = new Track("",
			100,
			-1,
			Track.START,
			0,
			null);
	
	private StopDialog dialog;
	private ArrayList<Track> tracks;
	private ArrayList<ActionListener> actionlisteners;
	private String fileName;
	private JFrame parentFrame = null;

	private AudioFormat format;

	private volatile boolean terminateSound;

	public TrackList() {
		format = null;
		tracks = new ArrayList<Track>();
		actionlisteners = new ArrayList<ActionListener>();
		format = new AudioFormat(44100, 16, 2, true, false);
	}

	public void add(Track newTrack) {
		tracks.add(newTrack);
//		this.format = getHighestQualityFormat();
		updateActionListeners();
	}

	public Track get(int index) {
		return tracks.get(index);
	}
	
	public Track getByID(int ID)
	{
		if (ID == 0) {
			return TrackList.START;
		}
		else {
			for(Track t : tracks)
			{
				if(t.getID() == ID)
					return t;
			}
			return null;
		}
	}

	public Track remove(int index) {
		int newRelID = 0;
		if (this.get(index).startTime() != 0) {
			double closest = this.totalLength();
			for (Track t : this.getTracks()) {
				if (t == this.get(index)) continue;
				if ((this.get(index).startTime()-(t.startTime()+t.getLength()) >= 0) && (this.get(index).startTime()-(t.startTime()+t.getLength()) < closest)) {
					newRelID = t.getID();
				}
			}
		}
		for (Track t : this.getTracks()) {
			if (t.getRelativeID() == this.get(index).getID()) {
				t.setRelativeTo(newRelID);
			}
		}
		Track track = tracks.remove(index);
		updateActionListeners();
		return track;
	}

	public int numTracks() {
		return tracks.size();
	}

	public ArrayList<Track> failedTracks() {
		ArrayList<Track> failedTracks = new ArrayList<Track>();
		for (Track track : tracks) {
			if (!track.isGood()) {
				failedTracks.add(track);
			}
		}
		return failedTracks;
	}

	public ArrayList<Track> getTracks() {
		return tracks;
	}

	public double totalLength(){
		double end = 0;
		for(Track t : tracks)
		{
			if(t.startTime() + t.getLength() > end)
				end = t.startTime() + t.getLength();
		}
		return end;
	}

	public long totalLengthInSamples()
	{
		long end = 0;
		for(Track t : tracks)
		{
			if(t.startSample() + t.getLengthInSamples() > end)
				end = t.startSample() + t.getLengthInSamples();
		}
		return end;
	}
	
	public void play() 
	{
		if(failedTracks().size() > 0) {
			ArrayList<String> failedFilenames = new ArrayList<String>();
			for (Track track : this.failedTracks()) {
				failedFilenames.add(track.getFileName());
			}
			new FileNotFound(failedFilenames);
			return;
		}
		
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				JOptionPane pane = new JOptionPane("Playing Script...", JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
				dialog = new StopDialog((JFrame)parentFrame, "Preview", false, TrackList.this);
				dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				dialog.setResizable(false);
				dialog.setAlwaysOnTop(true);
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
				dialog.add(pane);
				dialog.addWindowListener(new WindowAdapter() {
					public void windowClosed(WindowEvent ev) {
						terminateSound = true;
					}
					
					public void windowClosing(WindowEvent ev) {
						terminateSound = true;
					}
				});
				dialog.pack();
				dialog.setVisible(true);

			}
		});
		Thread t = new Thread(this);
		t.start();
	}
	
	public void run() {
		double currentTime = 0;
		terminateSound = false;

		long beginTime = System.currentTimeMillis();
		//ArrayList<Integer> playedIDs = new ArrayList<Integer>();
		boolean[] playedAlready = new boolean[this.numTracks()];
		
		while(currentTime < totalLength() && !terminateSound)
		{
			//currentTime += ((double)(System.currentTimeMillis() - lastTime)) / (1000.0);
			//lastTime = System.currentTimeMillis();
			currentTime = (System.currentTimeMillis() - beginTime)/1000.0;
			
			for (int i = 0; i < this.numTracks(); ++i) {
				if (!playedAlready[i] && (this.get(i).startTime() < currentTime)) {
					playedAlready[i] = true;
					Thread t = new Thread(this.get(i));
					t.start();
				}
			}
		}
		for(Track track : tracks)
		{
			track.stop();
		}
		for(Track track : tracks)
		{
			try 
			{
				track.loadStream();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		dialog.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "stopPlay"));
		terminateSound = true;
	}

	public void addActionListener(ActionListener listener) 
	{
		actionlisteners.add(listener);
	}

	public void updateActionListeners() {
		for (ActionListener listener : actionlisteners) {
			ActionEvent ev = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "updateScript");
			listener.actionPerformed(ev);
		}
		try {
			if (this.getFileName() != null) {
				this.save(this.getFileName());
			}
		} catch (BadPathException ex) {
			JOptionPane.showMessageDialog(null, "The path for saving the script isn't accessible.");
		}
	}

	public void save(String filename) throws BadPathException {
		try {
			DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dBuilderFactory.newDocumentBuilder();
			Document script = dBuilder.newDocument();
			Element rootElement = script.createElement("script");
			script.appendChild(rootElement);
			
			for (Track track : this.getTracks()) {
				Element trackElement = script.createElement("track");
				rootElement.appendChild(trackElement);
				
				Attr id = script.createAttribute("id");
				id.setValue(Integer.toString(track.getID()));
				trackElement.setAttributeNode(id);
				
				Element filename_xml = script.createElement("filename");
				trackElement.appendChild(filename_xml);
				filename_xml.appendChild(script.createTextNode(track.getFileName()));
				trackElement.appendChild(filename_xml);
				
				Element relativeTo = script.createElement("relativeto");
				trackElement.appendChild(relativeTo);
				relativeTo.appendChild(script.createTextNode(Integer.toString(track.getRelativeID())));
				trackElement.appendChild(relativeTo);
				
				Element relativePosition = script.createElement("relativeposition");
				trackElement.appendChild(relativePosition);
				String relativePositionString = "";
				if (track.getStartEnd() == Track.START) {
					relativePositionString = "start";
				}
				else {
					relativePositionString = "end";
				}
				relativePosition.appendChild(script.createTextNode(relativePositionString));
				trackElement.appendChild(relativePosition);
				
				Element intensity = script.createElement("intensity");
				trackElement.appendChild(intensity);
				intensity.appendChild(script.createTextNode(Double.toString(track.getIntensity())));
				trackElement.appendChild(intensity);
				
				Element length = script.createElement("length");
				trackElement.appendChild(length);
				length.appendChild(script.createTextNode("0"));
				trackElement.appendChild(length);
			}
			
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(script);
			File file = new File(filename);
			StreamResult sresult = new StreamResult(file);
			transformer.transform(source, sresult);
		}
		catch (ParserConfigurationException ex) {
			throw new BadPathException();
		} catch (TransformerException ex) {
			throw new BadPathException();
		}
	}
	
	public void setFileName(String fileName) throws BadFileException, BadPathException {
		tracks = new ArrayList<Track>();
		try {
			this.fileName = fileName;
			File scriptFile = new File(fileName);
			DocumentBuilderFactory bFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = bFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(scriptFile);
			if (doc.getDocumentElement().getNodeName().toLowerCase().equals("script")) {
				NodeList nList = doc.getElementsByTagName("track");
				for (int i = 0; i < nList.getLength(); ++i) {
					Node currNode = nList.item(i);
					if (currNode.getNodeType() == Node.ELEMENT_NODE) {
						Element element = (Element)currNode;
						String id = element.getAttribute("id");
						String trackName = element.getElementsByTagName("filename").item(0).getTextContent();
						String relativeTo = element.getElementsByTagName("relativeto").item(0).getTextContent();
						String relativePos = element.getElementsByTagName("relativeposition").item(0).getTextContent();
						String intensity = element.getElementsByTagName("intensity").item(0).getTextContent();
						
						boolean startend = false;
						if (relativePos.equals("start")) {
							startend = Track.START;
						}
						else {
							startend = Track.END;
						}
						Track track = new Track(
								trackName,
								Double.parseDouble(intensity),
								Integer.parseInt(relativeTo),
								startend,
								Integer.parseInt(id),
								this);
						this.add(track);
					}
				}
			}
			else {
				throw new BadFileException();
			}
		}
		catch (ParserConfigurationException e) {
			throw new BadFileException();
		}
		catch (SAXException e) {
			throw new BadFileException();
		}
		catch (IOException e) {
			throw new BadPathException();
		}
		updateActionListeners();
	}
	
	public void export(String fileName) throws BadPathException
	{
		if(failedTracks().size() > 0) {
			ArrayList<String> failedFilenames = new ArrayList<String>();
			for (Track track : this.failedTracks()) {
				failedFilenames.add(track.getFileName());
			}
			new FileNotFound(failedFilenames);
			return;
		}
		
		try {
			for(Track track : tracks)
			{
				track.loadStream();
			}
			File wavFile = new File(fileName);
			ClippingInputStream outStream = new ClippingInputStream(this);
			AudioSystem.write(outStream, AudioFileFormat.Type.WAVE, wavFile);
			for(Track track : tracks)
			{
				track.loadStream();
			}
		} 
		catch(IOException e)
		{
			throw new BadPathException();
		} 
		catch(UnsupportedAudioFileException e) 
		{
			e.printStackTrace();
		}
		catch(Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	public String getFileName() {
		return fileName;
	}

//	private AudioFormat getHighestQualityFormat()
//	{
//		int numChannels = 1;
//		float highestBitRate = 11025;
//		int sampleSize = 8;
//		for(Track t : tracks)
//		{
//			AudioFormat f = t.getFormat();
//			if(f.getChannels() > numChannels)
//			{
//				numChannels = f.getChannels();
//			}
//
//			if(f.getFrameRate() > highestBitRate)
//			{
//				highestBitRate = f.getFrameRate();
//			}
//
//			if(f.getSampleSizeInBits() > sampleSize)
//			{
//				sampleSize = f.getSampleSizeInBits();
//			}
//		}
//
//		return new AudioFormat(highestBitRate, sampleSize, numChannels, true, false);
//	}

	public AudioFormat getTrackListFormat()
	{
		return format;
	}

	public int nextID()
	{
		int highestID = 0;
		for (Track t : tracks) {
			if (t.getID() > highestID) highestID = t.getID();
		}
		return highestID + 1;
	}
	
	public File saveDialog(String fileExtension, String extensionDescription)
	{
		JFileChooser open = new JFileChooser();
		open.setFileFilter(new FileNameExtensionFilter(extensionDescription, fileExtension));
		open.showSaveDialog(null);
		if(!open.getSelectedFile().getAbsolutePath().endsWith(".wav"))
			return new File(open.getSelectedFile().getAbsolutePath() + "." + fileExtension);
		else
			return new File(open.getSelectedFile().getAbsolutePath());
			
	}
	
	public void clear() {
		tracks = new ArrayList<Track>();
		updateActionListeners();
	}
	
	public int getIndexByID(int ID) {
		for (int i = 0; i < this.numTracks(); ++i) {
			if (this.get(i).getID() == ID) {
				return i;
			}
		}
		return -1;
	}
	
	public void setParentFrame(JFrame frame) {
		this.parentFrame = frame;
	}
}
