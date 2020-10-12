import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;

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
	 * Reads directory for .tif images, calls convert to create gray scale buffered images, 
	 * adds images to collection, and generates image metadata.
	 * Note: images that are not .tif will be ignored.
	 * parameter: Directory
	 */
	public static void readDir(File dir) {
	    if(dir.exists() && dir.listFiles().length > 0) {
	    	for (File f : dir.listFiles()) {
	    		if(f.toString().endsWith(".tif")) {
	    			// convert to gray scale
	    			BufferedImage i = convert(bufImage(f));
	    			// add to image collection and metadata
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
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		
		// get current color of pixel, convert colors to equal gray, and set pixel to new color
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				Color c = new Color(img.getRGB(i, j));
				int color = (c.getRed() + (2 * c.getGreen()) + c.getBlue())/4;
				Color gray = new Color(color, color, color);
				result.setRGB(i, j, gray.getRGB());
			}
		}
		return result;
	}
	
	/*
	 * creates histogram of grey scale image
	 * parameter: image
	 * returns: histogram array
	 */
	public static int[] hist(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		int [] h = new int[256];
		
		// create raster for pixel manipulation
		WritableRaster imgR = img.getRaster();

		// incrementally increase histogram index from pixel gray scale value
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				int pixel = imgR.getSample(i, j, 0);
				h[pixel]++;		
			}
		}
		return h;
	}
	
	/*
	 * calculates cumulative sum of gray scale normalized histogram
	 * paramters: histogram
	 * return: cumulative sum of histogram
	 */
	public static int[] cumulativeSum(int[] h) {
		int[] cdf = new int[256];
		
		cdf[0] = h[0];
		
		for(int i = 1; i < 256; i++) {
			cdf[i] = cdf[i -1] + h[i];
		}
		return cdf;
	}

	/*
	 * Applies histogram equalization to image by normalizing histogram, applying cumulative distribution function,
	 * and scaling the value to within a 255 value range.
	 * paramter: gray scale image
	 * returns: image with equalized histogram
	 */
	public static BufferedImage equalize(BufferedImage img){
		int width = img.getWidth();
		int height = img.getHeight();
		int numPixels = width * height;
		
		// create blank image for pixel to be set to equalized value
	    BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
	    WritableRaster imgR = img.getRaster();
	    WritableRaster resR= result.getRaster();
	    int[] h = hist(img);
	    int[] cdf = cumulativeSum(h);

	    // equalize formula
	    float[] eq = new float[256];
	    for(int i=0;i<256;i++){
	        eq[i] =  (float)((cdf[i]*255.0)/(float)numPixels);
	    }

	    // set blank image pixel to equalized values
	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            int pixel = (int) eq[imgR.getSample(x, y, 0)];
	            resR.setSample(x, y, 0, pixel);
	        }
	    }
	    result.setData(resR);
	    return result;
	}
	
	/*
	 * Matches a target histogram reference to the histogram of image. Forming a mix
	 * for a new image that closely matches the targets histogram.
	 * paramters: gray scale image, target histogram to be matched
	 * returns: image that has histogram closely matching the target 
	 */
	public static BufferedImage match(BufferedImage img, int[] h) {
		int[] hist = hist(img);
		int[] cdf = cumulativeSum(hist);
		int[] cdfRef = cumulativeSum(h);
		int[] lt = new int[256];
		int width = img.getWidth();
		int height = img.getHeight();
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
		WritableRaster imgR = img.getRaster();
		WritableRaster resR = result.getRaster();
		
		// set reference to a similar ratio of original
		float ratio = cdf[255] / cdfRef[255];
		for(int i = 0; i < 256; i++) {
			cdfRef[i] = Math.round(cdfRef[i] * ratio);
		}
		
		// shift pixels in histogram to match similarly to target histogram
		for(int i = 0; i < 256; i++) {
			int levels = 255;
			while(levels >=0 && cdfRef[levels] > cdf[i]) {
				levels = levels - 1;
			}
			lt[i] = levels;
		}
	
		for(int i = 0; i < width ; i++) {
			for(int j = 0; j < height; j++) {
				resR.setSample(i, j, 0, lt[imgR.getSample(i, j, 0)]);
			}
		}
		result.setData(resR);
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
			images.add(match(bufImage.get(i), hist(bufImage.get(2))));
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
		
		int[] histogram = hist(images.get(index));
	    
		label.setIcon(new ImageIcon(images.get(index)));
		frame.getContentPane().add(label);
		frame.pack();
		frame.setVisible(true);
		
		System.out.println((index + 1)  + " " + labelText.get(index));
	    
		for(int x : histogram) {
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
