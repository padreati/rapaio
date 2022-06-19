/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.fx;

import java.util.Random;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public class AxisDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    PerspectiveCamera addCamera(Scene scene) {
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(false);
        scene.setCamera(perspectiveCamera);
        return perspectiveCamera;
    }

    PhongMaterial mkMaterial(Color color) {
        var boxMaterial = new PhongMaterial();
        boxMaterial.setDiffuseColor(color);
        boxMaterial.setSpecularColor(Color.WHITESMOKE);
        return boxMaterial;
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

    double anchorX;
    double anchorY;

    double xAngle = 0;
    double yAngle = 0;
    double xAngleDelta = 0;
    double yAngleDelta = 0;
    Rotate xRotation = new Rotate(xAngle, new Point3D(1, 0, 0));
    Rotate yRotation = new Rotate(yAngle, new Point3D(0, 1, 0));

    Mode mode = Mode.NONE;

    private enum Mode {
        NONE,
        ROTATE
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Axisdemo");

        int aw = 3;

        var xAxis = new Box(1000, aw, aw);
        xAxis.setMaterial(mkMaterial(Color.GREEN));
        xAxis.setTranslateX(500);

        var yAxis = new Box(aw, 1000, aw);
        yAxis.setMaterial(mkMaterial(Color.RED));
        yAxis.setTranslateY(500);

        var zAxis = new Box(aw, aw, 1000);
        zAxis.setMaterial(mkMaterial(Color.BLUE));
        zAxis.setTranslateZ(500);

        var zero = new Sphere(10);
        zero.setMaterial(mkMaterial(Color.BLACK));

        var parent = new Group(zero, xAxis, yAxis, zAxis);
        parent.setTranslateZ(1000);
//        parent.getChildren().addAll(IntStream.range(0, 100).mapToObj(i -> mkRandBox()).toList());


        MeshView tetha = new Tetrahedron3D(200);
        tetha.setMaterial(mkMaterial(Color.BLUE));
//        tetha.setTranslateX(500);
//        tetha.setTranslateY(500);
//        tetha.setTranslateZ(500);
        parent.getChildren().add(tetha);

        var root = new Group(parent);
        var scene = new Scene(root, 1000, 1000, true, SceneAntialiasing.BALANCED);

        scene.setOnKeyPressed(event -> mode = (event.getCode() == KeyCode.CONTROL) ? Mode.ROTATE : Mode.NONE);

        scene.setOnKeyReleased(event -> mode = Mode.NONE);

        scene.setOnMousePressed(event -> {

            anchorX = event.getSceneX();
            anchorY = event.getSceneY();

            xAngleDelta = 0;
            yAngleDelta = 0;
        });

        scene.setOnMouseDragged(event -> {
            if (mode == Mode.ROTATE) {
                xAngleDelta = -(anchorY - event.getSceneY()) / 10;
                xRotation = new Rotate(xAngle + xAngleDelta, 500, 500, 500, new Point3D(1, 0, 0));
                yAngleDelta = (anchorX - event.getSceneX()) / 10;
                yRotation = new Rotate(yAngle + yAngleDelta, 500, 500, 500, new Point3D(0, 1, 0));
                parent.getTransforms().clear();
                parent.getTransforms().addAll(xRotation, yRotation);
            }
        });

        scene.setOnMouseReleased(event -> {
            xAngle += xAngleDelta;
            yAngle += yAngleDelta;
        });

        var pointLight = new PointLight(Color.ANTIQUEWHITE);
        pointLight.setTranslateX(15);
        pointLight.setTranslateY(-10);
        pointLight.setTranslateZ(-100);

        root.getChildren().add(pointLight);

        addCamera(scene);
        primaryStage.setScene(scene);
        primaryStage.show();

    }
}
