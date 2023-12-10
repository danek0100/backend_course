package edu.project4.renderers;

import edu.project4.components.Color;
import edu.project4.components.FractalImage;
import edu.project4.components.Pixel;
import edu.project4.components.Point;
import edu.project4.components.Rect;
import edu.project4.transformations.ColorTransformation;
import edu.project4.transformations.Transformation;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class SingleRenderer implements Renderer {

    private final int symmetry;

    public SingleRenderer(int symmetry) {
        this.symmetry = symmetry;
    }


    @Override
    public FractalImage render(FractalImage canvas, Rect world, List<ColorTransformation> affine,
        List<Transformation> variations, int samples, int iterPerSample, int seed) {
        Random random = new Random(seed);

        for (int i = 0; i < samples; i++) {
            Point pw = randomPoint(world, random);
            System.out.println(i);

            for (short step = 0; step < iterPerSample; ++step) {
                ColorTransformation chosenAffine = affine.get(random.nextInt(affine.size()));
                Transformation variation = variations.get(random.nextInt(variations.size()));

                pw = variation.apply(chosenAffine.transformation().apply(pw));

                if (symmetry > 0) {
                    for (int s = 0; s < symmetry; s++) {
                        double theta = Math.PI * 2 / symmetry * s;
                        Point pwr = rotate(pw, theta);

                        applyChanges(canvas, world, pwr, chosenAffine);
                    }
                } else {
                    applyChanges(canvas, world, pw, chosenAffine);
                }
            }
        }

        return canvas;
    }

    private void applyChanges(FractalImage canvas, Rect world, Point pw, ColorTransformation chosenAffine) {
        if (world.contains(pw)) {
            int canvasX = extension(canvas.width(), world.x(), world.x() + world.width(), pw.x());
            int canvasY = extension(canvas.height(), world.y(), world.y() + world.height(), pw.y());

            if (canvas.contains(canvasX, canvasY)) {
                updatePixel(canvas, chosenAffine, canvasX, canvasY);
            }
        }
    }

    private Point randomPoint(Rect world, Random random) {

        double x = world.x() + (random.nextDouble() * world.width());
        double y = world.y() + (random.nextDouble() * world.height());

        return new Point(x, y);
    }

    private Point rotate(Point point, double theta) {
        double cosTheta = Math.cos(theta);
        double sinTheta = Math.sin(theta);

        double newX = cosTheta * point.x() - sinTheta * point.y();
        double newY = sinTheta * point.x() + cosTheta * point.y();

        return new Point(newX, newY);
    }

    private Color mixColor(Color first, Color second) {
        return new Color(
            (first.r() + second.r()) / 2,
            (first.g() + second.g()) / 2,
            (first.b() + second.b()) / 2
        );
    }

    private int extension(int size, double min, double max, double point) {
        double ratio = (point - min) / (max - min);
        return (int)(ratio * size);
    }

    private void updatePixel(FractalImage canvas, ColorTransformation colorTransformation, int x, int y) {
        Pixel oldPixel = canvas.pixel(x, y);
        Color newColor = mixColor(oldPixel.color(), colorTransformation.color());
        int newHitCount = oldPixel.hitCount() + 1;
        canvas.updatePixel(x, y, new Pixel(newColor, newHitCount));
    }
}
