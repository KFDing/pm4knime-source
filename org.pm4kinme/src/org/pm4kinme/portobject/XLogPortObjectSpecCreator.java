package org.pm4kinme.portobject;
/**
 * this class is used to create PortObject for XLog, which should connect the PortOject information
 * @author KFDing
 *
 */
public class XLogPortObjectSpecCreator {
	private XLogPortObjectSpec m_spec ;
	
	public XLogPortObjectSpecCreator() {
		m_spec =  new XLogPortObjectSpec();
	}
	public XLogPortObjectSpecCreator(final XLogPortObject portObject) {
		if(portObject != null) {
			m_spec = (XLogPortObjectSpec) portObject.getSpec();
		}
		m_spec =  new XLogPortObjectSpec();
	}
	
	public XLogPortObjectSpecCreator(final XLogPortObjectSpec spec) {
		// we need to change it anyway, by using the PortObject
		if(spec !=null)
			m_spec = spec;
		m_spec =  new XLogPortObjectSpec();
	}

	public XLogPortObjectSpec createSpec() {
		return m_spec;
	}
}
