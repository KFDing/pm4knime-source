package org.pm4knime.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.semantics.petrinet.Marking;

public class PetriNetUtil {

	public static Set<Marking> guessFinalMarking(Petrinet net) {
		// TODO Auto-generated method stub
		List<Place> placeList = getEndPlace(net);
		Set<Marking> finalSet = new HashSet<>();
		for(Place p: placeList) {
			Marking finalMarking = new Marking();
			finalMarking.add(p);
			finalSet.add(finalMarking);
		}
		return finalSet;
	}
	
	public static List<Place> getEndPlace(Petrinet net) {
		// firstly to get all places, if one place has no postset edges, then
		// it is the endPlace
		Collection<Place> places = net.getPlaces();
		Place p;
		List<Place> endp = new ArrayList<>();
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		Iterator<Place> pIterator = places.iterator();
		while (pIterator.hasNext()) {
			p = pIterator.next();
			postset = net.getOutEdges(p);
			if (postset.size() < 1) {
				endp.add(p);
			}
		}
		if (endp.isEmpty()) {
			System.out.println("There is no End Place and create end place");
			// and also the Arc to it 
		}
		return endp;
	}

}
