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

package rapaio.experiment.fx.bound;

import javafx.application.Application;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;
import rapaio.experiment.fx.BoundingBox;

public class BoundApp extends Application {

    private Scene scene;
    private Group root;

    @Override
    public void start(Stage stage) throws Exception {

        root = new Group();
        root.getChildren().add(createContent());
        scene = new Scene(root, 1440, 900, true, SceneAntialiasing.BALANCED);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-500);
        camera.setNearClip(0.1);
        camera.setFarClip(1000000.0);
        scene.setCamera(camera);

        stage.setTitle("Bounding Box");
        stage.setScene(scene);
        stage.show();
    }

    private Node createContent() {
        Group group = new Group();

        Sphere sphere = new Sphere(10);
        group.getChildren().add(new BoundingBox(100_000, 0));

        sphere.setMaterial(new PhongMaterial(Color.BLUE));
        group.getChildren().add(sphere);


        AmbientLight ambientLight = new AmbientLight(Color.rgb(64, 64, 64));
        group.getChildren().add(ambientLight);

        return group;
    }


    static void main() {
        launch();
    }
}
