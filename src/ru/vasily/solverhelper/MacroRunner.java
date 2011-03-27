package ru.vasily.solverhelper;

import java.io.File;
import java.io.IOException;

import ru.vasily.mydi.MyDI;
import ru.vasily.solverhelper.misc.DirWalker;
import ru.vasily.solverhelper.misc.FileTypeFilter;
import ru.vasily.solverhelper.tecplot.ITecplotManager;

import com.google.common.base.Function;
import com.google.common.base.Throwables;

public class MacroRunner {
	private final ITecplotManager tecplotManager;

	public MacroRunner(ITecplotManager tecplotManager) {
		this.tecplotManager = tecplotManager;
	}

	public void runMacro(File output) {
		new DirWalker(new Function<File, Void>() {

			@Override
			public Void apply(File input) {
				try {
					tecplotManager.runMacro(input);
					input.deleteOnExit();
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}
				return null;
			}
		}, FileTypeFilter.forFileType("mcr")).walkDirs(output);
	}

	public static void main(String[] args) {
		MacroRunner app = new MyDI(new AppConfig())
				.getInstanceViaDI(MacroRunner.class);
		app.runMacro(new File(args[0]));
	}
}
