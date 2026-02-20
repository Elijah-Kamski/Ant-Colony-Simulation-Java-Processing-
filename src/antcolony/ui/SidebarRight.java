package antcolony.ui;

import antcolony.AntColonySimulation;
import processing.core.PApplet;

/**
 * Right sidebar panel (Statistics and Graphs).
 * <p>
 * Responsible for real-time data visualization.
 * Displays population counts, food stocks, birth/death rates,
 * a comparative bar chart, and the ant state legend.
 * </p>
 */
public class SidebarRight
{
    private final AntColonySimulation sim;

    /**
     * Right Sidebar Constructor.
     * @param sim Reference to the main simulation for data retrieval.
     */
    public SidebarRight(AntColonySimulation sim)
    {
        this.sim = sim;
    }

    /**
     * Draws the statistics panel.
     * @param p PApplet reference.
     */
    public void draw(PApplet p)
    {
        p.pushMatrix();
        
        // Offset the origin to the top-right corner of the game area
        p.translate(p.width - sim.rightSidebarW, 0);

        // 1. Sidebar Background
        p.fill(40, 40, 45);
        p.noStroke();
        p.rect(0, 0, sim.rightSidebarW, p.height);

        // 2. Main Title
        p.fill(255);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.textSize(20);
        p.text("STATISTICS", 20, 20);
        
        p.stroke(255, 80);
        p.line(15, 55, sim.rightSidebarW - 15, 55);

        // Column Layout Configuration
        float col1 = 20;
        float col2 = sim.rightSidebarW / 2 + 10;
        float yStart = 80;
        float gap = 45; 
        float y = yStart;
        
        // --- BLUE COLUMN (Colony A) ---
        p.fill(100, 150, 255);
        p.textSize(16);
        p.text("BLUE (Native)", col1, y);
        
        y += 35;
        
        // Population calculation using Streams (Java 8+)
        long popA = sim.ants.stream().filter(a -> a.colonyId == 0).count();
        
        drawStat(p, "Population", String.valueOf(popA), col1, y);
        y += gap;
        
        drawStat(p, "Food Stock", String.valueOf(sim.foodStockA), col1, y);
        y += gap;
        
        drawStat(p, "Births/day", String.valueOf(sim.statsA.dailyBirths), col1, y);
        y += gap;
        
        drawStat(p, "Deaths/day", String.valueOf(sim.statsA.dailyDeaths), col1, y);
        
        // --- RED COLUMN (Colony B) ---
        y = yStart; // Reset Y to the top for the second column
        
        p.fill(255, 100, 100);
        p.textSize(16);
        p.text("RED (Invasive)", col2, y);
        
        y += 35;
        
        long popB = sim.ants.stream().filter(a -> a.colonyId == 1).count();
        
        drawStat(p, "Population", String.valueOf(popB), col2, y);
        y += gap;
        
        drawStat(p, "Food Stock", String.valueOf(sim.foodStockB), col2, y);
        y += gap;
        
        drawStat(p, "Births/day", String.valueOf(sim.statsB.dailyBirths), col2, y);
        y += gap;
        
        drawStat(p, "Deaths/day", String.valueOf(sim.statsB.dailyDeaths), col2, y);

        // --- COMPARATIVE CHART ---
        float graphY = 310; 
        
        p.stroke(255, 50);
        p.line(15, graphY - 20, sim.rightSidebarW - 15, graphY - 20);
        
        p.fill(200);
        p.textSize(14);
        p.text("Population Comparison", 20, graphY);
        
        float maxW = sim.rightSidebarW - 40;
        float totalPop = popA + popB;
        float barH = 30; 
        float barY = graphY + 35;
        
        // Draw empty "track" (chart background)
        p.noStroke();
        p.fill(0, 80);
        p.rect(20, barY, maxW, barH, 6);

        if (totalPop == 0)
        {
            // Display message if no ants remain
            p.fill(150);
            p.textSize(12);
            p.textAlign(PApplet.CENTER, PApplet.CENTER);
            p.text("COLONIES EXTINCT", 20 + maxW / 2, barY + barH / 2);
        }
        else
        {
            // Draw proportional bars
            float wA = (popA / (float)totalPop) * maxW;
            float wB = (popB / (float)totalPop) * maxW;
            
            p.textAlign(PApplet.LEFT, PApplet.TOP); 
            
            // Blue Bar (Left)
            if (popA > 0)
            {
                p.noStroke();
                p.fill(100, 150, 255);
                
                // Draw rect with rounded corners only on the left side
                p.rect(20, barY, wA, barH, 6, 0, 0, 6);
                
                p.fill(255);
                p.textSize(12);
                
                // Show percentage only if the bar has enough visual space
                if (wA > 30)
                {
                    p.text(String.format("%.0f%%", (popA / (float)totalPop) * 100), 28, barY + 8);
                }
            }
            
            // Red Bar (Right)
            if (popB > 0)
            {
                p.noStroke();
                p.fill(255, 100, 100);
                
                // Draw rect with rounded corners only on the right side
                p.rect(20 + wA, barY, wB, barH, 0, 6, 6, 0);
                
                p.fill(255);
                p.textSize(12);
                
                if (wB > 30)
                {
                    p.text(String.format("%.0f%%", (popB / (float)totalPop) * 100), 20 + wA + wB - 35, barY + 8);
                }
            }
        }

        // --- BEHAVIOR KEY LEGEND ---
        drawBehaviorLegend(p);

        p.popMatrix();
        
        // Restore global alignment to avoid affecting other components
        p.textAlign(PApplet.LEFT, PApplet.TOP);
    }

    /**
     * Helper method to draw a formatted Label/Value pair.
     * @param p PApplet reference.
     * @param label Statistic name (gray, small).
     * @param value Statistic value (white, large).
     * @param x X position.
     * @param y Y position.
     */
    private void drawStat(PApplet p, String label, String value, float x, float y)
    {
        // Value (large number)
        p.fill(255);
        p.textSize(18);
        p.text(value, x, y + 5);
        
        // Label (small text above)
        p.fill(150);
        p.textSize(11);
        p.text(label, x, y - 12);
    }

    /**
     * Draws the behavior legend at the bottom of the sidebar.
     * @param p PApplet reference for drawing.
     */
    private void drawBehaviorLegend(PApplet p)
    {
        // Position legend at the bottom of the screen
        float y = p.height - 130;
        
        // Separator line
        p.stroke(255, 50);
        p.line(15, y - 10, sim.rightSidebarW - 15, y - 10);
        
        // Section Title
        p.noStroke();
        p.fill(200);
        p.textSize(12);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
        p.text("BEHAVIOR (LETTERS)", 20, y);

        float col1 = 20;
        float row1 = y + 25;
        float row2 = y + 50;
        float row3 = y + 75;

        // --- Left Column ---
        drawLetterKey(p, col1, row1, "W", "Exploration (Wander)");
        drawLetterKey(p, col1, row2, "S", "Tracking (Searching)");
        drawLetterKey(p, col1, row3, "R", "Returning (Return)");
    }

    /**
     * Helper to draw a legend entry (Letter + Description).
     */
    private void drawLetterKey(PApplet p, float x, float y, String letter, String desc)
    {
        // Highlight letter in yellow
        p.fill(255, 220, 0);
        p.textSize(12);
        p.text(letter, x, y);
        
        // Description in gray
        p.fill(160);
        p.textSize(11);
        // Adjust X position of the description so it doesn't overlap the letter
        p.text(desc, x + 15, y + 1);
    }
}