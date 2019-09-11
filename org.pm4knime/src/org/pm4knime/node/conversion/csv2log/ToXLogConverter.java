package org.pm4knime.node.conversion.csv2log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataTable;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;
import org.processmining.log.utils.XUtils;

/**
 * this class belongs to utility. But currently use is to create an EventLog from CSV file.
 * @author kefang-pads
 * @reference org.processmining.log.csvimport.handler.XESConversionHandlerImpl
 */
public class ToXLogConverter {

	private static Pattern INVALID_MS_PATTERN = Pattern.compile("(:[0-5][0-9]\\.[0-9]{3})[0-9]*$");
	
	private XFactory factory = null;
	
	private XLog log = null;
	private XTrace currentTrace = null;
	private List<XEvent> currentEvents = new ArrayList<>();
	private boolean errorDetected = false;
	
	private XEvent currentEvent = null;
	private XEvent currentStartEvent;
	private int instanceCounter = 0;
	
	CSV2XLogConfigModel config = null;
	
	public void setConfig(CSV2XLogConfigModel config) {
		this.config = config;
		factory = config.getFactory();
	}
	
	
	/**
	 * convert CSV data table into xlog, but we need to know the column index,
	 * so we know which one is which event?
	 * @param logName
	 */
	public void convertCVS2Log(BufferedDataTable csvData) {
		String logName = csvData.getSpec().getName();
		startLog(logName + "event log");
		// here we need to set the idx for the table
//		int caseIDIdx = getColIndex(csvData, config.getMCaseID().getStringValue());
//		int eventIDIdx = getColIndex(csvData, config.getMEventID().getStringValue());
//		int cTimeIdx = getColIndex(csvData, config.getMCompleteTime().getStringValue());
//		int sTimeIdx = getColIndex(csvData, config.getMStartTime().getStringValue());
		
		int caseIDIdx = -1, eventIDIdx = -1, cTimeIdx = -1, sTimeIdx = -1;
		String[] colNames = csvData.getDataTableSpec().getColumnNames();
		List<Integer> otherIndices = new ArrayList<Integer>();
		for(int i=0; i< colNames.length; i++) {
			if(colNames[i].equals(config.getMCaseID().getStringValue())) {
				caseIDIdx = i;
			}else if(colNames[i].equals(config.getMEventID().getStringValue())) {
				eventIDIdx = i;
			}else if(colNames[i].equals(config.getMCompleteTime().getStringValue())) {
				cTimeIdx = i;
				// if they both have the same column, we must assign them both
				// with the same index
				if(config.isShouldAddStartEventAttributes() && colNames[i].equals(config.getMStartTime().getStringValue())) {
					sTimeIdx = i;
				}
			}else if(config.isShouldAddStartEventAttributes() && colNames[i].equals(config.getMStartTime().getStringValue())) {
				sTimeIdx = i;
				if(colNames[i].equals(config.getMCompleteTime().getStringValue())) {
					cTimeIdx = i;
				}
			}else {
				otherIndices.add(i);
			} 
		}
		
		int currentCaseID = -1, newCaseID;
		
		String cFormat = config.getMCFormat().getStringValue();
		String sFormat = null;
		if(config.isShouldAddStartEventAttributes())
			sFormat = config.getMSFormat().getStringValue();
		
		
		for(DataRow row : csvData) {
			newCaseID = ((IntCell) row.getCell(caseIDIdx)).getIntValue();
			
			if(newCaseID != currentCaseID) {
				// we meet a new trace, end old one and begin new one
				if(currentCaseID!=-1)
					endTrace(currentCaseID + ""); // make it as a string
				
				currentCaseID = newCaseID;

				startTrace(currentCaseID + "");
			}
			
			// deal with new event class, it can be in discrete value, 
			// but as ID, we assume it in dicrete value
			String eventClass = null;
			DataCell eventIDData = row.getCell(eventIDIdx);
			if(eventIDData.getType().equals(IntCell.TYPE)) {
				eventClass = ((IntCell)eventIDData).getIntValue() + "";
			}else if(eventIDData.getType().equals(StringCell.TYPE)) {
				eventClass = ((StringCell)eventIDData).getStringValue();
			}
				
			// read time stamp, one way, to convert due to the use of DataTable, 
			// another way, to convert them one by one
			
			// if we don't use the original convertion in knime, it should work
			try {
				String cTime = ((StringCell) row.getCell(cTimeIdx)).getStringValue();
				Date cTimeDate = convertString2Date(cFormat, cTime);
				Date sTimeDate = null;
				if(config.isShouldAddStartEventAttributes()) {
					String sTime = ((StringCell) row.getCell(sTimeIdx)).getStringValue();// need to make sure the format same??
					sTimeDate = convertString2Date(sFormat, sTime);
				}
				startEvent(eventClass, cTimeDate, sTimeDate);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// after this, we process other attributes, like resource, costs;; At this point, we need to differ their types 
			// and add attributes to the currentEventClass...
			// we have a list of colIdx to be checked
			for(int otherIdx: otherIndices) {
				DataCell otherData = row.getCell(otherIdx);
				
				if(otherData.getType().equals(IntCell.TYPE)){
					IntCell iCell = (IntCell) otherData;
					// here we set extension as null, but later we should improve it
					assignAttribute(currentEvent, factory.createAttributeDiscrete(colNames[otherIdx] ,iCell.getIntValue(), null));
				
				}else if(otherData.getType().equals(DoubleCell.TYPE)){
					DoubleCell dCell = (DoubleCell) otherData;
					// here we set extension as null, but later we should improve it
					assignAttribute(currentEvent, factory.createAttributeContinuous(colNames[otherIdx] ,dCell.getDoubleValue(), null));
				
				}else if(otherData.getType().equals(StringCell.TYPE)){
					StringCell sCell = (StringCell) otherData;
					// here we set extension as null, but later we should improve it
					assignAttribute(currentEvent, factory.createAttributeLiteral(colNames[otherIdx] ,sCell.getStringValue(), null));
				
				}else if(otherData.getType().equals(BooleanCell.TYPE)){
					BooleanCell bCell = (BooleanCell) otherData;
					// here we set extension as null, but later we should improve it
					assignAttribute(currentEvent, factory.createAttributeBoolean(colNames[otherIdx] ,bCell.getBooleanValue(), null));
				}
				// here could be DateTime type, but how to say it ??
				
			}
			
			endEvent();
		}
		// Close last trace
		endTrace(currentCaseID + "");	
	}
	
	/*
	 * create a log file w.r.t. DataTable input here
	 */
	public void startLog(String logName) {
		log = factory.createLog();
		// create attribute related to log
		// add name to log
		// String name = "temp name";
		// XAttribute nameAttr = factory.createAttributeLiteral(XConceptExtension.KEY_NAME, name, XConceptExtension.instance());
		// XUtils.putAttribute(log, nameAttr);
		assignName(factory, log, logName);
		// assign EventName Classifier to log
		log.getExtensions().add(XConceptExtension.instance());
		log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
		
		// assign time stamp related attributes
		log.getExtensions().add(XTimeExtension.instance());
		log.getExtensions().add(XLifecycleExtension.instance());
		log.getClassifiers().add(XUtils.STANDARDCLASSIFIER);
		
		// add other extensions for each column here
		// for organization
		XExtension orgExt =XOrganizationalExtension.instance();	
		log.getExtensions().add(orgExt);
		// for cost?
		
	}
	
	public void startTrace(String caseId) {
		currentEvents.clear();
		errorDetected = false;
		currentTrace = factory.createTrace();
		// what if sth attributes are trace attributes?? how to classify this stuff??
		// it will totally different, if we have set some values there as the trace attributes;
		// we will assign attributes to trace, but also to global attributes there
		// we need to do it there
		assignName(factory, currentTrace, caseId);
	}

	public void endTrace(String caseId) {
		if (errorDetected && config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_TRACE_ON_ERROR) {
			// Skip the entire trace
			return;
		}
		
		currentTrace.addAll(currentEvents);
		log.add(currentTrace);
	}
	
	public void startEvent(String eventClass, Date completionTime, Date startTime) {
		if (config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Include the other events in that trace
			errorDetected = false;
		}
		
		currentEvent = factory.createEvent();
		if (eventClass != null) {
			assignName(factory, currentEvent, eventClass);
		}

		if (startTime == null && completionTime == null) {
			// Both times are unknown only create an event assuming it is the completion event
			assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);
		} else if (startTime != null && completionTime != null) {
			// Both start and complete are present
			String instance = String.valueOf((instanceCounter++));

			// Assign attribute for complete event (currentEvent)			
			assignTimestamp(factory, currentEvent, completionTime);
			assignInstance(factory, currentEvent, instance);
			assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);

			// Add additional start event
			currentStartEvent = factory.createEvent();
			if (eventClass != null) {
				assignName(factory, currentStartEvent, eventClass);
			}
			assignTimestamp(factory, currentStartEvent, startTime);
			assignInstance(factory, currentStartEvent, instance);
			assignLifecycleTransition(factory, currentStartEvent, XLifecycleExtension.StandardModel.START);

		} else {
			// Either start or complete are present
			if (completionTime != null) {
				// Only create Complete
				assignTimestamp(factory, currentEvent, completionTime);
				assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);
			} else if (startTime != null) {
				// Only create Start
				assignTimestamp(factory, currentEvent, startTime);
				assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.START);
			} else {
				throw new IllegalStateException(
						"Both start and complete time are NULL. This should never be the case here!");
			}
		}
	}

	
	public void endEvent() {
		if (errorDetected && config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Do not include the event
			return;
		}
		// Add start event before complete event to guarantee order for events with same time-stamp
		if (currentStartEvent != null) {
			currentEvents.add(currentStartEvent);
			currentStartEvent = null;
		}
		currentEvents.add(currentEvent);
		currentEvent = null;
	}
	
	
	public XLog getXLog() {
		return log;
	}
	
	private static void assignAttribute(XAttributable a, XAttribute value) {
		XUtils.putAttribute(a, value);
	}

	private static void assignLifecycleTransition(XFactory factory, XAttributable a, StandardModel lifecycle) {
		assignAttribute(a, factory.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, lifecycle.getEncoding(),
				XLifecycleExtension.instance()));
	}

	private static void assignInstance(XFactory factory, XAttributable a, String value) {
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_INSTANCE, value, XConceptExtension.instance()));
	}

	private static void assignTimestamp(XFactory factory, XAttributable a, Date value) {
		assignAttribute(a,
				factory.createAttributeTimestamp(XTimeExtension.KEY_TIMESTAMP, value, XTimeExtension.instance()));
	}

	private static void assignName(XFactory factory, XAttributable a, String value) {
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, value, XConceptExtension.instance()));
	}
	
	// convert string to DateTime there
	public Date convertString2Date(String format, String value) throws ParseException {
		DateFormat customDateFormat = new SimpleDateFormat(format); 
		
		if (value == null) {
			throw new ParseException("Could not parse NULL timestamp!", 0);
		}

		if (customDateFormat != null) {
			ParsePosition pos = new ParsePosition(0);
			Date date = customDateFormat.parse(value, pos);
			
			// Fix if there are more than 3 digits for ms for example 44.00.540000, do not return and
			// ensure string is formatted to 540 ms instead of 540000 ms
			if (date != null && !INVALID_MS_PATTERN.matcher(value).find()) {
				return date;
			} else {
				String fixedValue = INVALID_MS_PATTERN.matcher(value).replaceFirst("$1");
				pos.setIndex(0);
				date = customDateFormat.parse(fixedValue, pos);
				if (date != null) {
					return date;
				} else {
					String pattern = "unkown";
					if (customDateFormat instanceof SimpleDateFormat) {
						pattern = ((SimpleDateFormat) customDateFormat).toPattern();
					}
					throw new ParseException("Could not parse " + value + " using pattern '" + pattern + "'",
							pos.getErrorIndex());
				}
			}
		}else
			throw new ParseException("Could not parse " + value, -1);
        
	}
	
}
