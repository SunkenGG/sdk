package gg.sunken.sdk.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.*;
import java.util.*;

public class ReflectionUtil {

    private static final Table<Class<?>, String, VarHandle> varHandleCache = HashBasedTable.create();
    private static final Objenesis objenesis = new ObjenesisStd();

    private static VarHandle findVarHandle(Class<?> clazz, String fieldName) throws NoSuchFieldException, IllegalAccessException {
        if (varHandleCache.contains(clazz, fieldName)) {
            return varHandleCache.get(clazz, fieldName);
        }

        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);

        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
        VarHandle handle = lookup.unreflectVarHandle(field);
        varHandleCache.put(clazz, fieldName, handle);
        return handle;
    }

    public static VarHandle findDeclaredField(Class<?> clazz, Class<?> type, String name) {
        try {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.getType().equals(type) && field.getName().equals(name)) {
                    MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                    VarHandle handle = lookup.unreflectVarHandle(field);
                    varHandleCache.put(clazz, name, handle);
                    return handle;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get VarHandle for " + clazz.getName() + "#" + name, e);
        }

        throw new IllegalStateException("Can't find field " + clazz.getName() + "#" + name);
    }

    public static Map<String, VarHandle> getAllVarHandles(Class<?> clazz) {
        Map<String, VarHandle> handles = new HashMap<>();

        try {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());
                VarHandle handle = lookup.unreflectVarHandle(field);
                handles.put(field.getName(), handle);
                varHandleCache.put(clazz, field.getName(), handle);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to collect VarHandles for " + clazz.getName(), e);
        }

        return handles;
    }

    public static void setPrivateField(Object object, String field, Object newValue) {
        try {
            VarHandle handle = findVarHandle(object.getClass(), field);
            handle.set(object, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setPrivateField(Class<?> clazz, String field, Object newValue) {
        try {
            VarHandle handle = findVarHandle(clazz, field);
            handle.set(null, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getPrivateField(Object object, String field) {
        try {
            VarHandle handle = findVarHandle(object.getClass(), field);
            return handle.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getPrivateField(Class<?> clazz, String field) {
        try {
            VarHandle handle = findVarHandle(clazz, field);
            return handle.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method findDeclaredMethod(Class<?> clazz, Class<?>[] paramTypes, Class<?> returnType, String name) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.getReturnType().equals(returnType)) continue;
            if (!Arrays.equals(paramTypes, method.getParameterTypes())) continue;

            method.setAccessible(true);
            return method;
        }

        throw new IllegalStateException("Can't find method " + clazz.getName() + "." + name);
    }

    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
        Method[] declaredMethods = clazz.getMethods();
        List<Method> methods = new ArrayList<>(declaredMethods.length);
        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(annotationClass)) {
                methods.add(method);
            }
        }
        return methods;
    }

    public static <V> V createInstance(Class<V> valueClass) {
        return createInstance(valueClass, new Class<?>[0], new Object[0]);
    }

    public static <V> V createInstance(Class<V> valueClass, Class<?>[] paramTypes, Object[] params) {
        try {
            return valueClass.getConstructor(paramTypes).newInstance(params);
        } catch (InstantiationException | IllegalAccessException |
                 NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static <V> V createInstance(Class<V> valueClass, Object... params) {
        Class<?>[] paramTypes = Arrays.stream(params).map(Object::getClass).toArray(Class<?>[]::new);
        return createInstance(valueClass, paramTypes, params);
    }

    public static <V> V createInstanceSilently(Class<V> valueClass) {
        return objenesis.newInstance(valueClass);
    }

    @Nullable
    public static <T> Class<T> getGenericType(Class<T> clazz, int index) {
        Type type = clazz.getGenericSuperclass();
        if (type instanceof ParameterizedType parameterizedType) {
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            try {
                return (Class<T>) actualTypeArguments[index];
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    public static <T> Class<T> getGenericType(Class<T> clazz) {
        return getGenericType(clazz, 0);
    }

    public static <V> Optional<Object> getFieldValue(V v, String fieldName) {
        try {
            VarHandle handle = findVarHandle(v.getClass(), fieldName);
            return Optional.ofNullable(handle.get(v));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
    }

    public static <V, T> Optional<T> getFieldValue(V v, String fieldName, Class<T> returnType) {
        return getFieldValue(v, fieldName).flatMap(o -> {
            try {
                return Optional.of(returnType.cast(o));
            } catch (ClassCastException e) {
                return Optional.empty();
            }
        });
    }
}
