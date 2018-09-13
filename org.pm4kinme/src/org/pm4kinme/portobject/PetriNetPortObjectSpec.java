package org.pm4kinme.portobject;

import javax.swing.JComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

public class PetriNetPortObjectSpec extends AbstractSimplePortObjectSpec {

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

}
