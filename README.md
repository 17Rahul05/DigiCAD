# DigiCAD - Digital Circuit Simulator

DigiCAD is a desktop application built with Java Swing for designing and simulating digital logic circuits. It allows users to place various logic gates, connect them with wires, and observe real-time signal propagation in a visual environment.

## Features

*   **Logic Gates:** AND, OR, NOT, XOR, NAND, NOR, XNOR.
*   **Advanced Components:** Multiplexer (MUX), Demultiplexer (DEMUX), Decoder, Encoder.
*   **I/O Devices:** Switches, LEDs, 7-Segment Displays.
*   **Sub-Circuits (WIP):** Encapsulate groups of components into reusable blocks.
*   **Real-time Simulation:** Event-driven propagation algorithm.
*   **Tools:** Truth Table generator, JSON-based save/load system.
*   **Customization:** Multiple UI themes (Light, Dark, etc.).

## Getting Started

### Prerequisites
*   **Java Development Kit (JDK) 21** or higher.

### Building the Project

You can use the provided build scripts to compile and package the project into a JAR file.

#### PowerShell:
```powershell
.\build.ps1
```

#### Command Prompt:
```batch
.\build.bat
```

### Running the Application
Once built, run the generated JAR:
```bash
java -jar DigiCAD.jar
```

## Architecture

*   **Simulation Engine:** `sim.CircuitManager` handles the lifecycle and signal propagation.
*   **UI:** `sim.ui.CanvasPanel` provides the interactive workspace.
*   **Persistence:** Custom `SimpleJson` utility for circuit serialization.

## License
This project is for educational purposes.
