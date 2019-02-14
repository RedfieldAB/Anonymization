package se.redfield.arxnode.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModel;

public interface SettingsModelConfig {

	default void save(NodeSettingsWO settings) {
		getModels().forEach(s -> s.saveSettingsTo(settings));
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsWO child = settings.addNodeSettings(c.getKey());
			c.save(child);
		}
	}

	default void load(NodeSettingsRO settings) throws InvalidSettingsException {
		for (SettingsModel s : getModels()) {
			s.loadSettingsFrom(settings);
		}
		for (SettingsModelConfig c : getChildred()) {
			NodeSettingsRO child = settings.getNodeSettings(c.getKey());
			c.load(child);
		}
	}

	default void validate() throws InvalidSettingsException {
		for (SettingsModelConfig c : getChildred()) {
			c.validate();
		}
	}

	default Collection<? extends SettingsModelConfig> getChildred() {
		return Collections.emptyList();
	}

	default List<SettingsModel> getModels() {
		return Collections.emptyList();
	}

	public String getKey();
}
