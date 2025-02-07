import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class RiverGUI {
    public static void runRiverGeneration(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java RiverCracker generate <seed> <output.png>");
            return;
        }
        long seed = Long.parseLong(args[0]);
        String outputPath = args[1];

        RiverGenerator.LayerStack stack = new RiverGenerator.LayerStack(11);
        RiverGenerator.setupGenerator(stack, seed);

        int size = 2048;
        int half = size / 2;
        int[] out = new int[size * size];
        RiverGenerator.genArea(stack.entry, out, -half, -half, size, size);

        
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                int val = out[x + z * size];
                int color = (val == RiverGenerator.RIVER) ? 0x0000FF : 0xFFFFFF;
                img.setRGB(x, z, color);
            }
        }

        
        try {
            ImageIO.write(img, "png", new File(outputPath));
            System.out.println("Wrote " + outputPath + " with seed=" + seed);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void initBiomes() {
    }
    public static BufferedImage generateRiverMapImage(long worldSeed, int centerX, int centerZ) {
        final int width = 1024;
        final int height = 1024;

        RiverGenerator.LayerStack stack = new RiverGenerator.LayerStack(11);
        RiverGenerator.setupGenerator(stack, worldSeed);
        RiverGenerator.Layer finalLayer = stack.entry;

        int[] biomesArray = new int[width * height];

        int startX = centerX - (width / 2);
        int startZ = centerZ - (height / 2);

        RiverGenerator.genArea(finalLayer, biomesArray, startX, startZ, width, height);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int biomeId = biomesArray[i + j * width];
                boolean isRiver = (biomeId == RiverGenerator.RIVER);
                int color = isRiver ? 0x0000FF : 0x000000;
                image.setRGB(i, j, color);
            }
        }
        return image;
    }

    static class RiverMapPanel extends JPanel {
        private BufferedImage image;

        public void setImage(BufferedImage img) {
            this.image = img;
            setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            revalidate();
        }

        public BufferedImage getImage() {
            return image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int x = (getWidth() - image.getWidth()) / 2;
                int y = (getHeight() - image.getHeight()) / 2;
                g.drawImage(image, x, y, this);
            }
        }
    }
    public static void showGUI() {
        initBiomes();

        JFrame frame = new JFrame("River Map Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        RiverMapPanel imagePanel = new RiverMapPanel();
        imagePanel.setPreferredSize(new Dimension(1024, 1024));
        frame.add(new JScrollPane(imagePanel), BorderLayout.CENTER);

        JPanel controlPanel = new JPanel(new FlowLayout());
        JLabel seedLabel = new JLabel("World Seed:");
        JTextField seedField = new JTextField("0", 10);
        JLabel centerXLabel = new JLabel("Center X:");
        JTextField centerXField = new JTextField("0", 5);
        JLabel centerZLabel = new JLabel("Center Z:");
        JTextField centerZField = new JTextField("0", 5);

        JButton generateButton = new JButton("Generate");
        JButton exportButton = new JButton("Export Image");

        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long seed;
                int centerX, centerZ;
                try {
                    seed = Long.parseLong(seedField.getText().trim());
                    centerX = Integer.parseInt(centerXField.getText().trim());
                    centerZ = Integer.parseInt(centerZField.getText().trim());
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input. Please enter valid numbers for seed, center X, and center Z.");
                    return;
                }
                BufferedImage img = generateRiverMapImage(seed, centerX, centerZ);
                imagePanel.setImage(img);
                imagePanel.repaint();
            }
        });

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage img = imagePanel.getImage();
                if (img == null) {
                    JOptionPane.showMessageDialog(frame, "No image available to export.", "Export Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showSaveDialog(frame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".png")) {
                        file = new File(file.getAbsolutePath() + ".png");
                    }
                    try {
                        ImageIO.write(img, "png", file);
                        JOptionPane.showMessageDialog(frame, "Image saved successfully to: " + file.getAbsolutePath(), "Export", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(frame, "Error saving image: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        controlPanel.add(seedLabel);
        controlPanel.add(seedField);
        controlPanel.add(centerXLabel);
        controlPanel.add(centerXField);
        controlPanel.add(centerZLabel);
        controlPanel.add(centerZField);
        controlPanel.add(generateButton);
        controlPanel.add(exportButton);

        frame.add(controlPanel, BorderLayout.NORTH);

        BufferedImage initialImage = generateRiverMapImage(0, 0, 0);
        imagePanel.setImage(initialImage);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
