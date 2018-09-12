package org.pm4kinme.node.io.read;

import java.io.File;
import java.io.IOException;

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
import org.pm4kinme.portobject.XLogPortObject;
import org.pm4kinme.settingsmodel.XLogReaderNodeSettingsModel;
import org.processmining.plugins.log.OpenNaiveLogFilePlugin;

public class XLogReaderNodeModel extends NodeModel {

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(XLogReaderNodeModel.class);
	private final XLogReaderNodeSettingsModel params = new XLogReaderNodeSettingsModel();

	protected XLogReaderNodeModel() {
		super(new PortType[] {},
				new PortType[] { PortTypeRegistry.getInstance().getPortType(XLogPortObject.class, false) });
	}

	@Override
	protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
		logger.info("start: import event log");
		File file = new File(params.getFilePathSettingsModel().getStringValue());
		XLog result = null;
		OpenNaiveLogFilePlugin plugin = new OpenNaiveLogFilePlugin();
		result = (XLog) plugin.importFile(
				PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(OpenNaiveLogFilePlugin.class), file);
		XLogPortObject po = new XLogPortObject();
		po.setLog(result);
		logger.info("end: import event log");
		return new PortObject[] { new XLogPortObject() };
	}

	@Override
	protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs) {
		return new PortObjectSpec[] { null };
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
	protected void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) {
		params.saveSettingsTo(settings);

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		params.validateSettings(settings);

	}

	@Override
	protected void loadValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		params.loadSettingsFrom(settings);
	}

}
