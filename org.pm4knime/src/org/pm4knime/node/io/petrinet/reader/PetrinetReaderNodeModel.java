package org.pm4knime.node.io.petrinet.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.knime.base.node.preproc.filter.row.RowFilterIterator;
import org.knime.base.node.preproc.sample.LinearSamplingRowFilter;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.util.CheckUtils;
import org.pm4knime.node.io.petrinet.writer.MarkingReaderWriter;
import org.pm4knime.portobject.petrinet.PetriNetPortObject;
import org.pm4knime.portobject.petrinet.PetriNetPortObjectSpec;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.pnml.importing.PnmlImportNet;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;


/**
 * This is the model implementation of PetrinetReader.
 * read Petri net from pnml file
 *
 * @author KFDing
 */
public class PetrinetReaderNodeModel extends NodeModel {
    
    // the logger instance
    private static final NodeLogger logger = NodeLogger
            .getLogger(PetrinetReaderNodeModel.class);
    
    public static final String CFG_FILE_NAME = "PetriNet fileName";

    // now we should assign one read types to the model
    public static final String GFG_PETRINET_TYPE = "petrinetType";
    // don't know the use of this parameter
	public static final String CFG_HISTORY_ID = "historyID";
	
	public static final String[] defaultValue = new String[] {"Petri Net"};

    
	private final SettingsModelString m_fileName = new SettingsModelString(PetrinetReaderNodeModel.CFG_FILE_NAME, "");
	private final SettingsModelString m_type = new SettingsModelString(GFG_PETRINET_TYPE, "");
	String subfix = "marking.txt";
	String markingFileName = m_fileName.getStringValue().split(".pnml")[0] + subfix;
	
	private PetriNetPortObject m_netPort = null;
	
    public PetrinetReaderNodeModel() {
    
        // TODO as one of those tests
        super(new PortType[] {PetriNetPortObject.TYPE_OPTIONAL}, new PortType[] {PetriNetPortObject.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	
        if(m_type.getStringValue().equals(defaultValue[0])) {
            logger.info("Read Naive Petri net !");
            
            m_netPort = new PetriNetPortObject();
            PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(ImportAcceptingPetriNetPlugin.class);
        	m_netPort.setContext(context);
        	// here we must specify the file name from it and then we can read it..
        	// else we can't...
        	AcceptingPetriNet aNet =  AcceptingPetriNetFactory.importFromStream(context, 
        			new FileInputStream(m_fileName.getStringValue()));
			
        	m_netPort.setNet(aNet.getNet());
        	m_netPort.setInitMarking(aNet.getInitialMarking());
        	m_netPort.setFinalMarkingSet(aNet.getFinalMarkings());
			
        }
		
		logger.info("end of reading of Petri net");
        return new PortObject[] {m_netPort};
    }
    
   
    
    private AcceptingPetriNet importAcceptingPetriNet() {
    	PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(ImportAcceptingPetriNetPlugin.class);
    	ImportAcceptingPetriNetPlugin plugin = new ImportAcceptingPetriNetPlugin();
    	try {
    		m_netPort.setContext(context);
			AcceptingPetriNet result =  (AcceptingPetriNet) plugin.importFile(context, m_fileName.getStringValue());
			
			return result;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return null;
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

    /**
     * we need to define our own Spec for the Petri net and not DataTableSpec
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        
        // here to check if the file exists,if not, it gives a warning information
    	String fileS = m_fileName.getStringValue();
    	String warning = CheckUtils.checkSourceFile(fileS);
    	if(warning != null ) {
    		setWarningMessage(warning);
    	}
    	// we need to get the values from inSpecs to m_fileName?? Or we have it directly?? 
    	URL url = getURLFromSettings(fileS);
    	if(url == null) {
    		throw new IllegalArgumentException("url can't be null");
    	}
    	String url2String ;
    	if("file".equals(url.getProtocol())) {
    		try {
    			url2String =  new File(url.toURI()).getAbsolutePath();
    		}catch(Exception e){
    			url2String = url.toString();
    			String msg = "File \"" + url + "\" is not a valid PMML file:\n" + e.getMessage();
    			setWarningMessage(msg);
    		}
    	}else {
    		url2String = url.toString();
    	}

    	// reader node, there is no inSpec for portObject, so we need to create Spec especially for
    	// Petri net 
    	// the use of  Spec is what ??
    	m_netPort = new PetriNetPortObject();
    	
    	PetriNetPortObjectSpec spec =  m_netPort.getSpec();
    	// spec.setFileName(url2String);
    	// for input data
    	
        return new PortObjectSpec[]{spec};
    }

    /** Convert argument string to a URL.
     * @param fileS The file string (a url or a file path)
     * @return The url (if it's a path then file access is checked)
     * @throws InvalidSettingsException If no valid url given.
     */
   private static URL getURLFromSettings(final String fileS)
       throws InvalidSettingsException {
       if (fileS == null || fileS.length() == 0) {
           throw new InvalidSettingsException("No file/url specified");
       }

       try {
           return new URL(fileS);
       } catch (MalformedURLException e) {
           File tmp = new File(fileS);
           if (tmp.isFile() && tmp.canRead()) {
               try  {
                   return tmp.getAbsoluteFile().toURI().toURL();
               } catch (MalformedURLException e1) {
                   throw new InvalidSettingsException(e1);
               }
           }
           throw new InvalidSettingsException("File/URL \"" + fileS
                      + "\" cannot be parsed as a URL or represents a non exising file location");
       }

   }
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {

        // TODO save user settings to the config object.
      m_fileName.saveSettingsTo(settings);
      m_type.saveSettingsTo(settings);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
            
        // TODO load (valid) settings from the config object.
        // It can be safely assumed that the settings are valided by the 
        // method below.
      m_fileName.loadSettingsFrom(settings);
      m_type.loadSettingsFrom(settings);
      
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	// here check if the source location has a valid syntax, like it is not empty or in the required extension format
    	// to make sure the setting can be loaded into the workflow
    	m_fileName.validateSettings(settings);
    	m_type.validateSettings(settings);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        
        // TODO load internal data. 
        // Everything handed to output ports is loaded automatically (data
        // returned by the execute method, models loaded in loadModelContent,
        // and user settings set through loadSettingsFrom - is all taken care 
        // of). Load here only the other internals that need to be restored
        // (e.g. data used by the views).

    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
       
        // TODO save internal models. 
        // Everything written to output ports is saved automatically (data
        // returned by the execute method, models saved in the saveModelContent,
        // and user settings saved through saveSettingsTo - is all taken care 
        // of). Save here only the other internals that need to be preserved
        // (e.g. data used by the views).

    }

}

