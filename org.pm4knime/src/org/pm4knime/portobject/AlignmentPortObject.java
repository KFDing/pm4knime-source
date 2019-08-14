package org.pm4knime.portobject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortObject.PortObjectSerializer;
import org.processmining.models.graphbased.NodeID;
import org.processmining.models.graphbased.directed.AbstractDirectedGraph;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
/**
 * this class serializes alignment in the form of SyncReplayResult. It contains DataTable to serve
 * as the port object?? Or we just use the java data?
 * 1. save as DataTable, buut needs access to create DataTable in execution part. 
 *     Haha, in serializer, we have access to ExecutionMonitor. But do we need to store them in such way?? Not really!!
 * 2. object in java form 
 * 3. to consider the whole list of ReplayResult, we need to traverse and serialize them all... How to do this?? 
 *    and one important stuff is that we don't really now the types of the
 * @author kefang-pads
 *
 */
public class AlignmentPortObject implements PortObject {

	private static final String ZIP_ENTRY_NAME = "AlignmentPortObject";
	// alignment result but only for one trace variance
	SyncReplayResult alignment;
	
	// serialize the whole result here. but by the way, if we want to store the Subset, just make sure 
	// that the element is serializable. It's ok.
	// PNRepResult pnAlignment;
	
	// it points to the related Petrinet object and xlog port object
	// no need to serialize the pnPO, but needs the reference there??? Difficulty here
	PetriNetPortObject pnPO;
	XLogPortObject xlogPO;
	
	public AlignmentPortObject(SyncReplayResult alignment, PetriNetPortObject pnPO, XLogPortObject xlogPO) {
		this.alignment = alignment;
		this.pnPO = pnPO;
		this.xlogPO = xlogPO;
	}
	
	public AlignmentPortObject() {}
	
	public void setAlignment(SyncReplayResult alignment) {
		this.alignment = alignment;
	}
	public SyncReplayResult getAlignment() {
		return alignment;
	}
	
	@Override
	public String getSummary() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PortObjectSpec getSpec() {
		// TODO Auto-generated method stub
		return new AlignmentPortObjectSpec();
	}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}
	
	// here we serialise the PortObject by using the prom plugin
	public static class AlignmentPortObjectSerializer extends PortObjectSerializer<AlignmentPortObject> {

		@Override
		public void savePortObject(AlignmentPortObject portObject, PortObjectZipOutputStream out, ExecutionMonitor exec)
				throws IOException, CanceledExecutionException {
			// TODO get item of alignment one item for another item to serialze them
			out.putNextEntry(new ZipEntry(ZIP_ENTRY_NAME));
			
			System.out.println("Enter the save PO in serializer");
			
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			SyncReplayResult alignment = portObject.getAlignment();
			
			// we just save all the object one by one, but if they can not be serialized, what to do then??
			// deep into the method?? A lot of trouble here!! 
			List<Object> nodeInstances = alignment.getNodeInstance();
			// Serialise it here, if we know the type of object
			// after this, we have the XEvent class, then we need to do?? 
			objOut.writeInt(nodeInstances.size());
			for(Object node: nodeInstances) {
				if(node instanceof XEventClass) {
					XEventClass ecls = (XEventClass) node;
					
					objOut.writeUTF(ecls.getClass().getName());
					objOut.writeUTF(ecls.getId());
					objOut.writeInt(ecls.getIndex());
					objOut.writeInt(ecls.size());
				}else if(node instanceof Transition) {
					// transition in Petri net, we need to store a lot of object here
					Transition t = (Transition) node;
					// we get the attribute of t ?? But how to store them?? we need to relate so much
					// we only store the label, id and related net here
					objOut.writeUTF(t.getClass().getName());
					objOut.writeUTF(t.getLabel());
					objOut.writeObject(t.getId());
					// objOut.writeObject(t.getLocalID());
					objOut.writeObject(t.getGraph());
					
				}
			}
			
			List<StepTypes> stepTypes = alignment.getStepTypes();
			objOut.writeObject(stepTypes);
			objOut.writeObject(alignment.getTraceIndex());
			objOut.writeBoolean(alignment.isReliable());
			objOut.writeObject(alignment.getInfo());
			
			objOut.close();
			
			
			out.close();
			
			System.out.println("Exit the save PO in serializer");
		}

		@Override
		public AlignmentPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {
			// TODO Auto-generated method stub
			// in the same order of writing part
			
			System.out.println("Enter the load PO in serializer");
			ZipEntry nextEntry = in.getNextEntry();
			if ((nextEntry == null) || !nextEntry.getName().equals(ZIP_ENTRY_NAME)) {
				throw new IOException("Expected zip entry '" + ZIP_ENTRY_NAME + "' not present");
			}
			// sth with PortObjectSpec here to guide the verification
			
			AlignmentPortObject alignmentPO = new AlignmentPortObject();
			ObjectInputStream inObj = new ObjectInputStream(in);
			
			List<Object> nodeInstances = new ArrayList();
			int size  = inObj.readInt();
			for(int i=0; i < size; i++) {
				
				// make sure of the class to read
				String classType = inObj.readUTF();
				if(classType.equals(XEventClass.class.getName())) {
					
					String Id = inObj.readUTF();
					int idx = inObj.readInt();
					int esize = inObj.readInt();
					
					XEventClass ecls = new XEventClass(Id, idx);
					ecls.setSize(esize);
					nodeInstances.add(ecls);
				}else if(classType.equals(Transition.class.getName())) {
					String label = inObj.readUTF();
					AbstractDirectedGraph<PetrinetNode, PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> net = null ;
					try {
						NodeID nId  = (NodeID) inObj.readObject();
						net = (AbstractDirectedGraph)inObj.readObject();
						
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Transition t = new Transition(label, net);
					nodeInstances.add(t);
				}
				
			}
			
			// read other attributes here
			try {
				List<StepTypes> stepTypes = (List<StepTypes>) inObj.readObject();
				SortedSet<Integer> traceIndex = (SortedSet<Integer>) inObj.readObject();
				boolean isReliable = inObj.readBoolean();
				Map<String, Double> info = (Map<String, Double>) inObj.readObject();
				
				SyncReplayResult result = new SyncReplayResult(nodeInstances, stepTypes, traceIndex.first());
				result.setTraceIndex(traceIndex);
				result.setReliable(isReliable);
				result.setInfo(info);
				
				alignmentPO.setAlignment(result);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			inObj.close();
			
			
			in.close();
			System.out.println("Exit the load PO in serializer");
			return alignmentPO;
		}

		// end of the serializaer
	}

}
