package ru.vasily.core.templates;

import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 2/10/12
 * Time: 10:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TemplateEngine
{
    Template createTemplate(String templateSource);
}
