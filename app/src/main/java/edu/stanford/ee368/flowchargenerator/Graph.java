package edu.stanford.ee368.flowchargenerator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by qianyu on 2018/3/1.
 */

public class Graph {
    List<FlowchartShape> flowchartShapes;
    Canvas canvas;

    public Graph() {
        flowchartShapes = new ArrayList<>();
        FlowchartShape shape = new Rectangle(new Point(200, 200), 200, 150);
        FlowchartShape shape1 = new Rhombus(new Point(500, 500), 200, 100);
        flowchartShapes.add(shape);
        flowchartShapes.add(shape1);
    }

    public void draw() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        for (FlowchartShape shape : flowchartShapes) {
            shape.draw(canvas, paint);
        }
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }
}
