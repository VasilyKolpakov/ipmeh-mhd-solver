package ru.vasily.solverhelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import com.google.common.base.Preconditions;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.Parameters;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.misc.FileTypeFilter;

public class ApplicationMain {

	private final IParamsLoader paramsLoader;
	private final ISolver solver;
	private final IResultWriter dataWriter;

	public ApplicationMain(IParamsLoader paramsLoader, ISolver solver,
			IResultWriter dataWriter) {
		this.paramsLoader = paramsLoader;
		this.solver = solver;
		this.dataWriter = dataWriter;
	}

	public void execute(String paramsPath, String outputPathStr, String templateDir) {
		File inputPath = new File(paramsPath);
		final File output = new File(outputPathStr);
		File template = new File(templateDir);
		File[] inputPaths = inputPath
				.listFiles((FilenameFilter) new FileTypeFilter("js"));
		for (File path : inputPaths) {
			try {
				Parameters param = paramsLoader.getParams(path);
				CalculationResult result = solver.solve(param);
				String outputDirName = path.getName();
				outputDirName = outputDirName.substring(0,
						outputDirName.length() - 3);
				dataWriter.createResultDir(new File(output, outputDirName),
						result, template);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) throws IOException {
		Preconditions.checkArgument(args.length > 3, "there must be 3 args at least");
		ApplicationMain app = new MyDI(new AppConfig())
				.getInstanceViaDI(ApplicationMain.class);
		app.execute(args[0], args[1], args[2]);
		if(args.length>=4&&args[3]!=null&&args[3].equals("s")){
			Runtime.getRuntime()
			.exec("shutdown /s");

		}
	}
}
