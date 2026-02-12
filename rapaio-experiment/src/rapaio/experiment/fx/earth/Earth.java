/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.fx.earth;


import assets.Asset;
import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Cursor;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import rapaio.experiment.fx.BoundingBox;
import rapaio.experiment.fx.MouseControl;

public class Earth extends Application {

    private static final double WIDTH = 2400;
    private static final double HEIGHT = 1200;


    private PerspectiveCamera camera;
    private MouseControl mouseControl;

    private final Sphere sphere = new Sphere(500, 1024);

    private void initialize() {
        camera = new PerspectiveCamera(true);
        camera.translateXProperty().set(0);
        camera.translateYProperty().set(0);
        camera.translateZProperty().set(-1200);
        camera.setFieldOfView(60);
        camera.setNearClip(0.1);
        camera.setFarClip(0);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        initialize();

        PhongMaterial earthMaterial = new PhongMaterial();
        earthMaterial.setDiffuseMap(Asset.image("8k_earth_daymap.jpg"));
//        earthMaterial.setSpecularMap(Asset.image("8k_earth_specular_map.tif"));
//        earthMaterial.setBumpMap(new Image(Earth.class.getResourceAsStream("/assets/textures/earthbump10k.jpg")));
        sphere.setMaterial(earthMaterial);

        Group mainGroup = new Group();

        Group group = new Group();
        group.getChildren().add(new BoundingBox(10_000, 0.01));
        group.getChildren().add(sphere);

        mainGroup.getChildren().add(group);


        Scene scene = new Scene(mainGroup, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);
        scene.setCursor(Cursor.CROSSHAIR);


        AmbientLight ambientLight = new AmbientLight(Color.rgb(64, 64, 64));
        group.getChildren().add(ambientLight);

        DirectionalLight light = new DirectionalLight(Color.WHITE);
        light.setDirection(new Point3D(0, 0, 1));
        mainGroup.getChildren().add(light);


        initMouseControl(scene, group);

        primaryStage.setTitle("Tutorial");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


    private void initMouseControl(Scene scene, Group group) {

        mouseControl = MouseControl.central(scene, group);
        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            camera.setTranslateZ(camera.getTranslateZ() + event.getDeltaY() / 4);
            mouseControl.getSpeedProperty().set(-1 * camera.getTranslateZ() / 5000);
        });
    }

    static void main(String[] args) {
        launch(args);
    }
}
