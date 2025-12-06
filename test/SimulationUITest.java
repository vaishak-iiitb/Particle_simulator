import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import custom.Pack.SimulationUI;
import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class SimulationUITest {

    private SimulationUI app;

    @Start
    public void start(Stage stage) {
        app = new SimulationUI();
        app.start(stage);
    }

    @Test
    public void testAddEmitterButton(FxRobot robot) {        
        robot.clickOn("Add Emitter");
    }

    @Test
    public void testClearButton(FxRobot robot) {
        robot.clickOn("Add Emitter");
        robot.clickOn("Add Attractor");

        robot.clickOn("Clear");
    }
    
    @Test
    public void testGravityToggle(FxRobot robot) {
        robot.clickOn("Toggle Gravity");
        robot.clickOn("Toggle Gravity");
    }
}