package org.pm4kinme.node.evaluator;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "RepairEvaluator" Node.
 * Evaluate the repair model method, to show the confusion matrix
 *
 * @author DKF
 */
public class RepairEvaluatorNodeView extends NodeView<RepairEvaluatorNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link RepairEvaluatorNodeModel})
     */
    protected RepairEvaluatorNodeView(final RepairEvaluatorNodeModel nodeModel) {
        super(nodeModel);
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
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

