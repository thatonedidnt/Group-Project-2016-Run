import java.util.LinkedList;

import org.jgrapht.Graph;


public class GraphUtility {
	public static <V,E> void removeAllEdges(Graph<V, E> graph) {
		LinkedList<E> copy = new LinkedList<E>();
		for (E e : graph.edgeSet()) {
			copy.add(e);
		}
		graph.removeAllEdges(copy);
	}

	public static <V,E> void clearGraph(Graph<V, E> graph) {
		removeAllEdges(graph);
		removeAllVertices(graph);
	}
	
	public static <V,E> void removeAllVertices(Graph<V, E> graph) {
		LinkedList<V> copy = new LinkedList<V>();
		for (V v : graph.vertexSet()) {
			copy.add(v);
		}
		graph.removeAllVertices(copy);
	}
}
