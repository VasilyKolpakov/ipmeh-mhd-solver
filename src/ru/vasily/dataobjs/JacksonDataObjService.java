package ru.vasily.dataobjs;

import java.io.IOException;
import java.io.Reader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;

public class JacksonDataObjService implements DataObjectService {
	private final ObjectReader reader;

	public JacksonDataObjService() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.ALLOW_COMMENTS, true);
		this.reader = mapper.reader();
	}

	@Override
	public DataObject readObject(Reader source) throws IOException {
		return new JsonNodeDataObj(reader.readTree(source));
	}

	private class JsonNodeDataObj implements DataObject {

		private final JsonNode node;

		public JsonNodeDataObj(JsonNode node) {
			this.node = node;
		}

		@Override
		public double getDouble(String valueName) {
			return node.get(valueName).getDoubleValue();
		}

		@Override
		public int getInt(String valueName) {
			return node.get(valueName).getIntValue();
		}

		@Override
		public DataObject getObj(String valueName) {
			return new JsonNodeDataObj(node.get(valueName));
		}

		@Override
		public boolean equals(Object obj) {
			return this == obj ||
					(obj instanceof JsonNodeDataObj &&
					((JsonNodeDataObj) obj).node.equals(node));
		}

		@Override
		public int hashCode() {
			return node.hashCode();
		}

	}
}
