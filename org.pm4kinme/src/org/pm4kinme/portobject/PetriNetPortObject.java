package org.pm4kinme.portobject;

import java.io.IOException;

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
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.plugins.petrinet.PetriNetVisualization;

public class PetriNetPortObject extends AbstractPortObject {

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class);

	private Petrinet net = null;

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
		return new PetriNetPortObjectSpec();
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
