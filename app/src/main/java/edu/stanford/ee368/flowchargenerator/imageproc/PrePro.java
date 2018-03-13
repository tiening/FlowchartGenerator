package edu.stanford.ee368.flowchargenerator.imageproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class PrePro {

	public static Mat[] prepro(Mat mat){
		try {
			System.out.println("Hello, World!");

			// get mat size
			int rows = mat.rows();
			int cols = mat.cols();

			// rgb2gray, generate gray image
			Mat gray = new Mat(rows,cols,CvType.CV_8UC1);
			Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY);
			System.out.println("Grayscale Done!");

			// binarize, generate new mat
			Mat bina = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.adaptiveThreshold(gray, bina, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 35, 40);
			System.out.println("Binarization Done!");

			// bit inverted
			Core.bitwise_not(bina, bina); 
			System.out.println("BitWiseNot Done!");

			// remove small regions
			int smallArea = 100;
			List<MatOfPoint> noiseContours = new ArrayList<>();
			Mat denoise = Mat.zeros(rows, cols, CvType.CV_8UC1);
			Scalar white = new Scalar(255);
			Imgproc.findContours(bina, noiseContours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			for (MatOfPoint contour: noiseContours) {
				if(contour.rows() > smallArea) {
					Imgproc.fillPoly(denoise, Arrays.asList(contour), white);
				}
			}
			System.out.println("Denoise Done!");

			// get edges
			Mat edges = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.Canny(denoise, edges, 100, 100, 3, false);
			System.out.println("GetEdge Done!");

			// hough transform
			Mat substitute = new Mat();
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

				// rotate image
				Mat rotMat = Imgproc.getRotationMatrix2D(new org.opencv.core.Point(xshift, yshift), degree, 1.0);
				Mat rotated = new Mat();
				Imgproc.warpAffine(shifted, rotated, rotMat, new Size(newWidth, newHeight), Imgproc.INTER_LINEAR);			
				System.out.println("Rotation Done!");

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
			for (MatOfPoint contour: contours) {
				if(contour.rows() > smallArea) {
					Imgproc.fillPoly(fill, Arrays.asList(contour), white);
				}
			}
			System.out.println("Fill Done!");

			// open
			int radius = 10;
			Mat seOpen = seGen(radius);
			Mat opened = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.morphologyEx(fill, opened, Imgproc.MORPH_OPEN, seOpen);
			System.out.println("Open Done!");

			// compute difference
			Mat diff = new Mat(rows, cols, CvType.CV_8UC1);
			Core.absdiff(fill, opened, diff);
			System.out.println("Diff Done!");

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

			// get rectangles and diamonds
			Mat blob = new Mat(rows, cols, CvType.CV_8UC1);
			Core.absdiff(fill, remv, blob);
			System.out.println("Blob Done!");

			// distinguish rectangles and diamonds
			// erode
			int r = 10;
			Mat seErode = seGen(r);
			Mat eroded = new Mat(rows, cols, CvType.CV_8UC1);
			Imgproc.erode(blob, eroded, seErode);
			System.out.println("Erode Done!");

			// get boxes
			Mat box = new Mat(rows, cols, CvType.CV_8UC1);
			Core.absdiff(blob, eroded, box);
			System.out.println("Box Done!");

			// remove small area

			// type conversion
			int[][] ret = new int[2][rows * cols];
			Mat[] result = new Mat[2];
			result[0] = box;
			result[1] = remv;
			System.out.println("Conversion Done!");

			// convert mat2 to image
			Mat mat2 = box;
			byte[] data2 = new byte[mat2.rows() * mat2.cols() * (int)(mat2.elemSize())];
			mat2.get(0, 0, data2);

			return result;

		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}

		return null;


	}

	public static int[] TwoDim2OneDim(int[][] input, int size) {
		int[] output = new int[size * size];
		for(int r = 0; r < size; r++) {
			for(int c = 0; c < size; c++) {
				output[c + r * size] = input[r][c];
			}
		}
		return output;
	}

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
