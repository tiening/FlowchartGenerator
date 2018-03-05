package edu.stanford.ee368.flowchargenerator;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.List;

/**
 * Created by qianyu on 2018/3/1.
 */

public abstract class FlowchartShape {
    public List<FlowchartShape> neighbors;

    public abstract void draw(Canvas canvas, Paint paint);
}
