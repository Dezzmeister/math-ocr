package test;

import java.io.IOException;

import dezzy.mathocr.RawImageFunctions;

public class ContrastTest {
	
	public static void main(String[] args) throws IOException {
		final var img0 = RawImageFunctions.load("test/calcimages/4.jpg");
		
		//Image is not scaled
		final var scaledImg0 = RawImageFunctions.resize(img0, img0.getWidth(), img0.getHeight());
		
		final var img0Pixels = RawImageFunctions.getPixels(scaledImg0);
		
		final float threshold = 0.5f;
		final var grayImg0 = RawImageFunctions.maxContrast(img0Pixels, threshold);
		
		RawImageFunctions.saveGrayscale(grayImg0, scaledImg0.getWidth(), scaledImg0.getHeight(), "test/contrast/img4.png");
	}
	
}
