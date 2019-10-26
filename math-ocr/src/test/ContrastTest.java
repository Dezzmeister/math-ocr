package test;

import java.io.IOException;

import dezzy.mathocr.RawImageFunctions;
import dezzy.neuronz2.math.constructs.Matrix;

public class ContrastTest {
	
	public static void main(String[] args) throws IOException {
		
		final Matrix edgeDetectionKernel = new Matrix(new double[][] {
			{-1, -1, -1},
			{-1, 8, -1},
			{-1, -1, -1}
		});
		
		final var img0 = RawImageFunctions.load("test/calcimages/4.jpg");
		
		//Image is not scaled
		final var scaledImg0 = RawImageFunctions.resize(img0, img0.getWidth(), img0.getHeight());
		
		final var img0Pixels = RawImageFunctions.getPixels(scaledImg0);
		final var img0Grayscale = RawImageFunctions.toGrayscale(img0Pixels);
		final var img0Pixels2D = RawImageFunctions.to2D(img0Grayscale, scaledImg0.getHeight(), scaledImg0.getWidth());
		final var convolvedPixels2D = RawImageFunctions.convolve(img0Pixels2D, edgeDetectionKernel, 0xFF);
		final var convolvedPixels1D = RawImageFunctions.to1D(convolvedPixels2D);
		
		//final var maxContrast = RawImageFunctions.maxContrast(convolvedPixels1D, 0.42f);
		
		
		RawImageFunctions.saveGrayscale(convolvedPixels1D, scaledImg0.getWidth(), scaledImg0.getHeight(), "test/contrast/img4-edgedetection.png");
	}
	
}
