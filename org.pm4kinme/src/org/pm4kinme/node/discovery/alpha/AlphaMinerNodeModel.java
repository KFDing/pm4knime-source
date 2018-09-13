package org.pm4kinme.node.discovery.alpha;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4kinme.portobject.PetriNetPortObject;
import org.pm4kinme.portobject.XLogPortObject;
import org.pm4kinme.portobject.XLogPortObjectSpec;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

public class AlphaMinerNodeModel extends NodeModel {

	// TODO: make the different versions of the alpha available through some
	// settings model
	// TODO: publish the marking as a separate output object

	private static final NodeLogger logger = NodeLogger.getLogger(AlphaMinerNodeModel.class);

	protected AlphaMinerNodeModel() {
		super(new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) },
				new PortType[] { PortTypeRegistry.getInstance().getPortType(PetriNetPortObject.class, false) });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		logger.info("start: alpha");
		XLog log = ((XLogPortObject) inObjects[0]).getLog();

		AlphaMinerParameters alphaParams = new AlphaMinerParameters(AlphaVersion.CLASSIC);
		Petrinet net = (Petrinet) AlphaMinerPlugin.apply(
				PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(AlphaMinerPlugin.class), log,
				new XEventNameClassifier(), alphaParams)[0];
		PetriNetPortObject po = new PetriNetPortObject();
		po.setNet(net);
		logger.info("end: alpha");
		return new PortObject[] { po };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) {
		return new PortObjectSpec[] { new XLogPortObjectSpec() };
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void reset() {
		// TODO Auto-generated method stub

	}

}
