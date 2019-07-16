package org.pm4knime.portobject;

import org.knime.core.data.DataType;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;
import org.knime.core.node.util.CheckUtils;
/**
 * it should include what property for one XAttribute in XLog??
 * @author dkf
 *
 */
public final class XAttributePortObjectSpec extends AbstractSimplePortObjectSpec{
	private String m_name=null;
	private DataType m_type =null;
	
	private static final String CFG_ATTRIBUTE_KEY = "attribute_key";
	private static final String CFG_ATTRIBUTE_TYPE = "attribute_type";
	// we can not define the values for attribute here!!! 
	XAttributePortObjectSpec(final String name, final DataType type){
		final String nullError = "Do not init DataColumnSpec with null arguments!";
		m_name = CheckUtils.checkArgumentNotNull(name, nullError);
		m_type = CheckUtils.checkArgumentNotNull(type, nullError);
	}
	
	@Override
	protected void save(ModelContentWO model) {
		// TODO Auto-generated method stub
		model.addString(CFG_ATTRIBUTE_KEY, m_name);
		m_type.save(model.addConfig(CFG_ATTRIBUTE_TYPE));
		
	}

	@Override
	public void load(ModelContentRO model) throws InvalidSettingsException {
		// TODO how to load all the stuff from model and then put them out ??
		
		if(model.containsKey(CFG_ATTRIBUTE_KEY)) 
			m_name = model.getString(CFG_ATTRIBUTE_KEY);
		m_type = DataType.load(model.getConfig(CFG_ATTRIBUTE_TYPE));
		
	}

}
