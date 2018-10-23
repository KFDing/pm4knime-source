package org.pm4kinme.portobject;

import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Set;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.PetriNetVisualization;

public class PetriNetPortObject extends AbstractPortObject {

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class);

	private PetriNetPortObjectSpec m_spec ;
	private Petrinet net = null;
	private Marking initMarking = null;
	private Marking finalMarking = null;

	private  Set<Marking> finalMarkingSet = null;
	
	private SettingsModelString m_fileName;

	public PetriNetPortObject(final PetriNetPortObjectSpec spec) {
		if(spec != null)
			m_spec = spec;
		else
			m_spec = new PetriNetPortObjectSpec();
	}
	
	public PetriNetPortObject(PetriNetPortObjectSpec petriNetPortObjectSpec, SettingsModelString m_fileName) {
		// TODO Auto-generated constructor stub
		this.m_fileName = m_fileName;
		if(petriNetPortObjectSpec != null)
			m_spec =  petriNetPortObjectSpec;
		else
			// usually we need to create it by using the creator,but I don't really understand its meaning..
			m_spec = new PetriNetPortObjectSpec();
	}

	public PetriNetPortObject() {
		// TODO Auto-generated constructor stub
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
	public PortObjectSpec getSpec() {
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

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	// initialize the PortObject based on the input stream
	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		System.out.println("invoked load method");

	}

	public static class PetriNetPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<PetriNetPortObject> {

	}

}
