package ru.vasily.dataobjs;

import java.io.IOException;
import java.io.Reader;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;

import static com.google.common.base.Preconditions.*;

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

	private final class JsonNodeDataObj implements DataObject {

		private final JsonNode node;

		public JsonNodeDataObj(JsonNode node) {
			this.node = node;
		}

		@Override
		public double getDouble(String valueName) {
			JsonNode value = getJsonVal(valueName);
			checkArgument(value.isNumber(), "the value \'%s\' is not a number, val = %s",
					valueName,
					value);
			return value.getDoubleValue();
		}

		@Override
		public int getInt(String valueName) {
			JsonNode jsonVal = getJsonVal(valueName);
			checkArgument(jsonVal.isInt(), "the value \'%s\' is not an int, val = %s", valueName,
					jsonVal);
			return jsonVal.getIntValue();
		}

		@Override
		public String getString(String valueName) {
			JsonNode value = node.get(valueName);
			checkNotNull(value, "there is no value with name \'%s\'", valueName);
			return value.getTextValue();
		}

		@Override
		public DataObject getObj(String valueName) {
			return new JsonNodeDataObj(node.get(valueName));
		}

		private JsonNode getJsonVal(String valueName) {
			JsonNode value = node.get(valueName);
			checkNotNull(value, "there is no value with name \'%s\'", valueName);
			return value;
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
