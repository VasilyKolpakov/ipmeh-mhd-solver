package ru.vasily.core.templates;

import ru.vasily.core.Writable;

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
    Writable evaluate(Map<String, ?> context);
}
