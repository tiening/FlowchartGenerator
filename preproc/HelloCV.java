package opencv;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.core.Mat;

public class HelloCV {

	public static void main(String[] args){
		String fileName = "/Users/zhangrao/Desktop/fig.jpg";
		Mat[] mats = PreProc.preproc(fileName);
		try {
			saveImage(mats[0], "boxing");
			saveImage(mats[1], "arrows");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

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

}
