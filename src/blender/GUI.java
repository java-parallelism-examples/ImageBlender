package blender;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import util.Timer;

import blender.parallel.BlenderParallel;
import blender.sequential.BlenderSequential;
import blender.thread.pool.BlenderPool;
import blender.threads.BlenderThreads;

public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;

	/* Configuration */
	private Dimension displaySize = new Dimension(400, 300);
	private String imgFile1Path = "img/40c.jpg";
	private String imgFile2Path = "img/dreamliner.jpg";
	/* ************** */

	private BufferedImage img1;
	private BufferedImage img2;

	private Dimension imageSize;
	private ImagePanel img1Panel;
	private ImagePanel img2Panel;
	ImagePanel resultImagePanel;
	public ImageIcon icon;

	private int[] resultBuffer;
	private MemoryImageSource resultImageSource;

	public GUI() {
		super("Blender");

		initFrame();
		setVisible(true);
	}

	public void initFrame() {

		try {
			setDefaultCloseOperation(EXIT_ON_CLOSE);
			setLayout(new BorderLayout());
			add(getImagesPanel(), BorderLayout.CENTER);
			add(getInfoPanel(), BorderLayout.SOUTH);
			
			pack();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private JPanel getImagesPanel() throws IOException {
		JPanel imgPanels = new JPanel();
		img1Panel = new ImagePanel();
		imgPanels.add(img1Panel);
		img2Panel = new ImagePanel();
		imgPanels.add(img2Panel);
		resultImagePanel = new ImagePanel();
		imgPanels.add(resultImagePanel);

		initImagePanels();

		resultBuffer = new int[imageSize.height * imageSize.width];
		resultImageSource = new MemoryImageSource(imageSize.width,
				imageSize.height, resultBuffer, 0, imageSize.width);

		resultImageSource.setAnimated(true);
		resultImagePanel.setImage(resultImagePanel
				.createImage(resultImageSource));

		resetResultsPanel();

		return imgPanels;
	}

	private JPanel getInfoPanel() {

		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new GridLayout(5, 1));

		// Sequential
		Blender sequentialBlender = new BlenderSequential(img1, img2,
				getFrame(), resultBuffer, resultImageSource);
		infoPanel.add(getPanelFor("Sequential", sequentialBlender));

		// Sequential
		Blender threadsBlender = new BlenderThreads(img1, img2, getFrame(),
				resultBuffer, resultImageSource);
		infoPanel.add(getPanelFor("Threads", threadsBlender));

		// Pool
		Blender poolBlender = new BlenderPool(img1, img2, getFrame(),
				resultBuffer, resultImageSource);
		infoPanel.add(getPanelFor("Thread pool", poolBlender));

		// Pool
		Blender forkJoinBlender = new BlenderPool(img1, img2, getFrame(),
				resultBuffer, resultImageSource);
		infoPanel.add(getPanelFor("ForkJoin", forkJoinBlender));
		
		Blender parallelBlender = new BlenderParallel(img1, img2, getFrame(),
				resultBuffer, resultImageSource);
		infoPanel.add(getPanelFor("Parallel", parallelBlender));

		return infoPanel;
	}

	private JPanel getPanelFor(String string, Blender blenderProcess) {
		JPanel panel = new JPanel();

		JButton button = new JButton(string);
		button.setPreferredSize(new Dimension(200, 25));
		panel.add(button);

		JLabel runtimeLabel = new JLabel();
		runtimeLabel.setPreferredSize(new Dimension(100, 25));
		panel.add(runtimeLabel);
		
		JLabel speedupLabel = new JLabel();
		speedupLabel.setPreferredSize(new Dimension(100, 25));
		panel.add(speedupLabel);
		
		ActionListener listener = new ButtonListener(blenderProcess, runtimeLabel, speedupLabel);
		button.addActionListener(listener);

		return panel;
	}

	public void resetResultsPanel() {
		for (int row = 0; row < imageSize.height; row++) {
			for (int col = 0; col < imageSize.width; col++) {
				resultBuffer[row * imageSize.width + col] = java.awt.Color.WHITE
						.getRGB();
			}
		}
		resultImageSource.newPixels();
	}

	public void initImagePanels() throws IOException {
		img1 = ImageIO.read(new File(imgFile1Path));
		img2 = ImageIO.read(new File(imgFile2Path));
		

		Graphics2D g2d = img1.createGraphics();
		g2d.drawImage(img1, 0, 0, null);
		g2d.dispose();

		g2d = img2.createGraphics();
		g2d.drawImage(img2, 0, 0, null);
		g2d.dispose();
		imageSize = new Dimension(img1.getWidth(), img1.getHeight());

		img1Panel.setImage(img1);
		img2Panel.setImage(img2);
	}

	public GUI getFrame() {
		return this;
	}

	protected long sequentialRuntime;
	private final class ButtonListener implements ActionListener {
		private final Blender blenderProcess;
		private final JLabel runtimeLabel;
		private final JLabel speedupLabel;

		public ButtonListener(Blender blenderProcess, JLabel runtimeLabel, JLabel speedupLabel) {
			this.runtimeLabel = runtimeLabel;
			
			this.blenderProcess = blenderProcess;
			this.speedupLabel = speedupLabel;
			
			runtimeLabel.setText(Timer.getRuntime()+"ms");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			resetResultsPanel();
			this.blenderProcess.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent e) {
						Long runtime = (Long) e.getNewValue();
						if(e.getSource() instanceof BlenderSequential)
							sequentialRuntime = (Long) runtime;
						runtimeLabel.setText(runtime+"ms");
						if(sequentialRuntime > 0) {
							double speedup = sequentialRuntime*1.0/runtime;
							String speedupText = String.format("%.2f", speedup);
							speedupLabel.setText(speedupText+"x");
						}
				}
			});
			Thread t = new Thread(this.blenderProcess);
			t.start();
		}
	}

	class ImagePanel extends JPanel {

		private static final long serialVersionUID = -6160628506441192086L;
		private Image img;

		public ImagePanel() {
			setPreferredSize(displaySize);

		}

		public void setImage(Image img) {
			this.img = img;
			repaint();
		}

		public void paintComponent(Graphics g) {
			if (img != null) {
				Dimension size = getSize();
				g.drawImage(img, 0, 0, size.width, size.height, 0, 0,
						img.getWidth(null), img.getHeight(null), this);
			}
		}
	}

	public static void main(String[] args) {

		@SuppressWarnings("unused")
		GUI window = new GUI();

	}

}