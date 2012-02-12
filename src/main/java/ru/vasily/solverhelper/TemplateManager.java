package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Charsets;

import static com.google.common.base.Preconditions.*;
import static ru.vasily.core.io.Parsers.asString;
import static ru.vasily.core.io.Writables.asWritable;

import com.google.common.collect.ImmutableMap;

import ru.vasily.core.io.FileSystem;
import ru.vasily.solverhelper.misc.IStringParameterizerFacrory;
import ru.vasily.solverhelper.misc.IStringParameterizerFacrory.StringParameterizer;

public class TemplateManager implements ITemplateManager
{
    private final IStringParameterizerFacrory stringParameterizerFacrory;
    private final FileSystem fileSystem;

    public TemplateManager(IStringParameterizerFacrory stringParameterizerFacrory,
                           FileSystem fileSystem)
    {
        this.stringParameterizerFacrory = stringParameterizerFacrory;
        this.fileSystem = fileSystem;
    }

    private Map<String, TemplateDirTree> loadTemplates(File templateDir) throws IOException
    {
        checkArgument(
                fileSystem.isDirectory(templateDir.toString()),
                "templates dir is not correct :"
                        + fileSystem.getAbsolutePath(templateDir.toString()));
        Map<String, TemplateDirTree> templates = new HashMap<String, TemplateManager.TemplateDirTree>();
        for (String templateTreeDir : fileSystem.listDirContents(templateDir.toString()))
        {
            templates.put(fileSystem.getFileName(templateTreeDir), loadTemplateTree(templateTreeDir));
        }
        return templates;
    }

    private TemplateDirTree loadTemplateTree(String templateTreeDirPath) throws IOException
    {
        checkArgument(
                fileSystem.isDirectory(templateTreeDirPath),
                "template tree dir is not correct :"
                        + fileSystem.getAbsolutePath(templateTreeDirPath));
        ImmutableMap.Builder<String, TemplateDirTree> dirs = ImmutableMap.builder();
        ImmutableMap.Builder<String, String> files = ImmutableMap.builder();
        for (String file : fileSystem.listDirContents(templateTreeDirPath))
        {
            if (fileSystem.isDirectory(file))
            {
                dirs.put(fileSystem.getFileName(file), loadTemplateTree(file));
            }
            if (fileSystem.isFile(file))
            {
                files.put(fileSystem.getFileName(file), fileSystem.parse(file, asString()));
            }
        }
        return new TemplateDirTree(dirs.build(), files.build());
    }

    private static class TemplateDirTree
    {
        private final ImmutableMap<String, TemplateDirTree> dirs;
        private final ImmutableMap<String, String> files;

        public TemplateDirTree(ImmutableMap<String, TemplateDirTree> dirs,
                               ImmutableMap<String, String> files)
        {
            this.dirs = dirs;
            this.files = files;
        }

        public Iterable<Entry<String, String>> getFiles()
        {
            return files.entrySet();
        }

        public Iterable<Entry<String, TemplateDirTree>> getDirs()
        {
            return dirs.entrySet();
        }
    }

    @Override
    public Templater loadTemplate(File templateDir, File out)
    {
        try
        {
            return new TemplaterImpl(loadTemplates(templateDir), out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private class TemplaterImpl implements Templater
    {

        private final File outputDir;
        private final Map<String, TemplateDirTree> templates;

        public TemplaterImpl(Map<String, TemplateDirTree> map, File out)
        {
            this.templates = map;
            this.outputDir = out;
        }

        @Override
        public void writeLayout(String type, Map<String, String> params)
        {
            TemplateDirTree templateDirTree = templates.get(type);
            checkNotNull(templateDirTree, "wrong template type name: %s avialable templates = %s",
                         type, templates.keySet());
            TemplateDirTree template = templateDirTree;

            StringParameterizer fileNameParams = stringParameterizerFacrory
                    .getStringParameterizer("(", ")", params);
            StringParameterizer fileContentParams = stringParameterizerFacrory
                    .getStringParameterizer("[", "]", params);
            writeFiles(template, outputDir, fileNameParams, fileContentParams);
        }

        private void writeFiles(TemplateDirTree dirTree, File outputDir,
                                StringParameterizer fileNameParams, StringParameterizer fileContentParams)
        {
            for (Entry<String, TemplateDirTree> dir : dirTree.getDirs())
            {
                File newDir = new File(outputDir, fileNameParams.insertParams(dir.getKey()));
                if (!fileSystem.exists(newDir.getPath()))
                {
                    fileSystem.mkdir(newDir.getPath());
                }
                writeFiles(dir.getValue(), newDir, fileNameParams, fileContentParams);
            }
            try
            {
                for (Entry<String, String> file : dirTree.getFiles())
                {
                    String newPath = fileSystem.createPath(outputDir.getPath(),
                                                           fileNameParams.insertParams(file.getKey()));
                    String content = fileContentParams.insertParams(file.getValue());
                    fileSystem.write(asWritable(content), newPath);
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
