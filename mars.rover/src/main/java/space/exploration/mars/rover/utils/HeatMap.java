package space.exploration.mars.rover.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.exploration.mars.rover.learning.RCell;
import space.exploration.mars.rover.learning.ReinforcementLearner;

public class HeatMap {
    private ReinforcementLearner reinforcementLearner = null;
    private Logger               logger               = LoggerFactory.getLogger(HeatMap.class);

    public void updateReinforcementLearner(ReinforcementLearner reinforcementLearner) {
        this.reinforcementLearner = reinforcementLearner;
        renderHeatMap();
        dumpHeatMap();
    }

    public void renderHeatMap() {
        double maxQValue = getQValue(true);
        double minQValue = getQValue(false);

        logger.info(" MaxQValue = " + maxQValue);
        logger.info(" MinQValue = " + minQValue);
    }

    private double getQValue(boolean max) {
        double returnQVal = 0.0d;
        if (max) {
            for (RCell[] rCell : reinforcementLearner.getNavGrid()) {
                for (RCell rCell1 : rCell) {
                    if (rCell1.getqValue() >= returnQVal) {
                        returnQVal = rCell1.getqValue();
                    }
                }
            }
        } else {
            for (RCell[] rCell : reinforcementLearner.getNavGrid()) {
                for (RCell rCell1 : rCell) {
                    if (rCell1.getqValue() <= returnQVal) {
                        returnQVal = rCell1.getqValue();
                    }
                }
            }

        }

        return returnQVal;
    }

    private void dumpHeatMap() {
        for (RCell[] rCell : reinforcementLearner.getNavGrid()) {
            for (RCell rCell1 : rCell) {
                logger.debug(rCell1.getBestAction().toString());
            }
        }
    }
}
