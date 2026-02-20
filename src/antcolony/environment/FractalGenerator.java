package antcolony.environment;

import processing.core.PApplet;
import processing.core.PGraphics;

/**
 * Procedural texture generator using fractal mathematics.
 * <p>
 * This utility class generates images based on the Julia Set.
 * These organic and branching textures are ideal for simulating underground roots
 * without the need to load external images (PNGs/JPGs).
 * </p>
 */
public class FractalGenerator
{
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FractalGenerator()
    {
        // Intentionally empty
    }

    /**
     * Generates a fractal texture (Julia Set) in a transparent graphical buffer.
     * <p>
     * The texture is drawn entirely in white (with transparency/Alpha variation).
     * This allows the {@link antcolony.entities.StaticTree} class to apply any
     * desired color using `p.tint()` during rendering.
     * </p>
     * @param p Reference to PApplet (to create the buffer and colors).
     * @param w Texture width.
     * @param h Texture height.
     * @return A PGraphics object containing the generated texture.
     */
    public static PGraphics createJuliaTexture(PApplet p, int w, int h)
    {
        // Creates an off-screen graphical buffer
        PGraphics pg = p.createGraphics(w, h);
        
        // Disables smoothing for a sharper, "pixel-art" look in the roots
        pg.noSmooth();
        
        pg.beginDraw();
        pg.clear(); // Clears with a transparent background (0,0,0,0)

        // --- Fractal Parameters ---
        // The Julia Set is defined by the formula: Z = Z^2 + C
        // Where C is a complex constant. Small variations in C drastically change the shape.
        
        float baseCx = -0.74f;
        float baseCy = 0.1f;
        
        // Adds randomness so each tree has unique roots
        float cx = baseCx + p.random(-0.08f, 0.08f);
        float cy = baseCy + p.random(-0.08f, 0.08f);
        
        int maxIterations = 50;

        // Iterate through each pixel of the texture
        for (int py = 0; py < h; py++)
        {
            for (int px = 0; px < w; px++)
            {
                // Mapping pixel coordinates to the complex plane
                // Adjusted to create a vertical (root-like) shape
                float zx = PApplet.map(px, 0, w, -1.5f, 1.5f);
                float zy = PApplet.map(py, 0, h, 0.2f, -1.2f);

                int iter = 0;
                
                // Iteration of the Z^2 + C function
                // Checks if the point escapes to infinity (magnitude > 4.0)
                while (zx * zx + zy * zy < 4.0 && iter < maxIterations)
                {
                    float xtemp = zx * zx - zy * zy + cx;
                    zy = 2 * zx * zy + cy;
                    zx = xtemp;
                    iter++;
                }

                // --- Pixel Rendering ---
                // We only draw points that remained "trapped" in the set or near it
                
                if (iter > 5 && iter < maxIterations)
                {
                    // Edge points: Draw translucent white
                    // More iterations result in "thinner" detail
                    int alpha = (int) PApplet.map(iter, 5, maxIterations, 180, 20);
                    pg.set(px, py, p.color(255, 255, 255, alpha));
                }
                else if (iter == maxIterations)
                {
                    // Inside the set: Draw solid white
                    pg.set(px, py, p.color(255, 255, 255));
                }
            }
        }
        
        pg.endDraw();
        
        return pg;
    }
}