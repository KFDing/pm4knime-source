package org.pm4kinme.portobject.petrinet;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.importing.PnmlImportNet;
import org.processmining.plugins.pnml.importing.PnmlImportUtils;
import org.processmining.plugins.utils.ProvidedObjectHelper;

public class PetriNetPortObject implements PortObject {

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class);
	public static final PortType TYPE_OPTIONAL =
			PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, true);
	
	private PetriNetPortObjectSpec m_spec ;
	private Petrinet net = null;
	private Marking initMarking = null;
	private Marking finalMarking = null;

	PluginContext context ;

	private  Set<Marking> finalMarkingSet = null;
	

	public PetriNetPortObject(final PetriNetPortObjectSpec spec) {
		System.out.println("PN Port Object Constructor with Spec is used ");
		if(spec != null)
			m_spec = spec;
		else
			m_spec = new PetriNetPortObjectSpec();
	}

	public PetriNetPortObject() {
		System.out.println("PN Port Object Default Constructor is used ");
		m_spec = new PetriNetPortObjectSpec();
	}

	public Marking getInitMarking() {
		return initMarking;
	}

	public void setInitMarking(Marking initMarking) {
		this.initMarking = initMarking;
	}

	public Marking getFinalMarking() {
		return finalMarking;
	}

	public void setFinalMarking(Marking finalMarking) {
		this.finalMarking = finalMarking;
	}

	public void setFinalMarking(Set<Marking> s) {
		this.finalMarkingSet = s;
	}
	
	public PluginContext getContext() {
		return context;
	}

	public void setContext(PluginContext context) {
		this.context = context;
	}
	
	public Petrinet getNet() {
		return net;
	}

	public void setNet(Petrinet net) {
		this.net = net;
	}

	@Override
	public String getSummary() {
		return "This port contains a Petri net object";
	}

	@Override
	public PetriNetPortObjectSpec getSpec() {
		return m_spec;
	}

	@Override
	public JComponent[] getViews() {
		if (net != null) {
			PetriNetVisualization visualizer = new PetriNetVisualization();
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
			if (net instanceof Petrinet)
				return new JComponent[] { visualizer.visualize(context, (Petrinet) net) };
			if (net instanceof ResetInhibitorNet)
				return new JComponent[] { visualizer.visualize(context, (ResetInhibitorNet) net) };
			if (net instanceof ResetNet)
				return new JComponent[] { visualizer.visualize(context, (ResetNet) net) };
			if (net instanceof InhibitorNet)
				return new JComponent[] { visualizer.visualize(context, (InhibitorNet) net) };
		}
		return new JComponent[] {};
	}

	
	public PetriNetPortObject loadFrom(final PetriNetPortObjectSpec spec, final PortObjectZipInputStream is)
			throws Exception{
		// first the Spec has the file address to load the file name 
		m_spec = spec;
		// first we need to put the is into a temp file and then read from that file
		PnmlImportUtils utils = new PnmlImportUtils();
		if(context == null) {
			
			context =  PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PnmlImportNet.class);
		}
		Pnml pnml = utils.importPnmlFromStream(context, is, null, -1);
		if (pnml != null) {
			PetrinetGraph netGraph = PetrinetFactory.newPetrinet(pnml.getLabel());
			Object[] result = (Object[]) utils.connectNet(context, pnml, netGraph);
			net = (Petrinet) result[0];
			initMarking = (Marking) result[1];
		}
		return this;
	}

	/**
	 * this is used to save petri net as pnml String, so the InputStream and OutputStream and read and write string.
	 * this method is mainly copied from code PnmlExportNet.class
	 * @return text to output
	 */
	public String toPnmlString() {
		
		try {// mark there, we might use another way to do it !! It causes the background error
			initMarking = context.tryToFindOrConstructFirstObject(Marking.class, InitialMarkingConnection.class,
					InitialMarkingConnection.MARKING, net);
		} catch (ConnectionCannotBeObtained e) {
			// use empty marking\
			initMarking = new Marking();
		}
		
		Collection<Marking> finalMarkings = new HashSet<Marking>();
		try {
			Collection<FinalMarkingConnection> connections = context.getConnectionManager().getConnections(
					FinalMarkingConnection.class, context, net);
			for (FinalMarkingConnection connection : connections) {
				finalMarkings.add((Marking) connection.getObjectWithRole(FinalMarkingConnection.MARKING));
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		
		GraphLayoutConnection layout;
		try {
			layout = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class, context, net);
		} catch (ConnectionCannotBeObtained e) {
			layout = new GraphLayoutConnection(net);
		}
		HashMap<PetrinetGraph, Marking> markedNets = new HashMap<PetrinetGraph, Marking>();
		HashMap<PetrinetGraph, Collection<Marking>> finalMarkedNets = new HashMap<PetrinetGraph, Collection<Marking>>();
		markedNets.put(net, initMarking);
		finalMarkedNets.put(net, finalMarkings);

		Pnml pnml = new Pnml();
		FullPnmlElementFactory factory = new FullPnmlElementFactory();
		synchronized (factory) {
			Pnml.setFactory(factory);
			pnml = pnml.convertFromNet(markedNets, finalMarkedNets, layout);
		}
		pnml.setType(Pnml.PnmlType.PNML);
		updateName(context, pnml, net);
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);
		
		return text;
	}
	
	private static void updateName(PluginContext context, Pnml pnml, PetrinetGraph net) {
		String name = ProvidedObjectHelper.getProvidedObjectLabel(context, net);
		if (name != null) {
			pnml.setName(name);
		}
	}

}
