package antcolony.environment;

import antcolony.AntColonySimulation;
import processing.core.PApplet;

/**
 * Manager for simulation colors and visual environment.
 * <p>
 * This class centralizes all color definitions. It is responsible for:
 * 1. Defining seasonal palettes (Spring, Summer, etc.).
 * 2. Calculating the day/night cycle (sky color interpolation).
 * 3. Defining colors for ants and colonies.
 * </p>
 */
public class ColorScheme
{
    // --- Environment Colors (Calculated dynamically) ---

    /**
     * Base sky color during the day.
     */
    public int cDay;

    /**
     * Ground color during the day (changes with the season).
     */
    public int cGroundDay;

    /**
     * Ground color at night (darkened version).
     */
    public int cGroundNight;

    /**
     * Base leaf color (changes with the season).
     */
    public int cCurrentLeafGlobal;

    // --- Day/Night Cycle Colors ---
    
    public int cNight;
    public int cDawn;
    public int cDusk;
    
    // --- Colony Colors ---
    
    public int colA_Normal;
    public int colA_Carry;
    public int colB_Normal;
    public int colB_Carry;

    /**
     * Default constructor.
     */
    public ColorScheme()
    {
        // Initialization performed in updateSeasonalColors
    }

    /**
     * Updates the global color palette based on the current season.
     * <p>
     * Should be called whenever the season changes or at simulation startup.
     * </p>
     * @param p Reference to PApplet.
     * @param sim Reference to the simulation (to determine the current season).
     */
    public void updateSeasonalColors(PApplet p, AntColonySimulation sim)
    {
        int season = sim.time.curSeasonIdx;

        // 1. Ant Color Definitions
        // Colony A (Blue)
        colA_Normal = p.color(30, 80, 200);
        colA_Carry  = p.color(100, 200, 255);
        
        // Colony B (Red)
        colB_Normal = p.color(200, 30, 30);
        colB_Carry  = p.color(255, 150, 150);

        // 2. World Color Definitions
        cCurrentLeafGlobal = getTargetLeafColor(p, season);
        cGroundDay = getTargetGround(p, season);
        
        // Ground at night is nearly black/dark purple
        cGroundNight = p.color(15, 10, 20); 

        // 3. Solar Cycle Definition (Sky)
        cDay   = p.color(100, 180, 255); // Sky Blue
        cNight = p.color(10, 10, 35);    // Deep Blue
        cDawn  = p.color(255, 170, 120); // Pale Orange
        cDusk  = p.color(220, 120, 140); // Pink/Purple
    }
    
    /**
     * Obtains the correct color for an ant based on its colony and state.
     * @param colonyId ID of the colony (0 or 1).
     * @param hasFood Whether the ant is carrying food (lighter color).
     * @return The color as an integer value.
     */
    public int getAntColor(int colonyId, boolean hasFood)
    {
        if (colonyId == 0)
        {
            if (hasFood)
            {
                return colA_Carry;
            }
            else
            {
                return colA_Normal;
            }
        }
        else
        {
            if (hasFood)
            {
                return colB_Carry;
            }
            else
            {
                return colB_Normal;
            }
        }
    }

    /**
     * Defines leaf colors for each season.
     */
    public int getTargetLeafColor(PApplet p, int s)
    {
        switch (s)
        {
            case 0: // Spring (Light Green)
                return p.color(100, 255, 100, 220);
                
            case 1: // Summer (Dark Green)
                return p.color(34, 100, 34, 240);
                
            case 2: // Autumn (Orange/Brown)
                return p.color(205, 92, 10, 220);
                
            default: // Winter (Translucent White/Ice)
                return p.color(240, 240, 255, 150);
        }
    }

    /**
     * Defines ground colors for each season.
     */
    public int getTargetGround(PApplet p, int s)
    {
        switch (s)
        {
            case 0: 
                return p.color(120, 110, 70); // Spring
                
            case 1: 
                return p.color(160, 140, 60); // Summer
                
            case 2: 
                return p.color(120, 60, 30);  // Autumn
                
            default: 
                // WINTER: Brownish Gray / Slush
                return p.color(150, 130, 110);
        }
    }

    /**
     * Calculates sky color based on the time of day.
     * Performs linear interpolation (lerp) between Night, Dawn, Day, and Dusk.
     * @param p Reference to PApplet.
     * @param sim Reference to the simulation (for time data).
     * @return Current sky color.
     */
    public int getSkyColor(PApplet p, AntColonySimulation sim)
    {
        // Decimal hour (e.g., 14.5 for 14:30)
        float h = sim.time.curHour + sim.time.curMin / 60f;

        // Dead of night
        if (h < 4)
        {
            return cNight;
        }
        // Dawn (4 AM to 6 AM)
        else if (h < 6)
        {
            return p.lerpColor(cNight, cDawn, PApplet.map(h, 4, 6, 0, 1));
        }
        // Sunrise (6 AM to 8 AM)
        else if (h < 8)
        {
            return p.lerpColor(cDawn, cDay, PApplet.map(h, 6, 8, 0, 1));
        }
        // Full Day (8 AM to 6 PM)
        else if (h < 18)
        {
            return cDay;
        }
        // Dusk (6 PM to 8 PM)
        else if (h < 20)
        {
            return p.lerpColor(cDay, cDusk, PApplet.map(h, 18, 20, 0, 1));
        }
        // Nightfall (8 PM to 10 PM)
        else if (h < 22)
        {
            return p.lerpColor(cDusk, cNight, PApplet.map(h, 20, 22, 0, 1));
        }
        // Deep Night
        else
        {
            return cNight;
        }
    }

    /**
     * Calculates a color variation for an individual tree.
     * Ensures not all trees have the exact same color.
     * @param p Reference to PApplet.
     * @param sim Reference to the simulation.
     * @param offset Unique random value for the tree (-0.05 to 0.05).
     * @return Adjusted final color.
     */
    public int calculateTreeColor(PApplet p, AntColonySimulation sim, float offset)
    {
        int base = cCurrentLeafGlobal;
        
        // Extract RGB components and apply offset
        float r = p.red(base) + offset * 100;
        float g = p.green(base) + offset * 100;
        float b = p.blue(base) + offset * 100;
        float a = p.alpha(base);

        // Reconstruct color ensuring values remain between 0 and 255
        return p.color(
            PApplet.constrain(r, 0, 255),
            PApplet.constrain(g, 0, 255),
            PApplet.constrain(b, 0, 255),
            a
        );
    }
}