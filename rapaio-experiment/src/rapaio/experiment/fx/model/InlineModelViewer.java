/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.experiment.fx.model;

import java.io.IOException;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import rapaio.experiment.fx.MeshImporter;


public class InlineModelViewer extends Application {
    private static final int VIEWPORT_SIZE = 2048;

    private static final double MODEL_SCALE_FACTOR = 0.1;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 0;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE / 2;

    private PhongMaterial texturedMaterial = new PhongMaterial();

    private MeshView meshView = new MeshView(loadDefaultMesh());

    private Mesh loadDefaultMesh() {
        float[] points = {
                -5, 5, 0,
                -5, -5, 0,
                5, 5, 0,
                5, -5, 0
        };
        float[] texCoords = {
                1, 1,
                1, 0,
                0, 1,
                0, 0
        };
        int[] faces = {
                2, 2, 1, 1, 0, 0,
                2, 2, 3, 3, 1, 1
        };

        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().setAll(points);
        mesh.getTexCoords().setAll(texCoords);
        mesh.getFaces().setAll(faces);
        return mesh;
    }

    private Mesh loadMeshFromFile() {
        try {
            return MeshImporter.loadAsciiStl(getClass().getResourceAsStream("/assets/stl/Sphericon.stl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Group buildScene() {
        meshView.setTranslateX(VIEWPORT_SIZE / 2 + MODEL_X_OFFSET);
        meshView.setTranslateY(VIEWPORT_SIZE / 2 * 9.0 / 16 + MODEL_Y_OFFSET);
        meshView.setTranslateZ(VIEWPORT_SIZE / 2 + MODEL_Z_OFFSET);
        meshView.setScaleX(MODEL_SCALE_FACTOR);
        meshView.setScaleY(MODEL_SCALE_FACTOR);
        meshView.setScaleZ(MODEL_SCALE_FACTOR);

        return new Group(meshView);
    }

    @Override
    public void start(Stage stage) {
        texturedMaterial.setDiffuseMap(new Image(getClass().getResourceAsStream("/assets/textures/stone-marble.jpg")));

        Group group = buildScene();

        RotateTransition rotate = rotate3dGroup(group);

        VBox layout = new VBox(
                createControls(rotate),
                createScene3D(group)
        );

        layout.fillWidthProperty().set(true);
        layout.prefWidthProperty().bind(stage.widthProperty());

        stage.setTitle("Model Viewer");

        Scene scene = new Scene(layout, Color.CORNSILK);
        stage.setScene(scene);
        stage.show();
    }

    private SubScene createScene3D(Group group) {
        SubScene scene3d = new SubScene(group, VIEWPORT_SIZE, VIEWPORT_SIZE * 9.0 / 16, true, SceneAntialiasing.BALANCED);
        scene3d.setFill(Color.rgb(10, 10, 40));
        var camera = new PerspectiveCamera();
        scene3d.setCamera(camera);
        scene3d.setOnScroll(event -> camera.setTranslateZ(camera.getTranslateZ() + event.getDeltaY()));
        return scene3d;
    }

    private HBox createControls(RotateTransition rotateTransition) {
        CheckBox cull = new CheckBox("Cull Back");
        meshView.cullFaceProperty().bind(
                Bindings.when(
                                cull.selectedProperty())
                        .then(CullFace.BACK)
                        .otherwise(CullFace.NONE)
        );
        CheckBox wireframe = new CheckBox("Wireframe");
        meshView.drawModeProperty().bind(
                Bindings.when(wireframe.selectedProperty())
                        .then(DrawMode.LINE)
                        .otherwise(DrawMode.FILL)
        );

        CheckBox rotate = new CheckBox("Rotate");
        rotate.selectedProperty().addListener(observable -> {
            if (rotate.isSelected()) {
                rotateTransition.play();
            } else {
                rotateTransition.pause();
            }
        });

        CheckBox texture = new CheckBox("Texture");
        meshView.setMaterial(texturedMaterial);

        VBox controls = new VBox(10, rotate, texture, cull, wireframe);
        controls.setPadding(new Insets(10));

        Button selectDefault = new Button("Select Default");
        selectDefault.setOnAction(_ -> meshView.setMesh(loadDefaultMesh()));

        Button selectFile = new Button("Select from file");
        selectFile.setOnAction(_ -> meshView.setMesh(loadMeshFromFile()));

        VBox buttons = new VBox(10, selectDefault, selectFile);
        buttons.setPadding(new Insets(10));

        HBox sideControls = new HBox(10, controls, buttons);
        return sideControls;
    }

    private RotateTransition rotate3dGroup(Group group) {
        RotateTransition rotate = new RotateTransition(Duration.seconds(10), group);
        rotate.setAxis(Rotate.Y_AXIS);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.setCycleCount(RotateTransition.INDEFINITE);

        return rotate;
    }

    public static void main(String[] args) {
        System.setProperty("prism.dirtyopts", "false");
        launch(args);
    }
}