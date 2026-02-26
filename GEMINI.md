# Digital Circuit Simulator

A desktop application built with Java Swing for designing and simulating digital logic circuits. Users can place gates, connect them with wires, and observe real-time signal propagation.

## Project Overview

*   **Main Technology:** Java 21+, Swing (UI).
*   **Architecture:**
    *   `sim.Simulate`: The main entry point and UI layout.
    *   `sim.CircuitManager`: The heart of the simulation, handling component lifecycle, wire connections, and the propagation algorithm.
    *   `sim.CircuitComponent`: Abstract base class for all components (Gates, I/O, etc.).
    *   `sim.model`: Contains specific implementations of circuit components.
    *   `sim.util`: Helper classes for JSON serialization (`SimpleJson`), themes (`ThemeManager`), and pin management (`PinID`).
    *   `sim.ui`: Custom Swing components (`CanvasPanel`) and input handling (`MouseController`).

## Features

*   **Logic Gates:** AND, OR, NOT, XOR, NAND, NOR, XNOR.
*   **Advanced Components:** Multiplexer (MUX), Demultiplexer (DEMUX), Decoder, Encoder.
*   **I/O Devices:** Switches, LEDs, 7-Segment Displays.
*   **Sub-Circuits (WIP):** Encapsulate a group of components into a single reusable block.
*   **Tools:** Truth Table generator, real-time simulation, saving/loading circuits in `.json` format.
*   **Themes:** Customizable UI colors (Light, Dark, etc.).

## Building and Running

### Prerequisites
*   Java Development Kit (JDK) 21 or higher.

### Automated Build (Recommended)
You can use the provided build scripts to clean, compile, and package the project into a JAR.

**PowerShell:**
```powershell
.\build.ps1
```

**Command Prompt (Batch):**
```batch
.\build.bat
```

### Manual Build
To manually compile and run from the root directory:

```powershell
# Compile all source files recursively
javac -d bin (Get-ChildItem -Path sim -Filter *.java -Recurse)

# Package into JAR
jar cvfm DigiCAD.jar manifest.txt -C bin .

# Run the application
java -jar DigiCAD.jar
```

## Development Conventions

*   **Coordinate System:** Components use a grid-based coordinate system (though snapping is currently a TODO).
*   **Simulation Loop:** Propagation is event-driven; when a component's input changes, it notifies the `CircuitManager` to update the state until stability is reached (or a maximum iteration limit is hit).
*   **Serialization:** Uses a custom `SimpleJson` utility for reading and writing `.json` circuit files.
*   **UI Updates:** Uses `ThemeManager` with a listener pattern to update component colors dynamically when a theme is changed.

## Current TODOs (from `sim/todos.txt`)

1.  **Sub-Circuits:** Fully implement the abstraction and library feature.
2.  **Configurable Gates:** Allow changing the number of inputs for logic gates.
3.  **Keyboard Shortcuts:** Add Swing Key Bindings for common actions.
4.  **Orthogonal Wiring:** Implement 90-degree wire routing (Manhattan routing).
5.  **Pin-to-Grid Alignment:** Standardize component sizes and pin offsets so pins always land on grid intersections.
6.  **Text Labels:** Allow users to annotate their circuits.
