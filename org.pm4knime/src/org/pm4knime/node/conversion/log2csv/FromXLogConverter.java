package org.pm4knime.node.conversion.log2csv;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataCellFactory;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.DoubleCell.DoubleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.IntCell.IntCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.StringCell.StringCellFactory;
import org.knime.core.data.time.localdatetime.LocalDateTimeCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCell;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
/**
 * this class accepts an xlog file and convert it into a Datatable format, not in CSV format, please notice
 * @author kefang-pads
 *
 */
public class FromXLogConverter {
	final static XLifecycleExtension lfExt = XLifecycleExtension.instance();
	final static XConceptExtension cpExt=XConceptExtension.instance();
	final static XTimeExtension timeExt = XTimeExtension.instance();
	
	static void convert(XLog log, BufferedDataContainer buf) {
		// we need to check if event has ID if they have, we use it, else we create one for it?
		
		// data table should have a spec created from the log... by extract its attributes before or just like this?
		// get all possible attributes
		DataTableSpec spec = buf.getTableSpec();
		int colNum = spec.getColumnNames().length;
		int eventCount = 0;
		for (XTrace trace : log) {
			// save as one attribute to row
			// String caseID = cpExt.extractName(trace);
			
			// wait until the event class we have
			for(XEvent event : trace) {
				DataCell[] cells = new DataCell[colNum];
				
				// there are two concept name, which one is which one, we don't really know. 
				
				for (String attrKey : trace.getAttributes().keySet()) {
					// find the spec with the same column but how to do this?
					// it is only the attribute, we don't have the caseID, actually, that's the info we need
					
					int colIdx = spec.findColumnIndex("trace#" + attrKey);
					// not find this column, what to do?? One side, we need to know the global value 
					if(colIdx >= 0) {
						
						cells[colIdx] = createDataCell(trace.getAttributes().get(attrKey));
					}else {
						// we need to create a new column spec for it here
						
						
					}
					
					
				}
				
				// check the event attribute 
				for (String attrKey : event.getAttributes().keySet()) {
					int colIdx = spec.findColumnIndex("event#" + attrKey);
					if(colIdx >= 0) {
						// not so well here, we need the help from exe!!
						cells[colIdx] = createDataCell(event.getAttributes().get(attrKey));
					}
				}
				
				DataRow eventRow = new DefaultRow("Event " + (eventCount++), cells);	
				buf.addRowToTable(eventRow);
			}
			
		}
		
	}
	
	static DataCell createDataCell(XAttribute attr) {
		if(attr instanceof XAttributeLiteral) {
			XAttributeLiteral tmp = (XAttributeLiteral) attr;
			return StringCellFactory.create(tmp.getValue());
		}else if(attr instanceof XAttributeBoolean) {
			XAttributeBoolean tmp = (XAttributeBoolean) attr;
			return BooleanCellFactory.create(tmp.getValue());
		}else if(attr instanceof XAttributeDiscrete) {
			XAttributeDiscrete tmp = (XAttributeDiscrete) attr;
			return IntCellFactory.create((int) tmp.getValue());
		}else if(attr instanceof XAttributeContinuous) {
			XAttributeContinuous tmp = (XAttributeContinuous) attr;
			return DoubleCellFactory.create(tmp.getValue());
		}else if(attr instanceof XAttributeTimestamp){
			XAttributeTimestamp tmp = (XAttributeTimestamp) attr;
			//TODO: convert the time format
			Instant instant = tmp.getValue().toInstant();
			// what if there is zoned information in the event log? what can we do it??
			ZonedDateTime ztime = instant.atZone(ZoneId.systemDefault());
			return ZonedDateTimeCellFactory.create(ztime); 
			
		}else
			System.out.println("Unknown attribute type there");
			
		return null;
	}
	
	// we need to get all the attributes of an xlog
	static DataTableSpec createSpec(XLog log) {
		
		System.out.println("Begin reading attributes");
		XAttributeMap logAttrMap = log.getAttributes();
		String logName = "";
		for(String key :logAttrMap.keySet()) {
			logName += "##" + logAttrMap.get(key)+ "##";
		}
		
		List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		
//		attrNames.add("caseID");
//		attrTypes.add(StringCell.TYPE);
//		attrNames.add("eventID");
//		attrTypes.add(StringCell.TYPE);
//		if we haven't set global attribute into log, we can retrieve it here, so it reminds us to set the ones before
		List<XAttribute> traceAttrs = log.getGlobalTraceAttributes();
		// from trace attributes to a list of type there 
		// if traceAttrs is empty, to create corresponding traceAttr column, we need to
		// verify some trace in log!! To get their attributes, or when we visit their attr, 
		// we create column spec and append them there. 
		if(traceAttrs.isEmpty()) {
			
			
		}
		
		for(XAttribute attr : traceAttrs) {
			attrNames.add("trace#" +attr.getKey());
			attrTypes.add(findDataType(attr));
		}
		
		List<XAttribute> eventAttrs = log.getGlobalEventAttributes();
		if(eventAttrs.isEmpty()) {
			
		}
		for(XAttribute attr : eventAttrs) {
			attrNames.add("event#" + attr.getKey());
			attrTypes.add(findDataType(attr));
		}
		
		System.out.println("Finish reading attributes");
		// after getting all the attributes, we creat name and types for them
		String[] colNames = new String[attrNames.size()];
		attrNames.toArray(colNames);
		DataType[] colTypes = new DataType[attrTypes.size()];
		attrTypes.toArray(colTypes);
		
		// DataTableSpec.createColumnSpecs(colNames, colTypes);
		DataTableSpec outSpec = new DataTableSpec(logName, colNames, colTypes);
		
		
		return outSpec;
	}

	private static DataType findDataType(XAttribute attr) {
		// TODO Auto-generated method stub
		if(attr instanceof XAttributeLiteral) {
			return StringCell.TYPE;
		}else if(attr instanceof XAttributeBoolean) {
			return BooleanCell.TYPE;
		}else if(attr instanceof XAttributeDiscrete) {
			return IntCell.TYPE;
		}else if(attr instanceof XAttributeContinuous) {
			return DoubleCell.TYPE;
		}else if(attr instanceof XAttributeTimestamp){
			return DataType.getType(ZonedDateTimeCell.class);
		}else
			System.out.println("Unknown attribute type there");
		
		return null;
	}
}
