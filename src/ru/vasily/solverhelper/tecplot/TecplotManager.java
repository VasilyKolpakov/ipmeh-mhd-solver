package ru.vasily.solverhelper.tecplot;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class TecplotManager implements ITecplotManager {


	@Override
	public void runMacro(final File macro) throws IOException {
		Runnable task = new CreateTecplotImageTask(macro);
		task.run();
	}

	@Override
	public void runMacro(Iterable<File> macro) throws IOException {
		for (File mcr : macro) {
			runMacro(mcr);
		}
	}

	private static final class CreateTecplotImageTask implements Runnable {
		private final File macro;

		private CreateTecplotImageTask(File macro) {
			this.macro = macro;
		}

		@Override
		public void run() {
			try {
				Runtime.getRuntime()
						.exec("tecplot -p " + macro.getName(), (String[]) null,
								new File(macro.getParent())).waitFor();
				Thread.sleep(1000);
				macro.deleteOnExit();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
