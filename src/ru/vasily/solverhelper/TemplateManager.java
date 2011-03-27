package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import ru.vasily.solverhelper.misc.IStringParameterizerFacrory;
import ru.vasily.solverhelper.misc.IStringParameterizerFacrory.StringParameterizer;

public class TemplateManager implements ITemplateManager {
	private final IStringParameterizerFacrory stringParameterizerFacrory;

	public TemplateManager(IStringParameterizerFacrory stringParameterizerFacrory) {
		this.stringParameterizerFacrory = stringParameterizerFacrory;
	}

	@Override
	public void createLayoutFiles(File templateDir, Iterable<Map<String, String>> data, File outputDir)
			throws IOException {
		TemplateDirTree template = loadTemplate(templateDir);
		for (Map<String, String> params : data)
		{
			StringParameterizer fileNameParams = stringParameterizerFacrory
					.getStringParameterizer("(", ")", params);
			StringParameterizer fileContentParams = stringParameterizerFacrory
					.getStringParameterizer("[", "]", params);
			writeFiles(template, outputDir, fileNameParams, fileContentParams);
		}
	}

	private void writeFiles(TemplateDirTree dirTree, File outputDir,
			StringParameterizer fileNameParams, StringParameterizer fileContentParams) throws IOException {
		for (Entry<String, TemplateDirTree> dir : dirTree.getDirs())
		{
			File newDir = new File(outputDir, fileNameParams.insertParams(dir.getKey()));
			newDir.mkdir();
			writeFiles(dir.getValue(), newDir, fileNameParams, fileContentParams);
		}
		for (Entry<String, String> file : dirTree.getFiles())
		{
			File newFile = new File(outputDir, fileNameParams.insertParams(file.getKey()));
			String content = fileContentParams.insertParams(file.getValue());
			Files.write(content, newFile, Charsets.UTF_8);
		}
	}

	private TemplateDirTree loadTemplate(File templateDir) throws IOException {
		Preconditions
				.checkArgument(templateDir.isDirectory(),
						"template dir is not correct :" + templateDir.getAbsolutePath());
		ImmutableMap.Builder<String, TemplateDirTree> dirs = ImmutableMap.builder();
		ImmutableMap.Builder<String, String> files = ImmutableMap.builder();
		for (File file : templateDir.listFiles())
		{
			if (file.isDirectory())
			{
				dirs.put(file.getName(), loadTemplate(file));
			}
			if (file.isFile())
			{
				files.put(file.getName(), Files.toString(file, Charsets.UTF_8));
			}
		}
		return new TemplateDirTree(dirs.build(), files.build());
	}

	private static class TemplateDirTree {
		private final ImmutableMap<String, TemplateDirTree> dirs;
		private final ImmutableMap<String, String> files;

		public TemplateDirTree(ImmutableMap<String, TemplateDirTree> dirs,
				ImmutableMap<String, String> files) {
			this.dirs = dirs;
			this.files = files;
		}

		public Iterable<Entry<String, String>> getFiles() {
			return files.entrySet();
		}

		public Iterable<Entry<String, TemplateDirTree>> getDirs() {
			return dirs.entrySet();
		}
	}

	public static void main(String[] args) throws Throwable {
		String s = Files.toString(new File(new File("template"),
				"layout.template"), Charsets.UTF_8);
		System.out.println(s);
	}
}
