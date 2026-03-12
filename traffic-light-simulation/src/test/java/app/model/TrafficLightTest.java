package app.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrafficLightTest {

    @Test
    void constructor_SetsInitialColour() {
        TrafficLight light = new TrafficLight(TrafficLightColour.RED);
        assertEquals(TrafficLightColour.RED, light.getColour());
    }

    @Test
    void setColour_ChangesColour() {
        TrafficLight light = new TrafficLight(TrafficLightColour.RED);
        
        light.setColour(TrafficLightColour.GREEN);
        assertEquals(TrafficLightColour.GREEN, light.getColour());
        
        light.setColour(TrafficLightColour.YELLOW);
        assertEquals(TrafficLightColour.YELLOW, light.getColour());
    }
}
