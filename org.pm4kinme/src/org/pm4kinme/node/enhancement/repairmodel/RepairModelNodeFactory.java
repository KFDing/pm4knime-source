package org.pm4kinme.node.enhancement.repairmodel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RepairModel" Node.
 * Wrapped Repair Model from Dirk Fahland
 *
 * @author Kefang Ding
 */
public class RepairModelNodeFactory 
        extends NodeFactory<RepairModelNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RepairModelNodeModel createNodeModel() {
        return new RepairModelNodeModel();
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
    public NodeView<RepairModelNodeModel> createNodeView(final int viewIndex,
            final RepairModelNodeModel nodeModel) {
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
        return new RepairModelNodeDialog();
    }

}

