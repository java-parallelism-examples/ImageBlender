package blender.threads;

import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;

import blender.Blender;
import blender.GUI;

public class BlenderThreads extends Blender {

	public BlenderThreads(BufferedImage img1, BufferedImage img2, GUI window,
			int[] imageBuffer, MemoryImageSource imageSource) {
		super(img1, img2, window, imageBuffer, imageSource);

	}

	@Override
	public void process() {
		int processorCount = Runtime.getRuntime().availableProcessors();

		BlenderThread[] blenderThreads = new BlenderThread[processorCount];

		for (int i = 0; i < processorCount; i++) {
			blenderThreads[i] = new BlenderThread(i * height / processorCount,
					(i + 1) * height / processorCount);
			blenderThreads[i].start();
		}

		try {
			for (int i = 0; i < processorCount; i++) {
				blenderThreads[i].join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class BlenderThread extends Thread {

		private final int from;
		private final int to;

		public BlenderThread(int from, int to) {
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
