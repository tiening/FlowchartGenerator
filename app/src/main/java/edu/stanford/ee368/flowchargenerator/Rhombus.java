package edu.stanford.ee368.flowchargenerator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Created by qianyu on 2018/3/1.
 */

public class Rhombus extends FlowchartShape {

    public Point center;
    public int width;
    public int height;

    private int halfWidth;
    private int halfHeight;

    public Rhombus(Point center, int width, int height) {
        this.center = center;
        this.width = width;
        this.height = height;
        this.halfWidth = width / 2;
        this.halfHeight = height / 2;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        Path path = new Path();
        path.moveTo(center.x, center.y + halfHeight); // Top
        path.lineTo(center.x - halfWidth, center.y); // Left
        path.lineTo(center.x, center.y - halfHeight); // Bottom
        path.lineTo(center.x + halfWidth, center.y); // Right
        path.lineTo(center.x, center.y + halfHeight); // Back to Top
        path.close();
        canvas.drawPath(path, paint);
    }
}
