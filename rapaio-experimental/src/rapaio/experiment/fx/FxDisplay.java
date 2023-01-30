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

package rapaio.fx;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import rapaio.graphics.opt.NColor;

public class FxDisplay extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        boolean is3DSupported = Platform.isSupported(ConditionalFeature.SCENE3D);
        if(!is3DSupported) {
            System.out.println("Sorry, 3D is not supported in JavaFX on this platform.");
            return;
        }

        PhongMaterial boxMaterial = new PhongMaterial(Color.color(
                NColor.tab_orange.getRed()/255.,
                NColor.tab_orange.getGreen()/255.,
                NColor.tab_orange.getBlue()/255.,
                NColor.tab_orange.getAlpha()/255.));

        Box box = new Box(100,100,100);
        box.setCullFace(CullFace.NONE);
        box.setMaterial(boxMaterial);
        box.setTranslateX(250);
        box.setTranslateY(100);
        box.setTranslateZ(400);


        PhongMaterial sphereMaterial = new PhongMaterial(Color.color(
                NColor.tab_purple.getRed()/255.,
                NColor.tab_purple.getGreen()/255.,
                NColor.tab_purple.getBlue()/255.
        ));
        Sphere sphere = new Sphere(100);
        sphere.setCullFace(CullFace.NONE);
        sphere.setMaterial(sphereMaterial);
        sphere.setTranslateX(600);
        sphere.setTranslateY(100);
        sphere.setTranslateZ(400);

        boolean fixedEyeAtCameraZero = false;
        PerspectiveCamera camera = new PerspectiveCamera(fixedEyeAtCameraZero);
        camera.setTranslateX(150);
        camera.setTranslateY(-100);
        camera.setTranslateZ(250);

        Group root = new Group(box, sphere);
        root.setRotationAxis(Rotate.X_AXIS);
        root.setRotate(30);

        Scene scene = new Scene(root, 500, 300, true, SceneAntialiasing.BALANCED);
        scene.setCamera(camera);
        primaryStage.setScene(scene);
        primaryStage.setTitle("3D Example");

        primaryStage.show();

        WritableImage wi = new WritableImage(800, 600);
        scene.snapshot(wi);
        BufferedImage bi = SwingFXUtils.fromFXImage(wi, null);
        ImageIO.write(bi, "png", new File("/home/ati/work/rapaio/src/rapaio/experiment/fx/image.png"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
