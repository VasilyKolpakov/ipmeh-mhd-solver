package ru.vasily.solverhelper.misc;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public final class FileTypeFilter implements FileFilter, FilenameFilter {
	private final Pattern namePattern;

	public static FileTypeFilter forFileType(String type){
		return new FileTypeFilter(type);
	}
	public FileTypeFilter(String fileType) {
		namePattern = Pattern.compile("\\Q" + '.' + fileType + "\\E$",
				Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean accept(File dir, String name) {
		return namePattern.matcher(name).find();
	}

	@Override
	public boolean accept(File pathname) {
		return namePattern.matcher(pathname.getName()).find();
	}
}
