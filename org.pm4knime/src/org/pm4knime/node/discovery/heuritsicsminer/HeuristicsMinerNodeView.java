package org.pm4knime.node.discovery.heuritsicsminer;

import javax.swing.JComponent;

import org.knime.core.node.NodeView;
import org.processmining.models.heuristics.HeuristicsNet;
import org.processmining.models.heuristics.HeuristicsNetGraph;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.HeuristicsNetVisualization;
import org.processmining.plugins.heuristicsnet.miner.heuristics.miner.gui.HeuristicsNetVisualizer;
import org.processmining.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationGenerator;
import org.processmining.plugins.heuristicsnet.visualizer.annotatedvisualization.AnnotatedVisualizationSettings;

/**
 * <code>NodeView</code> for the "HeuristicsMiner" node.
 *
 * @author Kefang Ding
 */
public class HeuristicsMinerNodeView extends NodeView<HeuristicsMinerNodeModel> {

	private JComponent m_hnetVisualization;
	private HeuristicsNet hnet;
    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link HeuristicsMinerNodeModel})
     */
    protected HeuristicsMinerNodeView(final HeuristicsMinerNodeModel nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
        hnet =  nodeModel.getHNet();
        AnnotatedVisualizationGenerator generator = new AnnotatedVisualizationGenerator();
		
		AnnotatedVisualizationSettings settings = new AnnotatedVisualizationSettings();
		HeuristicsNetGraph graph = generator.generate(hnet, settings);
		
		m_hnetVisualization = HeuristicsNetVisualizer.visualizeGraph(graph, hnet, settings, null);
		m_hnetVisualization.setSize(500, 400);
		setComponent(m_hnetVisualization);
		
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    	// hnet = ((HeuristicsMinerNodeModel) getNodeModel()).getHNet();
    	// repaint the object by repeat the codes?? Or we set it null??
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

