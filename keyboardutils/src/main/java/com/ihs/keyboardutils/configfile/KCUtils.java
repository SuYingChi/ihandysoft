package com.ihs.keyboardutils.configfile;

class KCUtils {
    static double checkDouble(double d) throws KCException {
        if (Double.isInfinite(d) || Double.isNaN(d)) {
            throw new KCException("Forbidden numeric value: " + d);
        }
        return d;
    }

    static boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            String stringValue = (String) value;
            if ("true".equalsIgnoreCase(stringValue)) {
                return true;
            } else if ("false".equalsIgnoreCase(stringValue)) {
                return false;
            }
        }
        return false;
    }

    static double toDouble(Object value) {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.valueOf((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    static int toInt(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return (int) Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    static long toLong(Object value) {
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return (long) Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    static String toString(Object value) {
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return String.valueOf(value);
        }
        return null;
    }

    public static KCException typeMismatch(Object indexOrName, Object actual,
                                           String requiredType) throws KCException {
        if (actual == null) {
            throw new KCException("Value at " + indexOrName + " is null.");
        } else {
            throw new KCException("Value " + actual + " at " + indexOrName
                    + " of type " + actual.getClass().getName()
                    + " cannot be converted to " + requiredType);
        }
    }

    public static UnsupportedOperationException unsupportedOperation() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("KCList/KCMap is immutable.");
    }

    public static KCException typeMismatch(Object actual, String requiredType)
            throws KCException {
        if (actual == null) {
            throw new KCException("Value is null.");
        } else {
            throw new KCException("Value " + actual
                    + " of type " + actual.getClass().getName()
                    + " cannot be converted to " + requiredType);
        }
    }
}
