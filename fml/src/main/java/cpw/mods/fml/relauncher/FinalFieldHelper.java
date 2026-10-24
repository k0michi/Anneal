package cpw.mods.fml.relauncher;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import sun.misc.Unsafe;

/**
 * Writes fields that reflection refuses to: static final fields on every Java version, and on Java 9 and later the
 * fields that used to be unlocked through sun.reflect.ReflectionFactory and Field.modifiers, which are gone or hidden.
 * Goes through sun.misc.Unsafe, which every Java version has; needs no setAccessible, and so no --add-opens.
 */
public class FinalFieldHelper
{
    private static final Unsafe UNSAFE;

    static
    {
        try
        {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            UNSAFE = (Unsafe)theUnsafe.get(null);
        }
        catch (Exception e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Sets a field as {@link Field#set} would, final or not; target is ignored for a static field. Primitive fields
     * take their boxed value, numeric ones widened as by Field.set.
     */
    public static void setField(Field field, Object target, Object value)
    {
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        Object base = isStatic ? UNSAFE.staticFieldBase(field) : target;
        long offset = isStatic ? UNSAFE.staticFieldOffset(field) : UNSAFE.objectFieldOffset(field);
        if (base == null)
        {
            throw new NullPointerException("No target for the instance field " + field);
        }
        Class<?> type = field.getType();
        if (!type.isPrimitive())
        {
            if (value != null && !type.isInstance(value))
            {
                throw new IllegalArgumentException("Can not set " + type.getName() + " field " + field + " to " + value.getClass().getName());
            }
            UNSAFE.putObjectVolatile(base, offset, value);
        }
        else if (type == boolean.class)
        {
            UNSAFE.putBooleanVolatile(base, offset, (Boolean)value);
        }
        else if (type == char.class)
        {
            UNSAFE.putCharVolatile(base, offset, (Character)value);
        }
        else
        {
            Number number = value instanceof Character ? Integer.valueOf((Character)value) : (Number)value;
            if (type == byte.class)
            {
                UNSAFE.putByteVolatile(base, offset, number.byteValue());
            }
            else if (type == short.class)
            {
                UNSAFE.putShortVolatile(base, offset, number.shortValue());
            }
            else if (type == int.class)
            {
                UNSAFE.putIntVolatile(base, offset, number.intValue());
            }
            else if (type == long.class)
            {
                UNSAFE.putLongVolatile(base, offset, number.longValue());
            }
            else if (type == float.class)
            {
                UNSAFE.putFloatVolatile(base, offset, number.floatValue());
            }
            else
            {
                UNSAFE.putDoubleVolatile(base, offset, number.doubleValue());
            }
        }
    }
}
