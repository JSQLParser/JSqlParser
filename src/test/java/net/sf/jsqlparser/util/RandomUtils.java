package net.sf.jsqlparser.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang3.RandomStringUtils;

/**
 * An utility to get a random value for any type given - see
 * {@link #getRandomValueForType(Class)}
 *
 * @author gitmotte
 * @see #pushObjects(List)
 */
public class RandomUtils {

    private static final Logger log = Logger.getLogger(RandomUtils.class.getName());
    private static final Random random = new Random();

    private static final ThreadLocal<Map<Class<?>, Object>> objects = new ThreadLocal<>();

    /**
     * register models (for use within method {@link #getRandomValueForType(Class)}
     */
    public static void pushObjects(List<Object> obj) {
        Map<Class<?>, Object> m = new HashMap<>();
        objects.set(m);
        obj.stream().forEach(o -> {
            m.put(o.getClass(), o);
            for (Class<?> iface : o.getClass().getInterfaces()) {
                // register object with its implemented interfaces
                // if we need an object for interface requested, an instance is available
                m.put(iface, o);
            }
        });

    }

    /**
     * @param <T>
     * @param type
     * @return a random non-<code>null</code> value for given type or
     *         <code>null</code> if not supported.
     */
    public static <T> T getRandomValueForType(Class<T> type) {
        Object value = null;
        if (Integer.class.equals(type) || int.class.equals(type)) {
            value = RandomUtils.random.nextInt();
        } else if (Long.class.equals(type) || long.class.equals(type)) {
            value = RandomUtils.random.nextLong();
        } else if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            value = RandomUtils.random.nextBoolean();
        } else if (Float.class.equals(type) || float.class.equals(type)) {
            value = RandomUtils.random.nextFloat();
        } else if (Double.class.equals(type) || double.class.equals(type)) {
            value = RandomUtils.random.nextDouble();
        } else if (Byte.class.equals(type) || byte.class.equals(type)) {
            byte[] b = new byte[1];
            RandomUtils.random.nextBytes(b);
            value = b[0];
        } else if (Short.class.equals(type) || short.class.equals(type)) {
            value = (short) RandomUtils.random.nextInt(15);
        } else if (char.class.equals(type)) {
            value = RandomStringUtils.random(1).toCharArray()[0];
        } else {
            int size = RandomUtils.random.nextInt(10);
            if (String.class.equals(type)) {
                value = RandomStringUtils.random(size);
            } else if (Collection.class.equals(type) || List.class.equals(type)) {
                List<Object> c = new ArrayList<>();
                value = c;
            } else if (Set.class.equals(type)) {
                Set<Object> c = new HashSet<>();
                value = c;
            } else if (type.isArray()) {
                Object [] a = (Object[]) Array.newInstance(type.getComponentType(), size);
                for (int i = 0; i < size; i++) {
                    a[i] = getRandomValueForType(type.getComponentType());
                }
                value = a;
            } else if (Map.class.equals(type)) {
                Map<Object, Object> c = new HashMap<>();
                value = c;
            } else if (LocalDateTime.class.equals(type)) {
                value = LocalDateTime.now();
            } else {
                // try to get an object from test-objects
                value = objects.get().get(type);
                if (value == null) {
                    try {
                        value = type.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                        // cannot get default instance with empty constructor
                        log.log(Level.WARNING, "cannot get default instance with reflection for type " + type);
                    }
                }

            }
        }
        return type.cast(value);
    }

}
