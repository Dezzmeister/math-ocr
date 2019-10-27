package dezzy.mathocr;

import java.awt.image.BufferedImage;

public class Metrics {
	
	/**
	 * Get the intensity histogram for a grayscale image. The histogram will have 256 bins (one for each intensity).
	 * 
	 * @param pixels grayscale image
	 * @return intensity histogram
	 */
	public static int[] intensityHistogram(final int[][] pixels) {
		final int[] intensities = new int[256];
		
		for (int row = 0; row < pixels.length; row++) {
			for (int col = 0; col < pixels[0].length; col++) {
				intensities[pixels[row][col]] += 1;
			}
		}
		
		return intensities;
	}
	
	public static BufferedImage intensityHistogramImage(final int[] histogram, final int width, final int height) {		
		int maxHeight = histogram[0];
		
		for (int i = 1; i < histogram.length; i++) {
			if (histogram[i] > maxHeight) {
				maxHeight = histogram[i];
			}
		}
		
		final int[][] originalImage = new int[256][histogram.length];
		
		for (int col = 0; col < originalImage[0].length; col++) {
			final int barHeight = (256 * histogram[col])/maxHeight;
			
			for (int row = 0; row < originalImage.length; row++) {
				 originalImage[row][col] = (originalImage.length - row > barHeight) ? 255 : 0;
			}
		}
		
		final BufferedImage originalHistogram = RawImageFunctions.createGrayscaleImage(originalImage);
		
		return RawImageFunctions.resize(originalHistogram, width, height);
	}
}
