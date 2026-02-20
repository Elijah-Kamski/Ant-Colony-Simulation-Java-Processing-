package antcolony.environment;

import processing.core.PApplet;

/**
 * Represents an individual star in the night sky.
 * <p>
 * Each star has a fixed position, but its brightness and size
 * oscillate based on a sine wave to create a twinkling effect.
 * </p>
 */
public class Stars
{
    /**
     * X position on the screen.
     */
    public float x;

    /**
     * Y position on the screen.
     */
    public float y;

    /**
     * Base size of the star (diameter).
     */
    public float baseSize;

    /**
     * Speed of the brightness oscillation (frequency).
     */
    public float twinkleSpeed;

    /**
     * Initial phase of the sine wave (so they don't all blink at the same time).
     */
    public float phase;

    /**
     * Star Constructor.
     * @param p PApplet reference.
     * @param xMin Left generation boundary.
     * @param xMax Right generation boundary.
     * @param yMin Top generation boundary.
     * @param yMax Bottom generation boundary.
     */
    public Stars(PApplet p, float xMin, float xMax, float yMin, float yMax)
    {
        this.x = p.random(xMin, xMax);
        this.y = p.random(yMin, yMax);
        
        this.baseSize = p.random(1.0f, 2.5f);
        this.twinkleSpeed = p.random(0.02f, 0.08f);
        
        // Defines a random starting point in the sine cycle (0 to 2PI)
        this.phase = p.random(PApplet.TWO_PI);
    }

    /**
     * Draws the star with the current twinkling effect.
     * @param p PApplet reference (required for frameCount and drawing functions).
     */
    public void display(PApplet p)
    {
        // Twinkle Factor Calculation
        // Uses frameCount to animate over time.
        // The result ranges from 0.3 (dark) to 1.0 (maximum brightness).
        float tw = 0.65f + 0.35f * PApplet.sin(phase + p.frameCount * twinkleSpeed);
        
        // Maps the factor to Alpha (transparency/brightness)
        int a = (int) (120 + 135 * tw);

        p.noStroke();
        p.fill(255, 255, 255, a);
        p.circle(x, y, baseSize);

        // Halo effect (Glow)
        // Only draws if the star is large enough
        if (baseSize > 2.0f)
        {
            // Halo is larger and much more transparent (1/4 of the original alpha)
            p.fill(255, 255, 255, a / 4);
            p.circle(x, y, baseSize * 3.0f);
        }
    }
}