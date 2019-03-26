package org.pm4knime.node.logmanipulation.classify;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;

/**
 * <code>NodeDialog</code> for the "RandomClassifier" Node.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 *  
 * 
 * @author Kefang Ding
 */
public class RandomClassifierNodeDialog extends DefaultNodeSettingsPane {
	
    /**
     * New pane for configuring the RandomClassifier node.
     */
    protected RandomClassifierNodeDialog() {
    	SettingsModelDoubleBounded m_overlapRate = RandomClassifierNodeModel.createSettingsModelOverlapRate();
    	SettingsModelDoubleBounded m_posRate = RandomClassifierNodeModel.createSettingsModelPosRate();
    	
    	DialogComponentNumber overlapRateComponent = new DialogComponentNumber(m_overlapRate, "Overlap Rate: ", 0);
    	addDialogComponent(overlapRateComponent);
    	

    	DialogComponentNumber posRateComponent = new DialogComponentNumber(m_posRate, "Positive Rate: ", 0);
    	addDialogComponent(posRateComponent);
    	
    }
}

