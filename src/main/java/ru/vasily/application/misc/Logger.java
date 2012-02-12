package ru.vasily.application.misc;


public class Logger implements ILogger
{

    @Override
    public void log(String format, Object... args)
    {
        StackTraceElement elem = Thread.currentThread().getStackTrace()[2];
        StringBuilder outLog = new StringBuilder();
        outLog.append(elem.getClassName());
        outLog.append('.');
        outLog.append(elem.getMethodName());
        outLog.append('(');
        outLog.append(elem.getFileName());
        outLog.append(':');
        outLog.append(elem.getLineNumber());
        outLog.append(')');
        outLog.append('\n');
        outLog.append("log message = {");
        outLog.append(String.format(format, args));
        outLog.append("}");
        System.out.println(outLog);
    }

}
