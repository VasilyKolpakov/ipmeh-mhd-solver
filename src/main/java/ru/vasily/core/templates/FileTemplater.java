package ru.vasily.core.templates;

import java.util.Map;

public interface FileTemplater
{
    void renderFile(String templatePath, Map<String, ?> context, String outputPath);
}
