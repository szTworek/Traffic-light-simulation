package app.model;

public class TrafficLight {
    private TrafficLightColour colour;

    public TrafficLight(TrafficLightColour colour) {
        this.colour = colour;
    }

    public TrafficLightColour getColour() {
        return colour;
    }

    public void setColour(TrafficLightColour colour) {
        this.colour = colour;
    }
}
