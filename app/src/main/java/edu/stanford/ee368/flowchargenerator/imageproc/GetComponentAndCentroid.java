package edu.stanford.ee368.flowchargenerator.imageproc;

public class GetComponentAndCentroid {
	//static int current[];
	//static int width;  static int height;
	//static int[] len; static int r;
	public static int[] componentLength(int[] labelMatrix,  int h, int w) {
		//get the number of pixels of each component
		int width = w;
		int height = h;
		int r=0;
		
		// find how many regions in total 
		for(int y = 0; y < height; y++){
        		for(int x = 0; x < width; x++){
        			if(labelMatrix[y * width + x] > r)
        				r = labelMatrix[y * width + x];
        		}
        }
		
		int len[];
		len = new int[r];
		
		// find length
		int idx;
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
					idx = labelMatrix[y*width+x] - 1;
					if(idx==-1)
						continue;
					len[idx] = len[idx] +1;
			}
    		}
		return len;
	}
	
	
	public static int[][][] getComponentMatrix (int[] labelMatrix, int height, int width, int[] len) {
		// get the component Matrix as (r, x, y)
		int idx;
		
		//System.out.println(len[0]);
		int mm = 0; // max size
		for (int i=0; i<len.length;++i) {
			if(len[i]>mm)
				mm = len[i];
		}
		//System.out.println(mm);
		
		int[] current;
		current = new int[len.length];
		
		int table[][][];
		table = new int[len.length][2][mm];
		
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				idx = labelMatrix[y*width+x]-1;
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
	
	public static int[][] getCentroid(int[][][] table, int[] len) {
		// get centroid of each component
		int center[][];
		center = new int[len.length][2];
		for(int i = 0; i < len.length; ++i) {
			int xc = 0;
			int yc = 0;
			for(int j = 0; j < len[i] ; ++j)
			{
				xc = xc + table[i][0][j];
				yc = yc + table[i][1][j];
			}
			xc = xc / len[i];
			yc = yc / len[i];
			center[i][0] = xc;
			center[i][1] = yc;
		}
		return center;
	}

	
}