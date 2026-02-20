package antcolony;

import processing.core.PApplet;
import setup.IProcessingApp;

/**
 * Entry point (Wrapper) class for the Ant Colony application.
 * <p>
 * This class implements the {@link IProcessingApp} interface, allowing the
 * simulation to be loaded by the main system (Menu/Launcher).
 * Its sole responsibility is to instantiate {@link AntColonySimulation} and
 * delegate life-cycle events (setup, draw, inputs) to it.
 * </p>
 */
public class AntColonyApp implements IProcessingApp
{
    /**
     * Instance of the main simulation logic.
     */
    private AntColonySimulation sim;

    /**
     * Initial configuration.
     * Instantiates the simulation and calls its setup method.
     * @param p Reference to PApplet.
     */
    @Override
    public void setup(PApplet p)
    {
        sim = new AntColonySimulation();
        sim.setup(p);
    }

    /**
     * Main draw loop.
     * @param p Reference to PApplet.
     * @param dt Delta time (time elapsed since the last frame).
     */
    @Override
    public void draw(PApplet p, float dt)
    {
        sim.draw(p, dt);
    }

    /**
     * Captures mouse click events and passes them to the simulation.
     * @param p Reference to PApplet.
     */
    @Override
    public void mousePressed(PApplet p)
    {
        sim.mousePressed(p);
    }

    /**
     * Captures keyboard events and passes them to the simulation.
     * @param p Reference to PApplet.
     */
    @Override
    public void keyPressed(PApplet p)
    {
        sim.keyPressed(p);
    }
}