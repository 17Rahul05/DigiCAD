package sim.util;

import java.awt.Point;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import sim.CircuitComponent;
import sim.gates.AndGate;
import sim.gates.Decoder;
import sim.gates.DemuxGate;
import sim.gates.Encoder;
import sim.gates.MuxGate;
import sim.gates.NandGate;
import sim.gates.NorGate;
import sim.gates.NotGate;
import sim.gates.OrGate;
import sim.gates.XnorGate;
import sim.gates.XorGate;
import sim.model.LED;
import sim.model.SevenSegmentDisplay;
import sim.model.Switch;
import sim.model.Tooltype;

public class ComponentFactory {
    private ComponentFactory() {
        /* This utility class should not be instantiated */
    }


    private static final Map<Tooltype, BiFunction<String, Point, CircuitComponent>> FACTORY = new EnumMap<>(Tooltype.class);
    
    @FunctionalInterface
    public interface LoadCreator {
        CircuitComponent create(String id, int x, int y, List<Integer> inPins, List<Integer> outPins);
    }
    
    private static final Map<Tooltype, LoadCreator> LOAD_FACTORY = new EnumMap<>(Tooltype.class);

    static {
        // Normal Creation
        FACTORY.put(Tooltype.AND, (id, p) -> new AndGate(id, p.x, p.y));
        FACTORY.put(Tooltype.OR, (id, p) -> new OrGate(id, p.x, p.y));
        FACTORY.put(Tooltype.NOT, (id, p) -> new NotGate(id, p.x, p.y));
        FACTORY.put(Tooltype.XOR, (id, p) -> new XorGate(id, p.x, p.y));
        FACTORY.put(Tooltype.NAND, (id, p) -> new NandGate(id, p.x, p.y));
        FACTORY.put(Tooltype.NOR, (id, p) -> new NorGate(id, p.x, p.y));
        FACTORY.put(Tooltype.XNOR, (id, p) -> new XnorGate(id, p.x, p.y));
        FACTORY.put(Tooltype.MUX, (id, p) -> new MuxGate(id, p.x, p.y));
        FACTORY.put(Tooltype.DEMUX, (id, p) -> new DemuxGate(id, p.x, p.y));
        FACTORY.put(Tooltype.DECODER, (id, p) -> new Decoder(id, p.x, p.y));
        FACTORY.put(Tooltype.ENCODER, (id, p) -> new Encoder(id, p.x, p.y));
        FACTORY.put(Tooltype.SWITCH, (id, p) -> new Switch(id, p.x, p.y));
        FACTORY.put(Tooltype.LED, (id, p) -> new LED(id, p.x, p.y));
        FACTORY.put(Tooltype.SEVEN_SEGMENT, (id, p) -> new SevenSegmentDisplay(id, p.x, p.y));

        // Loading Creation
        LOAD_FACTORY.put(Tooltype.AND, (id, x, y, inPins, outPins) -> new AndGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.OR, (id, x, y, inPins, outPins) -> new OrGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.NOT, (id, x, y, inPins, outPins) -> new NotGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.XOR, (id, x, y, inPins, outPins) -> new XorGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.NAND, (id, x, y, inPins, outPins) -> new NandGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.NOR, (id, x, y, inPins, outPins) -> new NorGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.XNOR, (id, x, y, inPins, outPins) -> new XnorGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.MUX, (id, x, y, inPins, outPins) -> new MuxGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.DEMUX, (id, x, y, inPins, outPins) -> new DemuxGate(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.DECODER, (id, x, y, inPins, outPins) -> new Decoder(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.ENCODER, (id, x, y, inPins, outPins) -> new Encoder(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.SWITCH, (id, x, y, inPins, outPins) -> new Switch(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.LED, (id, x, y, inPins, outPins) -> new LED(id, x, y, inPins, outPins));
        LOAD_FACTORY.put(Tooltype.SEVEN_SEGMENT, (id, x, y, inPins, outPins) -> new SevenSegmentDisplay(id, x, y, inPins, outPins));
    }

    public static CircuitComponent create(
        Tooltype tool,
        String id,
        int x,
        int y
    ) {
        BiFunction<String, Point, CircuitComponent> creator = FACTORY.get(tool);
        if (creator == null) return null;
        return creator.apply(id, new Point(x, y));
    }

    public static CircuitComponent createForLoad(
        Tooltype tool,
        String id,
        int x,
        int y,
        List<Integer> inPins,
        List<Integer> outPins
    ) {
        validatePins(tool, inPins, outPins);
        LoadCreator creator = LOAD_FACTORY.get(tool);
        if (creator == null) return null;
        return creator.create(id, x, y, inPins, outPins);
    }

    private static void validatePins(Tooltype tool, List<Integer> inPins, List<Integer> outPins) {
        int expectedIn = 0;
        int expectedOut = 0;

        switch (tool) {
            case AND, OR, NAND, NOR, XOR, XNOR:
                expectedIn = 2; expectedOut = 1; break;
            case NOT:
                expectedIn = 1; expectedOut = 1; break;
            case MUX:
                expectedIn = 3; expectedOut = 1; break;
            case DEMUX:
                expectedIn = 2; expectedOut = 2; break;
            case DECODER:
                expectedIn = 2; expectedOut = 4; break;
            case ENCODER:
                expectedIn = 4; expectedOut = 2; break;
            case SWITCH:
                expectedIn = 0; expectedOut = 1; break;
            case LED:
                expectedIn = 1; expectedOut = 0; break;
            case SEVEN_SEGMENT:
                expectedIn = 7; expectedOut = 0; break;
            default:
                return;
        }

        while (inPins.size() < expectedIn) {
            inPins.add(PinID.getNextPinID());
        }
        while (outPins.size() < expectedOut) {
            outPins.add(PinID.getNextPinID());
        }
    }
}
