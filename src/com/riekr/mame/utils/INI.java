package com.riekr.mame.utils;

import com.riekr.mame.tools.MameException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class INI {

	public static final Pattern PATTERN = Pattern.compile("\\h*^([^#][\\w-]+)(?:\\h+|(?:\\h*=\\h*))(.+)\\h*$");

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Config {
		String[] value() default "";
	}

	private INI() {
	}

	public static void load(Path file, Object pojo) {
		if (!Files.exists(file))
			return;
		Map<String, Field> fieldMap = new HashMap<>();
		Class<?> cl = pojo.getClass();
		for (Field field : cl.getDeclaredFields()) {
			Config config = field.getAnnotation(Config.class);
			if (config == null)
				continue;
			fieldMap.put(field.getName(), field);
			for (String key : config.value()) {
				if (fieldMap.put(key.toLowerCase(), field) != null)
					throw new MameException("Duplicate INI key '" + key + "' in " + file);
			}
		}
		Matcher m = PATTERN.matcher("");
		try (Stream<String> lines = Files.lines(file)) {
			lines.forEach(line -> {
				m.reset(line);
				if (m.matches()) {
					String key = m.group(1).toLowerCase();
					Field field = fieldMap.get(key);
					if (field != null) {
						String val = m.group(2);
						try {
							Method converter = INI.class.getDeclaredMethod(field.getGenericType().getTypeName().replaceAll("[.<>]", "_"), String.class);
							Object obj = converter.invoke(null, val);
							field.set(pojo, obj);
						} catch (IllegalAccessException e) {
							throw new MameException("Unable to set field " + field.getName() + " in " + cl.getName(), e);
						} catch (NoSuchMethodException e) {
							throw new MameException("Unable to get converter for " + field.getName() + " in " + cl.getName(), e);
						} catch (InvocationTargetException e) {
							throw new MameException("Unable to convert " + field.getName() + " in " + cl.getName(), e.getTargetException());
						}
					}
				}
			});
		} catch (Exception e) {
			throw new MameException("Unable to read " + file, e);
		}
	}

	@SuppressWarnings("unused")
	private static Set<Path> java_util_Set_java_nio_file_Path_(String s) {
		if (s == null || s.isBlank())
			return null;
		Set<Path> res = new HashSet<>();
		for (String p : s.split("[;:]"))
			res.add(java_nio_file_Path(p));
		return res;
	}

	private static Path java_nio_file_Path(String s) {
		if (s == null || s.isBlank())
			return null;
		return Path.of(s);
	}

}
