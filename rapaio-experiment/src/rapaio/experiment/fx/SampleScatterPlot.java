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

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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

public class SampleScatterPlot extends Application {

    public static final String TV = "TV";
    public static final String Radio = "Radio";
    public static final String Sales = "Sales";

    PhongMaterial material(Color color) {
        var boxMaterial = new PhongMaterial();
        boxMaterial.setDiffuseColor(color);
        boxMaterial.setSpecularColor(Color.BLACK);

        WritableImage img = new WritableImage(1, 1);
        PixelWriter pw = img.getPixelWriter();
        int[] pixels = new int[1];
        pixels[0] = 0xFF7F7F7F;
        pw.setPixels(0, 0, 1, 1, PixelFormat.getIntArgbInstance(), pixels, 0, 1);
        boxMaterial.setSelfIlluminationMap(img);

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

        double aw = 0.1;

        Point3D pmin = new Point3D(df.rvar(TV).darray_().amin(), df.rvar(Sales).darray_().amin(), df.rvar(Radio).darray_().amin());
        Point3D pmax = new Point3D(df.rvar(TV).darray_().amax(), df.rvar(Sales).darray_().amax(), df.rvar(Radio).darray_().amax());

        Point3D psize = pmax.subtract(pmin);

        Point3D pmid = pmin.midpoint(pmax);

        Point3D pminBound = pmin.subtract(psize.multiply(0.2));
        Point3D pmaxBound = pmax.add(psize.multiply(0.2));
        Point3D plenBound = pmaxBound.subtract(pminBound);
        Point3D pmidBound = pminBound.midpoint(pmaxBound);

        scale = Math.min(Screen.getPrimary().getBounds().getWidth() / plenBound.getX(),
                Screen.getPrimary().getBounds().getHeight() / plenBound.getY());

        var xAxis = new Box(psize.getX(), aw, aw);
        xAxis.setMaterial(material(Color.GREEN));
        xAxis.setTranslateX(pmid.getX());

        var yAxis = new Box(aw, psize.getY(), aw);
        yAxis.setMaterial(material(Color.RED));
        yAxis.setTranslateY(pmid.getY());

        var zAxis = new Box(aw, aw, psize.getZ());
        zAxis.setMaterial(material(Color.BLUE));
        zAxis.setTranslateZ(pmid.getZ());

        var zero = new Sphere(1);
        zero.setMaterial(material(Color.BLACK));

        Group root = new Group(zero, xAxis, yAxis, zAxis);
        root.getChildren().add(new BoundingBox(100000, 0.));
        root.getChildren().addAll(loadPoints(df));


        Scene scene = new Scene(root, 1440, 900, true, SceneAntialiasing.BALANCED);

        PerspectiveCamera camera = addCamera(scene);
        Translate camPosition = new Translate(pmid.getX(), pmin.getY(), -Math.max(psize.getX(), psize.getY()));
        camera.getTransforms().add(camPosition);
        camera.setFieldOfView(90);
        camera.setNearClip(0.01);
        camera.setFarClip(1e10);

        primaryStage.setScene(scene);
        primaryStage.setTitle("3D Example");

        MouseControl.central(scene, root);

        scene.setOnScroll(event -> {
            camera.setTranslateZ(camera.getTranslateZ() + event.getDeltaY() / 4);
        });

        primaryStage.show();
    }

    private List<Node> loadPoints(Frame df) {

        List<Node> list = new ArrayList<>();
        for (int i = 0; i < df.rowCount(); i++) {
            PhongMaterial material = material(Color.STEELBLUE);
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
