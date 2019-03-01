package org.pm4knime.ding.inductiveminer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;
import org.pm4kinme.external.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4kinme.portobject.PetriNetPortObject;
import org.pm4kinme.portobject.PetriNetPortObjectSpec;
import org.pm4kinme.portobject.XLogPortObject;
import org.pm4kinme.portobject.XLogPortObjectSpec;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.mining.MiningParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersEKS;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMf;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIMflc;
import org.processmining.plugins.InductiveMiner.plugins.IM;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of InductiveMiner.
 * use the inductive miner to do process discovery
 *
 * @author KFDing
 */
public class InductiveMinerNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(InductiveMinerNodeModel.class);
  
    public static final int IN_PORT = 0;
	public static final String[] defaultType = new String[] {
			"Inductive Miner", //
			"Inductive Miner - Infrequent", //
			"Inductive Miner - Incompleteness", //
			"Inductive Miner - exhaustive K-successor", //
			"Inductive Miner - Life cycle"
	};

	public static final String CFGKEY_METHOD_TYPE = "InductiveMinerMethod";

	public static final String CFGKEY_NOISE_THRESHOLD = "NoiseThreshold";
	public static final String CFGKEY_CLASSIFIER = "classifier";
	
	private static boolean withNoiseThreshold = false;

	// we should get it from the event log and check how many classifiers it has!!!
	// and then use it as one parameter into next field to choose it 
	// here I think it is a map from which string to which correspingd values 
	public static List<String> defaultClassifer = new ArrayList<String>();
	
	private static final  SettingsModelString m_type =  new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE, defaultType[0]);

	private SettingsModelDoubleBounded m_noiseThreshold = new SettingsModelDoubleBounded(InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.0, 0, 1.0);
	private SettingsModelString m_classifier = new SettingsModelString(InductiveMinerNodeModel.CFGKEY_CLASSIFIER, "");
	
	// private MiningParameters param ; // = new MiningParametersIM();
	private XLog log = null;
	private XLogPortObject m_logPortObject ;
	private PetriNetPortObjectSpec pnSpec = null;
	PetriNetPortObject pnPortObject;
	Collection<XEventClassifier> classifiers;
	private Map<String, XEventClassifier> map;
    /**
     * Constructor for the node model.
     */
    protected InductiveMinerNodeModel() {
    	// here we should also create the context and put it here, but how to accept it 
    	// actually from the event reader and then give it here. 
        // super(new PortType[] {XLogPortObject.TYPE}, new PortType[] { PetriNetPortObject.TYPE});
    	super(new PortType[] { XLogPortObject.TYPE },
				new PortType[] { PetriNetPortObject.TYPE });
    	// we need to get such information from log, so we need the in data spec
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        logger.info("Begin Inductive Miner");
        // get the log from inData
        for(PortObject obj: inData)
        	if(obj instanceof XLogPortObject) {
        		m_logPortObject = (XLogPortObject)obj;
        		break;
        	}
        
        log = m_logPortObject.getLog();
        
        // second to give the mining context
        PluginContext context =  PM4KNIMEGlobalContext.instance().getPluginContext(); //.getFutureResultAwarePluginContext(IM.class);
        
        Object[] result = IM.minePetriNet(context, log, createParameters());
        // Object[] result = IMPetriNet.minePetriNet(context, log , param);
        // create the output port object
        
        pnPortObject.setNet((Petrinet) result[0]);
        pnPortObject.setInitMarking((Marking) result[1]);
        pnPortObject.setFinalMarking((Marking) result[2]);
        
        logger.info("End of the Inductive Miner");

        return new PortObject[] { pnPortObject };
    }

    private  MiningParameters createParameters() throws InvalidSettingsException {
    	MiningParameters param;
    	
    	if(m_type.getStringValue().equals(defaultType[0]))
        	param = new MiningParametersIM();
        else if(m_type.getStringValue().equals(defaultType[1]))
        	param = new MiningParametersIMf();
        else if(m_type.getStringValue().equals(defaultType[2]))
        	param = new MiningParametersIMf();
        else if(m_type.getStringValue().equals(defaultType[3]))
        	param = new MiningParametersEKS();
        else if(m_type.getStringValue().equals(defaultType[4]))
        	param = new MiningParametersIMflc();
        else 
        	throw new InvalidSettingsException("unknown inductive miner type "+ m_type.getStringValue());
        param.setNoiseThreshold((float) m_noiseThreshold.getDoubleValue());
        // we need to give one of the classifier to have it
        if(m_classifier.isEnabled()) {
        	param.setClassifier(map.get(m_classifier.getStringValue()));
        }
    	
        return param;
    } 
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO Code executed on reset.
        // Models build during execute are cleared here.
        // Also data handled in load/saveInternals will be erased here.
    }
    private Collection<XEventClassifier> setDefaultClassifier(){
    	Collection<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
		classifiers.add(new XEventNameClassifier());
		return classifiers;
    }
    
    private Collection<XEventClassifier> assignClassifier(PortObjectSpec[] inSpecs){
    	Collection<XEventClassifier> classifiers =null;
    	
    	for(PortObjectSpec spec: inSpecs) {
    		if(spec instanceof XLogPortObjectSpec) {
    			classifiers = ((XLogPortObjectSpec)spec).getClassifiers();
    			if(classifiers==null || classifiers.isEmpty()) {
    				classifiers = setDefaultClassifier();
    			}
    				
    			createClassifierMap(classifiers, defaultClassifer);
    			// m_classifier =  new SettingsModelString(InductiveMinerNodeModel.CFGKEY_CLASSIFIER, defaultClassifer.get(0));
    			break;
    		}
    	}
    	return classifiers;
    }
    
    // we need to get the classifiers from event log, or inspec specification..go to configuration
    private void createClassifierMap(Collection<XEventClassifier> classifiers, List<String> defaultClassifier){
    	// but we don't consider the real connection of model and log
    	map =  new HashMap<String, XEventClassifier>();
    	
    	int i=0;
		for(XEventClassifier clf: classifiers) {
			defaultClassifier.add(i, clf.name());
    		map.put(clf.name(), clf);
    		i++;
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	
    	if(classifiers == null || classifiers.isEmpty())
    		classifiers = assignClassifier(inSpecs);
    	if(classifiers == null) {
    		throw new InvalidSettingsException("No corresponding classifier available");
    	}
    	boolean hasXLog = false;
    	if(!classifiers.isEmpty())
    		hasXLog = true;
    	
    	if(! hasXLog) {
    		throw new InvalidSettingsException("The inport is not an xlog file");
    	}
    	
    	pnPortObject =  new PetriNetPortObject();
    	PetriNetPortObjectSpec pnSpec = pnPortObject.getSpec();
    	
        return new PortObjectSpec[]{pnSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_type.saveSettingsTo(settings);
       if(isWithNoiseThreshold())
    	   m_noiseThreshold.saveSettingsTo(settings);
    }

    public static List<String> getClassifier(){
    		return defaultClassifer;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
          m_type.loadSettingsFrom(settings);
          if(isWithNoiseThreshold())
        	  m_noiseThreshold.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
       m_type.validateSettings(settings);
       if(isWithNoiseThreshold())
    	   m_noiseThreshold.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {

    }

	public  boolean isWithNoiseThreshold() {
		return withNoiseThreshold;
	}

	public void setWithNoiseThreshold() {
		if(!m_type.getStringValue().equals(defaultType[0])) {
			withNoiseThreshold = true;
			m_noiseThreshold = new SettingsModelDoubleBounded(CFGKEY_NOISE_THRESHOLD, 0.2, 0, 1.0);
		}
	}

}

