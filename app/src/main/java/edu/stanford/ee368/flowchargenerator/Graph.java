package edu.stanford.ee368.flowchargenerator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Created by qianyu on 2018/3/1.
 */

public class Graph {
    List<FlowchartShape> flowchartShapes;
    Canvas canvas;

    public Graph() {
        flowchartShapes = new ArrayList<>();
        FlowchartShape shape1 = new Rectangle(new Point(300, 300), 200, 200);
        FlowchartShape shape2 = new Rectangle(new Point(900, 300), 200, 200);
        FlowchartShape shape3 = new Rectangle(new Point(1500, 300), 200, 200);
        FlowchartShape shape4 = new Rhombus(new Point(900, 900), 200, 200);
        FlowchartShape shape5 = new Rectangle(new Point(300, 1500), 200, 200);
        FlowchartShape shape6 = new Rectangle(new Point(1500, 1500), 200, 200);
        flowchartShapes.add(shape1);
        flowchartShapes.add(shape2);
        flowchartShapes.add(shape3);
        flowchartShapes.add(shape4);
        flowchartShapes.add(shape5);
        flowchartShapes.add(shape6);
        shape1.neighbors.add(shape2);
        shape3.neighbors.add(shape2);
        shape2.neighbors.add(shape4);
        shape4.neighbors.add(shape5);
        shape4.neighbors.add(shape6);
    }

    public void draw() {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        drawShapes(paint);
        paint.setColor(Color.BLACK);
        drawArrows(paint);
    }

    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    private void drawShapes(Paint paint) {
        for (FlowchartShape shape : flowchartShapes) {
            shape.draw(canvas, paint);
        }
    }

    private void drawArrows(Paint paint) {
        Map<FlowchartShape, Integer> indegree = new HashMap<>();
        for (FlowchartShape shape : flowchartShapes) {
            indegree.put(shape, 0);
        }
        for (FlowchartShape from : flowchartShapes) {
            for (FlowchartShape to : from.neighbors) {
                indegree.put(to, indegree.get(to) + 1);
            }
        }
        Queue<FlowchartShape> queue = new LinkedList<>();
        for (FlowchartShape shape : indegree.keySet()) {
            int indeg = indegree.get(shape);
            if (indeg == 0) {
                queue.add(shape);
            }
        }
        while (!queue.isEmpty()) {
            FlowchartShape from = queue.poll();
            for (FlowchartShape to : from.neighbors) {
                drawArrow(from.getAnchor(), to.getAnchor(), paint);
                int indeg = indegree.get(to) - 1;
                indegree.put(to, indeg);
                if (indeg == 0) {
                    queue.add(to);
                }
            }
        }
    }

    private void drawArrow(Point from, Point to, Paint paint) {
        double H = 30;
        double L = 20;
        int x3 = 0;
        int y3 = 0;
        int x4 = 0;
        int y4 = 0;
        double awrad = Math.atan(L / H);
        double arraow_len = Math.sqrt(L * L + H * H);
        double[] arrXY_1 = rotateVec(to.x - from.x, to.y - from.y, awrad, true, arraow_len);
        double[] arrXY_2 = rotateVec(to.x - from.x, to.y - from.y, -awrad, true, arraow_len);
        double x_3 = to.x - arrXY_1[0];
        double y_3 = to.y - arrXY_1[1];
        double x_4 = to.x - arrXY_2[0];
        double y_4 = to.y - arrXY_2[1];
        Double X3 = new Double(x_3);
        x3 = X3.intValue();
        Double Y3 = new Double(y_3);
        y3 = Y3.intValue();
        Double X4 = new Double(x_4);
        x4 = X4.intValue();
        Double Y4 = new Double(y_4);
        y4 = Y4.intValue();
        Path triangle = new Path();
        triangle.moveTo(to.x, to.y);
        triangle.lineTo(x3, y3);
        triangle.lineTo(x4, y4);
        triangle.close();

        canvas.drawPath(triangle,paint);
        canvas.drawLine(from.x, from.y, to.x, to.y, paint);
    }

    public double[] rotateVec(int px, int py, double ang, boolean isChLen, double newLen)
    {
        double mathstr[] = new double[2];
        double vx = px * Math.cos(ang) - py * Math.sin(ang);
        double vy = px * Math.sin(ang) + py * Math.cos(ang);
        if (isChLen) {
            double d = Math.sqrt(vx * vx + vy * vy);
            vx = vx / d * newLen;
            vy = vy / d * newLen;
            mathstr[0] = vx;
            mathstr[1] = vy;
        }
        return mathstr;
    }
}
