package setup;

import processing.core.PApplet;

/**
 * Low-level Processing Engine Wrapper.
 * <p>
 * This class acts as the bridge between the Processing library and the 
 * modular app architecture. It manages the window lifecycle, calculates 
 * Delta Time (dt) for framerate independence, and forwards hardware 
 * events to the current active app.
 * </p>
 */
public class ProcessingSetup extends PApplet 
{
    /** * The modular application instance. 
     * Defined as static to be accessible before the PApplet lifecycle begins.
     */
    public static IProcessingApp app;

    /** * Internal timestamp of the last update (in milliseconds). 
     */
    private int lastUpdate;

    /**
     * Configuration of the graphics window.
     */
    @Override
    public void settings() 
    {
        // Standard resolution for desktop/laptop displays
        size(1400, 700);
    }

    /**
     * Initial engine setup.
     */
    @Override
    public void setup() 
    {
		surface.setTitle("Ant Colony Simulation");
        if (app != null) {
            app.setup(this);
        }
        lastUpdate = millis();
    }

    /**
     * Main execution loop.
     * Calculates elapsed time (dt) and triggers the app's internal logic and rendering.
     */
    @Override
    public void draw() 
    {
        int now = millis();
        
        // Calculate Delta Time (dt) in seconds 
        // Essential for consistent physics regardless of FPS
        float dt = (now - lastUpdate) / 1000f;
        lastUpdate = now;

        if (app != null) {
            app.draw(this, dt);
        }
    }

    /**
     * Forwards mouse press events to the active application.
     */
    @Override
    public void mousePressed() 
    {
        if (app != null) {
            app.mousePressed(this);
        }
    }

    /**
     * Forwards keyboard events to the active application.
     */
    @Override
    public void keyPressed() 
    {
        if (app != null) {
            app.keyPressed(this);
        }
    }
}