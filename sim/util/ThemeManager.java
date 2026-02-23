package sim.util;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {
    private ThemeManager() {
        /* This utility class should not be instantiated */
    }

    private static final Theme[] ALL_THEMES = {
        Theme.DARK,
        Theme.LIGHT,
        Theme.BLUEPRINT,
        Theme.INDUSTRIAL,
        Theme.NORD,
        Theme.COFFEE,
        Theme.NORDIC_NIGHT,
        Theme.WARM_SEPIA,
        Theme.FROST,
        Theme.MIDNIGHT_PURPLE,
        Theme.SOLARIZED_DARK,
        Theme.GRUVBOX,
        Theme.ROSE_PINE
    };
    
    private static Theme currentTheme = ALL_THEMES[0]; // Default to the first theme
    private static final List<Runnable> listeners = new ArrayList<>();

    public static Theme getTheme() {
        return currentTheme;
    }

    public static void setTheme(String themeName) {
        for (Theme theme : ALL_THEMES) {
            if (theme.name.equals(themeName)) {
                currentTheme = theme;
                notifyListeners();
                return;
            }
        }
        System.err.println("Theme with name '" + themeName + "' not found.");
    }

    public static List<String> getThemeNames() {
        List<String> names = new ArrayList<>();
        for (Theme theme : ALL_THEMES) {
            names.add(theme.name);
        }
        return names;
    }

    public static void addThemeListener(Runnable r) {
        listeners.add(r);
    }

    private static void notifyListeners() {
        for (Runnable r : listeners) {
            r.run();
        }
    }
}
