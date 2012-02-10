package ru.vasily.core.templates;

import java.io.Reader;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 2/10/12
 * Time: 10:17 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TemplateEngine
{
    void evaluate(Map<String, ?> context, Appendable output, Reader templateReader);

}
