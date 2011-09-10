package ru.vasily.solverhelper;

import java.io.File;
import java.util.Map;

public interface ITemplateManager {
	Templater loadTemplate(File templateDir, File outputDir);

	public interface Templater {
		void writeLayout(String type, Map<String, String> data);
	}
}
