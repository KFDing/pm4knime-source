package org.pm4knime.node.conversion.csv2log;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.processmining.log.csvimport.config.CSVConversionConfig;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVEmptyCellHandlingMode;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;

public class CSV2XLogConfigModel{
	
	public static final String CFG_KEY_XFACTORY = "Factory Name";
	public static final String CFG_KEY_ERROR_HANDLE_MODE = "Error Handling Mode";
	public static final String CFG_KEY_EMPTY_HANDLE_MODE = "Empty Cell Handling Mode";
	public static final String CFG_KEY_ADD_START_EA = "Add Start Event Attribute";
	
	// we add the column configuration into this config model
	// here we have two model to choose for caseID and eventID items
	public static final String CFG_KEY_CASEID = "Case ID";
	public static final String CFG_KEY_EVENTID = "Event ID";
	public static final String CFG_KEY_COMPLETE_TIME = "Complete Time";
	public static final String CFG_KEY_WITH_START_TIME = "With Start Time";
	public static final String CFG_KEY_START_TIME = "Start Time";
	
	private SettingsModelString m_caseID = new SettingsModelString(CFG_KEY_CASEID, "CaseID");
	private SettingsModelString m_eventID = new SettingsModelString(CFG_KEY_EVENTID, "EventID");
	
	private SettingsModelString  m_completeTime = new SettingsModelString(CFG_KEY_COMPLETE_TIME, "Complete Time");
	private SettingsModelString m_startTime = new SettingsModelString(CFG_KEY_START_TIME, "Start Time");
	
	private SettingsModelString m_sFormat = new SettingsModelString(CFG_KEY_START_TIME + " Date Format", "dd-MM-yyyy:T:HH:mm[:ss[.SSS]]");
	private SettingsModelString m_cFormat = new SettingsModelString(CFG_KEY_COMPLETE_TIME + " Date Format", "dd-MM-yyyy:T:HH:mm[:ss[.SSS]]");
	
	private SettingsModelBoolean m_withStartTime = new SettingsModelBoolean(CFG_KEY_WITH_START_TIME, false);

	// for the factory, we can return the values later, but now we need to save the values here
	private XFactory factory = XFactoryRegistry.instance().currentDefault();
	// Various "expert" configuration options	
	private CSVErrorHandlingMode errorHandlingMode = CSVErrorHandlingMode.OMIT_TRACE_ON_ERROR;
	private CSVEmptyCellHandlingMode emptyCellHandlingMode = CSVEmptyCellHandlingMode.SPARSE;
	
	

	public void setErrorHandlingMode(CSVErrorHandlingMode errorHandlingMode) {
		// TODO Auto-generated method stub
		this.errorHandlingMode = errorHandlingMode;
	}
	public CSVErrorHandlingMode getErrorHandlingMode() {
		return errorHandlingMode;
	}

	public void setEmptyCellHandlingMode(CSVEmptyCellHandlingMode emptyCellHandlingMode) {
		// TODO Auto-generated method stub
		this.emptyCellHandlingMode = emptyCellHandlingMode;
	}

	public SettingsModelBoolean getMWithSTime() {
		return m_withStartTime;
	}
	
	public void setShouldAddStartEventAttributes(SettingsModelBoolean bModel) {
		// TODO Auto-generated method stub
		m_withStartTime = bModel;
	}
	
	public void setShouldAddStartEventAttributes(boolean value) {
		// TODO Auto-generated method stub
		m_withStartTime.setBooleanValue(value);
	}
	
	public boolean isShouldAddStartEventAttributes() {
		return m_withStartTime.getBooleanValue();
	}

	public void setFactory(XFactory factory) {
		// TODO Auto-generated method stub
		this.factory = factory;
	}

	public XFactory getFactory() {
		return factory;
	}
	
	
	static Collection<String> createPredefinedFormats() {
        // unique values
        Set<String> formats = new LinkedHashSet<String>();
        formats.add("yyyy-MM-dd'T'HH:mm[:ss[.SSS]]");
        formats.add("yyyy-MM-dd;HH:mm:ss[.SSS][.SS][.S]");
        formats.add("dd.MM.yyyy;HH:mm:ss.S");
        formats.add("yyyy-MM-dd HH:mm:ss.S");
        formats.add("dd.MM.yyyy HH:mm:ss.S");
        formats.add("yyyy-MM-dd'T'HH:mm:ss.SSS");
        formats.add("yyyy-MM-dd'T'HH:mm[:ss[.SSS]]VV['['zzzz']']");
        formats.add("yyyy-MM-dd;HH:mm:ssVV");
        formats.add("yyyy-MM-dd'T'HH:mm:ss.SSSVV");
        formats.add("yyyy-MM-dd'T'HH:mm:ss.SSSVV'['zzzz']'");
        formats.add("yyyy-MM-dd");
        formats.add("yyyy/dd/MM");
        formats.add("dd.MM.yyyy");
        formats.add("HH:mm[:ss[.SSS]]");
        formats.add("HH:mm:ss");
        
        return formats;
    }
	
	
	public void saveSettings(final NodeSettingsWO settings) {
		m_caseID.saveSettingsTo(settings);
    	m_eventID.saveSettingsTo(settings);
    	m_startTime.saveSettingsTo(settings);
    	m_completeTime.saveSettingsTo(settings);
    	m_sFormat.saveSettingsTo(settings);
    	m_cFormat.saveSettingsTo(settings);
    	m_withStartTime.saveSettingsTo(settings);
    	
		settings.addString(CFG_KEY_XFACTORY, factory.getName());
		settings.addString(CFG_KEY_ERROR_HANDLE_MODE, errorHandlingMode.name());
		settings.addString(CFG_KEY_EMPTY_HANDLE_MODE, emptyCellHandlingMode.name());
		
		
    	
	}

	public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		
		m_caseID.loadSettingsFrom(settings);
    	m_eventID.loadSettingsFrom(settings);
    	m_startTime.loadSettingsFrom(settings);
    	m_completeTime.loadSettingsFrom(settings);
    	m_sFormat.loadSettingsFrom(settings);
    	m_cFormat.loadSettingsFrom(settings);
    	m_withStartTime.loadSettingsFrom(settings);
		
		String fName = settings.getString(CFG_KEY_XFACTORY);
		
		Set<XFactory> fSet =  XFactoryRegistry.instance().getAvailable();
		for(XFactory f : fSet) {
			if(f.getName().equals(fName)) {
				factory = f;
				break;
			}
		}
		
		String errorName = settings.getString(CFG_KEY_ERROR_HANDLE_MODE);
		errorHandlingMode = CSVConversionConfig.CSVErrorHandlingMode.valueOf(errorName);
		
		String emptyName = settings.getString(CFG_KEY_EMPTY_HANDLE_MODE);
		emptyCellHandlingMode = CSVConversionConfig.CSVEmptyCellHandlingMode.valueOf(emptyName);
		
	}
	public SettingsModelString getMCaseID() {
		return m_caseID;
	}
	public void setMCaseID(SettingsModelString m_caseID) {
		this.m_caseID = m_caseID;
	}
	public SettingsModelString getMEventID() {
		return m_eventID;
	}
	public void setMEventID(SettingsModelString m_eventID) {
		this.m_eventID = m_eventID;
	}
	public SettingsModelString getMStartTime() {
		return m_startTime;
	}
	public void setMStartTime(SettingsModelString m_startTime) {
		this.m_startTime = m_startTime;
	}
	public SettingsModelString getMCompleteTime() {
		return m_completeTime;
	}
	public void setMCompleteTime(SettingsModelString m_completeTime) {
		this.m_completeTime = m_completeTime;
	}
	public SettingsModelString getMSFormat() {
		return m_sFormat;
	}
	public void setMSFormat(SettingsModelString m_sFormat) {
		this.m_sFormat = m_sFormat;
	}
	public SettingsModelString getMCFormat() {
		return m_cFormat;
	}
	public void setMCFormat(SettingsModelString m_cFormat) {
		this.m_cFormat = m_cFormat;
	}
}
