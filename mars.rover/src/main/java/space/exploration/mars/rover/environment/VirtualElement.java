package space.exploration.mars.rover.environment;

import java.awt.*;

public abstract class VirtualElement extends MatrixElement {
    private static final long serialVersionUID = 9218386250122842797L;

    public abstract void draw(Graphics2D g2);

    public abstract void build();
}