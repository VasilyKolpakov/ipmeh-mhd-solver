package ru.vasily.solverhelper;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;

import ru.vasily.core.FileSystem;
import ru.vasily.core.FileSystem.Parser;
import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.DataObject;
import ru.vasily.dataobjs.DataObjectService;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.ISolver.IterativeSolver;
import ru.vasily.solverhelper.misc.FileTypeFilter;

public class ApplicationMain
{

	private static final String PARAMS_FILE_EXTENSION = "js";
	private final DataObjectService paramsLoader;
	private final ISolver solver;
	private final IResultWriter dataWriter;
	private final FileSystem fileSystem;

	public ApplicationMain(DataObjectService paramsLoader, ISolver solver,
			IResultWriter dataWriter, FileSystem fileSystem)
	{
		this.paramsLoader = paramsLoader;
		this.solver = solver;
		this.dataWriter = dataWriter;
		this.fileSystem = fileSystem;
	}

	public void execute(String paramsPath, String outputPathStr,
			String templateDir, boolean iterative)
	{
		File inputPath = new File(paramsPath);
		final File output = new File(outputPathStr);
		File template = new File(templateDir);
		File[] inputPaths = fileSystem.listFiles(inputPath, (FilenameFilter) new FileTypeFilter(
				PARAMS_FILE_EXTENSION));
		for (File path : inputPaths)
		{
			try
			{
				DataObject param = fileSystem.parse(new Parser<DataObject>() {

					@Override
					public DataObject parseFrom(Reader in) throws IOException {
						return paramsLoader.readObject(in);
					}
				}, path);
				
				if (iterative)
				{
					IterativeSolver iterativeSolver = solver.getSolver(param);
					{
						CalculationResult result = iterativeSolver.next(0);
						System.out.println(result.getLog());
						writeResult(output, template, path, result);
					}
					while (true)
					{
						String input = System.console().readLine(
								"write number of iterations or \'skip\':\n");
						if ("skip".equals(input))
							break;
						int n = parseInt(input);
						CalculationResult result = iterativeSolver.next(n);
						System.out.println(result.getLog());
						writeResult(output, template, path,
								result);
					}
				}
				else
				{
					CalculationResult result = solver.solve(param);
					writeResult(output, template, path, result);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private int parseInt(String input)
	{

		int result = 0;
		try
		{
			result = Integer.parseInt(input);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
		return result < 0 ? 0 : result;
	}

	private void writeResult(final File output, File template, File path,
			CalculationResult result) throws IOException
	{
		dataWriter.createResultDir(new File(output, path.getName().substring(0,
				path.getName().length() - PARAMS_FILE_EXTENSION.length()
						- 1)),
				result, template);
	}

	public static void main(String[] args) throws IOException
	{
		Preconditions.checkArgument(args.length >= 3,
				"there must be 3 args at least : " + Arrays.toString(args));
		MyDI myDI = new MyDI(new AppConfig());
		ApplicationMain app = myDI.getInstanceViaDI(ApplicationMain.class);
		Set<String> flags = new HashSet<String>();
		for (int i = 3; i < args.length; i++)
		{
			flags.add(args[i]);
		}
		app.execute(args[0], args[1], args[2], flags.contains("i"));
		if (flags.contains("m"))
		{
			myDI.getInstanceViaDI(MacroRunner.class)
					.runMacro(new File(args[1]));
		}
		if (flags.contains("s"))
		{
			Runtime.getRuntime().exec("shutdown /s");
		}
	}
}
