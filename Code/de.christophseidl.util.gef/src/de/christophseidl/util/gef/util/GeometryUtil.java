package de.christophseidl.util.gef.util;

import java.awt.geom.Rectangle2D;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Path;

public class GeometryUtil {
	public static int getTextWidth(String text, Font font) {
		Label label = new Label();
		
		label.setText(text);
		label.setFont(font);
		
		return label.getPreferredSize().width;
	}

	public static int getTextHeight(String text, Font font) {
		Label label = new Label();
		
		label.setText(text);
		label.setFont(font);

		return label.getPreferredSize().height;
	}
	
	public static double calculateRotationAngleForLineWithEndpoints(Point startPoint, Point endPoint) {
		double deltaX = endPoint.x - startPoint.x;
		double deltaY = endPoint.y - startPoint.y;
		
		return Math.atan2(deltaY, deltaX);
	}
	
	public static Rectangle createBoundsFromPath(Path path, float lineWidth) {
		float[] rawBounds = new float[4];
		path.getBounds(rawBounds);
		return new Rectangle((int) Math.floor(rawBounds[0] - lineWidth / 2f) , (int) Math.floor(rawBounds[1] - lineWidth / 2f), (int) Math.ceil(rawBounds[2] + lineWidth), (int) Math.ceil(rawBounds[3] + lineWidth));
	}
	
	public static Rectangle rectangle2DToDraw2DRectangle(Rectangle2D nodeBounds) {
		return new Rectangle((int) nodeBounds.getX(), (int) nodeBounds.getY(), (int) nodeBounds.getWidth(), (int) nodeBounds.getHeight());
	}
}
