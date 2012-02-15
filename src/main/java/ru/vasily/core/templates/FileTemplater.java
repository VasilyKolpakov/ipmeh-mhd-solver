package ru.vasily.core.templates;

import java.util.Map;

public interface FileTemplater
{
    // TODO refactor to factory
    void renderFile(String templatePath, Map<String, ?> context, String outputPath);
}
