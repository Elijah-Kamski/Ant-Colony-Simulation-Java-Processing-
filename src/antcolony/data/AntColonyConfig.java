package antcolony.data;

/**
 * Global configuration class for the Ant simulation.
 * <p>
 * This class stores all static constants that define the window dimensions,
 * the graphical interface layout, the pheromone grid resolution, and the
 * initial time cycle parameters.
 * </p>
 * <p>
 * This is a utility class and should not be instantiated.
 * </p>
 */
public class AntColonyConfig
{
    /**
     * Total width of the application window in pixels.
     */
    public static final int WIDTH = 1400;

    /**
     * Total height of the application window in pixels.
     * <p>
     * Set to 700px to ensure compatibility with standard laptop screens.
     * </p>
     */
    public static final int HEIGHT = 700;

    /**
     * Width of the left sidebar (Controls and Legend).
     */
    public static final int LEFT_SIDEBAR_W = 280;

    /**
     * Width of the right sidebar (Statistics and Graphs).
     */
    public static final int RIGHT_SIDEBAR_W = 320;

    /**
     * Pheromone grid resolution.
     * <p>
     * Defines the pixel size of each grid cell.
     * A value of 4 means each pheromone block occupies 4x4 pixels.
     * </p>
     */
    public static final int RESOLUTION = 4;

    /**
     * Initial number of ants to create at simulation startup.
     * <p>
     * This value represents the total initial population, which will be distributed
     * among the existing colonies.
     * </p>
     */
    public static final int INITIAL_ANTS = 50;

    /**
     * Duration of a virtual day in simulation "ticks".
     * <p>
     * 1440 corresponds to the minutes in a real day (24h * 60m).
     * </p>
     */
    public static final int DAY_LENGTH = 1440;

    /**
     * Duration of each season in virtual days.
     */
    public static final int SEASON_LENGTH_DAYS = 3;

    /**
     * Private constructor to prevent instantiation of this class.
     */
    private AntColonyConfig()
    {
        // Intentionally empty
    }
}