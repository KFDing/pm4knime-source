package org.pm4knime.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetFactory;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.framework.plugin.PluginContext;

public class PetrinetTest {
	 // test the import method, if it is right to use it 
    public static void main(String[] args) {
    	// String fileName = "D:\\ProcessMining\\Programs\\MSProject\\dataset\\property-experiment\\sequence\\model_01_sequence.pnml";
    	String fileName = "D:\\ProcessMining\\Programs\\MSProject\\dataset\\synthetic\\specific-data\\tc_and_01_01.pnml";
    	PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(ImportAcceptingPetriNetPlugin.class);
    	try {
    		ImportAcceptingPetriNetPlugin plugin = new ImportAcceptingPetriNetPlugin();
    		AcceptingPetriNet anet =  (AcceptingPetriNet) plugin.importFile(context, fileName);
//    		AcceptingPetriNet aNet =  AcceptingPetriNetFactory.importFromStream(context, 
//					new FileInputStream(fileName));
//			
    		
    		
    		PluginContext context2 =  PM4KNIMEGlobalContext.instance().getPM4KNIMEPluginContext();
//    		
    		
    		AcceptingPetriNet net = AcceptingPetriNetFactory.createAcceptingPetriNet();
    		net.importFromStream(context2, new FileInputStream(fileName));
    		System.out.println("use deprecated method");
    		System.out.println(net.getNet().getPlaces().size());
    		
			AcceptingPetriNet anet2 = AcceptingPetriNetFactory.importFromStream(context2, new FileInputStream(fileName));
			System.out.println("problem checking with import methods");
			
    		System.out.println(anet.getNet().getPlaces().size());
    		
    		
    		System.out.println(anet2.getNet().getPlaces().size());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
   
}
