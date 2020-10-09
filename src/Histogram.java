import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;

/**
 * @author corwin
 *	
 *	This program reads image files from directory, converts each image to grey scale, performs histogram equalization,
 *	and then performs histogram specification.
 *	User is able to view these operations in a browsing interface and the corresponding metadata for the image is printed
 *	to terminal.
 */

public class Histogram {
	static ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	static ArrayList<String> labelText = new ArrayList<String>();
	static BufferedImage current;
	static JFrame frame = new JFrame();
	private static JLabel label = new JLabel();
	static int index = 0;
	
	public static void main(String args[]) throws IOException {
	    System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	    	    
	    // ensuring an argument was passed through command line.
	    //if(args.length == 0) {
	    	//System.out.println("No directory submitted.");
	    	//System.exit(0);
	    //}
	    // file directory of images
	    //File dir = new File(args[0]);
	    File dir = new File("/home/corwin/samples/");
	    
	    System.out.println("Directory:" + dir.toString());
	    
	    if(dir.exists() && dir.listFiles().length > 0) {
	    	for (File f : dir.listFiles()) {
	    		if(f.toString().endsWith(".jpeg")) {
	    			BufferedImage i = convert(f);
	    			images.add(i);
	    			labelText.add(f.toString());
	    		}
	    	}
	    }else {
	    	System.out.println("Incorrect directory. Use directory that contains provided .tif images.");
	    	System.exit(0);
	    }
	    
	    listener();
	    controller();
	}
	
	/*
	 * parameter: file of image to be converted
	 * returns: grey scale image
	 */
	public static BufferedImage convert(File f) {
		BufferedImage img = null;
		try {
			img = ImageIO.read(f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int width = img.getWidth();
		int height = img.getHeight();
		
		// get current color of pixel, convert colors to equal grey, and set pixel to new color
		for(int i = 0; i < width; i++) {
			for(int j = 0; j < height; j++) {
				Color c = new Color(img.getRGB(i, j));
				int color = (c.getRed() + (2 * c.getGreen()) + c.getBlue())/4;
				Color grey = new Color(color, color, color);
				img.setRGB(i, j, grey.getRGB());
			}
		}
		return img;
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
		label.setIcon(new ImageIcon(images.get(index)));
		frame.getContentPane().add(label);
		frame.pack();
		frame.setVisible(true);
		System.out.println(index + " " + labelText.get(index));
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
