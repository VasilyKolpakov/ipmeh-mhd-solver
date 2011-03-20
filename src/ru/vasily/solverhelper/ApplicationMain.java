package ru.vasily.solverhelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import ru.vasily.dataobjs.CalculationResult;
import ru.vasily.dataobjs.Parameters;
import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.misc.DirWalker;
import ru.vasily.solverhelper.misc.FileTypeFilter;
import ru.vasily.solverhelper.tecplot.ITecplotManager;

public class ApplicationMain {

	private final class ShowCreateImagesDialog implements Runnable {
		private final File output;

		private ShowCreateImagesDialog(File output) {
			this.output = output;
		}

		public void run() {
			if (JOptionPane.showConfirmDialog(null, "create images?") == 0) {
				createImages(output);
			}
		}
	}

	private final IParamsLoader paramsLoader;
	private final ISolver solver;
	private final IResultWriter dataWriter;
	private final ITecplotManager tecplotManager;

	public ApplicationMain(IParamsLoader paramsLoader, ISolver solver,
			IResultWriter dataWriter, ITecplotManager tecplotManager) {
		this.paramsLoader = paramsLoader;
		this.solver = solver;
		this.dataWriter = dataWriter;
		this.tecplotManager = tecplotManager;
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
//		SwingUtilities.invokeLater(new ShowCreateImagesDialog(output));
	}
	private void createImages(File output) {
		new DirWalker(new Function<File, Void>() {

			@Override
			public Void apply(File input) {
				try {
					tecplotManager.runMacro(input);
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}
				return null;
			}
		}, FileTypeFilter.forFileType("mcr")).walkDirs(output);
	}

	public static void main(String[] args) {
		Preconditions.checkArgument(args.length == 3, "there must be 3 args");
		ApplicationMain app = new MyDI(new AppConfig())
				.getInstanceViaDI(ApplicationMain.class);
		app.execute(args[0], args[1], args[2]);
	}
}
