package org.pm4kinme.node.enhancement.repairmodel;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4kinme.external.connectors.prom.PM4KNIMEPluginDescriptor;
import org.pm4kinme.external.connectors.prom.PM4KNIMEPluginManager;
import org.pm4kinme.portobject.petrinet.PetriNetPortObject;
import org.pm4kinme.portobject.petrinet.PetriNetPortObjectSpec;
import org.pm4kinme.portobject.xlog.XLogPortObject;
import org.pm4kinme.portobject.xlog.XLogPortObjectSpec;
import org.processmining.framework.packages.PackageDescriptor;
import org.processmining.framework.packages.PackageManager;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.incorporatenegativeinformation.help.NetUtilities;
import org.processmining.modelrepair.parameters.RepairConfiguration;
import org.processmining.modelrepair.plugins.Uma_RepairModel_Plugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.ilpminer.ILPMiner;
import org.processmining.plugins.log.exporting.ExportLogXes;
import org.processmining.plugins.log.logabstraction.LogRelationProvider;
import org.processmining.yawl.ext.javax.persistence.criteria.Predicate.BooleanOperator;

/**
 * This is the model implementation of RepairModel.
 * Wrapped Repair Model from Dirk Fahland
 *
 * @author Kefang Ding
 */
public class RepairModelNodeModel extends NodeModel {
	private static final int INPORT_LOG = 0;
	private static final int INPORT_PETRINET = 1;
	
	public static String CFG_DETECTLOOPS           =     "detectLoops";
	public static String CFG_LOOPMODELMOVECOSTS    =     "loopModelMoveCosts";
	public static String CFG_DETECTSUBPROCESSES  	=    "detectSubProcesses";
	public static String CFG_REMOVEINFREQUENTNODES	=    "removeInfrequentNodes";
	public static String CFG_REMOVE_KEEPIFATLEAST 	=    "remove_keepIfMoreThan";
	public static String CFG_GLOBALCOSTALIGNMENT 	=    "globalCostAlignment";
	public static String CFG_GLOBALCOST_MAXITERATIONS =   "globalCost_maxIterations";
	public static String CFG_ALIGNALIGNMENTS	 	 =   "alignAlignments";
	public static String CFG_REPAIRFINALMARKING	 =       "repairFinalMarking";
	
	private SettingsModelBoolean m_detectLoops 			= new SettingsModelBoolean(CFG_DETECTLOOPS         ,true);  
	private SettingsModelInteger m_loopModelMoveCosts 	= new SettingsModelInteger(CFG_LOOPMODELMOVECOSTS   , 0);
	private SettingsModelBoolean m_detectSubProcesses  	= new SettingsModelBoolean(CFG_DETECTSUBPROCESSES   ,true);
	private SettingsModelBoolean m_removeInfrequentNodes	= new SettingsModelBoolean(CFG_REMOVEINFREQUENTNODES ,true);
	private SettingsModelInteger m_remove_keepIfAtLeast	= new SettingsModelInteger(CFG_REMOVE_KEEPIFATLEAST ,1);
	private SettingsModelBoolean m_globalCostAlignment 	= new SettingsModelBoolean(CFG_GLOBALCOSTALIGNMENT  ,true);
	private SettingsModelInteger m_globalCost_maxIterations = new SettingsModelInteger(CFG_GLOBALCOST_MAXITERATIONS ,1);
	private SettingsModelBoolean m_alignAlignments 		= new SettingsModelBoolean(CFG_ALIGNALIGNMENTS	    ,true);
	private SettingsModelBoolean m_repairFinalMarking 	= new SettingsModelBoolean(CFG_REPAIRFINALMARKING	,true);
	
	static XEventClassifier classifier = new XEventNameClassifier();
	private PetriNetPortObjectSpec  pnSpec;
    /**
     * Constructor for the node model.
     */
    protected RepairModelNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: return repaired petri net
    	XLogPortObject logPortData =  (XLogPortObject) inData[INPORT_LOG];
    	PetriNetPortObject inNetData =  (PetriNetPortObject) inData[INPORT_PETRINET];
    	
    	if(logPortData.getLog().isEmpty())
    		throw new InvalidAlgorithmParameterException("Log should not be empty");
    	
    	// check the netPortData, if it has no final marking, we need to assign the final marking into it
    	if(inNetData.getInitMarking() == null) {
    		Marking initMarking = NetUtilities.guessInitialMarking(inNetData.getNet());
    		inNetData.setInitMarking(initMarking);
    	}
    	
    	if(inNetData.getFinalMarking() == null) {
    		// need to change the codes later
    		Marking finalMarking = NetUtilities.guessFinalMarking(inNetData.getNet());
    		inNetData.setFinalMarking(finalMarking);
    	
    	}
    	
    	prepareEnv();
    	
    	// get the repair configuration parameters
    	RepairConfiguration config = createRepairConfig();
    	// input into the codes
    	PluginContext context =  PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(Uma_RepairModel_Plugin.class);
    	
    	// PackageManager.getInstance().findOrInstallPackages(packageNames)
    	Uma_RepairModel_Plugin repairer = new Uma_RepairModel_Plugin();
    	Object[] result = repairer.repairModel_buildT2Econnection(context, logPortData.getLog(),
    			inNetData.getNet(), inNetData.getInitMarking(), inNetData.getFinalMarking(), config, classifier);
    	
    	PetriNetPortObject outNetObject = new PetriNetPortObject();
    	outNetObject.setNet((Petrinet) result[0]);
    	outNetObject.setInitMarking((Marking) result[1]);
    	outNetObject.setFinalMarking((Marking) result[2]);
    	outNetObject.setContext(context);
    	
        return new PortObject[]{outNetObject};
    }

    private void prepareEnv() {
    	String url = "/home/dkf/ProcessMining/programs/KNIME_Development/PM4KNIME/pm4knime-lib";
    	
    	// ClassLoader sysloader = ClassLoader.getSystemClassLoader();
    	org.processmining.framework.boot.Boot.PACKAGE_FOLDER = url;
    	PackageManager.getInstance().initialize(org.processmining.framework.boot.Boot.Level.ALL);
    	
    	PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(LogRelationProvider.class);
    	PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(ILPMiner.class);
    	
    }
    
    private RepairConfiguration createRepairConfig() {
		// TODO Auto-generated method stub
    	RepairConfiguration config =  new RepairConfiguration();
    	
    	config.detectLoops 			=  	m_detectLoops.getBooleanValue();		               
    	config.loopModelMoveCosts	=  	m_loopModelMoveCosts.getIntValue();	       
    	config.detectSubProcesses	=	m_detectSubProcesses.getBooleanValue();  		      
    	config.removeInfrequentNodes=	m_removeInfrequentNodes.getBooleanValue();	      
    	config.remove_keepIfAtLeast =  	m_remove_keepIfAtLeast.getIntValue();	  
    	config.globalCostAlignment 	=  	m_globalCostAlignment.getBooleanValue(); 	  
    	config.globalCost_maxIterations=m_globalCost_maxIterations.getIntValue();
    	config.alignAlignments		=  	m_alignAlignments.getBooleanValue(); 		         
    	config.repairFinalMarking	=  	m_repairFinalMarking.getBooleanValue(); 	       
    	
		return config;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
        // TODO: check the input spec are event log and petri net 
    	if(!inSpecs[INPORT_LOG].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	if(!inSpecs[INPORT_PETRINET].getClass().equals(PetriNetPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid petri net!");
    	
    	// we need to creat two out port petri net spec
    	pnSpec = new PetriNetPortObjectSpec();
    	pnSpec.setFileName("Petri net After Repair");
    	
    	
        return new PortObjectSpec[]{pnSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_detectLoops.saveSettingsTo(settings);			
    	m_loopModelMoveCosts.saveSettingsTo(settings);			 	
    	m_detectSubProcesses.saveSettingsTo(settings);			  	
    	m_removeInfrequentNodes.saveSettingsTo(settings);			
    	m_remove_keepIfAtLeast.saveSettingsTo(settings);				
    	m_globalCostAlignment.saveSettingsTo(settings);			 	
    	m_globalCost_maxIterations.saveSettingsTo(settings);			
    	m_alignAlignments.saveSettingsTo(settings);			 		
    	m_repairFinalMarking.saveSettingsTo(settings);			 	
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_detectLoops.loadSettingsFrom(settings);			
    	m_loopModelMoveCosts.loadSettingsFrom(settings);
    	m_detectSubProcesses.loadSettingsFrom(settings);
    	m_removeInfrequentNodes.loadSettingsFrom(settings);
    	m_remove_keepIfAtLeast.loadSettingsFrom(settings);	
    	m_globalCostAlignment.loadSettingsFrom(settings);
    	m_globalCost_maxIterations.loadSettingsFrom(settings);
    	m_alignAlignments.loadSettingsFrom(settings);
    	m_repairFinalMarking.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_detectLoops.validateSettings(settings);
    	m_loopModelMoveCosts.validateSettings(settings);
    	m_detectSubProcesses.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

}

