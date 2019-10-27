package dezzy.mathocr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.imageio.ImageIO;

import dezzy.neuronz2.math.constructs.Matrix;

/**
 * Functions to handle raw math images and prepare them for processing.
 *
 * @author Joe Desmond
 */
public final class RawImageFunctions {
	
	/**
	 * Loads an image from a file.
	 * 
	 * @param path path to the file
	 * @return the image
	 * @throws IOException if there is a problem loading the image
	 */
	public static final BufferedImage load(final String path) throws IOException {
		return ImageIO.read(new File(path));
	}
	
	/**
	 * Gets the RGB pixels from an image.
	 * 
	 * @param image the image
	 * @return RGB array
	 */
	public static final int[] getPixels(final BufferedImage image) {
		final int[] pixels = new int[image.getWidth() * image.getHeight()];
		
		image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
		
		return pixels;
	}
	
	/**
	 * Converts an <code>int[]</code> representation of an image into an equivalent
	 * <code>int[][]</code> representation. (Reverses {@link #to1D(int[][]) to1D()})
	 * 
	 * @param pixels input pixels
	 * @param rows height of the image
	 * @param cols width of the image
	 * @return a 2D representation of the image
	 */
	public static final int[][] to2D(final int[] pixels, final int rows, final int cols) {
		final int[][] output = new int[rows][cols];
		
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				output[row][col] = pixels[col + (row * cols)];
			}
		}
		
		return output;
	}
	
	/**
	 * Converts an <code>int[][]</code> representation of an image into an equivalent
	 * <code>int[]</code> representation. (Reverses {@link #to2D(int[], int, int) to2D()})
	 * 
	 * @param pixels input pixels
	 * @return a 1D representation of the image
	 */
	public static final int[] to1D(final int[][] pixels) {
		final int rows = pixels.length;
		final int cols = pixels[0].length;
		final int[] output = new int[rows * cols];
		
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < cols; col++) {
				output[(row * cols) + col] = pixels[row][col];
			}
		}
		
		return output;
	}
	
	/**
	 * Maps the pixels of an image to new pixel values. For each pixel, if its value is 
	 * less than <code>(256 * threshold)</code>, then the output is 0, otherwise it's 255.
	 * 
	 * @param pixels grayscale pixel array
	 * @param threshold normalized value where 0 is black and 1 is white
	 * @return pixels that are either white or black (only least significant byte is defined)
	 */
	public static final int[] maxContrast(final int[] pixels, final float threshold) {
		final int barrier = (int)(threshold * 256);
		final int[] newPixels = new int[pixels.length];
		
		for (int i = 0; i < pixels.length; i++) {			
			newPixels[i] = (pixels[i] < barrier) ? 0 : 255;
		}
		
		return newPixels;
	}
	
	/**
	 * Saves an <code>int[]</code> as an image. The image will be grayscale, and will treat each element of the input array as a gray value from
	 * 0 to 255. Only the least significant byte is used.
	 * 
	 * @param pixels grayscale pixels of the image
	 * @param width width of the image
	 * @param height height of the image
	 * @param path path to the image file
	 * @throws IOException if there is a problem creating or writing to the file
	 */
	public static void saveGrayscale(final int[] pixels, final int width, final int height, final String path) throws IOException {
		final BufferedImage image = createGrayscaleImage(pixels, width, height);
		final String fileExtension = path.substring(path.lastIndexOf(".") + 1);
		
		ImageIO.write(image, fileExtension, new File(path));
	}
	
	/**
	 * Saves an <code>int[][]</code> as an image. The image will be grayscale, and will treat each element of the input array as gray value from
	 * 0 to 255. Only the least significant byte is used.
	 * 
	 * @param pixels grayscale pixels of the image
	 * @param path path to the image file
	 * @throws IOException if there is a problem creating or writing to the file
	 */
	public static void saveGrayscale(final int[][] pixels, final String path) throws IOException {
		saveGrayscale(to1D(pixels), pixels[0].length, pixels.length, path);
	}
	
	/**
	 * Creates a {@link BufferedImage} from a grayscale image.
	 * 
	 * @param pixels grayscale pixels
	 * @param width width of the image
	 * @param height height of the image
	 * @return the image
	 */
	public static BufferedImage createGrayscaleImage(final int[] pixels, final int width, final int height) {
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final int[] pixelData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		for (int i = 0; i < pixels.length; i++) {
			final int gray = pixels[i];
			pixelData[i] = (gray << 16) | (gray << 8) | gray;
		}
		
		return image;
	}
	
	/**
	 * Creates a {@link BufferedImage} from a grayscale image.
	 * 
	 * @param pixels grayscale pixels
	 * @return the image
	 */
	public static BufferedImage createGrayscaleImage(final int[][] pixels) {
		return createGrayscaleImage(to1D(pixels), pixels[0].length, pixels.length);
	}
	
	/**
	 * Resizes an image.
	 * 
	 * @param image image
	 * @param newWidth scaled width
	 * @param newHeight scaled height
	 * @return scaled image
	 */
	public static BufferedImage resize(final BufferedImage image, final int newWidth, final int newHeight) {
		final BufferedImage newImage = new BufferedImage(newWidth, newHeight, image.getType());
		final Graphics2D g2d = newImage.createGraphics();
		g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
		g2d.dispose();
		
		return newImage;
	}
	
	/**
	 * Converts an RGB image into a grayscale image with one channel.
	 * 
	 * @param rgbPixels rgb pixels
	 * @return grayscale image
	 */
	public static int[] toGrayscale(final int[] rgbPixels) {
		final int[] output = new int[rgbPixels.length];
		
		for (int i = 0; i < rgbPixels.length; i++) {
			final int rawRGB = rgbPixels[i];
			final int red = (rawRGB >>> 16) & 0xFF;
			final int blue = (rawRGB >>> 8) & 0xFF;
			final int green = rawRGB & 0xFF;
			
			output[i] = (red + green + blue) / 3;
		}
		
		return output;
	}
	
	/**
	 * Performs a map operation on pixels.
	 * 
	 * @param pixels pixels
	 * @param operation map operation
	 * @return new pixel array with operation applied per element
	 */
	public static int[][] map(final int[][] pixels, final Function<Integer, Integer> operation) {
		final int[][] output = new int[pixels.length][pixels[0].length];
		
		for (int row = 0; row < pixels.length; row++) {
			for (int col = 0; col < pixels[0].length; col++) {
				output[row][col] = operation.apply(pixels[row][col]);
			}
		}
		
		return output;
	}
	
	/**
	 * Performs a map2 operation on the pixels of two images.
	 * 
	 * @param pixels1 first image
	 * @param pixels2 second image
	 * @param operation function of two pixels
	 * @return new pixel array with operation applied per pair of elements
	 * @throws IllegalArgumentException if the images are not the same size
	 */
	public static int[][] map2(final int[][] pixels1, final int[][] pixels2, final BiFunction<Integer, Integer, Integer> operation) {
		if (pixels1.length != pixels2.length || pixels1[0].length != pixels2[0].length) {
			throw new IllegalArgumentException("Pixel arrays must have the same dimensions!");
		}
		
		final int[][] output = new int[pixels1.length][pixels1[0].length];
		
		for (int row = 0; row < pixels1.length; row++) {
			for (int col = 0; col < pixels1[0].length; col++) {
				output[row][col] = operation.apply(pixels1[row][col], pixels2[row][col]);
			}
		}
		
		return output;
	}
	
	/**
	 * Constrains each pixel value to the range 0-255.
	 * 
	 * @param pixels grayscale pixels
	 * @return 
	 */
	public static int[][] constrain(final int[][] pixels) {
		final Function<Integer, Integer> operation = (x) -> {
			if (x < 0) {
				return 0;
			} else if (x > 255) {
				return 255;
			} else {
				return x;
			}
		};
		
		return map(pixels, operation);
	}
	
	/**
	 * Applies a convolution matrix to an image on a specified RGB channel.
	 * 
	 * @param pixels image
	 * @param kernel convolution matrix
	 * @param channelMask Example for RGB pixels: 0xFF0000 will apply the matrix only to red values
	 * @return new image with convolution matrix applied
	 */
	public static int[][] convolve(final int[][] pixels, final Matrix kernel, final int channelMask) {
		if (channelMask == 0) {
			throw new IllegalArgumentException("Channel mask must not be zero!");
		}
		
		int shiftValue = 0;
		
		int channel = channelMask;
		while ((channel & 1) != 1) {
			shiftValue++;
			channel >>>= 1;
		}
		
		final int rows = pixels.length;
		final int cols = pixels[0].length;
		final int[][] output = new int[rows][cols];
		
		for (int row = 0; row < (rows - kernel.rows); row++) {
			for (int col = 0; col < (cols - kernel.cols); col++) {
				float accumulation = 0;
				
				for (int kernelRow = 0; kernelRow < kernel.rows; kernelRow++) {
					for (int kernelCol = 0; kernelCol < kernel.cols; kernelCol++) {
						accumulation += (pixels[row + kernelRow][col + kernelCol] * kernel.get(kernelRow, kernelCol));
					}
				}
				
				final int newValue;
				
				newValue = (int) accumulation;
				
				final int oldColor = pixels[row][col];
				final int newColor = (~channelMask & oldColor) | (newValue << shiftValue);
				
				output[row][col] = newColor;
			}
		}
		
		return output;
	}
}
