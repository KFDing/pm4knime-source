package org.pm4knime.node.conformance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.base.node.io.filereader.ColProperty;
import org.knime.base.node.io.tablecreator.table.Spreadsheet;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.container.ContainerTable;
import org.knime.core.data.container.DataContainer;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.util.XEventClassifierInterface;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;


/**
 * <code>NodeDialog</code> for the "ConformanceChecker" node.
 * it reads from event log and Petri net PO, choose the event classifier for them both
 * get all the event class and transitions from the input and arrange them in table
 * @author Kefang Ding
 */
public class ConformanceCheckerNodeDialog extends DataAwareNodeDialogPane {
	private static final int CFG_COST_TYPE_NUM = 6;
	public static String[] CFG_MCOST_KEY = {"model move cost", "log move cost", "sync move cost"};
	public static String[] CFG_MOVE_KEY = {"model move", "log move", "sync move"};
	
	
	XLogPortObject logPO;
	PetriNetPortObject netPO;
	// parameters to choose
	SettingsModelString m_strategy = new SettingsModelString(ConformanceCheckerNodeModel.CFGKEY_STRATEGY_TYPE, ConformanceCheckerNodeModel.strategyList[0]);
	
	// classifier to use for the event log, when the net is already shown there..
	List<XEventClassifier> classifierList = new ArrayList();
	List<String> classifierNames = new ArrayList();
	SettingsModelString m_classifierName = new SettingsModelString(XEventClassifierInterface.CKF_KEY_EVENT_CLASSIFIER, "");
	
	// create Jcomponent 
	JPanel optionPanel ; 
	Spreadsheet m_spreadsheet ; 
	
	Map<XEventClass, Integer> mapEvClass2Cost = null;
	Map<Transition, Integer> mapTrans2Cost = null;
	Map<Transition, Integer> mapSync2Cost = null;
	
	/*
	To get those values, we should have multiple settings here.
	one is for the default model move or log move, as default values
	one is can be set by individual, the options are important at first!! 
	one is the cost for syn move. we also need to give the values there.. But here, we need
	to have the information from event log, like the LogInfo in the Spec, then we need to serialize it here
	 */
    protected ConformanceCheckerNodeDialog() {
    	// how to create new item here?? 
    	// addDialogComponent(new DialogComponentStringSelection(m_variant, "Select Inductive Miner Type", variantList));
    	initializeClassifiers();
    	
    	optionPanel = new JPanel();
    	optionPanel.setLayout(new BoxLayout(optionPanel,
                BoxLayout.Y_AXIS));
    	addTab("Options", optionPanel, true);
    	
    	m_spreadsheet = new Spreadsheet();
    	optionPanel.add(m_spreadsheet);
    	
    	// add other component in JPanel here
    	DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(m_strategy, "Select Replay Strategy", ConformanceCheckerNodeModel.strategyList);
    	optionPanel.add(m_strategyComp.getComponentPanel());
    	
    	// setClassifierNames();
    	DialogComponentStringSelection m_ClassifierComp = new DialogComponentStringSelection(m_classifierName, "Select Classifier Name", classifierNames );
    	optionPanel.add(m_ClassifierComp.getComponentPanel());
    	
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO for the ones that could be saved, then we save it there, others values, not saving..
		m_strategy.saveSettingsTo(settings);
		m_classifierName.saveSettingsTo(settings);
	}
	
	
	// here we create table to get the cost-table mapping, or we just get the event names and transitions
	// but the relation to event classifier, we need to consider it. In default, we choose the EventName here
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
		// inputs are XLog and Petri net
		if (!(input[ConformanceCheckerNodeModel.INPORT_LOG] instanceof XLogPortObject))
			throw new NotConfigurableException("Input is not a valid event log!");

		if (!(input[ConformanceCheckerNodeModel.INPORT_PETRINET] instanceof PetriNetPortObject))
			throw new NotConfigurableException("Input is not a valid Petri net!");
		
		logPO = (XLogPortObject) input[ConformanceCheckerNodeModel.INPORT_LOG];
		netPO = (PetriNetPortObject) input[ConformanceCheckerNodeModel.INPORT_PETRINET];
		
		// but could we store the event classifier somewhere?? So we don't need to?? Not really!!
		// we can set the default values
		XLog log = logPO.getLog();
		// here we can have different types of classifier for an event log
		// classifierList = log.getClassifiers();
		// System.out.println("Classifier size is " + classifierList.size());
		// as one option here to choose, after it is chosen, we can show the cost information
		// we can have a default one, but if we have chosen, then update!!
		
		
		XEventClassifier eventClassifier = new XEventNameClassifier();
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  summary.getEventClasses().getClasses();
		System.out.println("Event Class  size is " + eventClasses.size());
		
		Collection<Transition> transitions = netPO.getANet().getNet().getTransitions();
		System.out.println("Transition size is " + transitions.size());
		// get the list of event names here, but if we don't have the same event name classifier, what to do??
		// then we need to make sure the classifier should be in the PetriNetPortObjectSpec.. Or 
		// or another option is, we have transitions here, so we just choose the event log classifier to map!!!
		System.out.println("Here we need to create three tables for the costs");
		// two tables, or we can even have three tables, to define different costs for each moves
		// to save space, we use 
		
	}
	// we create 1 table for three types of cost to show them at first
	private void setCostTable() {
		
		SortedMap<Integer, ColProperty> m_colProps = new TreeMap<Integer, ColProperty>();
		
		// we could create a table and get its data and put it there..
		
		DataColumnSpec[] specs = new DataColumnSpec[CFG_COST_TYPE_NUM];
		int i=0;
		while(i < CFG_COST_TYPE_NUM) {
			specs[i++] = new DataColumnSpecCreator(CFG_MOVE_KEY[i/2], StringCell.TYPE).createSpec();
			specs[i++] = new DataColumnSpecCreator(CFG_MCOST_KEY[i/2], IntCell.TYPE).createSpec();
			
		}
		
		// here we create a table from setting in NodeModel, but just save it here.. But anyway, could we get it by all??
		
		
		DataTableSpec tableSpec = new DataTableSpec(specs);
		ColumnRearranger c = new ColumnRearranger(tableSpec);
		
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
}

