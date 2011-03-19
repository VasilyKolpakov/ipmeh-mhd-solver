package ru.vasily.solverhelper.misc;



public class Logger implements ILogger {

	@Override
	public void log(String log) {
		StackTraceElement elem = Thread.currentThread().getStackTrace()[2];
		StringBuilder outLog= new StringBuilder();
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
		outLog.append(log);
		outLog.append("}");
		System.out.println(outLog);
	}

}
