package org.pm4kinme.portobject.petrinet;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.PetriNetVisualization;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.base.Pnml.PnmlType;
import org.processmining.plugins.pnml.base.PnmlElementFactory;


public class PetriNetPortObject  extends AbstractPortObject{

	public static final class Serializer extends AbstractPortObjectSerializer<PetriNetPortObject>{};
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
	
	public PetriNetPortObject(final PetriNetPortObject obj,final PetriNetPortObjectSpec spec) {
		System.out.println("PN Port Object Constructor with Spec is used ");
		if(spec == null || obj == null)
			throw new NullPointerException("Argument must not be null.");
		this.net = obj.getNet();
		this.initMarking = obj.getInitMarking();
		this.finalMarkingSet = obj.getFinalMarkingSet();
		
		m_spec = spec;
	}

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
		if(finalMarking == null && finalMarkingSet !=null)
			finalMarking = finalMarkingSet.iterator().next();
		
		return finalMarking;
	}

	public Set<Marking> getFinalMarkingSet(){
		if(finalMarkingSet == null && finalMarking!=null) {
			finalMarkingSet = new HashSet<>();
			finalMarkingSet.add(finalMarking);
		}
		return finalMarkingSet;
	}
	
	public void setFinalMarking(Marking finalMarking) {
		this.finalMarking = finalMarking;
	}
	public void setFinalMarkingSet(Set<Marking> fSet) {
		finalMarkingSet = fSet;
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

	public String convert2String() {
		GraphLayoutConnection layout;
		if(context == null) {
			context =  PM4KNIMEGlobalContext.instance().getPM4KNIMEPluginContext();
		}
		try {
			layout = context.getConnectionManager().getFirstConnection(GraphLayoutConnection.class,
					context, net);
		} catch (ConnectionCannotBeObtained e) {
			layout = new GraphLayoutConnection(net);
		}

		PnmlElementFactory factory = new FullPnmlElementFactory();
		Pnml pnml = new Pnml();
		synchronized (factory) {
			pnml.setFactory(factory);
			pnml = new Pnml().convertFromNet(net, initMarking, finalMarkingSet, layout);
			pnml.setType(PnmlType.PNML);
		}
		String text = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + pnml.exportElement(pnml);
		return text;
	}
	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO save the object into a file
		// here a bit complicated, due to no way to transform out into file 
		// out.putNextEntry(new ZipEntry(TYPE.getName()));
	
		out.write(this.convert2String().getBytes());
		out.close();
	}

	
	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO load this object from Inputstream in
		//ZipEntry nextEntry = in.getNextEntry();
		// here we only get the class name
		//String typeName = nextEntry.getName();
		// check the spec type is they are the same??
		
		
		if(context == null) {
			context =  PM4KNIMEGlobalContext.instance().getPM4KNIMEPluginContext();
		}
		// Pnml pnml = utils.importPnmlFromStream(context, is, null, -1);
		AcceptingPetriNet aNet;
		try {
			aNet = AcceptingPetriNetFactory.importFromStream(context, in);
			if (aNet != null) {
				// modify there like the acception graph?? We can use it right?? 
				
				net = aNet.getNet();
				initMarking = aNet.getInitialMarking();
				
				finalMarkingSet = aNet.getFinalMarkings();
				// in default the first one is as finalMarking
				finalMarking = finalMarkingSet.iterator().next();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
