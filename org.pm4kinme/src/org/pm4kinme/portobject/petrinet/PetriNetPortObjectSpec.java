package org.pm4kinme.portobject.petrinet;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.processmining.framework.plugin.PluginContext;
/**
 * when there is no need to serialize the Spec, we don't give it 
 * @author dkf
 *
 */
public class PetriNetPortObjectSpec extends AbstractSimplePortObjectSpec {
    
	public static String TEMP_FILE_NAME = "PetriNetPortObjectSpec.pnml";
	static int count = 0;
	// we need to record the context of this spec
	PluginContext context;
	
	
	String fileName = TEMP_FILE_NAME;
	@Override
	public JComponent[] getViews() {
		return new JComponent[] {};
	}

	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO Auto-generated method stub

	}

	public static class PetriNetPortObjectSpecSerializer
			extends AbstractSimplePortObjectSpec.AbstractSimplePortObjectSpecSerializer<PetriNetPortObjectSpec> {

	}
	
	public PluginContext getContext() {
		return context;
	}

	public void setContext(PluginContext context) {
		this.context = context;
	}

	public String getFileName() {
		// TODO Auto-generated method stub
		return fileName;
	}
	public void setFileName(String name) {
		fileName = name;
	}

}
