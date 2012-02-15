package ru.vasily.core.templates;

import ru.vasily.core.io.FileSystem;
import ru.vasily.core.io.Writables;

import java.io.IOException;
import java.util.Map;

import static java.lang.String.format;
import static ru.vasily.core.io.Parsers.asString;

public class SimpleFileTemplater implements FileTemplater
{
    private final TemplateEngine templateEngine;
    private final FileSystem fileSystem;

    public SimpleFileTemplater(TemplateEngine templateEngine, FileSystem fileSystem)
    {
        this.templateEngine = templateEngine;
        this.fileSystem = fileSystem;
    }

    @Override
    public void renderFile(String templatePath, Map<String, ?> context, String outputPath)
    {
        try
        {
            renderInternal(templatePath, context, outputPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void renderInternal(String templatePath, Map<String, ?> context, String outputPath) throws IOException
    {
        if (fileSystem.isDirectory(templatePath))
        {
            renderDirectoryInternal(templatePath, context, outputPath);
        }
        else if (fileSystem.isFile(templatePath))
        {
            renderFileInternal(templatePath, context, outputPath);
        }
    }

    private void renderFileInternal(String templatePath, Map<String, ?> context, String outputPath)
            throws IOException
    {
        String templateSource = fileSystem.parse(templatePath, asString());
        Template template = templateEngine.createTemplate(templateSource);
        fileSystem.write(template.evaluate(context), outputPath);
        Map<FileSystem.Permission, Boolean> permissions = fileSystem.getPermissions(templatePath);
        fileSystem.setPermissions(outputPath, permissions);
    }

    private void renderDirectoryInternal(String templatePath, Map<String, ?> context, String outputPath)
            throws IOException
    {
        if (!fileSystem.exists(outputPath))
        {
            fileSystem.mkdir(outputPath);
        }
        for (String templateSubPath : fileSystem.listDirContents(templatePath))
        {
            String templateSubPathName = fileSystem.getFileName(templateSubPath);
            // TODO replaceAll("@", "\\$") - this code depends on VelocityTemplateEngine
            Template filenameTemplate = templateEngine.createTemplate(templateSubPathName.replaceAll("@", "\\$"));
            String outputSubPathName = Writables.toString(filenameTemplate.evaluate(context));
            String outputSubPath = fileSystem.createPath(outputPath, outputSubPathName);
            renderInternal(templateSubPath, context, outputSubPath);
        }
    }
}
