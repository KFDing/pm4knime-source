package org.pm4kinme.node.enhancement.incorporatenegativeinformation;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;

/**
 * <code>NodeDialog</code> for the "IncorporateNegInfo" Node.
 * This node enhances an existing model by incorporate negative information from actual event log.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kefang Ding
 */
public class IncorporateNegInfoNodeDialog extends DefaultNodeSettingsPane {
	
	// weight setting
	private SettingsModelDoubleBounded m_extWeight;
	private SettingsModelDoubleBounded m_posWeight;
	private SettingsModelDoubleBounded m_negWeight;
	
	// for inductive miner
	private SettingsModelString m_type;
	private SettingsModelDoubleBounded m_noiseThreshold = null;
	
    /**
     * New pane for configuring the IncorporateNegInfo node.
     */
    protected IncorporateNegInfoNodeDialog() {
    	// setting weights here
    	m_extWeight = new SettingsModelDoubleBounded(
    			IncorporateNegInfoNodeModel.CFGKEY_EXT_WEIGHT, 1, 0, 1);
    	m_posWeight = new SettingsModelDoubleBounded(
    			IncorporateNegInfoNodeModel.CFGKEY_POS_WEIGHT, 1, 0, 1);
    	m_negWeight = new SettingsModelDoubleBounded(
    			IncorporateNegInfoNodeModel.CFGKEY_NEG_WEIGHT, 1, 0, 1);
    	
    	DialogComponentNumber extWeightComponent = new DialogComponentNumber(m_extWeight, "Ext Weight", 0.1);
    	addDialogComponent(extWeightComponent);
    	
    	DialogComponentNumber posWeightComponent = new DialogComponentNumber(m_posWeight, "Ext Weight", 0.1);
    	addDialogComponent(posWeightComponent);
    	
    	DialogComponentNumber negWeightComponent = new DialogComponentNumber(m_negWeight, "Ext Weight", 0.1);
    	addDialogComponent(negWeightComponent);
    	
    	
    	// for inductive miner
    	createNewGroup("Inductive Miner Choices: ");
    	String[] defaultValue =  IncorporateNegInfoNodeModel.defaultType;
        m_type = new SettingsModelString(IncorporateNegInfoNodeModel.CFGKEY_IM_TYPE, defaultValue[0]);
         
        m_noiseThreshold = new SettingsModelDoubleBounded(
        		 IncorporateNegInfoNodeModel.CFGKEY_IM_NOISE_THRESHOLD, 0.2, 0, 1);
        DialogComponentNumber noiseThresholdComponent = new DialogComponentNumber(m_noiseThreshold, "Write the Noise Threshold", 0.1);
        addDialogComponent(new DialogComponentStringSelection(m_type, "Select Inductive Miner Type", defaultValue));
        addDialogComponent(noiseThresholdComponent);
    	closeCurrentGroup();
    }
}

