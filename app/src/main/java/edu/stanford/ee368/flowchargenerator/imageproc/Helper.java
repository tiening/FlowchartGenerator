package edu.stanford.ee368.flowchargenerator.imageproc;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.ee368.flowchargenerator.Edge;
import edu.stanford.ee368.flowchargenerator.FlowchartShape;
import edu.stanford.ee368.flowchargenerator.Graph;
import edu.stanford.ee368.flowchargenerator.Point;
import edu.stanford.ee368.flowchargenerator.Rectangle;

public class Helper {
	
	public static List<Edge> BuildArrows(int r, int[][] center, int[][] tails) {
		
		//Edge edges[];
		//edges = new Edge[r];
		List<Edge> edges;
		edges = new ArrayList<>();
		
		for(int i=0; i<r; ++i) {
			//new Edge(new Point(400, 200), new Point(500, 200))
			Edge e = new Edge(new Point(tails[i][0], tails[i][1]), new Point(center[i][0], center[i][1]));
			edges.add(e);
		}
		return edges;
	}
	
	public static List<FlowchartShape> BuildRectangles(int r, int[][] anchors, int[][] center, int[][][] table) {
		
		//FlowchartShape rect[];
		//rect = new FlowchartShape[r];
		List<FlowchartShape> rect;
		rect = new ArrayList<>();
		
		int width;
		int height;
		int anchor1;
		int anchor2;
		int anchor3;
		int anchor4;
		
		//anchors1 upper right
		//anchors2 bottom left
		//anchors3 upper left
		//anchors4 bottom right
		
		for(int i=0; i<r; ++i) {
					
			anchor1 = anchors[i][0];
			anchor2 = anchors[i][1];
			anchor3 = anchors[i][2];
			anchor4 = anchors[i][3];
			
			width = (int) (Math.pow(table[i][0][anchor1] - table[i][0][anchor3],2) + Math.pow(table[i][1][anchor1] - table[i][1][anchor3],2));
			width = (int) Math.sqrt(width);
			
			height = (int) (Math.pow(table[i][0][anchor2] - table[i][0][anchor3],2) + Math.pow(table[i][1][anchor2] - table[i][1][anchor3],2));
			height = (int) Math.sqrt(height);
					
			FlowchartShape shape = new Rectangle(new Point(center[i][0], center[i][1]), width, height);
			rect.add(shape);
		}
		
		return rect;
		
	}

	public static int[][] Anchor(int r, int height, int width, int[][][] table, int[] len) {

		int anchors[][];
		anchors = new int[r][4];

		int anchor1 = 0;
		int anchor2 = 0;
		int anchor3 = 0;
		int anchor4 = 0;

		for(int i=0; i<r; ++i) {

			int maxx = 0;
			int maxy = 0;
			int minx = height+1;
			int miny = width+1;
			int minxy = height + width;
			int maxxy = 0;
			int compx, compy;

			// Upper left
			for(int j=0; j<len[i] ; ++j)
			{
				compx = table[i][0][j];
				compy = table[i][1][j];

				if(compx + compy <= minxy) {
					minxy = compx + compy;
					anchor3 = j;
				}
			}

			for(int j=0; j<len[i] ; ++j)
			{
				compy = table[i][1][j];
				compx = table[i][0][j];

				if(compy+compx >= maxxy) {
					maxxy = compx + compy;
					anchor4 = j;
				}
			}

			// Upper right
			int y_ref = table[i][1][anchor3];
			for(int j=0; j<len[i] ; ++j)
			{
				compx = table[i][0][j];
				compy = table[i][1][j];

				if(compx > maxx && compy == y_ref ) {
					maxx = compx;
					anchor1 = j;
				}
			}

			y_ref = table[i][1][anchor4];
			for(int j=0; j<len[i] ; ++j)
			{
				compy = table[i][1][j];

				if(compy > maxy && compy == y_ref) {
					maxy = compy;
					anchor2 = j;
				}
			}

			//anchor3 = 1;

			System.out.println("Anchor1 " + table[i][0][anchor1] + " " + table[i][1][anchor1]);
			System.out.println("Anchor2 " + table[i][0][anchor2] + " " + table[i][1][anchor2]);
			System.out.println("Anchor3 " + table[i][0][anchor3] + " " + table[i][1][anchor3]);
			System.out.println("Anchor4 " + table[i][0][anchor4] + " " + table[i][1][anchor4]);

			anchors[i][0] = anchor1;
			anchors[i][1] = anchor2;
			anchors[i][2] = anchor3;
			anchors[i][3] = anchor4;
		}

		return anchors;

	}

	public static int[][] findCenter(int[][][] table, int r, int[] len) {
		
		int center[][];
		center = new int[r][2];
		for(int i=0; i<r; ++i) {
			int xc = 0;
			int yc = 0;
			for(int j=0; j<len[i] ; ++j)
			{
				xc = xc + table[i][0][j];
				yc = yc + table[i][1][j];
			}
			xc = xc / len[i];
			yc = yc / len[i];
			center[i][0] = xc;
			center[i][1] = yc;
			
			System.out.println("X Center " + center[i][0]);
			System.out.println("Y Center " + center[i][1]);
		}
		
		return center;
	}


	public static int[][][] BuildTable(int r, int[] labelTest, int mm, int width, int height) {

		int idx;
		int current[];
		current = new int[r];

		int table[][][];
		table = new int[r][2][mm];

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				idx = labelTest[y*width+x]-1;
				//System.out.print(idx);
				if(idx==-1)
					continue;
				//System.out.println(current[idx]);
				//System.out.println(idx);
				table[idx][0][current[idx]] = x;
				table[idx][1][current[idx]] = y;
				//System.out.println(idx);
				//System.out.println(table[idx][1][current[idx]]);
				current[idx] = current[idx]+1;

			}
			//System.out.println();
		}

		return table;
	}

	public static Graph getGraph(Mat img) {
		int width = img.width();
		int height = img.height();

		Mat[] pre = new Mat[2];
		pre = PrePro.prepro(img);

		// TEST OUTPUT FROM PREPROCESS
		//Mat mat2 = new Mat(row, col, CvType.CV_8UC1);
		Mat mat2 = pre[0];
		//Mat mat3 = new Mat(height, width, CvType.CV_8UC1);
		Mat mat3 = pre[1];

		Bitmap bmp1 = null;
		bmp1 = Bitmap.createBitmap(mat2.cols(), mat2.rows(), Bitmap.Config.RGB_565);
		Bitmap bmp2 = null;
		bmp2 = Bitmap.createBitmap(mat3.cols(), mat3.rows(), Bitmap.Config.RGB_565);
		Utils.matToBitmap(mat2, bmp1);
		Utils.matToBitmap(mat3, bmp2);



		System.out.println("Row " + mat2.rows());
		System.out.println("Col " + mat2.cols());

		System.out.println("Height " + height);
		System.out.println("Width " + width);

		mat2.convertTo(mat2, CvType.CV_32SC1);
		mat3.convertTo(mat3, CvType.CV_32SC1);

		int row = mat2.rows();
		int col = mat2.cols();
		width = mat2.cols();
		height = mat2.rows();

		int[][] pre1 = new int[2][row*col];

		mat2.get(0, 0, pre1[0]);
		mat3.get(0, 0, pre1[1]);

		int [] gray_rect = pre1[0];
		int [] gray_arrow = pre1[1];


		boolean Flag = true;

		// --------------------------------------------

		int regions_rec, regions_arrow;
		mat2.convertTo(mat2, CvType.CV_8U);
		mat3.convertTo(mat3, CvType.CV_8U);
		Mat labels_rect = new Mat();
		Mat labels_arrow = new Mat();
		Mat stats = new Mat();
		Mat centroids = new Mat();
		regions_rec = Imgproc.connectedComponentsWithStats(mat2, labels_rect, stats, centroids);
		regions_arrow = Imgproc.connectedComponentsWithStats(mat3, labels_arrow, stats, centroids);
		System.out.println("Regions: " + regions_rec);

		labels_arrow.convertTo(labels_arrow, CvType.CV_32SC1);
		int[] label_arrow = new int[row * col];
		labels_arrow.get(0, 0, label_arrow);

		labels_rect.convertTo(labels_rect, CvType.CV_32SC1);
		int[] label_rect = new int[row * col];
		labels_rect.get(0, 0, label_rect);

		centroids.convertTo(centroids, CvType.CV_32SC1);
		int[] centroid = new int[row * col];
		centroids.get(0, 0, centroid);


		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(centroid[y*width+x]!=0)
					System.out.print("Centroid " + centroid[y*width+x]);
			}
		}

		// --------------------------------------------

		int r_rect=0;
		int r_arrow=0;

		// find how many regions in total
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(label_rect[y*width+x]>r_rect)
					r_rect = label_rect[y*width+x];
			}
		}

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if(label_arrow[y*width+x]>r_arrow)
					r_arrow = label_arrow[y*width+x];
			}
		}

		int len_rect[];
		len_rect = new int[r_rect];

		int len_arrow[];
		len_arrow = new int[r_arrow];

		// find length
		int idx;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				idx = label_rect[y*width+x]-1;
				if(idx==-1)
					continue;
				len_rect[idx] = len_rect[idx] +1;
			}
		}

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				idx = label_arrow[y*width+x]-1;
				if(idx==-1)
					continue;
				len_arrow[idx] = len_arrow[idx] +1;
			}
		}

		//System.out.println(len[0]);
		//System.out.println(len[1]);
		//System.out.println(len[2]);

		//int mm = 0;
		int mm_rect = 0;
		int mm_arrow = 0;

		for (int i=0; i<len_rect.length;++i) {
			if(len_rect[i]>mm_rect)
				mm_rect = len_rect[i];
		}

		for (int i=0; i<len_arrow.length;++i) {
			if(len_arrow[i]>mm_arrow)
				mm_arrow = len_arrow[i];
		}

		int[][][] table_rect = Helper.BuildTable(r_rect, label_rect, mm_rect, width, height);
		int[][][] table_arrow = Helper.BuildTable(r_arrow, label_arrow, mm_arrow, width, height);
		/*
		int current[];
		current = new int[r];

		int table[][][];
		table = new int[r][2][mm];

		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				idx = labelTest[y*width+x]-1;
				//System.out.print(idx);
				if(idx==-1)
					continue;
				//System.out.println(current[idx]);
				//System.out.println(idx);
				table[idx][0][current[idx]] = x;
				table[idx][1][current[idx]] = y;
				//System.out.println(idx);
				//System.out.println(table[idx][1][current[idx]]);
				current[idx] = current[idx]+1;

			}
			//System.out.println();
    	}
		*/

		// find center
		int[][] center_rect = Helper.findCenter(table_rect, r_rect, len_rect);
		int[][] center_arrow = Helper.findCenter(table_arrow, r_arrow, len_arrow);
		/*
		int center[][];
		center = new int[r][2];
		for(int i=0; i<r; ++i) {
			int xc = 0;
			int yc = 0;
			for(int j=0; j<len[i] ; ++j)
			{
				xc = xc + table[i][0][j];
				yc = yc + table[i][1][j];
			}
			xc = xc / len[i];
			yc = yc / len[i];
			center[i][0] = xc;
			center[i][1] = yc;
		}

		System.out.println("w " + width);
		System.out.println("h " + height);

		System.out.println("X Center " + center[0][0]);
		System.out.println("Y Center " + center[0][1]);

		System.out.println("X Center " + center[1][0]);
		System.out.println("Y Center " + center[1][1]);

		*/

		// ONLY PUT RECTANGLES HERE
		// find rectangles four points, only test one
		int anchors[][];
		anchors = new int[r_rect][4]; //r: which rectangle, 4: 4 anchors of one rectangle
		anchors = Helper.Anchor(r_rect, height, width, table_rect, len_rect);

		// ONLY PUT ARROWS HERE
		int tails[][];
		tails = new int[r_arrow][2];
		tails = GetTail.findTail(table_arrow, center_arrow, len_arrow);

		// ---BUILD RECTANGLES---
		// Compute halfwidth and halfheight to build the Flowchartshape Rectangles
		// They need Centroid, halfwidth and halfheight
		//ArrayList<FlowchartShape> rect = new ArrayList<>();
		List<FlowchartShape> rect = Helper.BuildRectangles(r_rect, anchors, center_rect, table_rect); // change center and table

		// ---BUILD EDGES---
		// Using tail and Centroid as end point
		//Edge edges[];
		//edges = new Edge[r]; // change r to arrow number
		List<Edge> edges = Helper.BuildArrows(r_arrow, center_arrow, tails);

		// ---BUILD GRAPH---
		Graph graph = new Graph(rect, edges);


		return graph;

	}

}
