/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Screen;
import javafx.stage.Stage;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.opt.NColor;

public class SampleScatterPlot extends Application {

    public static final String TV = "TV";
    public static final String Radio = "Radio";
    public static final String Sales = "Sales";

    PhongMaterial material(java.awt.Color color) {
        var boxMaterial = new PhongMaterial();
        boxMaterial.setDiffuseColor(Color.rgb(color.getRed(), color.getGreen(), color.getBlue()));
        boxMaterial.setSpecularColor(Color.WHITESMOKE);
        return boxMaterial;
    }

    PerspectiveCamera addCamera(Scene scene) {
        PerspectiveCamera perspectiveCamera = new PerspectiveCamera(true);
        scene.setCamera(perspectiveCamera);
        return perspectiveCamera;
    }


    double scale = 1;

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

        Frame df = Datasets.loadISLAdvertising();

        int aw = 3;

        Point3D pmin = new Point3D(df.rvar(TV).tensor_().min(), df.rvar(Sales).tensor_().min(), df.rvar(Radio).tensor_().min());
        Point3D pmax = new Point3D(df.rvar(TV).tensor_().max(), df.rvar(Sales).tensor_().max(), df.rvar(Radio).tensor_().max());

        Point3D plen = pmax.subtract(pmin);

        Point3D pmid = pmin.midpoint(pmax);

        Point3D pminBound = pmin.subtract(plen.multiply(0.2));
        Point3D pmaxBound = pmax.add(plen.multiply(0.2));
        Point3D plenBound = pmaxBound.subtract(pminBound);
        Point3D pmidBound = pminBound.midpoint(pmaxBound);

        scale = Math.min(Screen.getPrimary().getBounds().getWidth()/plenBound.getX(),
                Screen.getPrimary().getBounds().getHeight()/plenBound.getY());

        var xAxis = new Box(plen.getX(), aw, aw);
        xAxis.setMaterial(material(NColor.green));
        xAxis.setTranslateX(pmid.getX());

        var yAxis = new Box(aw, plen.getY(), aw);
        yAxis.setMaterial(material(NColor.red));
        yAxis.setTranslateY(pmid.getY());

        var zAxis = new Box(aw, aw, plen.getZ());
        zAxis.setMaterial(material(NColor.blue));
        zAxis.setTranslateZ(pmid.getZ());

        var zero = new Sphere(2);
        zero.setMaterial(material(NColor.black));

        Group root = new Group(zero, xAxis, yAxis, zAxis);
        root.getChildren().addAll(loadPoints(df));


        Scene scene = new Scene(root, plenBound.getX(), plenBound.getY(), true,
                SceneAntialiasing.BALANCED);
        PerspectiveCamera camera = addCamera(scene);
        Translate camPosition = new Translate(pmid.getX(), pmin.getY(), -Math.max(plen.getX(), plen.getY()));
        camera.getTransforms().add(camPosition);
        camera.setFieldOfView(90);
        camera.setNearClip(0.01);
        camera.setFarClip(1e3);

        primaryStage.setScene(scene);
        primaryStage.setTitle("3D Example");

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
                xAngleDelta = (anchorY - event.getSceneY()) / 10;
                xRotation = new Rotate(xAngle + xAngleDelta, pmid.getX(), pmid.getY(), pmid.getZ(), new Point3D(1, 0, 0));
                yAngleDelta = (anchorX - event.getSceneX()) / 10;
                yRotation = new Rotate(yAngle + yAngleDelta, pmid.getX(), pmid.getY(), pmid.getZ(), new Point3D(0, 1, 0));
                camera.getTransforms().clear();
                camera.getTransforms().addAll(camPosition, xRotation, yRotation);
            }
        });

        scene.setOnMouseReleased(event -> {
            xAngle += xAngleDelta;
            yAngle += yAngleDelta;
        });


        primaryStage.show();
    }

    private List<Node> loadPoints(Frame df) {

        List<Node> list = new ArrayList<>();
        for (int i = 0; i < df.rowCount(); i++) {
            PhongMaterial material = material(NColor.steelblue);
            Sphere sphere = new Sphere(3);
            sphere.setMaterial(material);
            sphere.setCullFace(CullFace.BACK);
            sphere.setTranslateX(df.getDouble(i, TV));
            sphere.setTranslateZ(df.getDouble(i, Radio));
            sphere.setTranslateY(df.getDouble(i, Sales));
            list.add(sphere);
        }
        return list;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
