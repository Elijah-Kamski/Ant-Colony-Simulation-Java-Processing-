package antcolony.entities;

import antcolony.AntColonySimulation;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Represents a leaf that falls from the top of the screen and serves as food.
 * <p>
 * The leaf has its own physics (simulated in {@link LeafPhysics}) and a resource
 * amount (`amount`). As the ants eat the leaf, its visual size decreases 
 * until it disappears.
 * </p>
 */
public class FallingLeaf
{
    /**
     * Physics component that controls the fall and position of the leaf.
     */
    public LeafPhysics phys;

    /**
     * Amount of food remaining in the leaf.
     * Starts at 250 and decreases as ants interact with it.
     */
    public float amount;

    /**
     * Color of the leaf (based on the season at the time of creation).
     */
    public int col;

    /**
     * Pixels Per Meter (PPM) scale.
     * <p>
     * Used for physics conversions. 100px = 1 virtual meter.
     * </p>
     */
    private static final float PPM = 100f;

    /**
     * Leaf Constructor.
     * @param p Reference to PApplet.
     * @param sim Reference to the simulation (to access current colors).
     * @param spawnPosPx Initial position in pixels.
     */
    public FallingLeaf(PApplet p, AntColonySimulation sim, PVector spawnPosPx)
    {
        this.phys = new LeafPhysics(p, spawnPosPx, PPM);
        this.amount = 250;
        // Gets the current seasonal color (defined in ColorScheme)
        this.col = sim.colors.cCurrentLeafGlobal;
    }

    /**
     * Updates the leaf's physics.
     * @param p Reference to PApplet.
     * @param sim Reference to the simulation (for world boundaries).
     * @param dt Delta time for smooth movement.
     */
    public void update(PApplet p, AntColonySimulation sim, float dt)
    {
        // Define lateral boundaries where the leaf can fall (between sidebars)
        // The '+ 6' and '- 6' serve as a safety margin
        float minX = sim.leftSidebarW + 6;
        float maxX = p.width - sim.rightSidebarW - 6;

        phys.update(p, dt, sim.surfaceY, minX, maxX);
    }

    /**
     * Draws the leaf on the screen.
     * @param p Reference to PApplet.
     */
    public void display(PApplet p)
    {
        p.noStroke();
        p.fill(col);

        // Calculates the visual size based on the amount of food remaining.
        // If amount is 250, size is 14. If it is 0, size is 6.
        float s = PApplet.map(amount, 0, 250, 6, 14);
        
        float x = phys.posPx.x;
        float y = phys.posPx.y;

        // Draws the leaf shape (two overlapping ellipses to provide texture)
        // Main body
        p.ellipse(x, y, s * 1.8f, s);
        
        // Side detail (shadow/volume)
        p.ellipse(x - s * 0.35f, y, s, s * 0.7f);

        // Draws the central vein of the leaf
        p.stroke(255, 80); // Translucent white
        p.strokeWeight(1);
        p.line(x - s * 0.8f, y, x + s * 0.8f, y);
    }
}