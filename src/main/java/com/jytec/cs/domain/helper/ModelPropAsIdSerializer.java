package com.jytec.cs.domain.helper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;

@SuppressWarnings("serial")
public class ModelPropAsIdSerializer extends StdSerializer<Object> {
	public ModelPropAsIdSerializer() {
		this(null);
	}

	public ModelPropAsIdSerializer(Class<Object> t) {
		super(t);
	}

	@Override
	public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value == null) {
			gen.writeNull();
		} else if (value instanceof Course) {
			//TODO: add @JsonPropery Or define custom annotation or Module.
			gen.writeString(Course.class.cast(value).getCode());
		} else if (value instanceof Major) {
			gen.writeNumber(((Major) value).getId());
		} else if (value instanceof Dept) {
			gen.writeNumber(((Dept) value).getId());
		} else {
			// else TODO:
			gen.writeNull();
		}

	}
}
