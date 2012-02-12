package ru.vasily.core.templates;

import com.google.common.collect.ImmutableMap;
import ru.vasily.mydi.MyDI;
import ru.vasily.application.AppConfig;

import static ru.vasily.core.collection.Range.range;

public class FileTemplaterTest
{
    public static void main(String[] args)
    {
        String templatePath = "/home/vasily/Development/temp/velocity/template";
        String outputPath = "/home/vasily/Development/temp/velocity/output";
        SimpleFileTemplater templater = new MyDI(new AppConfig()).getInstanceViaDI(SimpleFileTemplater.class);
        double[][] arr = new double[][]{{11, 22}, {33, 44}};

        ImmutableMap<String, ?> context = ImmutableMap.of("array", arr, "n", 2, "filename", "read_me");
        templater.renderFile(templatePath, context, outputPath);
    }
}
