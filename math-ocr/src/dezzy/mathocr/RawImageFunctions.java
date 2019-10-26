package dezzy.mathocr;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import dezzy.neuronz2.math.constructs.Matrix;
import dezzy.neuronz2.math.utility.DimensionMismatchException;

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
		final DataBuffer rgbData = new DataBufferInt(pixels, pixels.length);
		final WritableRaster raster = Raster.createPackedRaster(rgbData, width, height, width, new int[] {0xFF, 0xFF, 0xFF}, null);
		final ColorModel colorModel = new DirectColorModel(24, 0xFF, 0xFF, 0xFF);
		final BufferedImage image = new BufferedImage(colorModel, raster, false, null);
		final String fileExtension = path.substring(path.lastIndexOf(".") + 1);
		
		ImageIO.write(image, fileExtension, new File(path));
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
	 * Applies a convolution matrix to an image on a specified RGB channel.
	 * 
	 * @param pixels image
	 * @param kernel convolution matrix
	 * @param channelMask Example for RGB pixels: 0xFF0000 will apply the matrix only to red values
	 * @return new image with convolution matrix applied
	 */
	public static int[][] convolve(final int[][] pixels, final Matrix kernel, final int channelMask) {
		if (kernel.rows % 2 == 0 || kernel.cols % 2 == 0) {
			throw new DimensionMismatchException("Kernel must have odd number of rows and columns!");
		}
		
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
		final int kernelRowOffset = (kernel.rows / 2);
		final int kernelColOffset = (kernel.cols / 2);
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
				
				if (accumulation < 0) {
					newValue = 0;
				} else if (accumulation > 255) {
					newValue = 255;
				} else {
					newValue = (int) accumulation;
				}
				
				final int oldColor = pixels[row + kernelRowOffset][col + kernelColOffset];
				final int newColor = (~channelMask & oldColor) | (newValue << shiftValue);
				
				output[row + kernelRowOffset][col + kernelColOffset] = newColor;
			}
		}
		
		return output;
	}
}
