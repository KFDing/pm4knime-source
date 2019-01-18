package org.pm4kinme.node.evaluator;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

/**
 * <code>NodeDialog</code> for the "RepairEvaluator" Node.
 * Evaluate the repair model method, to show the confusion matrix
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author DKF
 */
public class RepairEvaluatorNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the RepairEvaluator node.
     */
    protected RepairEvaluatorNodeDialog() {

    }
}

