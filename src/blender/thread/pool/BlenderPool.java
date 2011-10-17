package blender.thread.pool;

import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import blender.Blender;
import blender.GUI;

public class BlenderPool extends Blender {

	public BlenderPool(BufferedImage img1, BufferedImage img2, GUI window,
			int[] imageBuffer, MemoryImageSource imageSource) {
		super(img1, img2, window, imageBuffer, imageSource);

	}

	@Override
	public void process() {
		ExecutorService threadPool = Executors.newCachedThreadPool();
		
		int taskCount = 100;
		for (int i = 0; i < taskCount; i++) {
			int taskSize = height / taskCount;
			threadPool.execute(new BlenderTask(i * taskSize ,
					(i + 1) * taskSize ));
		}
	}

	class BlenderTask implements Runnable {

		private final int from;
		private final int to;

		public BlenderTask(int from, int to) {
			this.from = from;
			this.to = to;
		}

		@Override
		public void run() {
			int[] rgbim1 = new int[width];
			int[] rgbim2 = new int[width];

			for (int row = from; row < to; row++) {
				img1.getRGB(0, row, width, 1, rgbim1, 0, width);
				img2.getRGB(0, row, width, 1, rgbim2, 0, width);

				for (int col = 0; col < width; col++) {
					int rgb1 = rgbim1[col];
					int r1 = (rgb1 >> 16) & 255;
					int g1 = (rgb1 >> 8) & 255;
					int b1 = rgb1 & 255;

					int rgb2 = rgbim2[col];
					int r2 = (rgb2 >> 16) & 255;
					int g2 = (rgb2 >> 8) & 255;
					int b2 = rgb2 & 255;

					int r3 = (int) (r1 * weight + r2 * (1.0 - weight));
					int g3 = (int) (g1 * weight + g2 * (1.0 - weight));
					int b3 = (int) (b1 * weight + b2 * (1.0 - weight));

					imageBuffer[row * width + col] = new java.awt.Color(r3, g3,
							b3).getRGB();
				}

				imageSource.newPixels(0, row, width, 1, true);

			}
		}
	}
}
