package antcolony.environment;

import antcolony.AntColonySimulation;
import processing.core.PApplet;
import processing.core.PVector;

/**
 * Spatial grid that stores pheromone levels.
 * <p>
 * The field is a two-dimensional matrix where each cell contains 4 information channels (float):
 * 0: Colony A Home
 * 1: Colony A Food
 * 2: Colony B Home
 * 3: Colony B Food
 * </p>
 * <p>
 * This class manages evaporation, simplified diffusion, and trail persistence.
 * </p>
 */
public class PheromoneField
{
    /**
     * Number of columns in the grid.
     */
    public final int cols;

    /**
     * Number of rows in the grid.
     */
    public final int rows;

    /**
     * Size of each cell in pixels (spatial resolution).
     */
    public final int resolution;

    /**
     * Three-dimensional matrix [column][row][channel].
     */
    public final float[][][] grid;

    /**
     * Pheromone Field Constructor.
     * @param cols Number of columns.
     * @param rows Number of rows.
     * @param resolution Cell size in pixels.
     */
    public PheromoneField(int cols, int rows, int resolution)
    {
        this.cols = cols;
        this.rows = rows;
        this.resolution = resolution;
        // 4 channels: [0]HomeA, [1]FoodA, [2]HomeB, [3]FoodB
        this.grid = new float[cols][rows][4];
    }

    /**
     * Clears all pheromones and resets the "Home" points (Nests).
     * @param queenLoc1 Queen A location.
     * @param queenLoc2 Queen B location.
     */
    public void reset(PVector queenLoc1, PVector queenLoc2)
    {
        for (int x = 0; x < cols; x++)
        {
            for (int y = 0; y < rows; y++)
            {
                grid[x][y][0] = 0;
                grid[x][y][1] = 0;
                grid[x][y][2] = 0;
                grid[x][y][3] = 0;
            }
        }
        
        addNestPheromone(queenLoc1, 0); // Channel 0: Home A
        addNestPheromone(queenLoc2, 2); // Channel 2: Home B
    }

    /**
     * Permanently marks the nest position on the grid.
     * @param loc Position vector.
     * @param channel Channel to mark (0 or 2).
     */
    private void addNestPheromone(PVector loc, int channel)
    {
        int qx = (int) (loc.x / resolution);
        int qy = (int) (loc.y / resolution);
        
        if (inBounds(qx, qy))
        {
            grid[qx][qy][channel] = 1.0f;
        }
    }

    /**
     * Applies evaporation to all grid cells.
     * <p>
     * Reduces pheromone intensity by multiplying by a factor (0.0 to 1.0).
     * It also keeps the nest zone permanently active.
     * </p>
     * @param p PApplet reference.
     * @param sim Simulation reference (for evaporation rates and positions).
     */
    public void evaporate(PApplet p, AntColonySimulation sim)
    {
        // Evaporation rate configuration based on sliders
        float evapHomeA = sim.evapRateA;
        float evapFoodA = sim.evapRateA - 0.01f; // Food evaporates faster
        
        if (evapFoodA < 0)
        {
            evapFoodA = 0;
        }

        float evapHomeB = sim.evapRateB;
        float evapFoodB = sim.evapRateB - 0.01f;
        
        if (evapFoodB < 0)
        {
            evapFoodB = 0;
        }

        for (int x = 0; x < cols; x++)
        {
            float wx = x * resolution;
            
            // Optimization: Ignores columns outside the playable area (under sidebars)
            if (wx < sim.leftSidebarW || wx > p.width - sim.rightSidebarW)
            {
                continue;
            }

            for (int y = 0; y < rows; y++)
            {
                float wy = y * resolution;
                
                // Optimization: Ignores the sky (above the surface)
                if (wy < sim.surfaceY)
                {
                    continue;
                }

                // Applies multiplicative evaporation
                grid[x][y][0] *= evapHomeA;
                grid[x][y][1] *= evapFoodA;
                
                grid[x][y][2] *= evapHomeB;
                grid[x][y][3] *= evapFoodB;

                // Keep the nest "fresh" (permanent zone around the queen)
                if (PApplet.dist(wx, wy, sim.queenLocA.x, sim.queenLocA.y) < 25)
                {
                    grid[x][y][0] = 1.0f;
                }
                
                if (PApplet.dist(wx, wy, sim.queenLocB.x, sim.queenLocB.y) < 25)
                {
                    grid[x][y][2] = 1.0f;
                }
            }
        }
    }

    /**
     * Obtains the pheromone value at a world position.
     * @param wx World X coordinate.
     * @param wy World Y coordinate.
     * @param channel Channel ID (0-3).
     * @return Intensity (0.0 to 1.0).
     */
    public float getWorld(float wx, float wy, int channel)
    {
        int x = (int) (wx / resolution);
        int y = (int) (wy / resolution);
        
        if (!inBounds(x, y))
        {
            return 0;
        }
        
        return grid[x][y][channel];
    }

    /**
     * Sets a specific pheromone value.
     */
    public void setWorld(float wx, float wy, int channel, float v)
    {
        int x = (int) (wx / resolution);
        int y = (int) (wy / resolution);
        
        if (!inBounds(x, y))
        {
            return;
        }
        
        grid[x][y][channel] = clamp01(v);
    }

    /**
     * Adds pheromone to a position (cumulative).
     * Used when ants leave a trail.
     */
    public void addWorld(float wx, float wy, int channel, float delta)
    {
        int x = (int) (wx / resolution);
        int y = (int) (wy / resolution);
        
        if (!inBounds(x, y))
        {
            return;
        }
        
        grid[x][y][channel] = clamp01(grid[x][y][channel] + delta);
    }

    /**
     * Checks if grid coordinates are valid.
     */
    private boolean inBounds(int x, int y)
    {
        return x >= 0 && x < cols && y >= 0 && y < rows;
    }

    /**
     * Constrains the value between 0.0 and 1.0.
     */
    private float clamp01(float v)
    {
        if (v < 0)
        {
            return 0;
        }
        
        return Math.min(v, 1.0f);
    }
}