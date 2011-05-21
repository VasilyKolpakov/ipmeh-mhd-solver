package ru.vasily.solverhelper;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import ru.vasily.dataobjs.Parameters;
import ru.vasily.solverhelper.misc.ISerializer;

public class ParamsLoader implements IParamsLoader {//TODO remove
	private final ISerializer serializer;

	public ParamsLoader(ISerializer serializer) {
		this.serializer = serializer;
	}

	@Override
	public Parameters getParams(File file) throws IOException {
		return serializer.readObject(new FileReader(file), Parameters.class);
	}

	public static void main(String[] args) throws Exception {
		ParamsLoader loader = new ParamsLoader(new ISerializer() {

			@Override
			public void writeObjects(Iterable<Object> obj, Appendable target) {
			}

			@Override
			public void writeObject(Object obj, Appendable target) {
			}

			@Override
			public <T> T readObject(Reader source, Class<T> clazz)
					throws IOException {
				return null;
			}
		});
		double ro = loader.getParams(new File("param.txt")).left_initial_values.rho;
		System.out.println(ro);
	}
}
