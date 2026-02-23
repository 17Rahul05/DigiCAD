package sim.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A very simple, lightweight JSON parser/builder for this project.
 * Handles: Objects {}, Arrays [], Strings "", Numbers, Booleans.
 */
public class SimpleJson {
    private SimpleJson() {
        /* This utility class should not be instantiated */
    }


    // --- BUILDER ---

    public static String serialize(Map<String, Object> object) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<String, Object> entry : object.entrySet()) {
            sb.append("  \"").append(entry.getKey()).append("\": ");
            sb.append(valueToString(entry.getValue()));
            if (i < object.size() - 1) sb.append(",");
            sb.append("\n");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String valueToString(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof List) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            List<?> list = (List<?>) value;
            for (int i = 0; i < list.size(); i++) {
                Object item = list.get(i);
                if (item instanceof Map) {
                    // Minify nested maps for cleaner file
                    sb.append(mapToStringMinified((Map<?, ?>) item)); 
                } else {
                    sb.append(valueToString(item));
                }
                if (i < list.size() - 1) sb.append(", ");
            }
            sb.append("]");
            return sb.toString();
        } else if (value instanceof Map) {
            return mapToStringMinified((Map<?, ?>) value);
        } else {
            return String.valueOf(value);
        }
    }

    private static String mapToStringMinified(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append(valueToString(entry.getValue()));
            if (i < map.size() - 1) sb.append(",");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }

    // --- PARSER ---

    public static Map<String, Object> parse(String json) {
        Map<String, Object> result = new HashMap<>();
        json = json.trim();
        
        // Remove outer braces
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1);
        }

        // Split by top-level commas (careful with nested structures)
        List<String> parts = splitTopLevel(json);

        for (String part : parts) {
            String[] kv = splitKeyValue(part);
            if (kv.length < 2) continue; // Skip malformed entries
            String key = kv[0].trim().replace("\"", "");
            Object value = parseValue(kv[1].trim());
            result.put(key, value);
        }
        return result;
    }

    private static Object parseValue(String value) {
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        } else if (value.startsWith("[") && value.endsWith("]")) {
            List<Object> list = new ArrayList<>();
            String inner = value.substring(1, value.length() - 1);
            if (inner.trim().isEmpty()) return list;

            List<String> items = splitTopLevel(inner);
            for (String item : items) {
                list.add(parseValue(item.trim()));
            }
            return list;
        } else if (value.startsWith("{") && value.endsWith("}")) {
            return parse(value);
        } else if (value.equals("true")) {
            return true;
        } else if (value.equals("false")) {
            return false;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException _) {
                return value;
            }
        }
    }

    // Helper to split by comma ONLY if not inside [] or {}
    private static List<String> splitTopLevel(String input) {
        List<String> result = new ArrayList<>();
        int braceDepth = 0;
        int bracketDepth = 0;
        StringBuilder current = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (c == '{') braceDepth++;
            if (c == '}') braceDepth--;
            if (c == '[') bracketDepth++;
            if (c == ']') bracketDepth--;

            if (c == ',' && braceDepth == 0 && bracketDepth == 0) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (!current.isEmpty()) result.add(current.toString().trim());
        return result;
    }

    private static String[] splitKeyValue(String pair) {
        int idx = pair.indexOf(":");
        if (idx == -1) idx = pair.indexOf("="); // Fallback for the bugged format
        if (idx == -1) return new String[0];
        return new String[]{pair.substring(0, idx), pair.substring(idx + 1)};
    }
}
