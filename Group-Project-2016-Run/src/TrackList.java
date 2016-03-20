import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.sound.sampled.AudioFormat;
import javax.swing.JOptionPane;
import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;

public class TrackList {
	
	private ArrayList<Track> tracks;
	private ArrayList<ActionListener> actionlisteners;
	private String fileName;
	
	private AudioFormat format;
	private static int currentID = 1;
	
	TrackList() {
		format = null;
		tracks = new ArrayList<Track>();
		actionlisteners = new ArrayList<ActionListener>();
	}

	/*
	TrackList(String name) throws BadFileException, BadPathException {
		tracks = new ArrayList<Track>();
		actionlisteners = new ArrayList<ActionListener>();
		try {
			File scriptFile = new File(name);
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
		format = getHighestQualityFormat();
		
		int highestID = 1;
		
		for(Track t : tracks)
		{
			if(t.getID() > highestID)
				highestID = t.getID();
		}
		
		currentID = highestID;
	}
	*/
	
	public void add(Track newTrack) {
		tracks.add(newTrack);
		updateActionListeners();
	}
	
	public Track get(int index) {
		return tracks.get(index);
	}
	
	public Track remove(int index) {
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
		long currentTime = System.currentTimeMillis();
		
		
	}
	
	public void addActionListener(ActionListener listener) {
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
	
	public String getFileName() {
		return fileName;
	}
	
	private AudioFormat getHighestQualityFormat()
	{
		int numChannels = 1;
		float highestBitRate = 11025;
		int sampleSize = 8;
		for(Track t : tracks)
		{
			AudioFormat f = t.getFormat();
			if(f.getChannels() > numChannels)
			{
				numChannels = f.getChannels();
			}
			
			if(f.getFrameRate() > highestBitRate)
			{
				highestBitRate = f.getFrameRate();
			}
				
			if(f.getSampleSizeInBits() > sampleSize)
			{
				sampleSize = f.getSampleSizeInBits();
			}
		}
		
		return new AudioFormat(highestBitRate, sampleSize, numChannels, true, false);
  	}
	
	public AudioFormat getTrackListFormat()
	{
		return format;
	}

	public static int nextID()
	{
		return currentID++;
	}
	
	public void export(String filename) {
		
	}
	
	public void clear() {
		tracks = new ArrayList<Track>();
		updateActionListeners();
	}
}
