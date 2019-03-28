package org.pm4kinme.node.enhancement.incorporatenegativeinformation;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4kinme.portobject.petrinet.PetriNetPortObject;
import org.pm4kinme.portobject.petrinet.PetriNetPortObjectSpec;
import org.pm4kinme.portobject.xlog.XLogPortObject;
import org.pm4kinme.portobject.xlog.XLogPortObjectSpec;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.incorporatenegativeinformation.algorithms.IncorporateNeg2Dfg;
import org.processmining.incorporatenegativeinformation.algorithms.NewLTDetector;
import org.processmining.incorporatenegativeinformation.algorithms.NewXORPairGenerator;
import org.processmining.incorporatenegativeinformation.algorithms.PN2DfgTransform;
import org.processmining.incorporatenegativeinformation.help.ProcessConfiguration;
import org.processmining.incorporatenegativeinformation.models.DfMatrix;
import org.processmining.incorporatenegativeinformation.models.DfgProcessResult;
import org.processmining.incorporatenegativeinformation.models.LTRule;
import org.processmining.incorporatenegativeinformation.models.XORCluster;
import org.processmining.incorporatenegativeinformation.models.XORClusterPair;
import org.processmining.incorporatenegativeinformation.parameters.ControlParameters;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParameters;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMcd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMd;
import org.processmining.plugins.InductiveMiner.dfgOnly.DfgMiningParametersIMfd;
import org.processmining.plugins.InductiveMiner.dfgOnly.plugins.IMdProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ProcessTreeElement;
import org.processmining.processtree.conversion.ProcessTree2Petrinet;
import org.processmining.processtree.conversion.ProcessTree2Petrinet.PetrinetWithMarkings;

/**
 * This is the model implementation of IncorporateNegInfo.
 * This node enhances an existing model by incorporate negative information from actual event log.
 * // it needs two inputs, event log and an existing model
    	// generates two outputs, one is pteri net with LT, one is without LT. 
    	// it needs setting for inductive miner and also the setting for ext, pos and neg weight
    	// the settings don't depend on the input, so we can use the DefaultDialog to generate the parameters
    	// we already set the parameters, then generate dfg, later it is the inductive miner to use dfg!!
    	// so the weight should be included into the same panel..
 * @author Kefang Ding
 */
public class IncorporateNegInfoNodeModel extends NodeModel {
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(IncorporateNegInfoNodeModel.class);
  
	private static final int INPORT_LOG = 0;
	private static final int INPORT_PETRINET = 1;
	
	private static final int OUTPORT_PETRINET_WITHLT = 0;
	private static final int OUTPORT_PETRINET_WITHOUTLT = 1;
	public static final String[] defaultType = new String[] {
			"Inductive Miner", //
			"Inductive Miner - Infrequent", //
			"Inductive Miner - Incompleteness" //
			// "Inductive Miner - exhaustive K-successor"
	};

	public static final String CFGKEY_IM_TYPE = "InductiveMinerMethod";
	public static final String CFGKEY_IM_NOISE_THRESHOLD = "NoiseThreshold";
	public static final String CFGKEY_CLASSIFIER = "classifier";
	public static final String CFGKEY_EXT_WEIGHT = "ext weight";
	public static final String CFGKEY_POS_WEIGHT = "positive weight";
	public static final String CFGKEY_NEG_WEIGHT = "negative weight";
	public static final int CFGKEY_EXT_WEIGHT_IDX = 0;
	public static final int CFGKEY_POS_WEIGHT_IDX = 1;
	public static final int CFGKEY_NEG_WEIGHT_IDX = 2;
	
	
	
	SettingsModelDoubleBounded m_extWeight = new SettingsModelDoubleBounded(
			IncorporateNegInfoNodeModel.CFGKEY_EXT_WEIGHT, 1, 0, 1);
	SettingsModelDoubleBounded m_posWeight = new SettingsModelDoubleBounded(
			IncorporateNegInfoNodeModel.CFGKEY_POS_WEIGHT, 1, 0, 1);
	SettingsModelDoubleBounded m_negWeight = new SettingsModelDoubleBounded(
			IncorporateNegInfoNodeModel.CFGKEY_NEG_WEIGHT, 1, 0, 1);
	
	
	private SettingsModelString m_type =  new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE, defaultType[0]);

	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.0, 0, 1.0);
	// we decide to use the XEventNameClassifier, so not as one option!!! 
	static XEventClassifier classifier = new XEventNameClassifier();
	static DfgProcessResult dfgResult;
	private PetriNetPortObjectSpec [] pnSpecs = new PetriNetPortObjectSpec[getNrOutPorts()];
    /**
     * Constructor for the node model.
     */
    protected IncorporateNegInfoNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE, PetriNetPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	// to get the input data
    	XLogPortObject logPortData =  (XLogPortObject) inData[INPORT_LOG];
    	PetriNetPortObject netPortData =  (PetriNetPortObject) inData[INPORT_PETRINET];
    	
    	XLog log = logPortData.getLog();
    	Petrinet net = netPortData.getNet();
    	
    	// 1. get DfMatrix from model, log in pos and neg
    	// need to find a way to deal with ui context creation
    	PluginContext context =  PM4KNIMEGlobalContext.instance().getPluginContext();
    	DfMatrix matrix = createDfMatrix(context, log, net, netPortData.getInitMarking());
    	// 2. use the weight to adapt the dfg
    	ControlParameters ctlParas = buildControlParameters();
    	
    	updateMatrix(matrix, ctlParas);
    	Dfg dfg = matrix.buildDfg();
    	
    	// 3. generate petri net with and without lt
    	DfgMiningParameters ptParas = buildProcessTreParameters();
    	ProcessTree pTree = IMdProcessTree.mineProcessTree(dfg, ptParas);
    	// net without lt
    	PetrinetWithMarkings mnet = ProcessTree2Petrinet.convert(pTree, true);
    	AcceptingPetriNet anetWithoutLT = new AcceptingPetriNetImpl(mnet.petrinet, mnet.initialMarking, mnet.finalMarking);
		// get the net with lt
		AcceptingPetriNet anetWithLT = createPNWithLT(pTree, log, ctlParas, matrix.getStandardCardinality());
		
		// 4. wrap them into the petri net object 
		PetriNetPortObject[] netPortObjects = new PetriNetPortObject[getNrOutPorts()];
		netPortObjects[OUTPORT_PETRINET_WITHOUTLT] = new PetriNetPortObject(pnSpecs[OUTPORT_PETRINET_WITHLT]);
		netPortObjects[OUTPORT_PETRINET_WITHOUTLT].setContext(context);
		netPortObjects[OUTPORT_PETRINET_WITHOUTLT].setNet(anetWithoutLT.getNet());
		netPortObjects[OUTPORT_PETRINET_WITHOUTLT].setInitMarking(anetWithoutLT.getInitialMarking());
		netPortObjects[OUTPORT_PETRINET_WITHOUTLT].setFinalMarking(anetWithoutLT.getFinalMarkings());
		
		netPortObjects[OUTPORT_PETRINET_WITHLT] = new PetriNetPortObject(pnSpecs[OUTPORT_PETRINET_WITHLT]);
		netPortObjects[OUTPORT_PETRINET_WITHLT].setContext(context);
		netPortObjects[OUTPORT_PETRINET_WITHLT].setNet(anetWithLT.getNet());
		netPortObjects[OUTPORT_PETRINET_WITHLT].setInitMarking(anetWithLT.getInitialMarking());
		netPortObjects[OUTPORT_PETRINET_WITHLT].setFinalMarking(anetWithLT.getFinalMarkings());
		
        return new PortObject[]{netPortObjects[OUTPORT_PETRINET_WITHLT], netPortObjects[OUTPORT_PETRINET_WITHOUTLT] };
    }
    
    // what could I do ?? The matrix should stay the same, but due to the parameters they change!! 
    // so we should store the DfgProcessMatrix, but later, we change the dfg into Petri net 
    // and then add the long-term dependency!!! 
    // but one thing, is if it worthy to store them here?? 
    // make it to be static, the others should be private
    private AcceptingPetriNet createPNWithLT(ProcessTree pTree, XLog log, ControlParameters parameters, long num) {

    	NewXORPairGenerator<ProcessTreeElement> generator = new NewXORPairGenerator<ProcessTreeElement>();
		generator.initialize(pTree);
		
		NewLTDetector detector  = new NewLTDetector(pTree, log, parameters, num);
		// in other mode, we need to define it another function
		generator.buildAllPairInOrder();
		List<XORClusterPair<ProcessTreeElement>> clusterPairs = generator.getClusterPair();
		List<LTRule<XORCluster<ProcessTreeElement>>> connSet = generator.getAllLTConnection();
		// generate all the pairs here 
		detector.addLTOnPairList(clusterPairs, connSet);
		return detector.getAcceptionPN();
	}
    
    /**
     * according to inductive miner type, we have different dfgMiningParameters, at first we need to test
     * @return
     */
    private DfgMiningParameters buildProcessTreParameters() {
    	DfgMiningParameters parameters = null;
    	if(m_type.getStringValue().equals(defaultType[0])) {
    		parameters = new DfgMiningParametersIMd();
    	}else if(m_type.getStringValue().equals(defaultType[1])) {
    		parameters = new DfgMiningParametersIMfd();
    	}else if(m_type.getStringValue().equals(defaultType[2])) {
    		parameters = new DfgMiningParametersIMcd();
    	}else {
    		System.out.println("not in this type");
    	}
	    return parameters;
	   }

	// we need to get the control parameters for neg and pos
    private ControlParameters buildControlParameters() {
    	ControlParameters parameters = new ControlParameters();
    	parameters.setExistWeight(m_extWeight.getDoubleValue());
    	parameters.setPosWeight(m_posWeight.getDoubleValue());
    	parameters.setNegWeight(m_negWeight.getDoubleValue());
    	return parameters;
    }
    
    private void updateMatrix(DfMatrix matrix, ControlParameters parameters) {
    	matrix.updateCardinality( CFGKEY_EXT_WEIGHT_IDX , parameters.getExistWeight());
    	matrix.updateCardinality( CFGKEY_POS_WEIGHT_IDX , parameters.getPosWeight());
    	matrix.updateCardinality( CFGKEY_NEG_WEIGHT_IDX , parameters.getNegWeight());
    }
    
    private DfMatrix createDfMatrix(PluginContext context, XLog log, Petrinet net, Marking marking) throws ConnectionCannotBeObtained {
    	
		int num = XLogInfoFactory.createLogInfo(log).getNumberOfTraces();
		// PN2DfgTransform.setCardinality(dfg, num);
		// -- incorporate the negative information and give out the Dfg and Petri net model
		Object[] result = IncorporateNeg2Dfg.splitEventLog(log);
		XLog pos_log = (XLog) result[0];
		XLog neg_log = (XLog) result[1];
		
		Dfg pos_dfg = Log2DfgTransformer.transform(pos_log, this.classifier);
		Dfg neg_dfg = Log2DfgTransformer.transform(neg_log, this.classifier);
		// one bug in KNIME could not find  transition system
		// Execute failed: org/processmining/models/graphbased/directed/transitionsystem/TransitionSystem
		Dfg dfg = PN2DfgTransform.transformPN2Dfg(context, net, marking);
		DfMatrix dfMatrix = IncorporateNeg2Dfg.createDfMatrix(dfg, pos_dfg, neg_dfg, num);
		
    	return dfMatrix;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: here could set all the values to default values.. but we can do later, not matter
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	// check the inport one is Event log, one is Petri net
    	if(!inSpecs[INPORT_LOG].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	if(!inSpecs[INPORT_PETRINET].getClass().equals(PetriNetPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid petri net!");
    	
    	// we need to creat two out port petri net spec
    	pnSpecs[OUTPORT_PETRINET_WITHLT] = new PetriNetPortObjectSpec();
    	pnSpecs[OUTPORT_PETRINET_WITHLT].setFileName("Petri net with LT");
    	
    	pnSpecs[OUTPORT_PETRINET_WITHOUTLT] = new PetriNetPortObjectSpec();
    	pnSpecs[OUTPORT_PETRINET_WITHOUTLT].setFileName("Petri net without LT");
    	
        return new PortObjectSpec[]{pnSpecs[OUTPORT_PETRINET_WITHLT], pnSpecs[OUTPORT_PETRINET_WITHOUTLT]};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: save the parameters here
    	m_extWeight.saveSettingsTo(settings);
    	m_posWeight.saveSettingsTo(settings);
    	m_negWeight.saveSettingsTo(settings);
    	
    	m_type.saveSettingsTo(settings);
    	m_noiseThreshold.saveSettingsTo(settings);
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_extWeight.loadSettingsFrom(settings);
    	m_posWeight.loadSettingsFrom(settings);
    	m_negWeight.loadSettingsFrom(settings);
    	
    	m_type.loadSettingsFrom(settings);
    	m_noiseThreshold.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: make sure there is sth data there??
    	m_extWeight.validateSettings(settings);
    	m_posWeight.validateSettings(settings);
    	m_negWeight.validateSettings(settings);
    	
    	m_type.validateSettings(settings);
    	m_noiseThreshold.validateSettings(settings);
    	
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

