package antcolony.environment;

import antcolony.AntColonySimulation;
import antcolony.entities.Ant;
import antcolony.entities.FallingLeaf;
import antcolony.entities.StaticTree;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.core.PVector;

/**
 * Environment rendering engine.
 * <p>
 * This class is responsible for drawing all visual layers of the simulation,
 * except for the UI. The drawing order is crucial:
 * Sky -> Stars -> Celestial Bodies -> Background/Ground -> Roots -> Pheromones -> Objects -> Ants.
 * </p>
 */
public class EnvironmentRenderer
{
    /**
     * Procedurally generated texture for the soil (ground).
     * Cached to avoid regeneration every frame.
     */
    private PImage groundTexture;

    /**
     * Forces the regeneration of the soil texture (e.g., if the window is resized).
     */
    public void resetTexture()
    {
        groundTexture = null;
    }

    /**
     * Draws the sky background (solid color that changes with the time of day).
     * @param p PApplet reference.
     * @param sim Simulation reference.
     */
    public void drawSky(PApplet p, AntColonySimulation sim)
    {
        p.noStroke();
        
        int sky = sim.colors.getSkyColor(p, sim);
        p.fill(sky);
        
        // Draws the sky rectangle in the central area (excluding sidebars)
        p.rect(sim.leftSidebarW, 0, p.width - sim.leftSidebarW - sim.rightSidebarW, sim.surfaceY);
        
        drawStarsIfNight(p, sim);
    }

    /**
     * Draws stars if it is nighttime.
     */
    private void drawStarsIfNight(PApplet p, AntColonySimulation sim)
    {
        float h = sim.time.curHour + sim.time.curMin / 60f;
        boolean night = (h >= 20 || h < 6);
        
        if (!night)
        {
            return;
        }
        
        for (Stars s : sim.stars)
        {
            s.display(p);
        }
    }

    /**
     * Draws the Sun and the Moon orbiting the world.
     */
    public void drawCelestialBodies(PApplet p, AntColonySimulation sim)
    {
        // Maps the day progress (0 to 1) to an arc from -90 to 270 degrees
        float a = PApplet.map(sim.time.dayProgress, 0, 1, -PApplet.PI / 2, 3 * PApplet.PI / 2);
        
        float r = (p.width - sim.leftSidebarW - sim.rightSidebarW) * 0.4f;
        float cx = sim.leftSidebarW + (p.width - sim.leftSidebarW - sim.rightSidebarW) / 2f;
        float cy = sim.surfaceY + 200;

        // --- SUN (Opposite to the Moon, offset by PI) ---
        float sx = cx + PApplet.cos(a + PApplet.PI) * r;
        float sy = cy + PApplet.sin(a + PApplet.PI) * r;
        
        // Only draws if it is above the horizon (with a 60px margin)
        if (sy < sim.surfaceY + 60)
        {
            // Aura/Glow
            p.fill(255, 255, 0, 50); 
            p.circle(sx, sy, 60);
            
            // Sun Body
            p.fill(255, 200, 0); 
            p.circle(sx, sy, 30);
        }

        // --- MOON ---
        float mx = cx + PApplet.cos(a) * r;
        float my = cy + PApplet.sin(a) * r;
        
        if (my < sim.surfaceY + 60)
        {
            // Moon Glow
            p.fill(255, 255, 255, 30); 
            p.circle(mx, my, 40);
            
            // Moon Body
            p.fill(240, 240, 255); 
            p.circle(mx, my, 25);
        }
    }

    /**
     * Draws the underground soil, ground texture, and trees (roots and canopy).
     */
    public void drawForestAndGround(PApplet p, AntColonySimulation sim)
    {
        float h = sim.time.curHour + sim.time.curMin / 60f;
        float l;

        // Ground lighting logic (0.0 = Dark, 1.0 = Light)
        if (h >= 6 && h <= 18)
        {
            l = 1f; // Full day
        }
        else if (h > 4 && h < 6)
        {
            l = PApplet.map(h, 4, 6, 0, 1); // Dawn
        }
        else if (h > 18 && h < 20)
        {
            l = PApplet.map(h, 18, 20, 1, 0); // Dusk
        }
        else
        {
            l = 0f; // Night
        }
        
        p.noStroke();
        
        // Interpolates the soil background color (earth)
        int groundColor = p.lerpColor(sim.colors.cGroundNight, sim.colors.cGroundDay, l);
        p.fill(groundColor);
        
        float w = p.width - sim.leftSidebarW - sim.rightSidebarW;
        float hg = p.height - sim.surfaceY;
        
        // Draws the base ground rectangle
        p.rect(sim.leftSidebarW, sim.surfaceY, w, hg);

        // Generates or draws the noise texture (earth)
        if (groundTexture == null || groundTexture.width != (int) w || groundTexture.height != (int) hg)
        {
            generateSoilTexture(p, (int) w, (int) hg);
        }
        
        if (groundTexture != null)
        {
            p.image(groundTexture, sim.leftSidebarW, sim.surfaceY);
        }
        
        // Calculates root color based on lighting
        int cRootNight = p.color(140, 100, 70);
        int cRootDay = p.lerpColor(groundColor, p.color(0), 0.5f);
        int finalRootColor = p.lerpColor(cRootNight, cRootDay, l);
        
        // Draws the forest (Trees and Roots)
        for (StaticTree t : sim.forest)
        {
            int tc = sim.colors.calculateTreeColor(p, sim, t.colorOffset);
            t.display(p, tc, sim.time.curSeasonIdx, finalRootColor);
        }
    }

    /**
     * Generates a Perlin noise texture to simulate earth/soil.
     */
    private void generateSoilTexture(PApplet p, int w, int h)
    {
        groundTexture = p.createImage(w, h, PConstants.ARGB);
        groundTexture.loadPixels();
        
        float noiseScale = 0.02f;
        
        for (int i = 0; i < groundTexture.pixels.length; i++)
        {
            int x = i % w;
            int y = i / w;
            
            float n = p.noise(x * noiseScale, y * noiseScale);
            
            // Maps noise to transparency (Alpha)
            int alpha = (int) PApplet.map(n, 0, 1, 0, 50);
            
            // Adds some random darker grains (stones/debris)
            if (p.random(1) < 0.15f)
            {
                alpha += 10;
            }
            
            groundTexture.pixels[i] = p.color(0, 0, 0, alpha);
        }
        
        groundTexture.updatePixels();
    }

    /**
     * Visualizes the pheromone grid.
     * Draws pixels directly to the screen for maximum performance.
     */
    public void drawPheromones(PApplet p, AntColonySimulation sim)
    {
        p.loadPixels();
        
        for (int x = 0; x < sim.cols; x++)
        {
            int sx = x * sim.resolution;
            
            // Ignores columns outside the visible area (under the sidebars)
            if (sx < sim.leftSidebarW || sx > p.width - sim.rightSidebarW)
            {
                continue;
            }

            for (int y = 0; y < sim.rows; y++)
            {
                int sy = y * sim.resolution;
                
                // Ignores lines above the surface (in the sky)
                if (sy < sim.surfaceY)
                {
                    continue;
                }

                // Checks distance to nests. If less than 25px, skips this pixel.
                boolean underQueenA = PApplet.dist(sx, sy, sim.queenLocA.x, sim.queenLocA.y) < 25;
                boolean underQueenB = PApplet.dist(sx, sy, sim.queenLocB.x, sim.queenLocB.y) < 25;

                if (underQueenA || underQueenB)
                {
                    continue; // Draws nothing here (remains transparent)
                }

                // Gets intensity of the 4 pheromones
                float hA = sim.pheromones.grid[x][y][0]; // Home A
                float fA = sim.pheromones.grid[x][y][1]; // Food A
                float hB = sim.pheromones.grid[x][y][2]; // Home B
                float fB = sim.pheromones.grid[x][y][3]; // Food B

                // If there is any pheromone in this cell
                if (hA > 0.01 || fA > 0.01 || hB > 0.01 || fB > 0.01)
                {
                    float r = 0, g = 0, b = 0, alpha = 0;

                    // Blends Colony A colors (Bluish)
                    if (hA > 0 || fA > 0)
                    {
                        b += 255 * hA + 255 * fA; 
                        g += 200 * fA;            
                        r += 20 * (hA + fA);      
                        alpha += (hA + fA) * 255;
                    }

                    // Blends Colony B colors (Reddish)
                    if (hB > 0 || fB > 0)
                    {
                        r += 255 * hB + 255 * fB; 
                        g += 140 * fB;            
                        b += 20 * (hB + fB);      
                        alpha += (hB + fB) * 255;
                    }
                    
                    // Draws pixel if visible
                    if (alpha > 0)
                    {
                        int c = p.color(Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
                        
                        // Fills resolution block (e.g., 4x4 pixels)
                        for (int px = 0; px < sim.resolution; px++)
                        {
                            for (int py = 0; py < sim.resolution; py++)
                            {
                                int ix = (sx + px) + (sy + py) * p.width;
                                
                                // Pixel array boundary check
                                if (ix >= 0 && ix < p.pixels.length)
                                {
                                    p.pixels[ix] = c;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        p.updatePixels();
    }

    /**
     * Draws the Colony Queen (Base).
     * @param p PApplet reference.
     * @param loc Queen's location.
     * @param type Colony type (0 or 1).
     */
    public void drawQueen(PApplet p, PVector loc, int type)
    {
        float x = loc.x;
        float y = loc.y;
        
        p.pushMatrix();
        p.translate(x, y);
        p.noStroke();
        
        int dark, mid, light;
        
        if (type == 0)
        {
            dark = p.color(50, 40, 30);
            mid = p.color(80, 60, 45);
            light = p.color(100, 80, 60);
        }
        else
        {
            dark = p.color(80, 40, 30);
            mid = p.color(120, 60, 40);
            light = p.color(160, 90, 60);
        }

        // Body drawing (segmented)
        p.fill(dark);
        p.ellipse(0, 0, 90, 50); // Abdomen
        
        p.fill(mid);
        p.ellipse(0, -5, 70, 40); // Thorax
        
        p.fill(light);
        p.ellipse(0, -10, 50, 25); // Head
        
        // Eyes
        p.fill(20, 10, 5);
        p.ellipse(0, -12, 25, 12);
        
        // Antennae
        p.stroke(0, 50);
        p.strokeWeight(1);
        p.line(0, -12, 0, -35);
        
        // Flag / Color indicator
        p.noStroke();
        if (type == 0)
        {
            p.fill(p.color(50, 100, 255));
        }
        else
        {
            p.fill(p.color(255, 50, 50));
        }
        
        p.triangle(0, -35, 15, -28, 0, -22);
        
        p.popMatrix();
    }

    /**
     * Draws all falling leaves.
     */
    public void drawFallingLeaves(PApplet p, AntColonySimulation sim)
    {
        for (FallingLeaf l : sim.fallingLeaves)
        {
            l.display(p);
        }
    }

    /**
     * Draws all ants.
     */
    public void drawAnts(PApplet p, AntColonySimulation sim)
    {
        for (Ant a : sim.ants)
        {
            a.display(p);
        }
    }
}