package org.pm4kinme.portobject;

import java.util.Collection;
import java.util.List;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClassifier;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
import org.knime.core.node.port.AbstractSimplePortObjectSpec;

public class XLogPortObjectSpec extends AbstractSimplePortObjectSpec {

	String title ;
	// to mark the bottom or top??
	public void setTitle(String value) {
		title = value;
	}
	
	public String getTitle(String value) {
		return title;
	}
	
	Collection<XEventClassifier> classifiers = null;
	
	public Collection<XEventClassifier> getClassifiers() {
		return classifiers;
	}

	public void setClassifiers(Collection<XEventClassifier> classifiers2) {
		this.classifiers = classifiers2;
	}

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

	public static class XLogPortObjectSpecSerializer
			extends AbstractSimplePortObjectSpec.AbstractSimplePortObjectSpecSerializer<XLogPortObjectSpec> {
	}

}
