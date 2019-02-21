package org.pm4kinme.node.evaluator;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RepairEvaluator" Node.
 * Evaluate the repair model method, to show the confusion matrix
 *
 * @author DKF
 */
public class RepairEvaluatorNodeFactory 
        extends NodeFactory<RepairEvaluatorNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RepairEvaluatorNodeModel createNodeModel() {
        return new RepairEvaluatorNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<RepairEvaluatorNodeModel> createNodeView(final int viewIndex,
            final RepairEvaluatorNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new RepairEvaluatorNodeDialog();
    }

}

