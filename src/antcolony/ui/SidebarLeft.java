package antcolony.ui;

import antcolony.AntColonySimulation;
import processing.core.PApplet;

/**
 * Left side panel (Controls and Legend).
 * <p>
 * Responsible for managing real-time configuration Sliders,
 * displaying current date/time, and showing the pheromone legend.
 * It also handles the Pause and Restart buttons with robust click logic.
 * </p>
 */
public class SidebarLeft
{
    private final AntColonySimulation sim;
    
    // --- Interaction State ---
    
    /** Stores the mouse state of the previous frame to detect single click events. */
    private boolean wasMousePressed = false;

    // --- Control Sliders ---
    public SimpleSlider speedSlider;
    public SimpleSlider leafSlider;
    
    public SimpleSlider metaASlider;
    public SimpleSlider costASlider;
    public SimpleSlider evapASlider;
    
    public SimpleSlider metaBSlider;
    public SimpleSlider costBSlider;
    public SimpleSlider evapBSlider;
    
    // Vertical positions for grouping sliders
    private float yGlobal;
    private float yGroupA;
    private float yGroupB;

    /**
     * Left Sidebar Constructor.
     * @param sim Reference to the main simulation instance.
     */
    public SidebarLeft(AntColonySimulation sim)
    {
        this.sim = sim;
        setupSliders();
    }

    /**
     * Initializes and positions all sliders in organized groups.
     */
    private void setupSliders()
    {
        int x = 25;
        int gap = 35;       // Space between sliders in the same group
        int groupGap = 80;  // Extra space between groups (Global, Blue, Red)
        int w = 230;

        // Base position (first global slider)
        yGlobal = 146;
        
        yGroupA = (yGlobal + gap) + groupGap;
        yGroupB = (yGroupA + gap * 2) + groupGap;
        
        // --- Global Group ---
        speedSlider = new SimpleSlider(x, yGlobal, w, 14, 1, 10, 1, "Time Acceleration");
        leafSlider  = new SimpleSlider(x, yGlobal + gap, w, 14, 0.001f, 0.05f, 0.042f, "Global Leaf Fall Rate");
        
        // --- Blue Colony Group (Native) ---
        metaASlider = new SimpleSlider(x, yGroupA, w, 14, 0.1f, 2.0f, 0.167f, "BLUE Metabolism");
        costASlider = new SimpleSlider(x, yGroupA + gap, w, 14, 1, 20, 4, "BLUE Spawn Cost");
        evapASlider = new SimpleSlider(x, yGroupA + gap * 2, w, 14, 0.900f, 0.999f, 0.995f, "BLUE Memory");
        
        // --- Red Colony Group (Invasive) ---
        metaBSlider = new SimpleSlider(x, yGroupB, w, 14, 0.1f, 2.0f, 0.334f, "RED Metabolism");
        costBSlider = new SimpleSlider(x, yGroupB + gap, w, 14, 1, 20, 3, "RED Spawn Cost");
        evapBSlider = new SimpleSlider(x, yGroupB + gap * 2, w, 14, 0.900f, 0.999f, 0.995f, "RED Memory");
    }

    /**
     * Updates interaction logic for all sliders.
     * @param mx Mouse X coordinate.
     * @param my Mouse Y coordinate.
     * @param pressed Whether the mouse is currently pressed.
     */
    public void update(int mx, int my, boolean pressed)
    {
        speedSlider.update(mx, my, pressed);
        leafSlider.update(mx, my, pressed);
        
        metaASlider.update(mx, my, pressed);
        costASlider.update(mx, my, pressed);
        evapASlider.update(mx, my, pressed);
        
        metaBSlider.update(mx, my, pressed);
        costBSlider.update(mx, my, pressed);
        evapBSlider.update(mx, my, pressed);
    }

    /**
     * Checks if the RESET button was clicked.
     * @param mx Mouse X coordinate.
     * @param my Mouse Y coordinate.
     * @return true if mouse is within reset button bounds.
     */
    public boolean isResetHit(int mx, int my)
    {
        return mx >= sim.btnResetX && mx <= sim.btnResetX + sim.btnResetW
            && my >= sim.btnResetY && my <= sim.btnResetY + sim.btnResetH;
    }

    /**
     * Draws the entire sidebar content.
     */
    public void draw(PApplet p)
    {
        // Dark sidebar background
        p.fill(30, 30, 35);
        p.noStroke();
        p.rect(0, 0, sim.leftSidebarW, p.height);

        drawHeader(p);
        drawPauseButton(p);
        drawResetButton(p);
        drawLegend(p);

        // --- DRAW GROUP TITLES ---
        p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
        p.textSize(14); 

        // Global Group Label
        p.fill(220);
        p.text("GLOBAL CONTROL", 25, yGlobal - 24);

        // Blue Colony Label
        p.fill(100, 150, 255);
        p.text("BLUE COLONY (Native)", 25, yGroupA - 24);
        
        // Red Colony Label
        p.fill(255, 100, 100);
        p.text("RED COLONY (Invasive)", 25, yGroupB - 24);

        // --- DRAW SLIDERS ---
        speedSlider.display(p);
        leafSlider.display(p);
        
        metaASlider.display(p);
        costASlider.display(p);
        evapASlider.display(p);
        
        metaBSlider.display(p);
        costBSlider.display(p);
        evapBSlider.display(p);

        // Capture mouse state for edge detection in the next frame
        wasMousePressed = p.mousePressed;
    }

    /**
     * Draws the Pause/Resume button with robust click detection.
     */
    private void drawPauseButton(PApplet p)
    {
        float x = sim.btnResetX;
        float w = sim.btnResetW;
        float h = sim.btnResetH;
        float y = sim.btnResetY - 45; // Fixed offset above Reset button

        // Bounds detection
        boolean hover = p.mouseX >= x && p.mouseX <= x + w && p.mouseY >= y && p.mouseY <= y + h;

        // Click logic: Trigger only on the first frame of a mouse press
        if (p.mousePressed == true && wasMousePressed == false && hover == true)
        {
            if (sim.isPaused == true)
            {
                sim.isPaused = false;
            }
            else
            {
                sim.isPaused = true;
            }
        }

        p.noStroke();
        if (hover == true)
        {
            p.fill(80);
        }
        else
        {
            p.fill(60);
        }
        
        p.rect(x, y, w, h, 6);
        
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.textSize(14);

        // Visual feedback based on pause state
        if (sim.isPaused == true)
        {
            p.fill(255, 220, 0); // Highlight yellow text when paused
            p.text("RESUME SIMULATION", x + w / 2, y + h / 2 - 2);
        }
        else
        {
            p.fill(255);
            p.text("PAUSE SIMULATION", x + w / 2, y + h / 2 - 2);
        }
    }

    /**
     * Draws the header with Day, Time, and Seasonal info.
     */
    private void drawHeader(PApplet p)
    {
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        float topY = 24;
        
        // Day and Time
        p.textSize(26);
        p.fill(255, 220, 0);
        p.text("DAY " + sim.time.curDay, 25, topY);
        
        float dayWidth = p.textWidth("DAY " + sim.time.curDay);
        
        p.fill(200);
        p.text(String.format("%02d:%02d", sim.time.curHour, sim.time.curMin), 25 + dayWidth + 15, topY);
        
        // Season information
        p.textSize(14);
        p.fill(150);
        
        String season = sim.time.seasonNames[sim.time.curSeasonIdx].toUpperCase();
        
        float mod = 0.2f;
        if (sim.time.curSeasonIdx == 2)
        {
            mod = 1.5f; // Autumn modifier
        }
        else if (sim.time.curSeasonIdx == 3)
        {
            mod = 0.05f; // Winter modifier
        }
        
        p.text(season + " (Rate: " + String.format("%.2f", mod) + "x)", 25, topY + 32);
        
        // Aesthetic separator line
        p.stroke(255, 30);
        p.line(20, topY + 60, sim.leftSidebarW - 20, topY + 60);
    }

    /**
     * Draws the Restart Simulation button.
     */
    private void drawResetButton(PApplet p)
    {
        float rx = sim.btnResetX;
        float ry = sim.btnResetY;
        float rw = sim.btnResetW;
        float rh = sim.btnResetH;
        
        boolean hover = p.mouseX >= rx && p.mouseX <= rx + rw && p.mouseY >= ry && p.mouseY <= ry + rh;
        
        p.noStroke();
        if (hover == true)
        {
            p.fill(200, 50, 50); // Red highlight
        }
        else
        {
            p.fill(150, 40, 40); // Dark red
        }
        
        p.rect(rx, ry, rw, rh, 6);
        
        p.fill(255);
        p.textSize(14);
        p.textAlign(PApplet.CENTER, PApplet.CENTER);
        p.text("RESTART SIMULATION", rx + rw / 2, ry + rh / 2 - 2);
    }

    /**
     * Draws the pheromone color legend at the bottom of the panel.
     */
    private void drawLegend(PApplet p)
    {
        // Positioned height - 175 for optimal spacing between UI elements
        float ly = p.height - 175; 
        
        p.textAlign(PApplet.LEFT, PApplet.CENTER);
        p.stroke(255, 50);
        p.line(20, ly - 10, sim.leftSidebarW - 20, ly - 10);
        
        p.noStroke();
        p.fill(200);
        p.textSize(12);
        p.text("PHEROMONE LEGEND", 25, ly);

        // Pheromone Color Swatches
        drawColorBox(p, 30, ly + 20, p.color(40, 90, 220), "Home (A)");
        drawColorBox(p, 140, ly + 20, p.color(100, 220, 255), "Food (A)");
        
        drawColorBox(p, 30, ly + 40, p.color(220, 40, 40), "Home (B)");
        drawColorBox(p, 140, ly + 40, p.color(255, 160, 40), "Food (B)"); 
    }

    /**
     * Helper to draw a color swatch with a text label.
     */
    private void drawColorBox(PApplet p, float x, float y, int c, String label)
    {
        p.fill(c);
        p.rect(x, y + 2, 10, 10);
        
        p.fill(180);
        p.textSize(11);
        p.text(label, x + 15, y + 6);
    }
}