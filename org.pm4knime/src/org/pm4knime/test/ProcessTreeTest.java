package org.pm4knime.test;

import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.processtree.ProcessTree;
import org.processmining.processtree.ptml.importing.PtmlImportTree;

/**
 * this class tests the class path loading situation in project and in KNIME. The difference part is in the MANIFEST>mf. 
 * 
 * @author kefang-pads
 *
 */
public class ProcessTreeTest {
	public static void main(String[] args) {
		ProcessTree tree = null;
		String fileName = "D:\\ProcessMining\\Programs\\MSProject\\dataset\\property-experiment\\model_pt_02_with_2_xor.ptml";
		// use the plugin to improt the tree and output the tree
		PluginContext context = PM4KNIMEGlobalContext.instance().getFutureResultAwarePluginContext(PtmlImportTree.class);
		
		
		PtmlImportTree importer = new PtmlImportTree();
		
		try {
			tree = (ProcessTree) importer.importFile(context, fileName);
			System.out.println(tree.getNodes().size());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
