package org.pm4knime.node.conformance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.enhancement.incorporatenegativeinformation.IncorporateNegInfoNodeModel;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.RepResultPortObjectSpec;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XEventClassifierInterface;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.FlexibleHeuristicsMinerPlugin;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

/**
 * <code>NodeModel</code> for the "ConformanceChecker" node.
 * ProM Plugin PNetReplayer is used to implement this function, which we could also get the performance measure 
 * in this plugin. At first, only the fitness part. Performance and Precision are in different nodes.
 * It outputs the fitness info in one table, the alignment info in another output. 
 * The colored Petri net are in a view.
 * 
 * The parameters in RapidProm differ from the ones in ProM, so we need to adapt this change. The chosen parameters are:: 
 *  <1> cost for log and transition move for each transition and event class :: show them in a table in advance setting
 *  <2> strategy chosen to calculate the alignment :: native library problem
 *  <3> ?? Marking, the initial marking and final marking as the parameters :: if not stored, we need to create by ourselves!!
 * @author Kefang Ding
 */
public class ConformanceCheckerNodeModel extends NodeModel implements XEventClassifierInterface{
    
	private static final NodeLogger logger = NodeLogger.getLogger(ConformanceCheckerNodeModel.class);
	
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;

	private static final int OUTPORT_FITNESS = 0;
	private static final int OUTPORT_ALIGNMENT = 1;
	
	private XLogPortObject logPO ;
	private PetriNetPortObject netPO ;
	private TransEvClassMapping mapping;
	
	RepResultPortObject repResultPO;
	// model related parameters
	// choose algorithms to use for replay
	public static final String CFGKEY_STRATEGY_TYPE = "Strategy type";
	public static final String[] strategyList = {"ILP Replayer","non-ILP Replayer"};
	SettingsModelString m_strategy = new SettingsModelString(CFGKEY_STRATEGY_TYPE, strategyList[0]);
	
	
	// set event classifier to choose from default List
	// but this can be changed due to different event log situation. So we need to keep them different!!
	List<XEventClassifier> classifierList ;
	public List<String> classifierNames ;
	SettingsModelString m_classifierName = new SettingsModelString(XEventClassifierInterface.CKF_KEY_EVENT_CLASSIFIER, "");
	
    /**
     * Constructor for the node model.
     */
    protected ConformanceCheckerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	// input ports include XLogPortObject, and PetriNetPortObject. Output ports include One table to show the fitness info,
    	// ont output PortObject for the alignment.
        
    	super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE }, new PortType[] {BufferedDataTable.TYPE, RepResultPortObject.TYPE });
        
        initializeClassifiers();
    }

    

	private void initializeClassifiers() {
		// TODO Auto-generated method stub
		classifierList= new ArrayList();
		classifierList.add(new XEventNameClassifier());
		classifierList.add(new XEventLifeTransClassifier());
		
		classifierNames = new ArrayList();
		for(XEventClassifier clf: classifierList) {
			classifierNames.add(clf.name());
		}
		
	}



	/**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

    	logger.info("Start: PNReplayer Conformance Checking");
    	logPO = (XLogPortObject) inData[INPORT_LOG];
		netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
		
		XLog log = logPO.getLog();
		// to replay the net, we need marking there
		AcceptingPetriNet anet = netPO.getANet();
    	
		XEventClassifier eventClassifier = getXEventClassifier();
		IPNReplayParameter parameters =  getParameters(log, anet, eventClassifier);
		// get the replayer for this, but it also depends on the parameters... we need to find another word for this.
		
		IPNReplayAlgorithm replayEngine = getReplayer();
		
		PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(PNLogReplayer.class);
		
		
		// 
		mapping = constructMapping(log, anet.getNet(), eventClassifier);
		
		PNRepResult result = replayEngine.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
		System.out.println("Replay result size : " + result.size());
		// after we have those result, we need to output the 
		// a table include the fitness statistics information, one is alignment PO
		BufferedDataTable bt = createInfoTable(result.getInfo(), exec);
		
		repResultPO = new RepResultPortObject(result, logPO);
		// alignPO.setRepResult(result);
		logger.info("End: PNReplayer Conformance Checking");
        return new PortObject[]{bt, repResultPO};
    }
    
    
	private BufferedDataTable createInfoTable(Map<String, Object> info, final ExecutionContext exec) {
    	
    	DataColumnSpec[] cSpec = new DataColumnSpec[2];
    	cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
    	cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();
    	
    	DataTableSpec tSpec = new DataTableSpec(cSpec);
    	// can not define the name for this table 
    	BufferedDataContainer buf = exec.createDataContainer(tSpec);
    	int i=0;
    	for(String key : info.keySet()) {
    		Double value = (Double) info.get(key);
    		
    		DataCell[] currentRow = new DataCell[2];
    		currentRow[0] = new StringCell(key);
    		currentRow[1] = new DoubleCell(value);
    		buf.addRowToTable(new DefaultRow(i+"", currentRow));
    		i++;
    	}
    	buf.close();
    	BufferedDataTable bt = buf.getTable();
    	
    	return bt;
    }

	private TransEvClassMapping constructMapping(XLog log, Petrinet net,  XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, new XEventClass("DUMMY", 99999));

		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (Transition t : net.getTransitions()) {
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				String id = evClass.getId();

				if (t.getLabel().equals(id)) {
					mapping.put(t, evClass);
					break;
				}
			}

		}

		return mapping;
	}

	public XLogPortObject getLogPO() {
		return logPO;
	}

	public void setLogPO(XLogPortObject logPO) {
		this.logPO = logPO;
	}

	public PetriNetPortObject getNetPO() {
		return netPO;
	}

	public void setNetPO(PetriNetPortObject netPO) {
		this.netPO = netPO;
	}
	
	public TransEvClassMapping getMapping() {
		return mapping;
	}

	public void setMapping(TransEvClassMapping mapping) {
		this.mapping = mapping;
	}
	
	public RepResultPortObject getRepResultPO() {
		return repResultPO;
	}

	public void setRepResultPO(RepResultPortObject repResultPO) {
		this.repResultPO = repResultPO;
	}

    private IPNReplayAlgorithm getReplayer() {
		// TODO according to dialog parameter, create petri net replayer
    	// currently we only has two types
    	IPNReplayAlgorithm replayEngine = null;
    	if(m_strategy.getStringValue().equals(strategyList[0])) {
    		replayEngine = new PetrinetReplayerWithILP();
    	}else if(m_strategy.getStringValue().equals(strategyList[1])) {
    		replayEngine = new PetrinetReplayerWithoutILP();
    	}
    	
		return replayEngine;
	}

	private IPNReplayParameter getParameters(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier ) {
    	// how to create a table to assign such values here?? 
		// if many event classes are available here?? 
    	
		XEventClass evClassDummy = new XEventClass("", 1);
		
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		
		IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses,
				evClassDummy, anet.getNet().getTransitions(), 2, 5);
		
		parameters.setInitialMarking(anet.getInitialMarking());
		// here cast needed to transfer from Set<Marking> to Marking[]
		Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
		int i = 0;
		for(Marking m : anet.getFinalMarkings())
			fmList[i++] = m;
    	
		parameters.setFinalMarkings(fmList);
		
    	parameters.setGUIMode(false);
		parameters.setCreateConn(false);
		
    	return parameters;
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

    	if (!inSpecs[INPORT_LOG].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid event log!");

		if (!inSpecs[INPORT_PETRINET].getClass().equals(PetriNetPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Petri net!");

		RepResultPortObjectSpec aSpec = new RepResultPortObjectSpec();
        return new PortObjectSpec[]{null ,aSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_strategy.saveSettingsTo(settings);
    	m_classifierName.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_strategy.loadSettingsFrom(settings);
    	m_classifierName.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
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

    // implement the event classifier interface to get the event classifier implementation
	@Override
	public XEventClassifier getXEventClassifier() {
		// TODO Auto-generated method stub
		for(XEventClassifier clf : classifierList) {
			if(clf.name().equals(m_classifierName.getStringValue()))
				return clf;
		}
		return null;
	}
	
}

