package org.pm4knime.node.io.petrinet.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.util.FileUtil;
import org.pm4knime.portobject.petrinet.PetriNetPortObject;
import org.pm4knime.portobject.petrinet.PetriNetPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.base.FullPnmlElementFactory;
import org.processmining.plugins.pnml.base.Pnml;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of PetrinetWriter.
 * Write Petri net into file to implement the serialization.
 * The input is Petri net, output is the nothing I guess
 * we need to configure the file name for output. That's all. 
 *
 * @author DKF
 */
public class PetrinetWriterNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(PetrinetWriterNodeModel.class);
        
    private SettingsModelString m_outNet = PetrinetWriterNodeDialog.createFileMode();
    
    String subfix = "marking.txt";
	
	/**
     * Constructor for the node model.
     */
    protected PetrinetWriterNodeModel() {
    
        // TODO one incoming port and one outgoing port is assumed
        super(new PortType[] {PetriNetPortObject.TYPE}, new PortType[] {});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,  final ExecutionContext exec) throws Exception {

        // TODO do something here
        logger.info("Begin to write Petri net into file");

        
        PetriNetPortObject pnObj = (PetriNetPortObject) inData[0];
        
        if(pnObj.getNet() != null) {
        	CheckUtils.checkDestinationFile(m_outNet.getStringValue(),true);
            
            URL url = FileUtil.toURL(m_outNet.getStringValue());
            Path localPath = FileUtil.resolveToPath(url);
            
        	File f =  createFile(localPath, url);
        	
			// we should also write the marking into disk
        	FileOutputStream out = new FileOutputStream(f);
        	out.write(pnObj.convert2String().getBytes());
    		out.close();
        }
        
        String markingFileName = m_outNet.getStringValue().split(".pnml")[0] + subfix;
        CheckUtils.checkDestinationFile(markingFileName,true);
        
        URL m_url = FileUtil.toURL(markingFileName);
        Path m_localPath = FileUtil.resolveToPath(m_url);
        
        if(pnObj.getInitMarking() !=null && pnObj.getFinalMarking() !=null) {
        	
        	MarkingReaderWriter rw = new MarkingReaderWriter();
        	List<Marking> mList = new ArrayList<>();
        	mList.add(pnObj.getInitMarking());
        	mList.add(pnObj.getFinalMarking());
        	
        	rw.writeMarking(mList, m_localPath.toString());
        	
        }
        logger.info("End to write Petri net into pnml file");
        return new PortObject[] {};
    }

    private static File createFile(final Path localPath, final URL url) throws IOException {
        if (localPath != null) {
            return localPath.toFile();
        } else {
            return new File(url.getPath());
        }
}
    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        
        String warning = CheckUtils.checkDestinationFile(m_outNet.getStringValue(), true);
        if(warning != null) {
        	setWarningMessage(warning);
        }
        if(inSpecs[0].getClass().equals(PetriNetPortObjectSpec.class))
        	return new PortObjectSpec[] {};
        else
        	throw new InvalidSettingsException("Not a Petri net to export");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_outNet.saveSettingsTo(settings);
       
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_outNet.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_outNet.validateSettings(settings);
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

}

