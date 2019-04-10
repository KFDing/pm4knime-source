package org.pm4kinme.node.evaluator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4kinme.portobject.petrinet.PetriNetPortObject;
import org.pm4kinme.portobject.petrinet.PetriNetPortObjectSpec;
import org.pm4kinme.portobject.xlog.XLogPortObject;
import org.pm4kinme.portobject.xlog.XLogPortObjectSpec;
import org.processmining.incorporatenegativeinformation.help.Configuration;
import org.processmining.incorporatenegativeinformation.plugins.EvaluateResult;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;

/**
 * This is the model implementation of RepairEvaluator.
 * Evaluate the repair model method, to show the confusion matrix
 * Input:: 
 *    event log, petri net, initial marking..
 * Output:: 
 *    confusion matrix and also the accuracy, fitness, and other measurements which is listed into
 *    DataTable... confusion matrix is an integer List, we need to show it in the result. 
 *    A DataTable for confusion matrix
 *    One DataTable for measurements. 
 *    
 *    
 * represented by a table, that's all, but now we need to change the structure of it::
 *    :: we only accept it here.
 * 
 * @author DKF
 */
public class RepairEvaluatorNodeModel extends NodeModel {
	/** The input port 0. */
    static final int INPORT_PetriNet = 1;
    static final int INPORT_Log = 0;
    /** The output port 0: confusion matrix. */
    static final int OUTPORT_CM = 0;

    /** The output port 1: accuracy measures. */
    static final int OUTPORT_M = 1;
	
	int cmColNum =  2;
    int cmRowNum =2;
    int mRowNum =4;
    
    static int rowID = 0;
    private static String[] CFG_CM_COLUMN_NAMES  =  {"Model Allowed Behaviour", "Model Not Allowed Behaviour"};
    private static String[] CFG_CM_ROW_NAMES = {"Positive Instance","Negative Instance" }; 
    
    
    private static final DataColumnSpec[] QUALITY_MEASURES_SPECS = new DataColumnSpec[]{
    		new DataColumnSpecCreator("TruePositives", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("FalsePositives", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("TrueNegatives", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("FalseNegatives", IntCell.TYPE).createSpec(),
            new DataColumnSpecCreator("Recall", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("Precision", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("Specifity", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("Accuracy", DoubleCell.TYPE).createSpec(),
            new DataColumnSpecCreator("F-measure", DoubleCell.TYPE).createSpec()
            //
           // new DataColumnSpecCreator("Cohen's kappa", DoubleCell.TYPE).createSpec()
            };
    /**
     * Constructor for the node model.
     */ 
    protected RepairEvaluatorNodeModel() {
        // TODO: Specify the amount of input and output ports needed.
    	// super(2,2);
        super(new PortType[] {XLogPortObject.TYPE, PetriNetPortObject.TYPE} , new PortType[] {BufferedDataTable.TYPE, BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

    	// transfer data into right form
    	PetriNetPortObject netPortObject = (PetriNetPortObject) inData[INPORT_PetriNet];
    	XLogPortObject logPortObject = (XLogPortObject) inData[INPORT_Log];
    	
    	Petrinet net = netPortObject.getNet();
    	Marking intialMarking = netPortObject.getInitMarking();
    	XLog log = logPortObject.getLog();
        // first to use the plugin to get the confusion matrix
    	
    	List<Integer> result = EvaluateResult.naiveCheckPN(log, net, intialMarking);
    	/*
    	int[][] confusion_matrix = new int[cmRowNum][cmColNum];
    	confusion_matrix[0][0] = result.get(Configuration.ALLOWED_POS_IDX);
    	confusion_matrix[0][1] = result.get(Configuration.NOT_ALLOWED_POS_IDX);
    	confusion_matrix[1][0] = result.get(Configuration.ALLOWED_NEG_IDX);
    	confusion_matrix[1][1] = result.get(Configuration.NOT_ALLOWED_NEG_IDX);
    	*/
    	// generate a table from it 
    	DataTableSpec cmSpec = createOutSpec();
    	BufferedDataContainer cm_container = exec.createDataContainer(cmSpec);
    	for(int i=0; i< cmRowNum; i++) {
    		DataRow row = new DefaultRow(CFG_CM_ROW_NAMES[i], result.get(Configuration.ALLOWED_POS_IDX), result.get(Configuration.NOT_ALLOWED_POS_IDX));
        	cm_container.addRowToTable(row);	
    	}
        cm_container.close();
    	BufferedDataTable cm_result = cm_container.getTable();
    	
    	// after this we need to get the measurements, reuse, please reuse!!
    	BufferedDataContainer m_container = exec.createDataContainer(new DataTableSpec(QUALITY_MEASURES_SPECS));
    	
    	int tp = result.get(Configuration.ALLOWED_POS_IDX); // true positives
        int fp = result.get(Configuration.NOT_ALLOWED_POS_IDX); // false positives
        int tn = result.get(Configuration.NOT_ALLOWED_NEG_IDX); // true negatives
        int fn = result.get(Configuration.ALLOWED_NEG_IDX); // false negatives
    	
       
        DoubleCell recall = null; // TP / (TP + FN)
        if (tp + fn > 0) {
            recall = new DoubleCell(1.0 * tp / (tp + fn));
        } 
        DoubleCell prec = null; // TP / (TP + FP)
        if (tp + fp > 0) {
            prec = new DoubleCell(1.0 * tp / (tp + fp));
        }
        
        DataCell specificity; // TN / (TN + FP)
        if (tn + fp > 0) {
            specificity = new DoubleCell(1.0 * tn / (tn + fp));
        } else {
            specificity = DataType.getMissingCell();
        }
        DataCell accuracy; // (TP + TN) /(TP +FP + FN + TN)
        if (tp + fp + fn + tn >0) {
        	accuracy = new DoubleCell(1.0 * (tp+ tn )/ (tp + fp+ fn + tn));
        } else {
        	accuracy = DataType.getMissingCell();
        }
        DataCell fmeasure; // 2 * Prec. * Recall / (Prec. + Recall)
        if (recall != null && prec != null) {
            fmeasure =
                new DoubleCell(2.0 * prec.getDoubleValue() * recall.getDoubleValue()
                    / (prec.getDoubleValue() + recall.getDoubleValue()));
        } else {
            fmeasure = DataType.getMissingCell();
        }
        // I forget to get the accuracy and other measurements, I could add them here..
        
        DataRow m_row =
                new DefaultRow(new RowKey("Quality Measurement"), new DataCell[]{new IntCell(tp), new IntCell(fp),
                    new IntCell(tn), new IntCell(fn), recall == null ? DataType.getMissingCell() : recall,
                    prec == null ? DataType.getMissingCell() : prec, specificity, accuracy, fmeasure
                    		/*DataType.getMissingCell(), DataType.getMissingCell()*/});
        
        m_container.addRowToTable(m_row);
        m_container.close();
        
        return new BufferedDataTable[]{cm_result, m_container.getTable()};
        
    }
    
    private DataTableSpec createOutSpec() throws InvalidSettingsException {
    	DataColumnSpec[] specs = new DataColumnSpec[cmColNum];
    	DataType type = IntCell.TYPE;
    	for(int i=0; i< cmColNum; i++) {
    		specs[i] = new DataColumnSpecCreator(CFG_CM_COLUMN_NAMES[i], type).createSpec();
    	}
    	
    	return new DataTableSpec(specs);
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
    protected DataTableSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: check the input ports and then generate teh output sepc
    	if (inSpecs.length < 2) {
            throw new InvalidSettingsException("The input must have at least two objects");
    	}
    	// check the inSpecs from them but, we use the portSpec for it 
    	// first inSpec should be petri net
    	if(! (inSpecs[INPORT_PetriNet] instanceof PetriNetPortObjectSpec)) {
    		throw new InvalidSettingsException("This Input port is for Petri net");
    	}
    	
    	// second is the log file in it 
    	if(! (inSpecs[INPORT_Log] instanceof XLogPortObjectSpec)) {
    		throw new InvalidSettingsException("This Input port is for XLog file");
    	}
    	
        return new DataTableSpec[]{createOutSpec(), new DataTableSpec(QUALITY_MEASURES_SPECS)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: what setting we need to save?? It seems no, we just get the confusion matrix from it
    	// should we save the model information to store without lt or with lt??
    	// keep it separate
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

