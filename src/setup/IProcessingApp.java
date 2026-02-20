package setup;

import processing.core.PApplet;

/**
 * Common interface for all applications/simulations in this project.
 * <p>
 * Defines the "contract" that a class must follow to be executed
 * by the main system (Main/Launcher). This allows switching between
 * different simulations without modifying the main loop code.
 * </p>
 */
public interface IProcessingApp
{
    /**
     * Initial application configuration.
     * Executed once at startup.
     * @param p Reference to PApplet.
     */
    public void setup(PApplet p);

    /**
     * Main logic and drawing loop.
     * Executed every frame.
     * @param p Reference to PApplet.
     * @param dt Delta time (time elapsed since the last frame in seconds).
     */
    public void draw(PApplet p, float dt);

    /**
     * Mouse click event hook.
     * @param p Reference to PApplet.
     */
    public void mousePressed(PApplet p);

    /**
     * Key pressed event hook.
     * @param p Reference to PApplet.
     */
    public void keyPressed(PApplet p);
}