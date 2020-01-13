package org.sunbird.config;

import java.lang.reflect.Field;
import java.util.HashMap;

/**
 * Created on 27/4/17.
 *
 * @author swayangjit
 */
public class ReflectionUtil {

    public static Object getStaticFieldValue(Class<?> clazz, String fieldName) {
        try {
            return clazz.getField(fieldName).get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T getInstance(Class<T> classInstance) {
        T instance = null;
        if (classInstance != null) {
            try {
                instance = classInstance.newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return instance;
    }

    public static HashMap getBuildConfigValues(Class<?> clazz) {
        Field[] fields = clazz.getFields();
        HashMap hashMap = new HashMap();
        for (int i=0;i<fields.length;i++) {
            try {
                Object object = clazz.getField(fields[i].getName()).get(null);
                hashMap.put(fields[i].getName(),object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return hashMap;
    }
}
