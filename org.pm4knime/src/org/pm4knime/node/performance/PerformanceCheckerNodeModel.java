package org.pm4knime.node.performance;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.conformance.ConformanceCheckerNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

/**
 * <code>NodeModel</code> for the "PerformanceChecker" node. 
 * Input: one XLogPortObject + PetriNetPortObject
 * Output:
 *    -- Alignment PortObject but it doesn't matter actually
 *    -- statistics information in three output tables, 
 *    		one for the global, 
 *    			-- @reference org.processmining.plugins.manifestanalysis.
 *    							visualization.performance.ManifestCaseStatPanel#showAllStats
 *    				
 *    		one is for transitions, 
 *    			-- @reference org.processmining.plugins.manifestanalysis.
 *                                 visualization.performance.ManifestElementStatPanel#showTransStats
 *    		one for source
 *    			-- @reference org.processmining.plugins.manifestanalysis.
 *                                 visualization.performance.ManifestElementStatPanel#showPlaceStats
 *    -- One view to show the Analysis result 
 *    -- No need to show it here:: one view to show the time between transitions But only the views there,
 *    or do we need another table to output it here??
 * 
 * Process: following the ones like ConformanceChecking and get the information there; but one stuff,
 * we don't want to popup too many things. avoid it if we can
 * @author Kefang Ding
 * @reference https://svn.win.tue.nl/repos/prom/Packages/PNetReplayer/Trunk/src/org/processmining/plugins/petrinet/manifestreplayer/PNManifestReplayer.java
 *     + https://github.com/rapidprom/rapidprom-source/blob/master/src/main/java/org/rapidprom/operators/conformance/PerformanceConformanceAnalysisOperator.java
 */
public class PerformanceCheckerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(PerformanceCheckerNodeModel.class);
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;
	
	private XLogPortObject logPO ;
	private PetriNetPortObject netPO ;
    /**
     * Constructor for the node model.
     */
    protected PerformanceCheckerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE }, new PortType[] {
    			BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE, RepResultPortObject.TYPE });
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: Return a BufferedDataTable for each output port 
    	logger.info("Start: ManifestReplayer Conformance Checking");
    	logPO = (XLogPortObject) inData[INPORT_LOG];
		netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
    	
		XLog log = logPO.getLog();
		AcceptingPetriNet anet = netPO.getANet();
    	
		
    	
    	DataTableSpec gSpec = createGlobalStatsTableSpec();
    	BufferedDataContainer gBuf = exec.createDataContainer(gSpec);
    	
    	// create one for transition, one for place there
    	DataTableSpec tSpec = createElemenentStatsTableSpec("Transition");
    	BufferedDataContainer tBuf = exec.createDataContainer(tSpec);
    	
    	DataTableSpec pSpec = createElemenentStatsTableSpec("Place");
    	BufferedDataContainer pBuf = exec.createDataContainer(pSpec);
    	
    	
    	gBuf.close();
    	tBuf.close();
    	pBuf.close();
    	logger.info("End: ManifestReplayer Conformance Checking");
        return new PortObject[]{gBuf.getTable(), tBuf.getTable(), pBuf.getTable()};
    }

    /**
     * this method create a table for the element statistics info. It can be used for transitions,
     * but also for the places. But how to get this?? We should have columnClassifier
     * 
     * From the parameters, we could create a table spec from it
     * @return
     */
    private DataTableSpec createElemenentStatsTableSpec(String itemColName) {
    	String[] columnNames = {itemColName, "Property", "Min.", "Max.", "Avg.", "Std. Dev", "Freq."};
    	DataType[] columnTypes ={StringCell.TYPE, StringCell.TYPE, DoubleCell.TYPE,DoubleCell.TYPE, DoubleCell.TYPE, DoubleCell.TYPE, DoubleCell.TYPE};
    	DataTableSpec tSpec = new DataTableSpec(itemColName + " Table", columnNames, columnTypes);
    	return tSpec;
    }
    /**
     * there is one global table for this, so 
     */
    private DataTableSpec createGlobalStatsTableSpec() {
    	String[] columnNames = { "Property", "Value"};
    	DataType[] columnTypes ={StringCell.TYPE, DoubleCell.TYPE};
    	DataTableSpec tSpec = new DataTableSpec( "Global Performance Statistics Table", columnNames, columnTypes);
    	return tSpec;
    }
    
    
    
    private IPNReplayParameter getParameters(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier ) {
    	// how to create a table to assign such values here?? 
		// if many event classes are available here?? 
    	
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		
		// here we need to add cost values here, if we have default values htere
//		IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses,
//				evClassDummy, anet.getNet().getTransitions(), 2, 5);
//		
		// set all cost here 
		IPNReplayParameter parameters = createCostParameter(eventClasses, anet.getNet().getTransitions());
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
    
	private IPNReplayParameter createCostParameter(Collection<XEventClass> eventClasses, Collection<Transition> tCol) {
		
		// put dummy transition into the map
		Map<Transition, Integer> mapTrans2Cost = new HashMap();
		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap();
		Map<Transition, Integer> mapSync2Cost = new HashMap();
		
		int[] rowIndices = m_costSettings.getRowIndices();
		int[] colIndices = m_costSettings.getColumnIndices();
		String[] valueList = m_costSettings.getValues();
		
		// recover the setting, change the names from string to EventClass or Transition
		for(int i=0; i< rowIndices.length ; i += 2) {
			// currently, rowIdx=rowIdx = 0, colIdx=0, colIdx = 1, values = A,1; 
			switch(colIndices[i]) {
			case 0: // the first model move name 
				String transitionName = valueList[i];
				// find the corresponding event class
				Transition t = ConformanceCheckerNodeModel.findTransition(transitionName, tCol);
				
				int mmCost = Integer.valueOf(valueList[i+1]);
				mapTrans2Cost.put(t, mmCost);
				
				break;
			case 2: // log  move name and cost 
				
				String eventName = valueList[i];
				// find the corresponding event class
				XEventClass eClass = findEventClass(eventName, eventClasses);
				
				int lmCost = Integer.valueOf(valueList[i+1]);
				mapEvClass2Cost.put(eClass, lmCost);
				break;
			case 4:
				String stName = valueList[i];
				// find the corresponding event class
				Transition st = findTransition(stName.split(" :")[0], tCol);
				
				int smCost = Integer.valueOf(valueList[i+1]);
				mapSync2Cost.put(st, smCost);
				break;
			default:
				System.out.println("Other situation exists");
			}
			
		}
		
		return new CostBasedCompleteParam(mapEvClass2Cost, mapTrans2Cost, mapSync2Cost);
		
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: generated method stub
        return new DataTableSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
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

}

