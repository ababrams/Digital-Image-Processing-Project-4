import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Point;

/**
 *	This program reads image files from directory, converts each image to gray scale, performs histogram equalization,
 *	and then performs histogram specification.
 *	User is able to view these operations in a browsing interface and the corresponding metadata for the image is printed
 *	to terminal.
 */

public class Histogram {
	static ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	static ArrayList<BufferedImage> bufImage = new ArrayList<BufferedImage>();
	static ArrayList<String> labels = new ArrayList<String>();
	static ArrayList<String> labelText = new ArrayList<String>();
	static BufferedImage current;
	static JFrame frame = new JFrame();
	private static JLabel label = new JLabel();
	static int index = 0;
	
	public static void main(String args[]) throws IOException {
	    System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	    	    
	    // ensuring an argument was passed through command line.
	    if(args.length == 0) {
	    	System.out.println("No directory submitted.");
	    	System.exit(0);
	    }
	    // file directory of images
	    File dir = new File(args[0]);
	    
	    readDir(dir);
	    setImages();
	    listener();
	    controller();
	}
	
	/*
	 * Reads directory for .tif images, calls convert to create buffered images, adds images to collection, and generates
	 * image metadata.
	 * Note: images that are not .tif will be ignored.
	 * parameter: Directory
	 */
	public static void readDir(File dir) {
	    if(dir.exists() && dir.listFiles().length > 0) {
	    	for (File f : dir.listFiles()) {
	    		if(f.toString().endsWith(".tif")) {
	    			BufferedImage i = convert(bufImage(f));
	    			bufImage.add(i);
	    			labels.add(f.toString().replace(dir.toString() + "/", ""));
	    		}
	    	}
	    }else {
	    	System.out.println("Directory does not exist.");
	    	System.exit(0);
	    }
	    
	    if(bufImage.isEmpty()) {
	    	System.out.println("No images were found in directory.");
	    } 
	}
	
	/*
	 * reads the appropriate file type to image
	 * parameter: .tif file
	 * return: image
	 */
	public static BufferedImage bufImage(File f) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return img;
	}
	
	/*
	 * Changes image to gray scale
	 * parameter: image to be converted
	 * returns: grey scale image
	 */
	public static BufferedImage convert(BufferedImage img) {
				
		int width = img.getWidth();
		int height = img.getHeight();
		
		// get current color of pixel, convert colors to equal gray, and set pixel to new color
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				Color c = new Color(img.getRGB(i, j));
				int color = (c.getRed() + (2 * c.getGreen()) + c.getBlue())/4;
				Color gray = new Color(color, color, color);
				img.setRGB(i, j, gray.getRGB());
			}
		}
		return img;
	}
	
	/*
	 * creates histogram of grey scale image
	 * parameter: image
	 * returns: histogram array
	 */
	public static double[] hist(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		double [] h = new double[256];
		
		for(int i = 0; i < h.length; i++) {
			h[i] = 0;	
		}
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int pixel = img.getRGB(i, j) & 0xFF;
				h[pixel]++;		
			}
		}
		return h;
	}
	
	/*
	 * normalizes the gray scale image histogram
	 * parameter: gray scale image
	 * returns: normalized histogram array
	 */
	public static double[] normalize(BufferedImage img) {
		double[] h = hist(img);
		double[] result = new double[h.length];
		int numPixels = img.getHeight() * img.getWidth();
		
		for(int i = 0; i < h.length; i++) {
			result[i] = h[i] / numPixels;
		}		
		return result;
	}
	
	/*
	 * calculates cumulative sum of gray scale normalized histogram
	 * paramters: normalized histogram
	 * return: cumulative sum of normalized histogram
	 */
	public static double[] cumulativeSum(double[] h) {
		double[] cdf = new double[h.length];
		
		cdf[0] = h[0];
		
		for(int i = 1; i < h.length - 1; i++) {
			cdf[i] = cdf[i -1] + h[i];
		}
		
		return cdf;
	}
	
	/*
	 * Applies histogram equalization to image by normalizing histogram, and applying cumulative distribution function.
	 * paramter: gray scale image
	 * returns: image with equalized histogram
	 */
	public static BufferedImage equalize(BufferedImage img) {
		double[] h = normalize(img);
		double[] c = cumulativeSum(h);
		int width = img.getWidth();
		int height = img.getHeight();
		
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
				
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				result.setRGB(i,j, (int) Math.round(255 * c[img.getRGB(i,j) & 0xFF]));
			}
		}
		
		result = convert(result);
		
		return result;
	}
	
	/*
	 * Matches a target histogram reference to the histogram of image. Forming a mix
	 * for a new image that closely matches the targets histogram.
	 * paramters: gray scale image, target histogram to be matched
	 * returns: image that has histogram closely matching the target 
	 */
	public static BufferedImage match(BufferedImage img, double[] h) {
		double [] hist = normalize(img);
		double[] cdf = cumulativeSum(hist);
		double[] cdfRef = cumulativeSum(h);
		double[] lt = new double[256];
		int width = img.getWidth();
		int height = img.getHeight();
		int pix = 0;
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for(int levels = 0; levels < 255; levels++) {
			int levels_ = 255;
			while(levels_ >= 0 && cdfRef[levels_] < cdf[levels]) {
				lt[levels] = levels_;
				levels_ = levels_ - 1;
			}
		}
		
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				result.setRGB(i,j, (int) lt[img.getRGB(i,j) & 0xFF]);
			}
		}
		
		result = convert(result);
		
		return result;
	}
	
	/*
	 * Sets images into the browser array
	 */
	public static void setImages() {
		for(int i = 0; i < bufImage.size(); i++) {
			images.add(bufImage.get(i));
			labelText.add(labels.get(i));
			images.add(equalize(bufImage.get(i)));
			labelText.add("Histogram Equalization: " + labels.get(i));
			images.add(match(bufImage.get(i), hist(bufImage.get(1))));
			labelText.add("Histogram Matching: " + labels.get(i));
		}
	}

	/**
	 * adds key listener to frame.
	 * n/space - next image
	 * p - previous image
	 * q - exit program
	 */
	public static void listener() {
		frame.addKeyListener(new KeyListener(){

			@Override
			public void keyPressed(KeyEvent a) {
				if(a.getKeyChar() == 'n' || a.getKeyCode() == KeyEvent.VK_SPACE) {
					nextImage();
				}
				
				if(a.getKeyChar() == 'p') {
					previousImage();
				}
				
				if(a.getKeyChar() == 'q') {
					System.exit(0);
				}	
			}

			@Override
			public void keyReleased(KeyEvent a) {}
			@Override
			public void keyTyped(KeyEvent a) {}
		});
	}
		
	/**
	 * displays image to frame GUI and prints image details
	 */
	public static void controller() {
		
		double[] histogram = hist(images.get(index));
	    
		label.setIcon(new ImageIcon(images.get(index)));
		frame.getContentPane().add(label);
		frame.pack();
		frame.setVisible(true);
		
		System.out.println((index + 1)  + " " + labelText.get(index));
	    
		for(double x : histogram) {
	    	System.out.print(x + "|");
	    }
		
		System.out.println();
	}
	
	/**
	 * sets index to next within bounds value
	 */
	public static void nextImage() {
		if (index < images.size() - 1) {
			index++;
		} else {
			index = 0;
		}
		controller();
	}
	
	/**
	 * sets index to previous within bounds value
	 */
	public static void previousImage() {
		if (index == 0) {
			index = images.size() - 1;
		} else {
			index--;
		}
		controller();
	}

}
