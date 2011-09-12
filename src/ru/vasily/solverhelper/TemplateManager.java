package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import ru.vasily.core.FileSystem;
import ru.vasily.solverhelper.misc.IStringParameterizerFacrory;
import ru.vasily.solverhelper.misc.IStringParameterizerFacrory.StringParameterizer;

public class TemplateManager implements ITemplateManager {
	private final IStringParameterizerFacrory stringParameterizerFacrory;
	private final FileSystem fileSystem;

	public TemplateManager(IStringParameterizerFacrory stringParameterizerFacrory,
			FileSystem fileSystem) {
		this.stringParameterizerFacrory = stringParameterizerFacrory;
		this.fileSystem = fileSystem;
	}

	private TemplateDirTree loadTemplateTree(File templateDir) throws IOException {
		Preconditions
				.checkArgument(fileSystem.isDirectory(templateDir),
						"template dir is not correct :" + fileSystem.getAbsolutePath(templateDir));
		ImmutableMap.Builder<String, TemplateDirTree> dirs = ImmutableMap.builder();
		ImmutableMap.Builder<String, String> files = ImmutableMap.builder();
		for (File file : fileSystem.listFiles(templateDir))
		{
			if (fileSystem.isDirectory(file))
			{
				dirs.put(file.getName(), loadTemplateTree(file));
			}
			if (fileSystem.isFile(file))
			{
				files.put(file.getName(), fileSystem.toString(file, Charsets.UTF_8));
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

	@Override
	public Templater loadTemplate(File templateDir, File out) {
		try
		{
			return new TemplaterImpl(loadTemplateTree(templateDir), out);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private class TemplaterImpl implements Templater {

		private final TemplateDirTree template;
		private final File outputDir;

		public TemplaterImpl(TemplateDirTree template, File out) {
			this.template = template;
			this.outputDir = out;
		}

		@Override
		public void writeLayout(String type, Map<String, String> params) {
			StringParameterizer fileNameParams = stringParameterizerFacrory
					.getStringParameterizer("(", ")", params);
			StringParameterizer fileContentParams = stringParameterizerFacrory
					.getStringParameterizer("[", "]", params);
			writeFiles(template, outputDir, fileNameParams, fileContentParams);
		}

		private void writeFiles(TemplateDirTree dirTree, File outputDir,
				StringParameterizer fileNameParams, StringParameterizer fileContentParams) {
			for (Entry<String, TemplateDirTree> dir : dirTree.getDirs())
			{
				File newDir = new File(outputDir, fileNameParams.insertParams(dir.getKey()));
				if (!fileSystem.exists(newDir))
				{
					fileSystem.mkdir(newDir);
				}
				writeFiles(dir.getValue(), newDir, fileNameParams, fileContentParams);
			}
			try
			{
				for (Entry<String, String> file : dirTree.getFiles())
				{
					File newFile = new File(outputDir, fileNameParams.insertParams(file.getKey()));
					String content = fileContentParams.insertParams(file.getValue());
					fileSystem.write(content, newFile, Charsets.UTF_8);
				}
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
}
