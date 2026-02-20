package antcolony.ui;

import processing.core.PApplet;
import java.util.Locale;

/**
 * Graphical User Interface (UI) component for adjusting values.
 * <p>
 * A horizontal slider that displays values in real physical units 
 * (e.g., nrg/s, f/s, seconds) instead of generic percentages.
 * </p>
 */
public class SimpleSlider
{
    // --- Positioning and Dimension Properties ---
    public float x;
    public float y;
    public float w;
    public float h;

    // --- Value Properties ---
    public float min;
    public float max;
    public float value;

    // --- State and Text ---
    public String label;
    public boolean dragging;

    /**
     * Slider Constructor.
     * @param x X position.
     * @param y Y position.
     * @param w Width.
     * @param h Height.
     * @param min Minimum value.
     * @param max Maximum value.
     * @param val Initial value.
     * @param l Slider label.
     */
    public SimpleSlider(float x, float y, float w, float h, float min, float max, float val, String l)
    {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        
        this.min = min;
        this.max = max;
        this.value = val;
        
        this.label = l;
    }

    /**
     * Updates interaction logic (mouse input).
     */
    public void update(int mx, int my, boolean pressed)
    {
        if (pressed)
        {
            boolean over = (mx >= x && mx <= x + w && my >= y && my <= y + h);
            if (over || dragging)
            {
                dragging = true;
                float rawVal = PApplet.map(mx, x, x + w, min, max);
                value = PApplet.constrain(rawVal, min, max);
            }
        }
        else
        {
            dragging = false;
        }
    }

    /**
     * Helper to calculate the percentage (0-100%) relative to the range.
     * Kept as a fallback for unknown labels.
     */
    private int percentByRuleOf3()
    {
        if (max == min) return 0;
        float norm = (value - min) / (max - min);
        return Math.round(PApplet.constrain(norm, 0, 1) * 100f);
    }

    /**
     * Draws the slider on the screen with real unit formatting.
     */
    public void display(PApplet p)
    {
        // 1. Draw the Label
        p.fill(255);
        p.textSize(11);
        p.textAlign(PApplet.LEFT, PApplet.BOTTOM);
        p.text(label, x, y - 4);

        // 2. Slider Background
        p.noStroke();
        p.fill(0, 100);
        p.rect(x, y, w, h, 5);

        // 3. Progress Bar
        float k = PApplet.map(value, min, max, x, x + w);
        p.fill(100, 150, 255);
        p.rect(x, y, k - x, h, 5);

        // 4. Handle (Knob)
        p.fill(255);
        p.ellipse(k, y + h / 2, h + 4, h + 4);

        // 5. Formatted Value with Real Units
        p.fill(255);
        p.textAlign(PApplet.RIGHT, PApplet.BOTTOM);

        String lower;
        if (label == null)
        {
            lower = "";
        }
        else
        {
            lower = label.toLowerCase();
        }
        String valStr;

        // --- REAL UNIT LOGIC ---
        if (lower.contains("aceleração tempo") || lower.contains("time acceleration")) 
        {
            valStr = String.format(Locale.US, "%.0fx", value);
        } 
        else if (lower.contains("custo spawn") || lower.contains("spawn cost")) 
        {
            valStr = String.format(Locale.US, "%.0f leaves", value);
        } 
        else if (lower.contains("queda folhas") || lower.contains("leaf fall")) 
        {
            // Converts probability per frame to leaves per second (assuming 60fps)
            valStr = String.format(Locale.US, "%.1f f/s", value * 60);
        } 
        else if (lower.contains("metabolismo") || lower.contains("metabolism")) 
        {
            // Shows energy cost per second
            valStr = String.format(Locale.US, "%.1f nrg/s", value * 60);
        } 
        else if (lower.contains("memória") || lower.contains("memory")) 
        {
            // Calculates temporal persistence based on evaporation rate
            if (value >= 0.999f)
            {
                valStr = "Persistent";
            }
            else
            {
                // Estimation of time until significant dissipation
                float persistenceS = (1.0f / (1.0f - value)) / 60.0f;
                valStr = String.format(Locale.US, "%.1fs", persistenceS);
            }
        } 
        else 
        {
            // Fallback: Percentage for other cases
            valStr = percentByRuleOf3() + "%";
        }

        p.text(valStr, x + w, y - 4);
        p.textAlign(PApplet.LEFT, PApplet.TOP);
    }
}