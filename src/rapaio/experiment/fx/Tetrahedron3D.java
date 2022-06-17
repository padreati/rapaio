package rapaio.experiment.fx;

import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public class Tetrahedron3D extends MeshView {

    private final float[] points = {
            0f, 0f, 1f,
            0f, 1f, 0f,
            1f, 0f, 0f,
            0f, -1f, 0f,
            -1f, 0f, 0f,
            0f, 0f, -1f};
    private final float[] tex = {
            0.571f, 0.27f,
            0.143f, 0.508f,
            0.429f, 0.508f,
            0.714f, 0.508f,
            1f, 0.508f,
            0f, 0.7567f,
            0.286f, 0.7567f,
            0.571f, 0.7567f,
            0.857f, 0.7567f,
            0.429f, 1};
    private final int[] faces = {
            0, 0, 3, 2, 2, 3,
            0, 2, 2, 7, 1, 3,
            0, 2, 4, 6, 3, 7,
            0, 2, 1, 1, 4, 6,
            1, 1, 2, 5, 5, 6,
            2, 6, 3, 9, 5, 7,
            3, 7, 4, 8, 5, 3,
            4, 3, 1, 8, 5, 4};

    public Tetrahedron3D(double len) {
        float[] adjustedPoints = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            adjustedPoints[i] = (float) (points[i] * len);
        }
        TriangleMesh mesh = new TriangleMesh();
        mesh.getPoints().addAll(adjustedPoints);
        mesh.getTexCoords().addAll(tex);
        mesh.getFaces().addAll(faces);

        setMesh(mesh);
    }


}
