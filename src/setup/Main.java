package setup;

import antcolony.AntColonyApp;
import processing.core.PApplet;

/**
 * Main Entry Point of the Application.
 * <p>
 * This class follows the Separation of Concerns principle. Its only 
 * responsibility is to instantiate the specific application implementation 
 * and launch the Processing engine.
 * </p>
 */
public class Main 
{
    /**
     * Java Main method.
     * @param args Command line arguments.
     */
    public static void main(String[] args) 
    {
        // 1. Assign the specific app implementation to the engine
        // This allows for easy switching between different simulations.
        ProcessingSetup.app = new AntColonyApp();
        
        // 2. Start the Processing PApplet launcher
        PApplet.main("setup.ProcessingSetup");
    }
}