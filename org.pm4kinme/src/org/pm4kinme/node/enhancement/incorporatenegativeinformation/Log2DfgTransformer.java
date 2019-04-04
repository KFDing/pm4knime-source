package org.pm4kinme.node.enhancement.incorporatenegativeinformation;

import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.plugins.InductiveMiner.AttributeClassifiers;
import org.processmining.plugins.InductiveMiner.dfgOnly.Dfg;
import org.processmining.plugins.InductiveMiner.dfgOnly.log2logInfo.IMLog2IMLogInfoDefault;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLog;
import org.processmining.plugins.InductiveMiner.mining.logs.IMLogImpl;

/**
 * this class is used to transform log into dfg without dialog to get the parameters,
 * we can set it before, or simply to use the defaultValue.
 * -- first, we use the default value
 * -- it has two parameters, one is the log, one is the type to use
 * @author dkf
 *
 */
public class Log2DfgTransformer {
	public static Dfg transform(XLog log, XEventClassifier classifer) {
		
		IMLog IMlog = new IMLogImpl(log, classifer);
		return new IMLog2IMLogInfoDefault().createLogInfo(IMlog).getDfg();
	}
}
