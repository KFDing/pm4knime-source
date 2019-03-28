package org.pm4kinme.node.io.log.reader;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.pm4kinme.settingsmodel.XLogReaderNodeSettingsModel;

public class XLogReaderNodeDialog extends DefaultNodeSettingsPane {
	
	private final XLogReaderNodeSettingsModel sm = new XLogReaderNodeSettingsModel();

	public XLogReaderNodeDialog() {
		super();
		addDialogComponent(new DialogComponentFileChooser(sm.getFilePathSettingsModel(), "", new String[]{".xes"}));
	}

}