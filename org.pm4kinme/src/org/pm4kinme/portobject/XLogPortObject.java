package org.pm4kinme.portobject;

import java.io.IOException;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.AbstractPortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

public class XLogPortObject extends AbstractPortObject {
	
	/**
     * Define port type of objects of this class when used as PortObjects.
     */
    public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(XLogPortObject.class);
	
	private final Object log;
	
	public XLogPortObject(Object log) {
		this.log = log;
	}
	
	public Object get() {
		return log;
	}

	@Override
	public String getSummary() {
		return "this is an event log";
	}
		

	@Override
	public PortObjectSpec getSpec() {
		return new XLogPortObjectSpec();
	}

	@Override
	public JComponent[] getViews() {
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
	}
	
	public static class XLogPortObjectSerializer extends AbstractPortObject.AbstractPortObjectSerializer<XLogPortObject> {
		
	}

}
