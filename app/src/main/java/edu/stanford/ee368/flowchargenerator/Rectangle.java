package edu.stanford.ee368.flowchargenerator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

/**
 * Created by qianyu on 2018/3/1.
 */

public class Rectangle extends FlowchartShape {

    public Point center;
    public int width;
    public int height;

    private int halfWidth;
    private int halfHeight;


    public Rectangle(Point center, int width, int height) {
        this.center = center;
        this.width = width;
        this.height = height;
        this.halfWidth = width / 2;
        this.halfHeight = height / 2;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        canvas.drawRect(center.x - halfWidth, center.y - halfHeight, center.x + halfWidth, center.y + halfHeight, paint);
    }
}
