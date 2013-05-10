package cydep;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Util {

    public static final String GET = "get";
    public static final String IS = "is";
    public static final String HAS = "has";
    public static final String[] SPECIALS = {"hashcode", "size", "length"};

    public static void dump(String label, Object o, Formatter f) {
        dump(label, o, f, false);
    }

    public static void dump(String label, Object o, Formatter f, boolean shouldExtractPropertyNames) {
        Class<? extends Object> c = o.getClass();
        SortedMap<String, Method> methods = new TreeMap<String, Method>();
        Map<String, String> returnTypes = new HashMap<String, String>();
        int widthPropertyNames = 0;
        int widthReturnTypes = 0;
        for (Method method : c.getMethods()) {
            String name = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Class<?> returnType = method.getReturnType();
            int modifiers = method.getModifiers();
            boolean isPublic = Modifier.isPublic(modifiers);
            boolean isNotStatic = !Modifier.isStatic(modifiers);
            boolean hasNoParameters = parameterTypes.length == 0;
            boolean hasReturnValue = !Void.TYPE.equals(returnType);
            if (isPublic && isNotStatic && hasNoParameters && hasReturnValue) {
                boolean isBoolean = boolean.class.equals(returnType);
                String propertyName = shouldExtractPropertyNames ? extractPropertyName(name, isBoolean) : name;
                if (propertyName != null) {
                    methods.put(propertyName, method);
                    String returnTypeSimpleName = returnType.getSimpleName();
                    returnTypes.put(propertyName, returnTypeSimpleName);
                    if (propertyName.length() > widthPropertyNames) {
                        widthPropertyNames = propertyName.length();
                    }
                    if (returnTypeSimpleName.length() > widthReturnTypes) {
                        widthReturnTypes = returnTypeSimpleName.length();
                    }
                }
            }
        }
        f.format("+ %s\n", label);
        String format = String.format("| %%-%ds | %%%ds | %%s\n", widthPropertyNames, widthReturnTypes);
        for (Map.Entry<String, Method> entry : methods.entrySet()) {
            String propertyName = entry.getKey();
            Method method = entry.getValue();
            try {
                Object result = method.invoke(o, (Object[]) null);
                if (result != null) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType.isArray()) {
                        Method toString = Arrays.class.getMethod("toString", returnType);
                        result = String.format("(%d) %s", Array.getLength(result), toString.invoke(null, result));
                    } else {
                        try {
                            Method size = returnType.getMethod("size", (Class<?>[]) null);
                            boolean isInt = int.class.equals(size.getReturnType());
                            if (isInt) {
                                result = String.format("(%d) %s", size.invoke(result, (Object[]) null), result);
                            }
                        } catch (NoSuchMethodException e) {
                            // ignore
                        }
                    }
                }
                f.format(format, propertyName, method.getReturnType().getSimpleName(), result);
            } catch (Exception ex) {
                Logger.getLogger(Util.class.getName()).log(Level.SEVERE, propertyName, ex);
            }
        }
        f.format("+ ----------\n");
    }

    private static String extractPropertyName(String methodName, boolean isBoolean) {
        for (String special : SPECIALS) {
            if (special.equals(methodName)) {
                return special;
            }
        }
        String propertyName;
        if (isBoolean) {
            if ((propertyName = extractPropertyName(methodName, IS)) != null) {
                return propertyName;
            }
            if ((propertyName = extractPropertyName(methodName, HAS)) != null) {
                return propertyName;
            }
        }
        if ((propertyName = extractPropertyName(methodName, GET)) != null) {
            return propertyName;
        }
        return null;
    }

    private static String extractPropertyName(String methodName, String prefix) {
        String propertyName = null;
        if (methodName.startsWith(prefix) && methodName.length() > prefix.length()) {
            char firstChar = methodName.charAt(prefix.length());
            if (Character.isUpperCase(firstChar)) {
                propertyName = Character.toLowerCase(firstChar) + methodName.substring(prefix.length() + 1);
            }
        }
        return propertyName;
    }
}
