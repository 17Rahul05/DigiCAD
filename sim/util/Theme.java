package sim.util;

import java.awt.Color;

public class Theme {
    public final String name;
    public final Color bg;
    public final Color componentBody;
    public final Color componentBorder;
    public final Color text;
    public final Color pinLine;
    public final Color wireActive;
    public final Color wireInactive;
    public final Color wireGlow;
    public final Color wireFloating;
    public final Color ledOff;
    public final Color ledFloating;
    public final Color toolbarBg;
    public final Color buttonBg;
    public final Color selection;

    public Theme(String name, Color bg, Color componentBody, Color componentBorder, Color text, 
                 Color pinLine, Color wireActive, Color wireInactive, Color wireGlow, 
                 Color wireFloating, Color ledOff, Color ledFloating, Color toolbarBg, Color buttonBg, Color selection) {
        this.name = name;
        this.bg = bg;
        this.componentBody = componentBody;
        this.componentBorder = componentBorder;
        this.text = text;
        this.pinLine = pinLine;
        this.wireActive = wireActive;
        this.wireInactive = wireInactive;
        this.wireGlow = wireGlow;
        this.wireFloating = wireFloating;
        this.ledOff = ledOff;
        this.ledFloating = ledFloating;
        this.toolbarBg = toolbarBg;
        this.buttonBg = buttonBg;
        this.selection = selection;
    }

    public static final Theme DARK = new Theme(
        "Dark",
        new Color(30, 30, 30),      // bg
        new Color(50, 50, 50),      // componentBody
        new Color(200, 200, 200),   // componentBorder
        Color.WHITE,                // text
        Color.LIGHT_GRAY,           // pinLine
        Color.GREEN,                // wireActive
        new Color(100, 100, 100),   // wireInactive
        new Color(0, 255, 0, 50),   // wireGlow
        new Color(90, 90, 130),     // wireFloating
        new Color(60, 60, 60),      // ledOff
        new Color(90, 90, 130),     // ledFloating
        Color.DARK_GRAY,            // toolbarBg
        new Color(60, 60, 60),      // buttonBg
        new Color(0, 255, 0, 40)    // selection
    );

    public static final Theme LIGHT = new Theme(
        "Light",
        new Color(240, 240, 240),   // bg
        new Color(220, 220, 220),   // componentBody
        new Color(40, 40, 40),      // componentBorder
        Color.BLACK,                // text
        Color.DARK_GRAY,            // pinLine
        new Color(0, 150, 0),       // wireActive
        new Color(180, 180, 180),   // wireInactive
        new Color(0, 255, 0, 30),   // wireGlow
        new Color(140, 140, 180),   // wireFloating
        new Color(200, 200, 200),   // ledOff
        new Color(140, 140, 180),   // ledFloating
        new Color(210, 210, 210),   // toolbarBg
        new Color(190, 190, 190),   // buttonBg
        new Color(0, 150, 0, 40)    // selection
    );

    public static final Theme BLUEPRINT = new Theme(
        "Blueprint",
        new Color(30, 30, 70),      // bg (dark blue)
        new Color(40, 40, 80),      // componentBody
        new Color(150, 150, 255),   // componentBorder (light blue)
        new Color(200, 200, 255),   // text (light blue/white)
        new Color(100, 100, 180),   // pinLine
        Color.CYAN,                 // wireActive (bright cyan)
        new Color(70, 70, 120),     // wireInactive
        new Color(0, 255, 255, 70), // wireGlow
        new Color(120, 120, 180),   // wireFloating
        new Color(100, 100, 150),   // ledOff
        new Color(120, 120, 180),   // ledFloating
        new Color(20, 20, 60),      // toolbarBg
        new Color(50, 50, 100),     // buttonBg
        new Color(0, 255, 255, 40)  // selection
    );

    public static final Theme INDUSTRIAL = new Theme(
        "Industrial",
        new Color(210, 210, 210),     // bg
        new Color(180, 180, 180),     // componentBody
        new Color(90, 90, 90),        // componentBorder
        new Color(30, 30, 30),        // text
        new Color(110, 110, 110),     // pinLine
        new Color(60, 160, 60),       // wireActive
        new Color(140, 140, 140),     // wireInactive
        new Color(60, 160, 60, 40),   // wireGlow
        new Color(110, 110, 130),     // wireFloating
        new Color(150, 150, 150),     // ledOff
        new Color(110, 110, 130),     // ledFloating
        new Color(170, 170, 170),     // toolbarBg
        new Color(160, 160, 160),     // buttonBg
        new Color(60, 160, 60, 40)    // selection
    );

    public static final Theme NORD = new Theme(
        "Nord",
        new Color(46, 52, 64),        // bg
        new Color(59, 66, 82),        // componentBody
        new Color(136, 192, 208),     // componentBorder (Frost blue)
        new Color(236, 239, 244),     // text
        new Color(76, 86, 106),       // pinLine
        new Color(163, 190, 140),     // wireActive (Green)
        new Color(67, 76, 94),        // wireInactive
        new Color(163, 190, 140, 50), // wireGlow
        new Color(129, 161, 193),     // wireFloating
        new Color(59, 66, 82),        // ledOff
        new Color(129, 161, 193),     // ledFloating
        new Color(46, 52, 64),        // toolbarBg
        new Color(67, 76, 94),        // buttonBg
        new Color(136, 192, 208, 40)  // selection
    );

    public static final Theme COFFEE = new Theme(
        "Coffee",
        new Color(50, 43, 40),        // bg
        new Color(65, 55, 52),        // componentBody
        new Color(180, 160, 150),     // componentBorder
        new Color(230, 220, 210),     // text
        new Color(90, 80, 75),        // pinLine
        new Color(255, 230, 200),     // wireActive (Warm Cream)
        new Color(75, 65, 60),        // wireInactive
        new Color(255, 230, 200, 40), // wireGlow
        new Color(120, 105, 100),     // wireFloating
        new Color(65, 55, 52),        // ledOff
        new Color(120, 105, 100),     // ledFloating
        new Color(40, 35, 32),        // toolbarBg
        new Color(65, 55, 52),        // buttonBg
        new Color(255, 230, 200, 20)  // selection
    );

    public static final Theme NORDIC_NIGHT = new Theme(
        "Nordic Night",
        new Color(36, 41, 51),        // bg (Even darker than Nord)
        new Color(46, 52, 64),        // componentBody
        new Color(112, 128, 144),     // componentBorder
        new Color(180, 190, 205),     // text
        new Color(60, 70, 85),        // pinLine
        new Color(136, 192, 208),     // wireActive (Nord Blue)
        new Color(50, 60, 75),        // wireInactive
        new Color(136, 192, 208, 30), // wireGlow
        new Color(85, 100, 115),      // wireFloating
        new Color(46, 52, 64),        // ledOff
        new Color(85, 100, 115),      // ledFloating
        new Color(30, 35, 45),        // toolbarBg
        new Color(46, 52, 64),        // buttonBg
        new Color(136, 192, 208, 20)  // selection
    );

    public static final Theme WARM_SEPIA = new Theme(
        "Warm Sepia",
        new Color(250, 240, 220),   // bg (Cream)
        new Color(230, 220, 200),   // componentBody
        new Color(150, 100, 50),    // componentBorder (Sepia brown)
        new Color(80, 50, 20),      // text (Dark sepia)
        new Color(180, 140, 90),    // pinLine
        new Color(100, 70, 40),     // wireActive
        new Color(200, 190, 170),   // wireInactive
        new Color(100, 70, 40, 50), // wireGlow
        new Color(150, 140, 160),   // wireFloating
        new Color(200, 190, 170),   // ledOff
        new Color(150, 140, 160),   // ledFloating
        new Color(220, 210, 190),   // toolbarBg
        new Color(210, 200, 180),   // buttonBg
        new Color(100, 70, 40, 40)  // selection
    );

    public static final Theme FROST = new Theme(
        "Frost",
        new Color(245, 248, 250),     // bg
        new Color(230, 235, 240),     // componentBody
        new Color(120, 140, 160),     // componentBorder
        new Color(30, 40, 50),        // text
        new Color(100, 120, 140),     // pinLine
        new Color(0, 170, 220),       // wireActive
        new Color(190, 200, 210),     // wireInactive
        new Color(0, 170, 220, 40),   // wireGlow
        new Color(130, 140, 170),     // wireFloating
        new Color(210, 220, 230),     // ledOff
        new Color(130, 140, 170),     // ledFloating
        new Color(225, 230, 235),     // toolbarBg
        new Color(210, 215, 220),     // buttonBg
        new Color(0, 170, 220, 40)    // selection
    );

    public static final Theme MIDNIGHT_PURPLE = new Theme(
        "Midnight Purple",
        new Color(20, 18, 30),        // bg
        new Color(40, 35, 60),        // componentBody
        new Color(160, 120, 220),     // componentBorder
        new Color(220, 210, 240),     // text
        new Color(130, 110, 180),     // pinLine
        new Color(180, 120, 255),     // wireActive
        new Color(80, 70, 100),       // wireInactive
        new Color(180, 120, 255, 60), // wireGlow
        new Color(110, 100, 150),     // wireFloating
        new Color(90, 80, 110),       // ledOff
        new Color(110, 100, 150),     // ledFloating
        new Color(30, 25, 45),        // toolbarBg
        new Color(50, 45, 70),        // buttonBg
        new Color(180, 120, 255, 40)  // selection
    );

    public static final Theme SOLARIZED_DARK = new Theme(
        "Solarized Dark",
        new Color(0, 43, 54),         // bg (Deep Teal)
        new Color(7, 54, 66),         // componentBody
        new Color(147, 161, 161),     // componentBorder
        new Color(131, 148, 150),     // text
        new Color(88, 110, 117),      // pinLine
        new Color(203, 75, 22),       // wireActive (Solarized Orange)
        new Color(7, 54, 66),         // wireInactive
        new Color(203, 75, 22, 60),   // wireGlow
        new Color(38, 139, 210),      // wireFloating (Solarized Blue)
        new Color(7, 54, 66),         // ledOff
        new Color(38, 139, 210),      // ledFloating
        new Color(0, 33, 43),         // toolbarBg
        new Color(7, 54, 66),         // buttonBg
        new Color(181, 137, 0, 40)    // selection (Yellow)
    );

    public static final Theme GRUVBOX = new Theme(
        "Gruvbox",
        new Color(40, 40, 40),        // bg (Dark Earth)
        new Color(60, 56, 54),        // componentBody
        new Color(168, 153, 132),     // componentBorder
        new Color(235, 219, 178),     // text (Cream)
        new Color(102, 92, 84),       // pinLine
        new Color(250, 189, 47),      // wireActive (Gruvbox Yellow)
        new Color(60, 56, 54),        // wireInactive
        new Color(250, 189, 47, 60),  // wireGlow
        new Color(131, 165, 152),     // wireFloating (Blue-Grey)
        new Color(60, 56, 54),        // ledOff
        new Color(131, 165, 152),     // ledFloating
        new Color(29, 32, 33),        // toolbarBg
        new Color(60, 56, 54),        // buttonBg
        new Color(250, 189, 47, 30)   // selection
    );

    public static final Theme ROSE_PINE = new Theme(
        "Rose Pine",
        new Color(25, 23, 36),        // bg (Midnight Purple-Grey)
        new Color(31, 29, 46),        // componentBody
        new Color(144, 140, 170),     // componentBorder
        new Color(224, 222, 244),     // text
        new Color(110, 106, 134),     // pinLine
        new Color(235, 188, 186),     // wireActive (Rose)
        new Color(31, 29, 46),        // wireInactive
        new Color(235, 188, 186, 60), // wireGlow
        new Color(246, 193, 119),     // wireFloating (Gold)
        new Color(31, 29, 46),        // ledOff
        new Color(246, 193, 119),     // ledFloating
        new Color(19, 17, 28),        // toolbarBg
        new Color(31, 29, 46),        // buttonBg
        new Color(49, 116, 143, 40)   // selection (Pine Blue)
    );
}
