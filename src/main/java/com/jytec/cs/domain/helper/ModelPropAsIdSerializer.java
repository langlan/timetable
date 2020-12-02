package com.jytec.cs.domain.helper;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.jytec.cs.domain.Course;
import com.jytec.cs.domain.Dept;
import com.jytec.cs.domain.Major;
import com.jytec.cs.domain.Site;
import com.jytec.cs.domain.Teacher;

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
			// [Class, Teacher, Site, Major, Dept]: have tried to use a super interface, but will cause
			// Error: Could not write JSON: could not initialize proxy [com.jytec.cs.domain.Site#33]
			// It seems like Hibernate proxy-strategy will try lazy-loading even invoke the method getId()
		} else if (value instanceof com.jytec.cs.domain.Class) {
			gen.writeNumber(((com.jytec.cs.domain.Class) value).getId());
		} else if (value instanceof Teacher) {
			gen.writeNumber(((Teacher) value).getId());
		} else if (value instanceof Site) {
			gen.writeNumber(((Site) value).getId());
		} else if (value instanceof Course) {
			gen.writeString(Course.class.cast(value).getCode());
		} else if (value instanceof Major) {
			gen.writeNumber(((Major) value).getId());
		} else if (value instanceof Dept) {
			gen.writeNumber(((Dept) value).getId());
		} else {
			throw new IllegalStateException("For Dev: Handle model-prop-as-id:" + value.getClass());
		}

	}
}
