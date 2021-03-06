package org.pm4kinme.portobject.xlog;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.plugins.log.ui.logdialog.SlickerOpenLogSettings;

import sun.util.logging.resources.logging;

public class XLogPortObject extends AbstractPortObject {

	/**
	 * Define port type of objects of this class when used as PortObjects.
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(XLogPortObject.class);

	private  XLogPortObjectSpec spec ;

	private static final String ZIP_ENTRY_NAME = "XLogObject";

	private XLog log = null;

	
	public XLog getLog() {
		return log;
	}

	public void setLog(final XLog log) {
		this.log = log;
	}

	@Override
	public String getSummary() {
		return "This port represents an event log object (XLog)";
	}

	public void setSpec(XLogPortObjectSpec m_spec) {
		spec = m_spec;
	}
	
	@Override
	public PortObjectSpec getSpec() {
		return spec;
	}
	

	@Override
	public JComponent[] getViews() {
		if (this.log == null) {
			return new JComponent[] {};
		} else {
			SlickerOpenLogSettings defViz = new SlickerOpenLogSettings();
			return new JComponent[] {
					defViz.showLogVis(PM4KNIMEGlobalContext.instance().getPluginContext(), this.log) };
		}
	}

	@Override
	protected void save(PortObjectZipOutputStream out, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
		final ObjectOutputStream objOut = new ObjectOutputStream(out);
		XSerializer serializer = new XesXmlSerializer();
		serializer.serialize(this.getLog(), objOut);
	}

	@Override
	protected void load(PortObjectZipInputStream in, PortObjectSpec spec, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		final ZipEntry entry = in.getNextEntry();
		if (!ZIP_ENTRY_NAME.equals(entry.getName())) {
			throw new IOException("Failed to load XLog port object. " + "Invalid zip entry name '" + entry.getName()
					+ "', expected '" + ZIP_ENTRY_NAME + "'.");
		}
		XParser parser = new XesXmlParser(XFactoryRegistry.instance().currentDefault());
		final ObjectInputStream objIn = new ObjectInputStream(in);
		List<XLog> log = new ArrayList<>();
		try {
			log = parser.parse(objIn);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setLog(log.get(0));
	}

	public static class XLogPortObjectSerializer
			extends AbstractPortObject.AbstractPortObjectSerializer<XLogPortObject> {
		
	}

}
