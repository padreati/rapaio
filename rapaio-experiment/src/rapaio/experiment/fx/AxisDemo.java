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

package rapaio.experiment.fx;

import java.util.Random;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class AxisDemo extends Application {

    static void main(String[] args) {
        launch(args);
    }

    private Camera camera;
    private Scene scene;

    PhongMaterial mkMaterial(Color color) {
        var boxMaterial = new PhongMaterial();
        boxMaterial.setDiffuseColor(color);
        boxMaterial.setSpecularColor(Color.WHITESMOKE);
        return boxMaterial;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Axisdemo");

        int aw = 1;

        var xAxis = new Box(1000, aw, aw);
        xAxis.setMaterial(mkMaterial(Color.GREEN));
//        xAxis.setTranslateX(500);

        var yAxis = new Box(aw, 1000, aw);
        yAxis.setMaterial(mkMaterial(Color.RED));
//        yAxis.setTranslateY(500);

        var zAxis = new Box(aw, aw, 1000);
        zAxis.setMaterial(mkMaterial(Color.BLUE));
//        zAxis.setTranslateZ(500);

        var parent = new Group(xAxis, yAxis, zAxis);
        parent.getTransforms().add(new Scale(1, -1, 1));
        parent.getChildren().addAll(IntStream.range(0, 100).mapToObj(i -> mkRandBox()).toList());

        MeshView tetha = new Tetrahedron3D(10);
        tetha.setMaterial(mkMaterial(Color.BLUE));
        parent.getChildren().add(tetha);

        var root = new Group(parent);
        scene = new Scene(root, 1000, 1000, true, SceneAntialiasing.BALANCED);

        MouseControl.central(scene, root);

        var pointLight = new PointLight(Color.ANTIQUEWHITE);
        pointLight.setTranslateZ(-100);

        root.getChildren().add(pointLight);

        setCamera(scene);
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    Color mkRandColor() {
        Random random = new Random();
        return Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255));
    }

    Box mkRandBox() {
        Random random = new Random();
        var width = random.nextInt(100);
        var height = random.nextInt(100);
        var depth = random.nextInt(100);
        var b = new Box(width, height, depth);
        b.setMaterial(mkMaterial(mkRandColor()));
        b.setTranslateX(random.nextInt(1000 - width));
        b.setTranslateY(random.nextInt(1000 - height));
        b.setTranslateZ(random.nextInt(1000 - depth));
        return b;
    }

    void setCamera(Scene scene) {
        camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-1000);
        camera.setNearClip(0.1);
        camera.setFarClip(100_000);

        scene.setCamera(camera);

        scene.setOnScroll(event -> camera.setTranslateZ(camera.getTranslateZ() + event.getDeltaY()));
    }
}
