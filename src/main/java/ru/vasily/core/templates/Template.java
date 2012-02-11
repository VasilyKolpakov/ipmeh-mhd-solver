package ru.vasily.core.templates;

import java.io.Reader;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: vasily
 * Date: 2/11/12
 * Time: 8:36 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Template
{
    void evaluate(Map<String, ?> context, Appendable output);
}
