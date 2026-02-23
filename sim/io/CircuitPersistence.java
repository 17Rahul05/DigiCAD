package sim.io;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sim.CircuitComponent;
import sim.CircuitManager;
import sim.model.SubCircuit;
import sim.model.Tooltype;
import sim.model.Wire;
import sim.util.ComponentFactory;
import sim.util.PinID;
import sim.util.SimpleJson;

public class CircuitPersistence {
    private CircuitPersistence() {
        /* This utility class should not be instantiated */
    }


    public static void save(CircuitManager manager, String filepath) throws IOException {
        Map<String, Object> data = serialize(manager);
        // Write
        try (FileWriter writer = new FileWriter(filepath)) {
            writer.write(SimpleJson.serialize(data));
        }
    }

    public static void load(CircuitManager manager, String filepath) throws IOException {
        String json = new String(Files.readAllBytes(Paths.get(filepath)));
        Map<String, Object> data = SimpleJson.parse(json);
        deserialize(manager, data);
    }

    public static Map<String, Object> serialize(CircuitManager manager) {
        Map<String, Object> data = new HashMap<>();
        
        // 1. Serialize Components
        List<Map<String, Object>> compList = new ArrayList<>();
        for (CircuitComponent c : manager.getComponents()) {
            Map<String, Object> cMap = new HashMap<>();
            
            // Determine type
            Tooltype type = c.getToolType();
            if (type == null) continue;

            cMap.put("type", type.toString());
            cMap.put("id", c.getID());
            cMap.put("x", c.getX());
            cMap.put("y", c.getY());
            cMap.put("inPins", c.getInputPinIDs());
            cMap.put("outPins", c.getOutputPinIDs());
            
            if (c instanceof SubCircuit) {
                cMap.put("data", ((SubCircuit) c).getDefinition());
            }
            
            compList.add(cMap);
        }
        data.put("components", compList);

        // 2. Serialize Wires
        List<Map<String, Object>> wireList = new ArrayList<>();
        for (Wire w : manager.getWires()) {
            Map<String, Object> wMap = new HashMap<>();
            wMap.put("src", w.getSourcePinID());
            wMap.put("dest", w.getDestPinID());
            wireList.add(wMap);
        }
        data.put("wires", wireList);
        
        return data;
    }

    @SuppressWarnings("unchecked")
    public static void deserialize(CircuitManager manager, Map<String, Object> data) {
        // Clear current
        manager.clear();

        // 1. Load Components
        List<Object> compList = (List<Object>) data.get("components");
        int maxPinID = 0;

        if (compList != null) {
            for (Object obj : compList) {
                Map<String, Object> map = (Map<String, Object>) obj;
                String typeStr = (String) map.get("type");
                Tooltype type = Tooltype.valueOf(typeStr);
                String id = (String) map.get("id");
                int x = ((Number) map.get("x")).intValue();
                int y = ((Number) map.get("y")).intValue();

                // Pins
                List<Integer> inPins = castToIntList(map.get("inPins"));
                List<Integer> outPins = castToIntList(map.get("outPins"));

                // Track IDs to update generator later
                for (int p : inPins) maxPinID = Math.max(maxPinID, p);
                for (int p : outPins) maxPinID = Math.max(maxPinID, p);

                CircuitComponent c = null;
                
                if (type == Tooltype.SUB_CIRCUIT) {
                    Map<String, Object> subData = (Map<String, Object>) map.get("data");
                    if (subData != null) {
                        c = new SubCircuit(subData, false);
                    } else {
                        // Fallback if data is missing (should not happen in valid saves)
                        c = ComponentFactory.createForLoad(type, id, x, y, inPins, outPins);
                    }
                } else {
                    c = ComponentFactory.createForLoad(type, id, x, y, inPins, outPins);
                }

                if (c != null) manager.addComponent(c);
            }
        }

        // 2. Load Wires
        List<Object> wireList = (List<Object>) data.get("wires");
        if (wireList != null) {
            for (Object obj : wireList) {
                Map<String, Object> map = (Map<String, Object>) obj;
                int src = ((Number) map.get("src")).intValue();
                int dest = ((Number) map.get("dest")).intValue();
                manager.addWireDirectly(new Wire(src, dest));
            }
        }

        // 3. Update PinID Generator
        PinID.setNextPinID(maxPinID + 1);
        
        manager.refreshAllPinLocations();
        manager.propagate();
    }

    // Helper
    public static List<Integer> castToIntList(Object obj) {
        List<Integer> result = new ArrayList<>();
        if (obj instanceof List) {
            for (Object o : (List<?>) obj) {
                if (o instanceof Number) result.add(((Number) o).intValue());
            }
        }
        return result;
    }
}
