package opencv;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class PreProc {


	public static Mat[] preproc(String fileName){
		try {
			System.out.println("Hello, World!");

			// load library, load image file
			System.loadLibrary( Core.NATIVE_LIBRARY_NAME );	
			File input = new File(fileName);	// jpg only!
			BufferedImage image = ImageIO.read(input);	

			// convert image to mat
			byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
			Mat mat = new Mat(image.getHeight(), image.getWidth(), CvType.CV_8UC3);
			mat.put(0, 0, data);

			// get mat size
			int rows = mat.rows();
			int cols = mat.cols();


			// rgb2gray, generate gray image
			Mat gray = new Mat(rows,cols,CvType.CV_8UC1);
			Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY);
			System.out.println("Grayscale Done!");
			saveImage(gray, "gray");
			
			// histogram equalization
//			Imgproc.equalizeHist(gray, gray);
//			System.out.println("Histeq Done!");
//			saveImage(gray, "histeq");

			// binarize, generate new mat
			Mat bina = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.adaptiveThreshold(gray, bina, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 35, 40);
//			Imgproc.threshold(gray, bina, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
			System.out.println("Binarization Done!");
			saveImage(bina, "bina");

			// bit inverted
			Core.bitwise_not(bina, bina); 
			System.out.println("BitWiseNot Done!");
			saveImage(bina, "invt");
			
			// remove small regions
			int smallArea = 100;
			List<MatOfPoint> noiseContours = new ArrayList<>();
			List<MatOfPoint> shapeContours = new ArrayList<>();
			Mat denoise = Mat.zeros(rows, cols, CvType.CV_8UC1);
			Scalar white = new Scalar(255);
			Scalar black = new Scalar(0);
			Imgproc.findContours(bina, noiseContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			for (MatOfPoint contour: noiseContours) {
				if(contour.rows() > smallArea) {
					Imgproc.fillPoly(denoise, Arrays.asList(contour), white);
				}
			}
//			Imgproc.drawContours(denoise, shapeContours, -1, white);		
			System.out.println("Denoise Done!");
			saveImage(denoise, "denoised");

			// get edges
			Mat edges = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.Canny(denoise, edges, 100, 100, 3, false);
			System.out.println("GetEdge Done!");
			saveImage(edges, "edges");

			Mat substitute = new Mat();
			// hough transform
			Mat lines = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.HoughLinesP(edges, lines, 1, Math.PI / 180.0, 100, 100, 5);
			if(lines.rows() > 0) {
				int[] nums = new int[lines.rows() * 4];
				double[] angles = new double[lines.rows()];
				lines.get(0, 0, nums);
				for(int i = 0; i < lines.rows() ; i++) {
					double x1 = nums[4 * i];
					double y1 = nums[4 * i + 1];
					double x2 = nums[4 * i + 2];
					double y2 = nums[4 * i + 3];
					angles[i] = Math.atan((y2 - y1) / (x2 - x1));
				}
				Arrays.sort(angles);
				double angle = angles[angles.length / 2];
				double degree = Math.toDegrees(angle);
				System.out.println("Hough Transform Done!");

				// image rotate
				int x = cols;
				int y = rows;
				// 4 points, (0, 0) (x, 0) (0, y) (x, y) to rotate transform (clockwise)
				// (0, 0), (x*cosA, -x*sinA) (y*sinA, y*cosA) (x*cosA+y*sinA, -x*sinA+y*cosA)
				int newWidth = 0;
				int newHeight = 0;
				int xshift = 0;
				int yshift = 0;
				if(angle >= 0) {
					newWidth = (int) Math.round(x * Math.cos(angle) + y * Math.sin(angle));
					newHeight = (int) Math.round(y * Math.cos(angle) + x * Math.sin(angle));
					yshift = (int) Math.round(x * Math.sin(angle));
				} else { // angle < 0
					newWidth = (int) Math.round(x * Math.cos(angle) - y * Math.sin(angle));
					newHeight = (int) Math.round(-x * Math.sin(angle) + y * Math.cos(angle));
					xshift = (int) Math.round(-y * Math.sin(angle));
				}

				// shift image
				Mat shifted = Mat.zeros(y + yshift, x + xshift, CvType.CV_8UC1);
				Mat shiftedSub = shifted.submat(yshift, y + yshift, xshift, x + xshift);
				denoise.copyTo(shiftedSub);
				System.out.println("Shift Done!");
				saveImage(shifted, "shifted");

				// rotate image
				Mat rotMat = Imgproc.getRotationMatrix2D(new Point(xshift, yshift), degree, 1.0);
				Mat rotated = new Mat();
				Imgproc.warpAffine(shifted, rotated, rotMat, new Size(newWidth, newHeight), Imgproc.INTER_LINEAR);			
				System.out.println("Rotation Done!");
				saveImage(rotated, "rotated");
				
				// parameter replace
				cols = rotated.cols();
				rows = rotated.rows();
				substitute = rotated.clone();
			} else {
				substitute = denoise.clone();
			}
			

			// Find contours
			smallArea = 100;
			List<MatOfPoint> contours = new ArrayList<>();
			Mat fill = Mat.zeros(rows, cols, CvType.CV_8UC1);
			Imgproc.findContours(substitute, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			// Draw contours in fill Mat
			// Imgproc.drawContours(fill, contours, -1, white);		
			for (MatOfPoint contour: contours) {
				if(contour.rows() > smallArea) {
					Imgproc.fillPoly(fill, Arrays.asList(contour), white);
				}
			}
			System.out.println("Fill Done!");
			saveImage(fill, "fill");

			// open
			int radius = 10;
			Mat seOpen = seGen(radius);
			Mat opened = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.morphologyEx(fill, opened, Imgproc.MORPH_OPEN, seOpen);
			System.out.println("Open Done!");
			saveImage(opened, "opened");

			// compute difference
			Mat diff = new Mat(rows, cols, CvType.CV_8UC1);
			Core.absdiff(fill, opened, diff);
			System.out.println("Diff Done!");
			saveImage(diff, "diff");

			// remove small area
			smallArea = 85;
			List<MatOfPoint> smallContours = new ArrayList<>();
			Mat remv = Mat.zeros(diff.size(), CvType.CV_8UC1);
			Imgproc.findContours(diff, smallContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			for (MatOfPoint contour: smallContours) {
				if(contour.rows() > smallArea) {
					Imgproc.fillPoly(remv, Arrays.asList(contour), white);
				}
			}
			System.out.println("SmallAreaRemoval Done for Arrow!");
			saveImage(remv, "remv");

			// get rectangles and diamonds
			Mat blob = new Mat(rows, cols, CvType.CV_8UC1);
			Core.absdiff(fill, remv, blob);
			System.out.println("Blob Done!");
			saveImage(blob, "blob");
			
			// distinguish rectangles and diamonds
//			List<MatOfPoint> blobContours = new ArrayList<>();
//			Mat rectangle = Mat.zeros(blob.size(), CvType.CV_8UC1);
//			Mat diamond = Mat.zeros(blob.size(),  CvType.CV_8UC1);
//			Imgproc.findContours(blob, blobContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//			for (MatOfPoint contour: blobContours) {
//				 Point[] points = contour.toArray();
//				 System.out.println(points.length);
//				 int ymin = blob.rows();
//				 int ymax = 0;
//				 int xmin = blob.cols();
//				 int xmax = 0;
//				 int count = 0;
//				 for(Point point : points) {
//					 int xtemp = (int) point.x;
//					 if(xtemp > xmax) {
//						 xmax = xtemp;
//					 }
//					 if(xtemp < xmin) {
//						 xmin = xtemp;
//					 }
//					 int ytemp = (int) point.y;
//					 if(ytemp > ymax) {
//						 ymax = ytemp;
//					 }
//					 if(ytemp < ymin) {
//						 ymin = ytemp;
//					 }
//					 count += 1;
//				 }
//				 System.out.println((xmax - xmin) + ":" + (ymax - ymin));
//				 System.out.println(count);
//				 int outerArea = (ymax - ymin) * (xmax - xmin);
//				 if(count / (double) outerArea > 0.75) {	// rectangular
//					 Imgproc.fillPoly(rectangle, Arrays.asList(contour), white);
//				 } else {	// diamond
//					 Imgproc.fillPoly(diamond, Arrays.asList(contour), white);
//				 }
//			}
//			System.out.println("Rectangule Diamond Done!");
//			saveImage(rectangle, "rect");
//			saveImage(diamond, "diamond");

			
			// erode
			int r = 10;
			Mat seErode = seGen(r);
			Mat eroded = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.erode(blob, eroded, seErode);
			System.out.println("Erode Done!");
			saveImage(eroded, "eroded");

			// get boxes
			Mat box = new Mat(rows, cols, CvType.CV_8UC1);
			Core.absdiff(blob, eroded, box);
			System.out.println("Box Done!");
			saveImage(box, "box");

//			// remove small area
//			smallArea = 100;
//			List<MatOfPoint> regions = new ArrayList<>();
//			Mat clear = Mat.zeros(box.size(), CvType.CV_8UC1);
//			Imgproc.findContours(box, regions, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
//			for (MatOfPoint contour: regions) {
//				if(contour.rows() > smallArea) {
//					Imgproc.fillPoly(clear, Arrays.asList(contour), white);
//				}
//			}
//			System.out.println("SmallAreaRemoval Done for Box!");
//			saveImage(clear, "clear");
			

			// type conversion
			int[][] ret = new int[2][rows * cols];
			Mat boxing = new Mat(rows, cols, CvType.CV_32SC1);
			Mat arrows = new Mat(rows, cols, CvType.CV_32SC1);
			box.convertTo(boxing, CvType.CV_32SC1);
			remv.convertTo(arrows, CvType.CV_32SC1);
			
			boxing.get(0, 0, ret[0]);
			arrows.get(0, 0, ret[1]);
			System.out.println("Conversion Done!");
			

			return new Mat[]{box, remv};

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}    	

		return null;

	}

	// save mat to image
	public static void saveImage(Mat mat, String fileName) throws IOException {
		// convert to image and write to file
		byte[] data = new byte[mat.rows() * mat.cols()];
		mat.get(0, 0, data);
		BufferedImage image = new BufferedImage(mat.cols(),mat.rows(), BufferedImage.TYPE_BYTE_GRAY);
		image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

		// write image0 to file
		File output = new File("/Users/zhangrao/Desktop/" + fileName + ".png");
		ImageIO.write(image, "png", output);
	}
	
	// reshape square int[][] to int[]
	public static int[] TwoDim2OneDim(int[][] input, int size) {
		int[] output = new int[size * size];
		for(int r = 0; r < size; r++) {
			for(int c = 0; c < size; c++) {
				output[c + r * size] = input[r][c];
			}
		}
		return output;
	}
	
	// generate structuring element
	public static Mat seGen(int radius) {
		int diameter = 2 * radius + 1;
		int[][] disk = new int[diameter][diameter];
		for(int x = 0; x < diameter; x++) {
			for(int y = 0; y < diameter; y++) {
				int xpos = x - 10;
				int ypos = y - 10;
				if(xpos*xpos + ypos*ypos <= radius*radius) {
					disk[x][y] = 1;
				} else {
					disk[x][y] = 0;
				}
			}
		}
		int[] diskFlat = TwoDim2OneDim(disk, diameter);
		Mat seOpen = new Mat(diameter, diameter, CvType.CV_32SC1);
		seOpen.put(0, 0, diskFlat);
		seOpen.convertTo(seOpen, CvType.CV_8UC1);
		return seOpen;
	}
	
	
	
}
