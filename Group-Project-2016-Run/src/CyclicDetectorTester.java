
public class CyclicDetectorTester {
	public static void main(String[] args) {
		TrackList tracklist = new TrackList();
		tracklist.add(new Track("alan",
				100,
				0,
				Track.START,
				1,
				tracklist));
		tracklist.add(new Track("bob",
				100,
				1,
				Track.START,
				2,
				tracklist));
		tracklist.add(new Track("carl",
				100,
				2,
				Track.START,
				3,
				tracklist));
		boolean is = tracklist.get(0).willBeCyclic(1);
		if (is) {
			System.out.println("will be rekt");
		}
		else {
			System.out.println("won't be rekt");
		}
	}

}
