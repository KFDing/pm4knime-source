package org.pm4kinme.node.enhancement.incorporatenegativeinformation;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "IncorporateNegInfo" Node.
 * This node enhances an existing model by incorporate negative information from actual event log.
 *
 * @author Kefang Ding
 */
public class IncorporateNegInfoNodeFactory 
        extends NodeFactory<IncorporateNegInfoNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public IncorporateNegInfoNodeModel createNodeModel() {
        return new IncorporateNegInfoNodeModel();
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
    public NodeView<IncorporateNegInfoNodeModel> createNodeView(final int viewIndex,
            final IncorporateNegInfoNodeModel nodeModel) {
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
        return new IncorporateNegInfoNodeDialog();
    }

}

