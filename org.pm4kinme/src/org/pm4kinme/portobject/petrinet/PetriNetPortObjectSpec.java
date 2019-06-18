package org.pm4kinme.portobject.petrinet;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
/**
 * when there is no need to serialize the Spec, we don't give it 
 * @author dkf
 *
 */
public class PetriNetPortObjectSpec extends AbstractSimplePortObjectSpec {
	
	public static class PetriNetPortObjectSpecSerializer
		extends AbstractSimplePortObjectSpec.AbstractSimplePortObjectSpecSerializer<PetriNetPortObjectSpec> {
    	}
	
	private DataType m_type;
	
	public PetriNetPortObjectSpec() {}
	
	public PetriNetPortObjectSpec(final DataType type) {
		if (type == null) {
            throw new NullPointerException("Argument must not be null.");
        }
		m_type = type;
	}
	
	public DataType getDataType() {
        return m_type;
	}
	
	@Override
	public JComponent[] getViews() {
		JLabel l = new JLabel("Image of type \"" + getDataType() + "\"");
		l.setName("Image Spec");
		return new JComponent[] {l};
	}

	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		m_type.save(model);
	}

	@Override
	protected void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_type = DataType.load(model);
	}

	

}
