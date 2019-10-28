package test;

import static dezzy.mathocr.RawImageFunctions.constrain;
import static dezzy.mathocr.RawImageFunctions.convolve;
import static dezzy.mathocr.RawImageFunctions.getPixels;
import static dezzy.mathocr.RawImageFunctions.load;
import static dezzy.mathocr.RawImageFunctions.threshold;
import static dezzy.mathocr.RawImageFunctions.resize;
import static dezzy.mathocr.RawImageFunctions.saveGrayscale;
import static dezzy.mathocr.RawImageFunctions.to1D;
import static dezzy.mathocr.RawImageFunctions.to2D;
import static dezzy.mathocr.RawImageFunctions.toGrayscale;

import java.io.IOException;

import dezzy.mathocr.Metrics;
import dezzy.mathocr.RawImageFunctions;
import dezzy.neuronz2.math.constructs.Matrix;

@SuppressWarnings("unused")
public class ContrastTest {
	
	public static void main(String[] args) throws IOException {
		test1();
	}
	
	private static void test1() throws IOException {
		final Matrix robertsCrossX = new Matrix(new double[][] {
			{1, 0},
			{0, -1}
		});
		
		final Matrix robertsCrossY = new Matrix(new double[][] {
			{0, 1},
			{-1, 0}
		});
		
		final var image = load("test/calcimages/4.jpg");
		final int width = image.getWidth();
		final int height = image.getHeight();
		final var pixels = to2D(toGrayscale(getPixels(image)), height, width);		
		
		final var convX = convolve(pixels, robertsCrossX, 0xFF);
		final var convY = convolve(pixels, robertsCrossY, 0xFF);
		
		final var conv = RawImageFunctions.map2(convX, convY, (x, y) -> {
			final int val = (int)Math.sqrt((x * x) + (y * y));
			
			if (val < 0) {
				return 0;
			} else if (val > 255) {
				return 255;
			} else {
				return val;
			}
		});
		
		final var histogram = Metrics.intensityHistogram(conv);
		int threshold = RawImageFunctions.otsuThresholding(histogram);
		//final var contrast = maxContrast
		
		saveGrayscale(conv, "test/contrast/img4-robertscross.png");
	}
	
	private static void test0() throws IOException {
		final Matrix edgeDetectionKernel = new Matrix(new double[][] {
			{-1, -1, -1},
			{-1, 8, -1},
			{-1, -1, -1}
		});
		
		final Matrix bigEdgeDetector = new Matrix(new double[][] {
			{-1, -1, -1, -1, -1},
			{-1, -1, -1, -1, -1},
			{-1, -1, 24, -1, -1},
			{-1, -1, -1, -1, -1},
			{-1, -1, -1, -1, -1}
		});
		
		final var img0 = load("test/calcimages/4.jpg");
		
		//Image is not scaled
		final var scaledImg0 = resize(img0, img0.getWidth(), img0.getHeight());
		
		final var img0Pixels = getPixels(scaledImg0);
		final var img0Grayscale = toGrayscale(img0Pixels);
		final var img0Pixels2D = to2D(img0Grayscale, scaledImg0.getHeight(), scaledImg0.getWidth());
		final var convolvedPixels2D = constrain(convolve(img0Pixels2D, bigEdgeDetector, 0xFF));
		final var convolvedPixels1D = to1D(convolvedPixels2D);
		
		//final var maxContrast = maxContrast(convolvedPixels1D, 127);
		
		
		saveGrayscale(convolvedPixels1D, scaledImg0.getWidth(), scaledImg0.getHeight(), "test/contrast/img4-bigEdgeDetector.png");
		
		final var originalIntensityHistogram = Metrics.intensityHistogram(img0Pixels2D);
		final var originalHistogramImage = Metrics.intensityHistogramImage(originalIntensityHistogram, 500, 500);
		RawImageFunctions.saveGrayscale(getPixels(originalHistogramImage), 500, 500, "test/histograms/img4.png");
		
		final var finalIntensityHistogram = Metrics.intensityHistogram(convolvedPixels2D);
		final var finalHistogramImage = Metrics.intensityHistogramImage(finalIntensityHistogram, 500, 500);
		RawImageFunctions.saveGrayscale(getPixels(finalHistogramImage), 500, 500, "test/histograms/img4-bigEdgeDetector.png");
	}	
}
