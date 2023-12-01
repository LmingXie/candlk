package com.candlk.common.redis;

import java.lang.reflect.Array;
import java.util.Arrays;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.ContextAutoTypeBeforeHandler;
import com.alibaba.fastjson2.writer.ObjectWriter;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.Assert;
import me.codeplayer.util.X;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@Getter
@Setter
public class FastJson2RedisSerializer implements RedisSerializer<Object> {

	/** 日期时间类型默认输出为 毫秒级时间戳 */
	public static final String dateFormat = "millis";

	private JSONReader.Context readerContext;
	private JSONWriter.Context writerContext;
	private boolean jsonb;

	public FastJson2RedisSerializer(JSONReader.Context readerContext, JSONWriter.Context writerContext) {
		setReaderContext(readerContext);
		setWriterContext(writerContext);
		applyRequiredConfig(readerContext, writerContext);
	}

	@SuppressWarnings("deprecation")
	public static void applyRequiredConfig(JSONReader.Context readerContext, JSONWriter.Context writerContext) {
		readerContext.setDateFormat(dateFormat);
		readerContext.config(
				JSONReader.Feature.FieldBased,
				JSONReader.Feature.SupportAutoType
		);
		writerContext.setDateFormat(dateFormat);
		writerContext.config(
				JSONWriter.Feature.FieldBased,
				JSONWriter.Feature.WriteByteArrayAsBase64,
				JSONWriter.Feature.WriteEnumsUsingName,
				JSONWriter.Feature.WriteClassName
		);
	}

	public void setReaderContext(JSONReader.Context readerContext) {
		this.readerContext = Assert.notNull(readerContext);
	}

	public void setWriterContext(JSONWriter.Context writerContext) {
		this.writerContext = Assert.notNull(writerContext);
	}

	public FastJson2RedisSerializer() {
		this(new JSONReader.Context(), new JSONWriter.Context());
	}

	public FastJson2RedisSerializer(String[] acceptNames, boolean jsonb) {
		this();
		readerContext.config(new ContextAutoTypeBeforeHandler(acceptNames));
		this.jsonb = jsonb;
	}

	public FastJson2RedisSerializer(String[] acceptNames) {
		this(acceptNames, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] addIfAbsent(T[] old, Class<? extends T[]> newType, T required) {
		final int size = X.size(old);
		if (size == 0 || !ArrayUtils.contains(old, required)) {
			final T[] newArray = old == null ? (T[]) Array.newInstance(newType.getComponentType(), 1)
					: Arrays.copyOf(old, size + 1, newType);
			newArray[size] = required;
			return newArray;
		}
		return old;
	}

	@Override
	public byte[] serialize(Object object) throws SerializationException {
		if (object == null) {
			return new byte[0];
		}
		try {
			if (jsonb) {
				return JSONB.toBytes(object, writerContext);
			} else {
				return toJSONBytes(object, writerContext);
			}
		} catch (Exception ex) {
			throw new SerializationException("Could not serialize: " + ex.getMessage(), ex);
		}
	}

	static byte[] toJSONBytes(Object object, JSONWriter.Context context) {
		try (JSONWriter writer = JSONWriter.ofUTF8(context)) {
			if (object == null) {
				writer.writeNull();
			} else {
				writer.setRootObject(object);
				Class<?> valueClass = object.getClass();
				ObjectWriter<?> objectWriter = context.getObjectWriter(valueClass, valueClass);
				objectWriter.write(writer, object, null, null, 0);
			}
			return writer.getBytes();
		}
	}

	@Override
	public Object deserialize(byte[] bytes) throws SerializationException {
		if (bytes == null || bytes.length == 0) {
			return null;
		}
		try {
			if (jsonb) {
				return JSONB.parseObject(bytes, Object.class, readerContext);
			} else {
				return JSON.parseObject(bytes, Object.class, readerContext);
			}
		} catch (Exception ex) {
			throw new SerializationException("Could not deserialize: " + ex.getMessage(), ex);
		}
	}

}
