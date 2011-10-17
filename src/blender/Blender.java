package blender;

import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import util.Timer;

public abstract class Blender implements Runnable{
	protected final BufferedImage img1;
	protected final BufferedImage img2;
	protected final GUI window;
	protected int[] imageBuffer;
	protected MemoryImageSource imageSource;
	protected final int height;
	protected final int width;
	private long runtime;

	protected static final double weight = 0.5;

	public Blender(BufferedImage img1, BufferedImage img2, GUI window,
			int[] imageBuffer, MemoryImageSource imageSource) {
		this.img1 = img1;
		this.img2 = img2;
		this.window = window;
		this.imageBuffer = imageBuffer;
		this.imageSource = imageSource;
		this.height = img1.getHeight();
		this.width = img1.getWidth();
	}

	@Override
	public void run() {
		Timer.start();
		process();
		Timer.stop();
		runtime = Timer.getRuntime();
		notifyListeners();
	}

	private void notifyListeners() {
		PropertyChangeEvent e = new PropertyChangeEvent(this, "runtime", 0, runtime);
		for(PropertyChangeListener l: listeners)
			l.propertyChange(e);
	}

	public abstract void process();

	Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();
	
	public void addPropertyChangeListener(
			PropertyChangeListener propertyChangeListener) {
		this.listeners.add(propertyChangeListener);
	}
}
