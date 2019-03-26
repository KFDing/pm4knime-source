package org.pm4kinme.node.enhancement.repairmodel;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.processmining.modelrepair.parameters.RepairConfiguration;
import org.processmining.modelrepair.plugins.Uma_RepairModel_UI;

/**
 * <code>NodeDialog</code> for the "RepairModel" Node.
 * Wrapped Repair Model from Dirk Fahland
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kefang Ding
 */
public class RepairModelNodeDialog extends DefaultNodeSettingsPane {
	
	
	private SettingsModelBoolean m_detectLoops 			;
	private SettingsModelInteger m_loopModelMoveCosts 	;
	private SettingsModelBoolean m_detectSubProcesses  	;
	private SettingsModelBoolean m_removeInfrequentNodes;
	private SettingsModelInteger m_remove_keepIfAtLeast	;
	private SettingsModelBoolean m_globalCostAlignment 	;
	private SettingsModelInteger m_globalCost_maxIterations;
	private SettingsModelBoolean m_alignAlignments 		;
	private SettingsModelBoolean m_repairFinalMarking 	;
	
    /**
     * New pane for configuring the RepairModel node.
     */
    protected RepairModelNodeDialog() {
    	RepairConfiguration config = new RepairConfiguration();		
		Uma_RepairModel_UI ui = new Uma_RepairModel_UI(config);
		// but how to put the values from config
		// if we want to get the flow variabel settings here, we need to expose all the models here like in SplitModel
		// RepairConfigurationSettingsModel  m_repairModel = new RepairConfigurationSettingsModel(RepairModelNodeModel.CFG_REPAIR_CONFIGURATION, config);
		// we need to create a lot of models to store the values there
		m_detectLoops 			= new SettingsModelBoolean(RepairModelNodeModel.CFG_DETECTLOOPS         ,true);       
		m_loopModelMoveCosts 	= new SettingsModelInteger(RepairModelNodeModel.CFG_LOOPMODELMOVECOSTS   , 0);        
		m_detectSubProcesses  	= new SettingsModelBoolean(RepairModelNodeModel.CFG_DETECTSUBPROCESSES   ,true);      
		m_removeInfrequentNodes	= new SettingsModelBoolean(RepairModelNodeModel.CFG_REMOVEINFREQUENTNODES ,true); 
		m_remove_keepIfAtLeast	= new SettingsModelInteger(RepairModelNodeModel.CFG_REMOVE_KEEPIFATLEAST ,1);         
		m_globalCostAlignment 	= new SettingsModelBoolean(RepairModelNodeModel.CFG_GLOBALCOSTALIGNMENT  ,true);      
		m_globalCost_maxIterations = new SettingsModelInteger(RepairModelNodeModel.CFG_GLOBALCOST_MAXITERATIONS ,1); 
		m_alignAlignments 		= new SettingsModelBoolean(RepairModelNodeModel.CFG_ALIGNALIGNMENTS	    ,true);      
		m_repairFinalMarking 	= new SettingsModelBoolean(RepairModelNodeModel.CFG_REPAIRFINALMARKING	,true);      
		
		createNewGroup("Running Options");
    	addDialogComponent(new DialogComponentBoolean(m_detectLoops,     RepairModelNodeModel.CFG_DETECTLOOPS));
    	addDialogComponent(new DialogComponentNumber(m_loopModelMoveCosts 		,     RepairModelNodeModel.CFG_LOOPMODELMOVECOSTS, 1));
    	addDialogComponent(new DialogComponentBoolean(m_detectSubProcesses  	,     RepairModelNodeModel.CFG_DETECTSUBPROCESSES  ));
    	addDialogComponent(new DialogComponentBoolean(m_removeInfrequentNodes	,     RepairModelNodeModel.CFG_REMOVEINFREQUENTNODES));
    	addDialogComponent(new DialogComponentNumber(m_remove_keepIfAtLeast	,     RepairModelNodeModel.CFG_REMOVE_KEEPIFATLEAST, 1));
    	closeCurrentGroup();
    	createNewGroup("Repair Options");
    	addDialogComponent(new DialogComponentBoolean(m_globalCostAlignment 	,     RepairModelNodeModel.CFG_GLOBALCOSTALIGNMENT ));
    	addDialogComponent(new DialogComponentNumber(m_globalCost_maxIterations,     RepairModelNodeModel.CFG_GLOBALCOST_MAXITERATIONS, 1));
        addDialogComponent(new DialogComponentBoolean(m_alignAlignments 		,	  RepairModelNodeModel.CFG_ALIGNALIGNMENTS	   ));  
	    addDialogComponent(new DialogComponentBoolean(m_repairFinalMarking 		,     RepairModelNodeModel.CFG_REPAIRFINALMARKING  ));	
	    closeCurrentGroup();
    }
    
    
}

