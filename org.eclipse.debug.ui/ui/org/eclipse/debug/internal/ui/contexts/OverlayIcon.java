package org.eclipse.debug.internal.ui.contexts;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * An OverlayIcon consists of a main icon and an overlay icon
 * @ince 3.9
 */
public class OverlayIcon extends CompositeImageDescriptor {
	private Point fSize = null;
    private ImageDescriptor fBase;
    private ImageDescriptor fOverlay;
    
    /**
     * @param base the main image 
     * @param overlay the additional image (a pin for example) May be <code>null</code>
     */
    public OverlayIcon(ImageDescriptor base, ImageDescriptor overlay) {
        fBase = base;
        ImageData baseImageData = base.getImageData();
        fSize = new Point(baseImageData.width, baseImageData.height);
        fOverlay = overlay;
    }

    protected void drawCompositeImage(int width, int height) {
        ImageData bg;
        if (fBase == null || (bg = fBase.getImageData()) == null) {
			bg = DEFAULT_IMAGE_DATA;
		}
        drawImage(bg, 0, 0);
        drawPlus();
    }

    private void drawPlus() {
    	if (fOverlay == null) return;
        int x = getSize().x;
        ImageData id = fOverlay.getImageData();
        x -= id.width;
        drawImage(id, x, 0);
    }

    protected Point getSize() {
        return fSize;
    }

    public int hashCode() {
        return fBase.hashCode() + (fOverlay != null ? fOverlay.hashCode() : 0);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof OverlayIcon)) {
			return false;
		}
        OverlayIcon other = (OverlayIcon) obj;
        return fBase.equals(other.fBase) &&
        	   ((fOverlay == null && other.fOverlay == null) || (fOverlay != null && fOverlay.equals(other.fOverlay)));
    }
}
