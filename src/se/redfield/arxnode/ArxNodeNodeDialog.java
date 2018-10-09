package se.redfield.arxnode;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

import se.redfield.arxnode.config.AttributeTypeOptions;
import se.redfield.arxnode.config.Config;

public class ArxNodeNodeDialog extends DefaultNodeSettingsPane {

	private static final NodeLogger logger = NodeLogger.getLogger(ArxNodeNodeDialog.class);

	private JPanel columnsPanel;
	private PrivacyModelsPane privacyPanel;
	private List<SettingsModel> models;

	protected ArxNodeNodeDialog() {
		super();
		logger.debug("Dialog.constructor");
		// addDialogComponent(
		// new DialogComponentNumber(new
		// SettingsModelIntegerBounded(Config.CONFIG_KANONYMITY_FACTOR_KEY,
		// Config.DEFAULT_KANONYMITY_FACTOR, 1, Integer.MAX_VALUE), "K-Anonymity
		// factor:", 1, 5));

		columnsPanel = new JPanel();
		privacyPanel = new PrivacyModelsPane();
		addTab("Columns", columnsPanel);
		addTab("Privacy Models", privacyPanel.getComponent());
		selectTab("Columns");
		removeTab("Options");
	}

	@Override
	public void loadAdditionalSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs)
			throws NotConfigurableException {
		logger.debug("Dialog.loadSettings");
		models = new ArrayList<>();
		initColumnsPanel(settings, specs[0]);
		privacyPanel.load(settings);
	}

	private void initColumnsPanel(NodeSettingsRO settings, DataTableSpec spec) {
		columnsPanel.removeAll();
		columnsPanel.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		for (DataColumnSpec columnSpec : spec) {
			SettingsModelString fileModel = new SettingsModelString(
					Config.CONFIG_HIERARCHY_FILE_PREFIX + columnSpec.getName(), "");
			SettingsModelString attrTypeModel = new SettingsModelString(
					Config.CONFIG_HIERARCHY_ATTR_TYPE_PREFIX + columnSpec.getName(), "");
			attrTypeModel.addChangeListener(e -> {
				AttributeTypeOptions opt = AttributeTypeOptions.valueOf(attrTypeModel.getStringValue());
				fileModel.setEnabled(opt == AttributeTypeOptions.QUASI_IDENTIFYING_ATTRIBUTE);
				if (!fileModel.isEnabled()) {
					fileModel.setStringValue("");
				}
			});
			try {
				fileModel.loadSettingsFrom(settings);
				attrTypeModel.loadSettingsFrom(settings);
			} catch (InvalidSettingsException e) {
				logger.warn(e.getMessage(), e);
			}
			models.add(fileModel);
			models.add(attrTypeModel);

			gc.gridx = 0;
			columnsPanel.add(new JLabel(columnSpec.getName()), gc);
			gc.gridx = 1;
			columnsPanel.add(new DialogComponentStringSelection(attrTypeModel, "", AttributeTypeOptions.stringValues())
					.getComponentPanel(), gc);
			gc.gridx = 2;
			columnsPanel.add(new DialogComponentFileChooser(fileModel, "ArxNode", "ahs").getComponentPanel(), gc);
			gc.gridy++;
		}
	}

	@Override
	public void saveAdditionalSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		logger.debug("Dialog.saveSettings");
		super.saveAdditionalSettingsTo(settings);
		models.forEach(m -> m.saveSettingsTo(settings));
		privacyPanel.save(settings);
	}

}
