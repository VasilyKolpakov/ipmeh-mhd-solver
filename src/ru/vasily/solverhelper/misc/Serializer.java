package ru.vasily.solverhelper.misc;

import com.google.common.base.Throwables;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

import java.io.IOException;
import java.io.Reader;

public class Serializer implements ISerializer {
	private final ObjectReader reader;
	private final ObjectWriter writer;

	public Serializer() {
		super();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.ALLOW_COMMENTS, true);
		this.reader = mapper.reader();
		this.writer = mapper.writer(new DefaultPrettyPrinter());
	}

	@Override
	public <T> T readObject(Reader source, Class<T> clazz) throws IOException {
		return (T)reader.withType(clazz).readValue(source);
	}

	@Override
	public void writeObject(Object obj, Appendable target) {
		try {
			String out = writer.writeValueAsString(obj);
			target.append(out);
		} catch (JsonGenerationException e) {
			throw Throwables.propagate(e);
		} catch (JsonMappingException e) {
			throw Throwables.propagate(e);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public void writeObjects(Iterable<Object> objs, Appendable target) {
		try {
			for (Object obj : objs) {
				String out = writer.writeValueAsString(obj);
				target.append(out);
			}
		} catch (JsonGenerationException e) {
			throw Throwables.propagate(e);
		} catch (JsonMappingException e) {
			throw Throwables.propagate(e);
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
	}

}
