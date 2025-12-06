package custom.Pack;

import custom.Pack.Emitter.*;
import custom.Pack.FieldPoint.*;
import custom.Pack.Particle.*;
import custom.Pack.ParticleSystem;
import java.util.Vector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser; // Correctly import FileChooser
import javafx.geometry.Insets;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SimulationUI extends Application {
    private static final int WIDTH = 1200; // Total screen width
    private static final int HEIGHT = 800; // Total screen height
    private static final int CONTROL_BOX_WIDTH = 300; // Width of the control box

    private ParticleSystem particleSystem; // Backend system
    private Canvas canvas;
    private Map<Button, File> presetMap = new HashMap<>();


    // Sliders for controlling parameters
    private Slider velocitySlider;
    private Slider spreadSlider;
    private Slider angleSlider;
    private Slider fieldForceSlider;

    private Emitter selectedEmitter; // Currently selected emitter
    private FieldPoint selectedFieldPoint; // Currently selected field point

    private boolean draggingEmitter = false; // Is an emitter being dragged?
    private boolean draggingField = false;  // Is a field being dragged?
    private boolean isPaused = false;
    private boolean isStepMode = false;
    private AnimationTimer timer;
    private VBox sliderContainer;
    private VBox fieldContainer;
    private boolean showVelocityColors = false;

    
    private HBox presetButtonBox; // Box to hold preset buttons

    @Override
    public void start(Stage stage) {
        // Initialize particleSystem and other components
        particleSystem = new ParticleSystem();

        BorderPane root = new BorderPane();
        canvas = new Canvas(WIDTH - CONTROL_BOX_WIDTH, HEIGHT);
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseReleased(this::handleMouseReleased);
        root.setCenter(canvas);

        VBox controlBox = createControls();
        controlBox.setPrefWidth(CONTROL_BOX_WIDTH);
        root.setRight(controlBox);

        // Initialize animation timer as before...
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused || isStepMode) {
                    update();
                    render();
                    isStepMode = false;
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.start();

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Interactive Particle Simulation");
        stage.show();
    }

    
private VBox createControls() {
    System.out.println("createControls");
    VBox controls = new VBox(10);
    controls.setStyle("-fx-background-color: #333333; -fx-padding: 10; -fx-border-color: white; -fx-border-width: 2;");

    //Adds buttons to the control panel for interactive simulation
    Button addEmitterButton = new Button("Add Emitter");
    Button addOscillatingEmitterButton = new Button("Add Oscillating Emitter");
    Button addPulseEmitterButton = new Button("Add Pulse Emitter");        
    Button addFieldAButton = new Button("Add Attractor");
    Button addFieldBButton = new Button("Add Repulsor");
    Button resetButton = new Button("Reset");
    Button toggleGravityButton = new Button("Toggle Gravity");
    Button pauseButton = new Button("Pause/Resume");
    Button stepButton = new Button("Step");
    Button savePreset = new Button("Save Preset");
    Button loadPreset = new Button("Load Preset");
    Button deleteEmitter = new Button("Delete Emitter");
    Button deleteField = new Button("Delete Field");
    Button showVelocity = new Button("Toggle Show Velocity");

    Button clear = new Button("Clear");
        // Sliders
        velocitySlider = createSlider(1, 10, 3, "Particle Velocity");
        spreadSlider = createSlider(0, 2 * 3.16, 1, "Emitter Spread Angle");
        angleSlider = createSlider(0, 2 * 3.16, 0, "Emission Angle");
        fieldForceSlider = createSlider(5, 15, 10, "Field Force");

        // Slider container that will be added when emitter is selected and not being dragged
        sliderContainer = new VBox(10);
        sliderContainer.setStyle("-fx-background-color: #555555; -fx-padding: 10; -fx-border-color: white; -fx-border-width: 1;");
        sliderContainer.getChildren().addAll(
                labeledSlider("Velocity:", velocitySlider),
                labeledSlider("Spread Angle:", spreadSlider),
                labeledSlider("Emission Angle:", angleSlider),
                deleteEmitter
        );
        sliderContainer.setVisible(false); // Hide initially

        fieldContainer = new VBox(10);
        fieldContainer.setStyle("-fx-background-color: #555555; -fx-padding: 10; -fx-border-color: white; -fx-border-width: 1;");
        fieldContainer.getChildren().addAll(
                labeledSlider("Field Strength:", fieldForceSlider),
                deleteField
        );
        fieldContainer.setVisible(false); // Hide initially

    // HBox to hold preset buttons
    presetButtonBox = new HBox(5); // Set spacing between buttons
    presetButtonBox.setPadding(new Insets(0, 0, 0, 0)); // Padding at the top
    presetButtonBox.setStyle("-fx-background-color: #444444; -fx-border-color: white; -fx-border-width: 1;");

    
        addEmitterButton.setOnAction(e -> {
            // Add an emitter at a random position
            particleSystem.addEmitter(randomPosition(), 3.0f, 1.0f, 0.0f, 1.0f);
        });

        addOscillatingEmitterButton.setOnAction(e -> {
            // Add an emitter at a random position
            particleSystem.addOscillatingEmitter(randomPosition(), 3.0f, 1.0f, 0.0f, 1.0f, 50f, 0.01f);
        });

        addPulseEmitterButton.setOnAction(e -> {
            // Add an emitter at a random position
            particleSystem.addPulseEmitter(randomPosition(), 3.0f, 1.0f, 0.0f, 1.0f, 0.002f);
        });        
        addFieldAButton.setOnAction(e -> {
            // Add a field point at a random position
            particleSystem.addFieldPoint(randomPosition(), 10.0f, "A");
        });

        addFieldBButton.setOnAction(e -> {
            // Add a field point at a random position
            particleSystem.addFieldPoint(randomPosition(), 10.0f, "B");
        });
    
        resetButton.setOnAction(e -> {
            // Clear all particles, emitters, and fields
            particleSystem.getParticles().clear();
        });
        clear.setOnAction(e -> {
            // Clear all particles, emitters, and fields
            particleSystem.getParticles().clear();
            particleSystem.getEmitters().clear();
            particleSystem.getFieldPoints().clear();
        });
    
        toggleGravityButton.setOnAction(e -> {
            // Toggle gravity for the particle system
            particleSystem.setGravityEnabled(particleSystem.isGravityEnabled() ^ 1);
        });
    
        pauseButton.setOnAction(e -> {
            isPaused = !isPaused;
        });
    
        stepButton.setOnAction(e -> {
            if (isPaused) {
                isStepMode = true;
            }
        });


        showVelocity.setOnAction(e -> {
            // Logic to toggle velocity-based coloring
            showVelocityColors = !showVelocityColors;
            updateControlBox();
        });


// Add the buttons to the control box
        controls.getChildren().addAll(showVelocity);        
    
        savePreset.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Preset");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Preset Files", "*.txt"));

            // Show save dialog
            File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
            if (file != null) {
                SystemPreset preset = new SystemPreset(particleSystem);
                preset.savePreset(file.getAbsolutePath()); // Save to the chosen file
            }
        });

        loadPreset.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Preset");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Preset Files", "*.txt"));
            File file = fileChooser.showOpenDialog(canvas.getScene().getWindow());
        
            if (file != null) {
                // Create a new ParticleSystem and load the preset file
                particleSystem = new ParticleSystem();
                SystemPreset preset = new SystemPreset(particleSystem);
                preset.loadPreset(file.getAbsolutePath());
        
                // Create a new button for this specific preset
                Button presetButton = new Button(file.getName());
                presetMap.put(presetButton, file); // Store the button-preset association in the map
                // Set an action for the preset button using the HashMap
                presetButton.setOnAction(event -> {
                    // Clear the current particle system
                    particleSystem = new ParticleSystem();
                    
                    // Retrieve and load the preset associated with this button
                    SystemPreset preset2=new SystemPreset(particleSystem);
                    File currentFile = presetMap.get(presetButton); // Retrieve the preset from the map
                    if (currentFile!=null) {
                        // Load the preset into the new particle system
                        preset2.loadPreset(currentFile.getAbsolutePath());

                    }
                });
                // Add button to presetButtonBox
                presetButtonBox.getChildren().add(presetButton);
            }
        });


        deleteEmitter.setOnAction(e -> {
            if(selectedEmitter != null)
            {
                particleSystem.getEmitters().remove(selectedEmitter);
                selectedEmitter = null;
                sliderContainer.setVisible(false);
            }
        });

        deleteField.setOnAction(e -> {
            if(selectedFieldPoint != null)
            {
                particleSystem.getFieldPoints().remove(selectedFieldPoint);
                selectedFieldPoint = null;
                fieldContainer.setVisible(false);
            }
        });
    
        // Add controls and conditionally add sliders
        controls.getChildren().addAll(
            new Label("Controls:"),
            addEmitterButton, addOscillatingEmitterButton, addPulseEmitterButton, addFieldAButton, addFieldBButton, resetButton,
            toggleGravityButton, pauseButton, stepButton, savePreset, loadPreset, clear
        );
        controls.getChildren().add(sliderContainer);
        controls.getChildren().add(fieldContainer);
    
    
    controls.getChildren().add(presetButtonBox); // Add the HBox at the bottom of the control panel

    return controls;
    }

    private HBox labeledSlider(String labelText, Slider slider) {
        Label label = new Label(labelText);
        label.setTextFill(Color.WHITE);
        HBox hbox = new HBox(10, label, slider);
        return hbox;
    }

    private Slider createSlider(double min, double max, double value, String tooltip) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickMarks(true);
        slider.setShowTickLabels(true);
        slider.setBlockIncrement(1);
        slider.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        return slider;
    }

    private Vector<Float> randomPosition() {
        Vector<Float> position = new Vector<>();
        position.add((float) (Math.random() * (WIDTH - CONTROL_BOX_WIDTH)));  // X-coordinate
        position.add((float) (Math.random() * HEIGHT)); // Y-coordinate
        return position;
    }

    private void handleMousePressed(MouseEvent e) {
        float x = (float) e.getX();
        float y = (float) e.getY();
    
        if (x > WIDTH - CONTROL_BOX_WIDTH) return; // Ignore clicks in the control box
    
        // Check for emitters near the click
        for (Emitter emitter : particleSystem.getEmitters()) {
            Vector<Float> position = emitter.getPosition();
            if (isNear(x, y, position)) {
                selectedEmitter = emitter;
                draggingEmitter = true;
                // Update control panel to show sliders
                //set slider values
                velocitySlider.setValue(emitter.getSpeed());
                spreadSlider.setValue(emitter.getSpread());
                angleSlider.setValue(emitter.getAngle());
                updateControlBox();
                return;
            }
        }
    
        // Check for field points near the click
        for (FieldPoint fieldPoint : particleSystem.getFieldPoints()) {
            Vector<Float> position = fieldPoint.getPosition();
            if (isNear(x, y, position)) {
                selectedFieldPoint = fieldPoint;
                draggingField = true;
                fieldForceSlider.setValue(fieldPoint.getFieldStrength());
                updateControlBox();
                return;
            }
        }
    }

    private void handleMouseDragged(MouseEvent e) {
        float x = (float) e.getX();
        float y = (float) e.getY();
    
        // Ensure emitter stays within bounds (not under the control box)
        if (draggingEmitter && selectedEmitter != null) {
            x = Math.max(0, Math.min(x, WIDTH - CONTROL_BOX_WIDTH)); // X bound check
            y = Math.max(0, Math.min(y, HEIGHT)); // Y bound check
            selectedEmitter.getPosition().set(0, x);
            selectedEmitter.getPosition().set(1, y);
        }
    
        // Ensure field stays within bounds
        if (draggingField && selectedFieldPoint != null) {
            x = Math.max(0, Math.min(x, WIDTH - CONTROL_BOX_WIDTH)); // X bound check
            y = Math.max(0, Math.min(y, HEIGHT)); // Y bound check
            selectedFieldPoint.getPosition().set(0, x);
            selectedFieldPoint.getPosition().set(1, y);
        }
    }

    private void handleMouseReleased(MouseEvent e) {
        draggingEmitter = false;
        draggingField = false;
    }
    
    // Update the control box UI when emitter is selected or dragged
    private void updateControlBox() {
        System.out.println("Update control box");
        VBox controlBox = (VBox) ((BorderPane) canvas.getScene().getRoot()).getRight();
        
        if (selectedEmitter != null) {
            sliderContainer.setVisible(true);
        } else {
            sliderContainer.setVisible(false);
        }
    
        if (selectedFieldPoint != null) {
            fieldContainer.setVisible(true);
        } else {
            fieldContainer.setVisible(false);
        }
    
        // Optionally, toggle button visibility if necessary
    }


    
    

    private boolean isNear(float x, float y, Vector<Float> position) {
        float dx = x - position.get(0);
        float dy = y - position.get(1);
        return (dx * dx + dy * dy) <= 100; // Within 10px radius
    }

    private void update() {
        // Update particle positions in the backend
        particleSystem.removeParticlesOutOfScreen(WIDTH-CONTROL_BOX_WIDTH, HEIGHT);
        float angle = (float) angleSlider.getValue();
        float spread = (float) spreadSlider.getValue();
        float speed = (float) velocitySlider.getValue();
        if (selectedEmitter != null) {
            selectedEmitter.setAngle(angle);
            selectedEmitter.setSpread(spread);
            selectedEmitter.setSpeed(speed);
        }
        float forceField = (float) fieldForceSlider.getValue();
        if (selectedFieldPoint != null) {
            selectedFieldPoint.setFieldStrength(forceField);
        }
        particleSystem.setForces();
        particleSystem.updateAll();

    }
    
    
    private Color interpolateColor(Color startColor, Color endColor, double factor) {
        double red = startColor.getRed() + factor * (endColor.getRed() - startColor.getRed());
        double green = startColor.getGreen() + factor * (endColor.getGreen() - startColor.getGreen());
        double blue = startColor.getBlue() + factor * (endColor.getBlue() - startColor.getBlue());
        return new Color(red, green, blue, 1.0); // Full opacity
    }
    
    private void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK); // Black background-03
        gc.fillRect(0, 0, WIDTH - CONTROL_BOX_WIDTH, HEIGHT);
    
        // Draw particles
        List<Particle> particles = particleSystem.getParticles();
    
        for (Particle particle : particles) {
            // Default color for particles
            Color particleColor = Color.GREY; 
    
            if (showVelocityColors) {
                double velocity = Math.sqrt(
                    Math.pow(particle.getVelocity().get(0), 2) +
                    Math.pow(particle.getVelocity().get(1), 2)
                );
                double maxVelocity = 20.0; // Adjust based on simulation's max velocity
    
                // Interpolation factor (between 0 and 1)
                double factor = Math.min(velocity / maxVelocity, 1.0);
    
                // Define the color based on the velocity
                particleColor = interpolateColor(
                    Color.rgb(173, 216, 230), // Light Blue
                    Color.rgb(0, 0, 139),    // Dark Blue
                    factor
                );
            }
    
            gc.setFill(particleColor);
            gc.fillOval(
                particle.getPosition().get(0),
                particle.getPosition().get(1),
                3, 3
            );
        }
    
        // Draw emitters
        gc.setFill(Color.GREEN);
        for (Emitter emitter : particleSystem.getEmitters()) {
            Vector<Float> position = emitter.getPosition();
            if (selectedEmitter != null && selectedEmitter.equals(emitter)) {
                gc.fillOval(position.get(0), position.get(1), 10, 10);
                gc.setStroke(Color.YELLOW); // Highlight selected emitter
                gc.setLineWidth(2);
                gc.strokeOval(position.get(0) - 2, position.get(1) - 2, 14, 14);
            } else {
                gc.fillOval(position.get(0), position.get(1), 10, 10);
            }
        }
    
        // Draw field points
        for (FieldPoint fieldPoint : particleSystem.getFieldPoints()) {
            Vector<Float> position = fieldPoint.getPosition();
            Color fieldColor = Color.GRAY; // Default color
    
            // Assign color based on field point type
            if ("A".equals(fieldPoint.getType())) {
                fieldColor = Color.CYAN; // Type A is Blue
            } else if ("B".equals(fieldPoint.getType())) {
                fieldColor = Color.RED; // Type B is Red
            }
    
            gc.setFill(fieldColor);
            if (selectedFieldPoint != null && selectedFieldPoint.equals(fieldPoint)) {
                gc.fillOval(position.get(0), position.get(1), 10, 10);
                gc.setStroke(Color.YELLOW); // Highlight selected field point
                gc.setLineWidth(2);
                gc.strokeOval(position.get(0) - 2, position.get(1) - 2, 14, 14);
            } else {
                gc.fillOval(position.get(0), position.get(1), 10, 10);
            }
        }
    }
    

    public static void main(String[] args) {
        launch(args);
    }
}