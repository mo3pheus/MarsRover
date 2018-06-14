package space.exploration.mars.rover.propulsion;

import space.exploration.mars.rover.kernel.Rover;
import space.exploration.mars.rover.learning.ReinforcementLearner;

import java.awt.*;

public class LearningPropulsionUnit extends PropulsionUnit {
    public LearningPropulsionUnit(Rover rover, Point source, Point destination) {
        super(rover, source, destination);
        requestPropulsion();
    }

    @Override
    void requestPropulsion() {
        ReinforcementLearner learningEngine = new ReinforcementLearner(rover.getRoverConfig().getMarsConfig());
        learningEngine.train(source, destination);
        trajectory = learningEngine.getShortestPath();
        computeTrajValidity();
    }
}
