package ru.vasily.dataobjs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DataFile {
	public static void createFile(String title, double[] xCoord, Map<String, double[]> data, File path)
			throws IOException {
		if (data.isEmpty())
			throw new IllegalArgumentException("no data");
		PrintWriter out = null;
		try
		{
			Set<String> keySet = data.keySet();
			int length = xCoord.length;
			for (String key : keySet)
			{
				if (length != data.get(key).length)
				{
					throw new IllegalArgumentException("lenghts are not equal");
				}
			}
			Iterator<String> iter = keySet.iterator();
			StringBuilder variables = new StringBuilder();
			iter = keySet.iterator();
			variables.append("\"");
			variables.append("X");
			variables.append("\"");
			variables.append(",");
			variables.append("\"");
			variables.append(iter.next());
			variables.append("\"");

			while (iter.hasNext())
			{
				variables.append(",");
				variables.append("\"");
				variables.append(iter.next());
				variables.append("\"");
			}

			out = new PrintWriter(new BufferedWriter(new FileWriter(path)));

			out.println("TITLE=\"" + title + "\"");
			out.println("VARIABLES = " + variables);
			out.println("ZONE  I=" + length + " F=POINT");
			for (int i = 0; i < length; i++)
			{
				out.print(xCoord[i]);
				out.print("\t");
				for (String key : keySet)
				{
					out.print(data.get(key)[i]);
					out.print("\t");
				}
				out.println();
			}
		} catch (IOException e)
		{
			throw e;
		} finally
		{
			if (out != null)
				out.close();
		}
	}

	public static void main(String[] args) throws IOException {
		double[] sinF = new double[100];
		double[] x = new double[100];
		for (int i = 0; i < x.length; i++)
		{
			x[i] = i * 0.1;
			sinF[i] = Math.sin(x[i]);
		}
		Map<String, double[]> data = new HashMap<String, double[]>();
		data.put("sin", sinF);
		createFile("test", x, data, new File("out.dat"));
	}
}
