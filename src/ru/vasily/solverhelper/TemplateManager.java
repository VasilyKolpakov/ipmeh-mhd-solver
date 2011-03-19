package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObj;
import ru.vasily.solverhelper.misc.IStringParameterizer;

public class TemplateManager implements ITemplateManager {
	private final IStringParameterizer stringParameterizer;

	public TemplateManager(IStringParameterizer stringParameterizer) {
		this.stringParameterizer = stringParameterizer;
	}

	@Override
	public void createLayoutFiles(File templateDir, CalculationResult resultData, File outputDir)
			throws IOException {
		String layoutTemplate = Files.toString(new File(templateDir,
				"layout.template"), Charsets.UTF_8);
		String macroTemplate = Files.toString(new File(templateDir,
				"macro.template"), Charsets.UTF_8);
		for (DataObj data : resultData.getData()) {
			Map<String, String> params = data.getParams();
			String layout = stringParameterizer.insertParams(layoutTemplate,
					params);
			String macro = stringParameterizer.insertParams(macroTemplate,
					params);
			Files.write(
					macro,
					new File(outputDir, params.get(DataObj.VALUE_NAME) + ".mcr"),
					Charsets.UTF_8);
			Files.write(
					layout,
					new File(outputDir, params.get(DataObj.VALUE_NAME) + ".lay"),
					Charsets.UTF_8);
		}
	}

	public static void main(String[] args) throws Throwable {
		String s = Files.toString(new File(new File("template"),
				"layout.template"), Charsets.UTF_8);
		System.out.println(s);
	}
}
