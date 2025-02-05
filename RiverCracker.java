import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


public class RiverCracker {
    //======================================================================
    // 1) Minecraft Versions
    //======================================================================
    public enum MCVersion {
        MC_1_7, MC_1_8, MC_1_9, MC_1_10, MC_1_11, MC_1_12, MC_1_13, MC_1_14, MC_1_15, MC_1_16, MC_1_17,
        MC_BE
    }

    public static MCVersion parseVersion(String ver) {
        // you could also add more error-checking or defaulting
        String underscore = ver.replace('.', '_');
        // e.g. "1.12" -> "1_12"
        // Then we prepend "MC_"
        String enumName = "MC_" + underscore;
        try {
            return MCVersion.valueOf(enumName);
        } catch (Exception e) {
            // fallback
            System.err.println("Unsupported version string: " + ver + ", defaulting to 1.12");
            return MCVersion.MC_1_12;
        }
    }
    public static final int NONE = -1;
    public static final int OCEAN = 0;
    public static final int PLAINS = 1;
    public static final int DESERT = 2;
    public static final int MOUNTAINS = 3;   // extremeHills
    public static final int FOREST = 4;
    public static final int TAIGA = 5;
    public static final int SWAMP = 6;   // swampland
    public static final int RIVER = 7;
    public static final int NETHER_WASTES = 8;   // hell
    public static final int THE_END = 9;   // sky
    public static final int FROZEN_OCEAN = 10;
    public static final int FROZEN_RIVER = 11;
    public static final int SNOWY_TUNDRA = 12;  // icePlains
    public static final int SNOWY_MOUNTAINS = 13;  // iceMountains
    public static final int MUSHROOM_FIELDS = 14;  // mushroomIsland
    public static final int MUSHROOM_FIELD_SHORE = 15; // mushroomIslandShore
    public static final int BEACH = 16;
    public static final int DESERT_HILLS = 17;
    public static final int WOODED_HILLS = 18;  // forestHills
    public static final int TAIGA_HILLS = 19;
    public static final int MOUNTAIN_EDGE = 20;  // extremeHillsEdge
    public static final int JUNGLE = 21;
    public static final int JUNGLE_HILLS = 22;
    public static final int JUNGLE_EDGE = 23;
    public static final int DEEP_OCEAN = 24;
    public static final int STONE_SHORE = 25;  // stoneBeach
    public static final int SNOWY_BEACH = 26;  // coldBeach
    public static final int BIRCH_FOREST = 27;
    public static final int BIRCH_FOREST_HILLS = 28;
    public static final int DARK_FOREST = 29;  // roofedForest
    public static final int SNOWY_TAIGA = 30;  // coldTaiga
    public static final int SNOWY_TAIGA_HILLS = 31;  // coldTaigaHills
    public static final int GIANT_TREE_TAIGA = 32;  // megaTaiga
    public static final int GIANT_TREE_TAIGA_HILLS = 33; // megaTaigaHills
    public static final int WOODED_MOUNTAINS = 34;  // extremeHillsPlus
    public static final int SAVANNA = 35;
    public static final int SAVANNA_PLATEAU = 36;
    public static final int BADLANDS = 37;  // mesa
    public static final int WOODED_BADLANDS_PLATEAU = 38; // mesaPlateau_F
    public static final int BADLANDS_PLATEAU = 39;  // mesaPlateau
    public static final int SMALL_END_ISLANDS = 40;
    public static final int END_MIDLANDS = 41;
    public static final int END_HIGHLANDS = 42;
    public static final int END_BARRENS = 43;
    public static final int WARM_OCEAN = 44;
    public static final int LUKEWARM_OCEAN = 45;
    public static final int COLD_OCEAN = 46;
    // 47 is not used here
    public static final int DEEP_WARM_OCEAN = 48;
    public static final int DEEP_LUKEWARM_OCEAN = 49;
    public static final int DEEP_COLD_OCEAN = 50;
    public static final int DEEP_FROZEN_OCEAN = 51;
    public static final int THE_VOID = 127;


    public static final int BAMBOO_JUNGLE = 168;
    public static final int BAMBOO_JUNGLE_HILLS = 169;
    public static final int SOUL_SAND_VALLEY = 170;
    public static final int CRIMSON_FOREST = 171;
    public static final int WARPED_FOREST = 172;
    public static final int BASALT_DELTAS = 173;


    public static final int VOID_TYPE = -1;
    public static final int OCEAN_TYPE = 0;
    public static final int PLAINS_TYPE = 1;
    public static final int DESERT_TYPE = 2;
    public static final int HILLS_TYPE = 3;
    public static final int FOREST_TYPE = 4;
    public static final int TAIGA_TYPE = 5;
    public static final int SWAMP_TYPE = 6;
    public static final int RIVER_TYPE = 7;
    public static final int NETHER_TYPE = 8;
    public static final int SKY_TYPE = 9;
    public static final int SNOW_TYPE = 10;
    public static final int MUSHROOM_TYPE = 11;
    public static final int BEACH_TYPE = 12;
    public static final int JUNGLE_TYPE = 13;
    public static final int STONEBEACH_TYPE = 14;
    public static final int SAVANNA_TYPE = 15;
    public static final int MESA_TYPE = 16;

    public static final int OCEANIC_TEMP = 0;
    public static final int WARM_TEMP = 1;
    public static final int LUSH_TEMP = 2;
    public static final int COLD_TEMP = 3;
    public static final int FREEZING_TEMP = 4;
    public static final int SPECIAL_TEMP = 5;

    public static class Biome {
        public int id;
        public int r, g, b;  // color for rendering
        public boolean valid;

        // Extra fields from Layers.c:
        public int type;        // e.g. OCEAN_TYPE, FOREST_TYPE, etc.
        public double height;   // used in the C code (like 0.1, -1.0, etc.)
        public double temp;     // used in the C code
        public int tempCat;     // e.g. OCEANIC_TEMP, WARM_TEMP, ...
        public int mutated;     // mutated variant ID or -1

        public Biome(int id, int r, int g, int b) {
            this.id = id;
            this.r = r;
            this.g = g;
            this.b = b;
            this.valid = true;
            this.type = VOID_TYPE;
            this.temp = 0.5;
            this.height = 0.0;
            this.tempCat = VOID_TYPE;
            this.mutated = -1;
        }
    }

    // We'll store up to 256 biomes:
    public static Biome[] biomes = new Biome[256];

    // Helper used by the "initBiomes()" to set color & mark valid
    private static void setBiomeColor(int id, int r, int g, int b) {
        if (id < 0 || id >= 256) return;
        biomes[id] = new Biome(id, r, g, b);
        biomes[id].valid = true;
    }

    /**
     * Helper that sets the fields "type, temp, height, tempCat, mutated"
     * (similar to initAddBiome in Layers.c).
     */
    private static void initAddBiome(int id, int tempCat, int biomeType, double temp, double height) {
        if (id < 0 || id >= 256) return;
        biomes[id].id = id;
        biomes[id].type = biomeType;
        biomes[id].temp = temp;
        biomes[id].height = height;
        biomes[id].tempCat = tempCat;
        biomes[id].mutated = -1;  // default
    }

    /**
     * Helper that creates a mutated version: e.g. plains -> sunflower_plains, etc.
     * The mutated ID is `id + 128`.
     */
    private static void createMutation(int id) {
        if (id < 0 || id + 128 >= 256) return;
        biomes[id].mutated = id + 128;
        // copy fields
        biomes[id + 128] = new Biome(id + 128, biomes[id].r, biomes[id].g, biomes[id].b);
        // copy the extended fields:
        biomes[id + 128].type = biomes[id].type;
        biomes[id + 128].temp = biomes[id].temp;
        biomes[id + 128].height = biomes[id].height;
        biomes[id + 128].tempCat = biomes[id].tempCat;
        // color is the same initially; you may adjust if needed
        biomes[id + 128].valid = true;
        biomes[id + 128].mutated = -1; // mutated-of-mutated is not used
    }

    //======================================================================
    // 4) Our revised initBiomes() that sets both color + type/height/...
    //    This merges your existing color definitions with the "Layers.c" logic.
    //======================================================================
    public static void initBiomes() {
        // Initialize every slot as invalid first.
        for (int i = 0; i < 256; i++) {
            biomes[i] = new Biome(i, 0, 0, 0);
            biomes[i].valid = false;
            biomes[i].type = VOID_TYPE;
            biomes[i].temp = 0.5;
            biomes[i].height = 0.0;
            biomes[i].tempCat = VOID_TYPE;
            biomes[i].mutated = -1;
        }

        // First do color sets for known IDs
        setBiomeColor(OCEAN, 0, 0, 112);
        setBiomeColor(PLAINS, 0, 0, 0);
        setBiomeColor(DESERT, 250, 148, 24);
        setBiomeColor(MOUNTAINS, 96, 96, 96);
        setBiomeColor(FOREST, 5, 102, 33);
        setBiomeColor(TAIGA, 11, 102, 89);
        setBiomeColor(SWAMP, 7, 249, 178);
        setBiomeColor(RIVER, 0, 0, 255);
        setBiomeColor(FROZEN_OCEAN, 112, 112, 214);
        setBiomeColor(FROZEN_RIVER, 160, 160, 255);
        setBiomeColor(SNOWY_TUNDRA, 255, 255, 255);
        setBiomeColor(SNOWY_MOUNTAINS, 160, 160, 160);
        setBiomeColor(MUSHROOM_FIELDS, 255, 0, 255);
        setBiomeColor(MUSHROOM_FIELD_SHORE, 160, 0, 255);
        setBiomeColor(BEACH, 250, 222, 85);
        setBiomeColor(DESERT_HILLS, 210, 95, 18);
        setBiomeColor(WOODED_HILLS, 34, 85, 28);
        setBiomeColor(TAIGA_HILLS, 22, 57, 51);
        setBiomeColor(DEEP_OCEAN, 0, 0, 48);
        setBiomeColor(WARM_OCEAN, 0, 0, 172);
        setBiomeColor(LUKEWARM_OCEAN, 0, 0, 144);
        setBiomeColor(COLD_OCEAN, 32, 32, 112);
        setBiomeColor(DEEP_WARM_OCEAN, 0, 0, 80);
        setBiomeColor(DEEP_LUKEWARM_OCEAN, 0, 0, 64);
        setBiomeColor(DEEP_COLD_OCEAN, 32, 32, 56);
        setBiomeColor(DEEP_FROZEN_OCEAN, 64, 64, 144);
        setBiomeColor(STONE_SHORE, 162, 162, 132);
        setBiomeColor(SNOWY_BEACH, 250, 240, 192);
        setBiomeColor(BIRCH_FOREST, 48, 116, 68);
        setBiomeColor(BIRCH_FOREST_HILLS, 31, 95, 50);
        setBiomeColor(DARK_FOREST, 64, 81, 26);
        setBiomeColor(SNOWY_TAIGA, 49, 85, 74);
        setBiomeColor(SNOWY_TAIGA_HILLS, 36, 63, 54);
        setBiomeColor(GIANT_TREE_TAIGA, 89, 102, 81);
        setBiomeColor(GIANT_TREE_TAIGA_HILLS, 69, 79, 62);
        setBiomeColor(WOODED_MOUNTAINS, 80, 112, 80);
        setBiomeColor(SAVANNA, 189, 178, 95);
        setBiomeColor(SAVANNA_PLATEAU, 167, 157, 100);
        setBiomeColor(BADLANDS, 217, 69, 21);
        setBiomeColor(WOODED_BADLANDS_PLATEAU, 176, 151, 101);
        setBiomeColor(BADLANDS_PLATEAU, 202, 140, 101);

        // 1.13
        setBiomeColor(SMALL_END_ISLANDS, 75, 75, 171);
        setBiomeColor(END_MIDLANDS, 201, 201, 89);
        setBiomeColor(END_HIGHLANDS, 181, 181, 54);
        setBiomeColor(END_BARRENS, 112, 112, 204);

        // 1.14
        setBiomeColor(BAMBOO_JUNGLE, 118, 142, 20);
        setBiomeColor(BAMBOO_JUNGLE_HILLS, 59, 71, 10);

        // 1.16
        setBiomeColor(SOUL_SAND_VALLEY, 77, 58, 46);
        setBiomeColor(CRIMSON_FOREST, 152, 26, 17);
        setBiomeColor(WARPED_FOREST, 73, 144, 123);
        setBiomeColor(BASALT_DELTAS, 100, 95, 99);

        // Now replicate the core logic from Layers.c -> initBiomes()

        // Some reference heights:
        double hDefault = 0.1, hShallowWaters = -0.5, hOceans = -1.0,
                hDeepOceans = -1.8, hLowPlains = 0.125, hMidPlains = 0.2,
                hLowHills = 0.45, hHighPlateaus = 1.5, hMidHills = 1.0,
                hShores = 0.0, hRockyWaters = 0.1, hLowIslands = 0.2,
                hPartiallySubmerged = -0.2;

        // initAddBiome(id, tempCategory, biomeType, temp, height)
        initAddBiome(OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hOceans);
        initAddBiome(PLAINS, LUSH_TEMP, PLAINS_TYPE, 0.8, hDefault);
        initAddBiome(DESERT, WARM_TEMP, DESERT_TYPE, 2.0, hLowPlains);
        initAddBiome(MOUNTAINS, LUSH_TEMP, HILLS_TYPE, 0.2, hMidHills);
        initAddBiome(FOREST, LUSH_TEMP, FOREST_TYPE, 0.7, hDefault);
        initAddBiome(TAIGA, LUSH_TEMP, TAIGA_TYPE, 0.25, hMidPlains);
        initAddBiome(SWAMP, LUSH_TEMP, SWAMP_TYPE, 0.8, hPartiallySubmerged);
        initAddBiome(RIVER, LUSH_TEMP, RIVER_TYPE, 0.5, hShallowWaters);
        initAddBiome(NETHER_WASTES, WARM_TEMP, NETHER_TYPE, 2.0, hDefault);
        initAddBiome(THE_END, LUSH_TEMP, SKY_TYPE, 0.5, hDefault);
        initAddBiome(FROZEN_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.0, hOceans);
        initAddBiome(FROZEN_RIVER, COLD_TEMP, RIVER_TYPE, 0.0, hShallowWaters);
        initAddBiome(SNOWY_TUNDRA, COLD_TEMP, SNOW_TYPE, 0.0, hLowPlains);
        initAddBiome(SNOWY_MOUNTAINS, COLD_TEMP, SNOW_TYPE, 0.0, hLowHills);
        initAddBiome(MUSHROOM_FIELDS, LUSH_TEMP, MUSHROOM_TYPE, 0.9, hLowIslands);
        initAddBiome(MUSHROOM_FIELD_SHORE, LUSH_TEMP, MUSHROOM_TYPE, 0.9, hShores);
        initAddBiome(BEACH, LUSH_TEMP, BEACH_TYPE, 0.8, hShores);
        initAddBiome(DESERT_HILLS, WARM_TEMP, DESERT_TYPE, 2.0, hLowHills);
        initAddBiome(WOODED_HILLS, LUSH_TEMP, FOREST_TYPE, 0.7, hLowHills);
        initAddBiome(TAIGA_HILLS, LUSH_TEMP, TAIGA_TYPE, 0.25, hLowHills);
        initAddBiome(MOUNTAIN_EDGE, LUSH_TEMP, HILLS_TYPE, 0.2, hMidHills);
        initAddBiome(JUNGLE, LUSH_TEMP, JUNGLE_TYPE, 0.95, hDefault);
        initAddBiome(JUNGLE_HILLS, LUSH_TEMP, JUNGLE_TYPE, 0.95, hLowHills);
        initAddBiome(JUNGLE_EDGE, LUSH_TEMP, JUNGLE_TYPE, 0.95, hDefault);
        initAddBiome(DEEP_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hDeepOceans);
        initAddBiome(STONE_SHORE, LUSH_TEMP, STONEBEACH_TYPE, 0.2, hRockyWaters);
        initAddBiome(SNOWY_BEACH, COLD_TEMP, BEACH_TYPE, 0.05, hShores);
        initAddBiome(BIRCH_FOREST, LUSH_TEMP, FOREST_TYPE, 0.6, hDefault);
        initAddBiome(BIRCH_FOREST_HILLS, LUSH_TEMP, FOREST_TYPE, 0.6, hLowHills);
        initAddBiome(DARK_FOREST, LUSH_TEMP, FOREST_TYPE, 0.7, hDefault);
        initAddBiome(SNOWY_TAIGA, COLD_TEMP, TAIGA_TYPE, -0.5, hMidPlains);
        initAddBiome(SNOWY_TAIGA_HILLS, COLD_TEMP, TAIGA_TYPE, -0.5, hLowHills);
        initAddBiome(GIANT_TREE_TAIGA, LUSH_TEMP, TAIGA_TYPE, 0.3, hMidPlains);
        initAddBiome(GIANT_TREE_TAIGA_HILLS, LUSH_TEMP, TAIGA_TYPE, 0.3, hLowHills);
        initAddBiome(WOODED_MOUNTAINS, LUSH_TEMP, HILLS_TYPE, 0.2, hMidHills);
        initAddBiome(SAVANNA, WARM_TEMP, SAVANNA_TYPE, 1.2, hLowPlains);
        initAddBiome(SAVANNA_PLATEAU, WARM_TEMP, SAVANNA_TYPE, 1.0, hHighPlateaus);
        initAddBiome(BADLANDS, WARM_TEMP, MESA_TYPE, 2.0, hDefault);
        initAddBiome(WOODED_BADLANDS_PLATEAU, WARM_TEMP, MESA_TYPE, 2.0, hHighPlateaus);
        initAddBiome(BADLANDS_PLATEAU, WARM_TEMP, MESA_TYPE, 2.0, hHighPlateaus);

        initAddBiome(SMALL_END_ISLANDS, LUSH_TEMP, SKY_TYPE, 0.5, hDefault);
        initAddBiome(END_MIDLANDS, LUSH_TEMP, SKY_TYPE, 0.5, hDefault);
        initAddBiome(END_HIGHLANDS, LUSH_TEMP, SKY_TYPE, 0.5, hDefault);
        initAddBiome(END_BARRENS, LUSH_TEMP, SKY_TYPE, 0.5, hDefault);
        initAddBiome(WARM_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hOceans);
        initAddBiome(LUKEWARM_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hOceans);
        initAddBiome(COLD_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hOceans);
        initAddBiome(DEEP_WARM_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hDeepOceans);
        initAddBiome(DEEP_LUKEWARM_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hDeepOceans);
        initAddBiome(DEEP_COLD_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hDeepOceans);
        initAddBiome(DEEP_FROZEN_OCEAN, OCEANIC_TEMP, OCEAN_TYPE, 0.5, hDeepOceans);

        initAddBiome(THE_VOID, VOID_TYPE, VOID_TYPE, 0.5, 0);

        // create mutated variants
        createMutation(PLAINS);
        createMutation(DESERT);
        createMutation(MOUNTAINS);
        createMutation(FOREST);
        createMutation(TAIGA);
        createMutation(SWAMP);
        createMutation(SNOWY_TUNDRA);
        createMutation(JUNGLE);
        createMutation(JUNGLE_EDGE);
        createMutation(BIRCH_FOREST);
        createMutation(BIRCH_FOREST_HILLS);
        createMutation(DARK_FOREST);
        createMutation(SNOWY_TAIGA);
        createMutation(GIANT_TREE_TAIGA);
        createMutation(GIANT_TREE_TAIGA_HILLS);
        createMutation(WOODED_MOUNTAINS);
        createMutation(SAVANNA);
        createMutation(SAVANNA_PLATEAU);
        createMutation(BADLANDS);
        createMutation(WOODED_BADLANDS_PLATEAU);
        createMutation(BADLANDS_PLATEAU);

        initAddBiome(BAMBOO_JUNGLE, LUSH_TEMP, JUNGLE_TYPE, 0.95, hDefault);
        initAddBiome(BAMBOO_JUNGLE_HILLS, LUSH_TEMP, JUNGLE_TYPE, 0.95, hLowHills);

        initAddBiome(SOUL_SAND_VALLEY, WARM_TEMP, NETHER_TYPE, 2.0, hDefault);
        initAddBiome(CRIMSON_FOREST, WARM_TEMP, NETHER_TYPE, 2.0, hDefault);
        initAddBiome(WARPED_FOREST, WARM_TEMP, NETHER_TYPE, 2.0, hDefault);
        initAddBiome(BASALT_DELTAS, WARM_TEMP, NETHER_TYPE, 2.0, hDefault);

        // Mark them valid if the ID is correct
        for (int i = 0; i < 256; i++) {
            if (biomes[i].id == i) {
                biomes[i].valid = true;
            }
        }
    }
    // In your showGUI() method, add two new text fields for centerX and centerZ:
    public static void showGUI() {
        // Make sure the biome definitions are initialized.
        initBiomes();

        // Create the main frame.
        JFrame frame = new JFrame("River Map Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Create a panel that will display the generated image.
        RiverMapPanel imagePanel = new RiverMapPanel();
        imagePanel.setPreferredSize(new Dimension(1024, 1024));
        frame.add(imagePanel, BorderLayout.CENTER);

        // Create a control panel at the top with input fields and a button.
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        // Seed field
        JLabel seedLabel = new JLabel("World Seed:");
        JTextField seedField = new JTextField("0", 10);
        controlPanel.add(seedLabel);
        controlPanel.add(seedField);
        // New center X field
        JLabel centerXLabel = new JLabel("Center X:");
        JTextField centerXField = new JTextField("0", 5);
        controlPanel.add(centerXLabel);
        controlPanel.add(centerXField);
        // New center Z field
        JLabel centerZLabel = new JLabel("Center Z:");
        JTextField centerZField = new JTextField("0", 5);
        controlPanel.add(centerZLabel);
        controlPanel.add(centerZField);

        // Generate button
        JButton generateButton = new JButton("Generate");
        controlPanel.add(generateButton);
        frame.add(controlPanel, BorderLayout.NORTH);

        // When the button is clicked, generate the river map image using the entered values.
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
                // Generate the new image (this may take a short time)
                BufferedImage img = generateRiverMapImage(seed, centerX, centerZ);
                imagePanel.setImage(img);
                imagePanel.repaint();
            }
        });

        // Generate an initial image using the default values (seed=0, centerX=0, centerZ=0).
        BufferedImage initialImage = generateRiverMapImage(0, 0, 0);
        imagePanel.setImage(initialImage);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    /**
     * Generates a river map image for the given seed.
     * The area covers x = -512 to 511 and z = -512 to 511 (1024x1024 pixels).
     * River pixels (cells equal to RiverCracker.RIVER) are drawn white; all others black.
     */
    /**
     * Generates a river map image for the given seed, centered at (centerX, centerZ).
     * The image is 1024x1024 pixels. The area covers:
     *   x from (centerX - 512) to (centerX + 511) and
     *   z from (centerZ - 512) to (centerZ + 511).
     */
    public static BufferedImage generateRiverMapImage(long worldSeed, int centerX, int centerZ) {
        final int width = 1024;
        final int height = 1024;

        // Create the biome generator layers.
        LayerStack stack = new LayerStack();
        // For this GUI we choose a default version and not large biomes.
        MCVersion mcver = MCVersion.MC_1_12;
        setupGenerator(stack, mcver);

        // Set the world seed on the final layer.
        setWorldSeed(stack.layers[L_VORONOI_ZOOM_1], worldSeed);
        Layer finalLayer = stack.entry_1;

        // Allocate an array to hold the biome IDs.
        int[] biomesArray = new int[width * height];

        // Calculate the starting coordinates so that the image is centered at (centerX, centerZ)
        int startX = centerX - (width / 2);
        int startZ = centerZ - (height / 2);

        // Generate the area.
        genArea(finalLayer, biomesArray, startX, startZ, width, height);

        // Create an image; we use TYPE_INT_RGB.
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Loop over every cell. For each cell, if the biome equals RIVER, paint white; otherwise black.
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int biomeId = biomesArray[i + j * width];
                // You can also include other river‐like biomes (e.g., FROZEN_RIVER) if desired.
                boolean isRiver = (biomeId == RIVER);
                int color = isRiver ? 0x0000FF : 0x000000; // white for river, black for everything else.
                image.setRGB(i, j, color);
            }
        }
        return image;
    }


    /**
     * A custom JPanel that displays a BufferedImage.
     */
    static class RiverMapPanel extends JPanel {
        private BufferedImage image;

        public void setImage(BufferedImage img) {
            this.image = img;
            // Adjust the preferred size if needed.
            setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // Center the image in the panel.
                int x = (getWidth() - image.getWidth()) / 2;
                int y = (getHeight() - image.getHeight()) / 2;
                g.drawImage(image, x, y, this);
            }
        }
    }
    /**
     * Check if a given seed has a river biome in *any* of the specified X/Z regions.
     * Returns true if found, false if not.
     */
    /**
     * Check if a given seed has at least one RIVER pixel in *each* of the specified X/Z regions.
     * Returns true if *all* regions contain a RIVER, false if at least one region does not.
     */
    public static boolean hasRiverInBoundingBox(
            long seed,
            MCVersion mcver,
            boolean largeBiomes,
            int xMin, int zMin,
            int xMax, int zMax
    ) {
        // 1) Create & set up the generator
        RiverCracker.LayerStack stack = new RiverCracker.LayerStack();
        if (!largeBiomes) {
            RiverCracker.setupGenerator(stack, mcver);
        } else {
            RiverCracker.setupLargeBiomesGenerator(stack, mcver);
        }

        // 2) Set world seed
        RiverCracker.setWorldSeed(stack.layers[RiverCracker.L_VORONOI_ZOOM_1], seed);
        RiverCracker.Layer finalLayer = stack.entry_1;

        // 3) Compute the *actual* width & height of your bounding box
        int actualWidth = xMax - xMin + 1;  // e.g. 2059..2063 => width=5
        int actualHeight = zMax - zMin + 1;  // e.g. 400..413 => height=14

        // 4) Decide how much padding to add.
        //    Usually 2 is enough for a few zoom levels.
        //    If you still see out-of-bounds, increase to 4.
        int pad = 12;

        // 5) Create the *bigger* area dimensions
        int bigWidth = actualWidth + pad * 2;
        int bigHeight = actualHeight + pad * 2;

        // 6) Allocate a bigger array
        int[] bigOut = new int[bigWidth * bigHeight];

        // 7) Call genArea for the bigger region
        //    starting from (xMin - pad, zMin - pad).
        //    That way, internal layers won't run out of bounds.
        RiverCracker.genArea(finalLayer, bigOut,
                xMin - pad, zMin - pad,
                bigWidth, bigHeight);

        // 8) Now, *within* bigOut, extract only the sub‐rectangle
        //    corresponding to [xMin..xMax, zMin..zMax].
        //    While extracting, check if any cell is RIVER.
        for (int localZ = 0; localZ < actualHeight; localZ++) {
            for (int localX = 0; localX < actualWidth; localX++) {
                // Convert (localX, localZ) in [0..actualWidth-1, 0..actualHeight-1]
                // to the index in bigOut:
                int bigIndex = (localZ + pad) * bigWidth + (localX + pad);

                int biomeID = bigOut[bigIndex];
                if (biomeID == RiverCracker.RIVER) {
                    return true;  // Found at least one RIVER cell in our bounding box
                }
            }
        }

        // If we reach here, no RIVER cell was found in that bounding box
        return false;
    }


    //======================================================================
    // 5) JavaRandom replication (48-bit).
    //======================================================================
    public static class JavaRandom {
        private long seed; // 48-bit seed

        public JavaRandom(long s) {
            setSeed(s);
        }

        public final void setSeed(long s) {
            seed = (s ^ 0x5deece66dL) & ((1L << 48) - 1);
        }

        private int next(int bits) {
            seed = (seed * 0x5deece66dL + 0xbL) & ((1L << 48) - 1);
            return (int) (seed >>> (48 - bits));
        }

        public int nextInt(int n) {
            if ((n & -n) == n) {
                return (int) ((n * (long) next(31)) >> 31);
            }
            int bits, val;
            do {
                bits = next(31);
                val = bits % n;
            } while (bits - val + (n - 1) < 0);
            return val;
        }

        public boolean nextBoolean() {
            return next(1) != 0;
        }

        public double nextDouble() {
            // standard java nextDouble approach
            long hi = (long) next(26);
            long lo = (long) next(27);
            return ((hi << 27) + lo) / (double) (1L << 53);
        }
    }

    //======================================================================
    // 6) Seeds, chunk seeds, etc.
    //======================================================================
    public static long mcStepSeed(long s, long salt) {
        return s * (s * 6364136223846793005L + 1442695040888963407L) + salt;
    }

    public static long getLayerSeed(long salt) {
        long ls = mcStepSeed(salt, salt);
        ls = mcStepSeed(ls, salt);
        ls = mcStepSeed(ls, salt);
        return ls;
    }

    public static long getChunkSeed(long ss, int x, int z) {
        long cs = ss + x;
        cs = mcStepSeed(cs, z);
        cs = mcStepSeed(cs, x);
        cs = mcStepSeed(cs, z);
        return cs;
    }
    public static long partner(long seed) {
        // There are four intervals in 26 bits:
        //   Interval A: 0-11118602 and Interval B: 11118603-22237205
        //   Interval C: 22237206-44673034 and Interval D: 44673035-67108863
        // The mapping for seeds in the first two intervals is:
        //   partner = 22237205 - seed,
        // and for the second two intervals:
        //   partner = 89346069 - seed.
        if (seed < 22237206L) {
            return 22237205L - seed;
        } else {
            return 89346069L - seed;
        }
    }

    public static int mcFirstInt(long s, int mod) {
        int ret = (int) ((s >>> 24) % mod);
        if (ret < 0) ret += mod;
        return ret;
    }

    public static boolean mcFirstIsZero(long s, int mod) {
        return ((s >>> 24) % mod) == 0;
    }

    //======================================================================
    // 7) Layers Setup
    //======================================================================
    public static final int L_ISLAND_4096 = 0;
    public static final int L_ZOOM_2048 = 1;
    public static final int L_ADD_ISLAND_2048 = 2;
    public static final int L_ZOOM_1024 = 3;
    public static final int L_ADD_ISLAND_1024A = 4;
    public static final int L_ADD_ISLAND_1024B = 5;
    public static final int L_ADD_ISLAND_1024C = 6;
    public static final int L_REMOVE_OCEAN_1024 = 7;
    public static final int L_ADD_SNOW_1024 = 8;
    public static final int L_ADD_ISLAND_1024D = 9;
    public static final int L_COOL_WARM_1024 = 10;
    public static final int L_HEAT_ICE_1024 = 11;
    public static final int L_SPECIAL_1024 = 12;
    public static final int L_ZOOM_512 = 13;
    public static final int L_ZOOM_256 = 14;
    public static final int L_ADD_ISLAND_256 = 15;
    public static final int L_ADD_MUSHROOM_256 = 16;
    public static final int L_DEEP_OCEAN_256 = 17;
    public static final int L_BIOME_256 = 18;
    public static final int L_ZOOM_128 = 19;
    public static final int L_ZOOM_64 = 20;
    public static final int L_BIOME_EDGE_64 = 21;
    public static final int L_RIVER_INIT_256 = 22;
    public static final int L_ZOOM_128_HILLS = 23;
    public static final int L_ZOOM_64_HILLS = 24;
    public static final int L_HILLS_64 = 25;
    public static final int L_RARE_BIOME_64 = 26;
    public static final int L_ZOOM_32 = 27;
    public static final int L_ADD_ISLAND_32 = 28;
    public static final int L_ZOOM_16 = 29;
    public static final int L_SHORE_16 = 30;
    public static final int L_ZOOM_8 = 31;
    public static final int L_ZOOM_4 = 32;
    public static final int L_SMOOTH_4 = 33;
    public static final int L_ZOOM_128_RIVER = 34;
    public static final int L_ZOOM_64_RIVER = 35;
    public static final int L_ZOOM_32_RIVER = 36;
    public static final int L_ZOOM_16_RIVER = 37;
    public static final int L_ZOOM_8_RIVER = 38;
    public static final int L_ZOOM_4_RIVER = 39;
    public static final int L_RIVER_4 = 40;
    public static final int L_SMOOTH_4_RIVER = 41;
    public static final int L_RIVER_MIX_4 = 42;
    public static final int L_VORONOI_ZOOM_1 = 43;

    // 1.13 layers
    public static final int L13_OCEAN_TEMP_256 = 44;
    public static final int L13_ZOOM_128 = 45;
    public static final int L13_ZOOM_64 = 46;
    public static final int L13_ZOOM_32 = 47;
    public static final int L13_ZOOM_16 = 48;
    public static final int L13_ZOOM_8 = 49;
    public static final int L13_ZOOM_4 = 50;
    public static final int L13_OCEAN_MIX_4 = 51;

    // 1.14 layer
    public static final int L14_BAMBOO_256 = 52;

    // largeBiomes chain
    public static final int L_ZOOM_LARGE_BIOME_A = 53;
    public static final int L_ZOOM_LARGE_BIOME_B = 54;

    public static final int L_NUM = 64;

    @FunctionalInterface
    public interface MapFunction {
        int apply(Layer l, int[] out, int x, int z, int w, int h);
    }

    public static class OceanRandom {
        public int[] d = new int[512];
        public double a, b, c;
    }

    public static class Layer {
        public long layerSeed;
        public long startSalt;
        public long startSeed;
        public OceanRandom oceanRnd;
        public MapFunction getMap;
        public Layer p, p2;
        public int scale;
        public int edge;
    }

    // Define a simple BoundingBox class.
    public static class BoundingBox {
        int xMin, zMin, xMax, zMax;

        public BoundingBox(int xMin, int zMin, int xMax, int zMax) {
            this.xMin = xMin;
            this.zMin = zMin;
            this.xMax = xMax;
            this.zMax = zMax;
        }
    }


    public static class LayerStack {
        public Layer[] layers = new Layer[L_NUM];
        public Layer entry_1;
        public Layer entry_4;
        public OceanRandom oceanRnd = new OceanRandom();

        public LayerStack() {
            for (int i = 0; i < L_NUM; i++) {
                layers[i] = new Layer();
            }
        }
    }

    public static void setWorldSeed(Layer layer, long worldSeed) {
        if (layer == null) return;
        if (layer.p2 != null &&
                layer.getMap != MAP_HILLS &&
                layer.getMap != MAP_HILLS_113) {
            setWorldSeed(layer.p2, worldSeed);
        }
        if (layer.p != null) {
            setWorldSeed(layer.p, worldSeed);
        }
        if (layer.oceanRnd != null) {
            oceanRndInit(layer.oceanRnd, worldSeed);
        }

        long st = worldSeed;
        st = mcStepSeed(st, layer.layerSeed);
        st = mcStepSeed(st, layer.layerSeed);
        st = mcStepSeed(st, layer.layerSeed);

        layer.startSalt = st;
        layer.startSeed = mcStepSeed(st, 0);
    }

    public static void setupLayer(Layer l, Layer p, int s, MapFunction func) {
        l.layerSeed = getLayerSeed(s);
        l.startSalt = 0;
        l.startSeed = 0;
        l.p = p;
        l.p2 = null;
        l.getMap = func;
        l.oceanRnd = null;
    }

    public static void setupMultiLayer(Layer l, Layer p1, Layer p2, int s, MapFunction func) {
        setupLayer(l, p1, s, func);
        l.p2 = p2;
    }

    public static void setupScale(Layer l, int scale) {
        if (l == null) return;
        l.scale = scale;
        int m = 1, e = 0;
        MapFunction map = l.getMap;

        if (map == MAP_ZOOM || map == MAP_ZOOM_ISLAND) {
            m = 2;
            e = 3;
        } else if (map == MAP_VORONOI_ZOOM) {
            m = 4;
            e = 7;
        } else if (map == MAP_OCEAN_MIX) {
            e = 17;
        } else if (map == MAP_ADD_ISLAND ||
                map == MAP_REMOVE_TOO_MUCH_OCEAN ||
                map == MAP_ADD_SNOW ||
                map == MAP_COOL_WARM ||
                map == MAP_HEAT_ICE ||
                map == MAP_ADD_MUSHROOM_ISLAND ||
                map == MAP_DEEP_OCEAN ||
                map == MAP_BIOME_EDGE ||
                map == MAP_HILLS ||
                map == MAP_HILLS_113 ||
                map == MAP_RIVER ||
                map == MAP_SMOOTH ||
                map == MAP_SHORE) {
            e = 2;
        } else {
            e = 0;
        }
        l.edge = e;

        if (l.p != null) {
            setupScale(l.p, scale * m);
        }
        if (l.p2 != null) {
            setupScale(l.p2, scale * m);
        }
    }

    public static void setupGeneratorImpl(LayerStack g, MCVersion mcversion, int largeBiomes) {
        // Zero out
        for (int i = 0; i < L_NUM; i++) {
            g.layers[i].layerSeed = 0;
            g.layers[i].startSalt = 0;
            g.layers[i].startSeed = 0;
            g.layers[i].p = null;
            g.layers[i].p2 = null;
            g.layers[i].getMap = null;
            g.layers[i].oceanRnd = null;
            g.layers[i].scale = 0;
            g.layers[i].edge = 0;
        }

        Layer[] l = g.layers;

        // Big chain
        setupLayer(l[L_ISLAND_4096], null, 1, MAP_ISLAND);
        setupLayer(l[L_ZOOM_2048], l[L_ISLAND_4096], 2000, MAP_ZOOM_ISLAND);
        setupLayer(l[L_ADD_ISLAND_2048], l[L_ZOOM_2048], 1, MAP_ADD_ISLAND);
        setupLayer(l[L_ZOOM_1024], l[L_ADD_ISLAND_2048], 2001, MAP_ZOOM);
        setupLayer(l[L_ADD_ISLAND_1024A], l[L_ZOOM_1024], 2, MAP_ADD_ISLAND);
        setupLayer(l[L_ADD_ISLAND_1024B], l[L_ADD_ISLAND_1024A], 50, MAP_ADD_ISLAND);
        setupLayer(l[L_ADD_ISLAND_1024C], l[L_ADD_ISLAND_1024B], 70, MAP_ADD_ISLAND);
        setupLayer(l[L_REMOVE_OCEAN_1024], l[L_ADD_ISLAND_1024C], 2, MAP_REMOVE_TOO_MUCH_OCEAN);
        setupLayer(l[L_ADD_SNOW_1024], l[L_REMOVE_OCEAN_1024], 2, MAP_ADD_SNOW);
        setupLayer(l[L_ADD_ISLAND_1024D], l[L_ADD_SNOW_1024], 3, MAP_ADD_ISLAND);
        setupLayer(l[L_COOL_WARM_1024], l[L_ADD_ISLAND_1024D], 2, MAP_COOL_WARM);
        setupLayer(l[L_HEAT_ICE_1024], l[L_COOL_WARM_1024], 2, MAP_HEAT_ICE);
        setupLayer(l[L_SPECIAL_1024], l[L_HEAT_ICE_1024], 3, MAP_SPECIAL);
        setupLayer(l[L_ZOOM_512], l[L_SPECIAL_1024], 2002, MAP_ZOOM);
        setupLayer(l[L_ZOOM_256], l[L_ZOOM_512], 2003, MAP_ZOOM);
        setupLayer(l[L_ADD_ISLAND_256], l[L_ZOOM_256], 4, MAP_ADD_ISLAND);
        setupLayer(l[L_ADD_MUSHROOM_256], l[L_ADD_ISLAND_256], 5, MAP_ADD_MUSHROOM_ISLAND);
        setupLayer(l[L_DEEP_OCEAN_256], l[L_ADD_MUSHROOM_256], 4, MAP_DEEP_OCEAN);

        // Biome chain
        if (mcversion != MCVersion.MC_BE) {
            setupLayer(l[L_BIOME_256], l[L_DEEP_OCEAN_256], 200, MAP_BIOME);
        } else {
            // bedrock
            setupLayer(l[L_BIOME_256], l[L_DEEP_OCEAN_256], 200, MAP_BIOME_BE);
        }

        boolean isAtLeast113 = (mcversion.ordinal() >= MCVersion.MC_1_13.ordinal());
        if (!isAtLeast113) {
            // up to 1.12
            setupLayer(l[L_ZOOM_128], l[L_BIOME_256], 1000, MAP_ZOOM);
        } else {
            // 1.13 or newer
            setupLayer(l[L14_BAMBOO_256], l[L_BIOME_256], 1001, MAP_ADD_BAMBOO);
            setupLayer(l[L_ZOOM_128], l[L14_BAMBOO_256], 1000, MAP_ZOOM);
        }
        setupLayer(l[L_ZOOM_64], l[L_ZOOM_128], 1001, MAP_ZOOM);
        setupLayer(l[L_BIOME_EDGE_64], l[L_ZOOM_64], 1000, MAP_BIOME_EDGE);

        // River chain
        setupLayer(l[L_RIVER_INIT_256], l[L_DEEP_OCEAN_256], 100, MAP_RIVER_INIT);
        setupLayer(l[L_ZOOM_128_HILLS], l[L_RIVER_INIT_256], 1000, MAP_ZOOM);
        setupLayer(l[L_ZOOM_64_HILLS], l[L_ZOOM_128_HILLS], 1001, MAP_ZOOM);

        // hills
        MapFunction hillsFunc = (!isAtLeast113) ? MAP_HILLS : MAP_HILLS_113;
        setupMultiLayer(l[L_HILLS_64], l[L_BIOME_EDGE_64], l[L_ZOOM_64_HILLS], 1000, hillsFunc);

        setupLayer(l[L_RARE_BIOME_64], l[L_HILLS_64], 1001, MAP_RARE_BIOME);
        setupLayer(l[L_ZOOM_32], l[L_RARE_BIOME_64], 1000, MAP_ZOOM);
        setupLayer(l[L_ADD_ISLAND_32], l[L_ZOOM_32], 3, MAP_ADD_ISLAND);
        setupLayer(l[L_ZOOM_16], l[L_ADD_ISLAND_32], 1001, MAP_ZOOM);
        setupLayer(l[L_SHORE_16], l[L_ZOOM_16], 1000, MAP_SHORE);
        setupLayer(l[L_ZOOM_8], l[L_SHORE_16], 1002, MAP_ZOOM);
        setupLayer(l[L_ZOOM_4], l[L_ZOOM_8], 1003, MAP_ZOOM);

        // Large Biomes option
        if (largeBiomes != 0) {
            setupLayer(l[L_ZOOM_LARGE_BIOME_A], l[L_ZOOM_4], 1004, MAP_ZOOM);
            setupLayer(l[L_ZOOM_LARGE_BIOME_B], l[L_ZOOM_LARGE_BIOME_A], 1005, MAP_ZOOM);
            setupLayer(l[L_SMOOTH_4], l[L_ZOOM_LARGE_BIOME_B], 1000, MAP_SMOOTH);
        } else {
            setupLayer(l[L_SMOOTH_4], l[L_ZOOM_4], 1000, MAP_SMOOTH);
        }

        // River chain
        setupLayer(l[L_ZOOM_128_RIVER], l[L_RIVER_INIT_256], 1000, MAP_ZOOM);
        setupLayer(l[L_ZOOM_64_RIVER], l[L_ZOOM_128_RIVER], 1001, MAP_ZOOM);
        setupLayer(l[L_ZOOM_32_RIVER], l[L_ZOOM_64_RIVER], 1000, MAP_ZOOM);
        setupLayer(l[L_ZOOM_16_RIVER], l[L_ZOOM_32_RIVER], 1001, MAP_ZOOM);
        setupLayer(l[L_ZOOM_8_RIVER], l[L_ZOOM_16_RIVER], 1002, MAP_ZOOM);
        setupLayer(l[L_ZOOM_4_RIVER], l[L_ZOOM_8_RIVER], 1003, MAP_ZOOM);
        setupLayer(l[L_RIVER_4], l[L_ZOOM_4_RIVER], 1, MAP_RIVER);
        setupLayer(l[L_SMOOTH_4_RIVER], l[L_RIVER_4], 1000, MAP_SMOOTH);

        setupMultiLayer(l[L_RIVER_MIX_4], l[L_SMOOTH_4], l[L_SMOOTH_4_RIVER], 100, MAP_RIVER_MIX);

        if (!isAtLeast113) {
            setupLayer(l[L_VORONOI_ZOOM_1], l[L_RIVER_MIX_4], 10, MAP_VORONOI_ZOOM);
            g.entry_4 = l[L_RIVER_MIX_4];
        } else {
            // 1.13 ocean
            setupLayer(l[L13_OCEAN_TEMP_256], null, 2, MAP_OCEAN_TEMP);
            l[L13_OCEAN_TEMP_256].oceanRnd = g.oceanRnd;
            setupLayer(l[L13_ZOOM_128], l[L13_OCEAN_TEMP_256], 2001, MAP_ZOOM);
            setupLayer(l[L13_ZOOM_64], l[L13_ZOOM_128], 2002, MAP_ZOOM);
            setupLayer(l[L13_ZOOM_32], l[L13_ZOOM_64], 2003, MAP_ZOOM);
            setupLayer(l[L13_ZOOM_16], l[L13_ZOOM_32], 2004, MAP_ZOOM);
            setupLayer(l[L13_ZOOM_8], l[L13_ZOOM_16], 2005, MAP_ZOOM);
            setupLayer(l[L13_ZOOM_4], l[L13_ZOOM_8], 2006, MAP_ZOOM);

            setupMultiLayer(l[L13_OCEAN_MIX_4], l[L_RIVER_MIX_4], l[L13_ZOOM_4], 100, MAP_OCEAN_MIX);

            setupLayer(l[L_VORONOI_ZOOM_1], l[L13_OCEAN_MIX_4], 10, MAP_VORONOI_ZOOM);
            g.entry_4 = l[L13_OCEAN_MIX_4];
        }

        setupScale(l[L_VORONOI_ZOOM_1], 1);
        g.entry_1 = l[L_VORONOI_ZOOM_1];
    }

    public static void setupGenerator(LayerStack g, MCVersion mc) {
        setupGeneratorImpl(g, mc, 0);
    }

    public static void setupLargeBiomesGenerator(LayerStack g, MCVersion mc) {
        setupGeneratorImpl(g, mc, 1);
    }

    public static int genArea(Layer layer, int[] out, int areaX, int areaZ, int w, int h) {
        Arrays.fill(out, 0);
        return layer.getMap.apply(layer, out, areaX, areaZ, w, h);
    }

    //======================================================================
    // 8) Ocean Random (for 1.13+ ocean layers)
    //======================================================================
    private static void oceanRndInit(OceanRandom rnd, long seed) {
        JavaRandom rand = new JavaRandom(seed);
        rnd.a = rand.nextDouble() * 256.0;
        rnd.b = rand.nextDouble() * 256.0;
        rnd.c = rand.nextDouble() * 256.0;
        for (int i = 0; i < 256; i++) {
            rnd.d[i] = i;
        }
        for (int i = 0; i < 256; i++) {
            int n3 = rand.nextInt(256 - i) + i;
            int tmp = rnd.d[i];
            rnd.d[i] = rnd.d[n3];
            rnd.d[n3] = tmp;
            rnd.d[i + 256] = rnd.d[i];
        }
    }

    //======================================================================
    // 9) The main map functions. Some are already shown (mapIsland, mapZoom, etc.).
    //    We now fill in the rest from Layers.c.
    //======================================================================

    // Already shown examples (mapIsland, mapZoomIsland, mapZoom).
    public static final MapFunction MAP_ISLAND = RiverCracker::mapIsland;
    public static final MapFunction MAP_ZOOM_ISLAND = RiverCracker::mapZoomIsland;
    public static final MapFunction MAP_ZOOM = RiverCracker::mapZoom;

    public static int mapIsland(Layer l, int[] out, int x, int z, int w, int h) {
        long ss = l.startSeed;
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                long cs = getChunkSeed(ss, i + x, j + z);
                // ORIGINAL: 10% chance to become land (1), otherwise ocean (0)
                // out[i + j * w] = mcFirstIsZero(cs, 10) ? 1 : 0;
                out[i + j * w] = 1; // always land
            }
        }
        // Ensure (0,0) is land if in range (optional)
        if (x > -w && x <= 0 && z > -h && z <= 0) {
            out[-x + -z * w] = 1;
        }
        return 0;
    }

    public static int mapZoomIsland(Layer l, int[] out, int x, int z, int w, int h) {
        int pX = x >> 1;
        int pZ = z >> 1;
        int pW = ((x + w) >> 1) - pX + 1;
        int pH = ((z + h) >> 1) - pZ + 1;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        int newW = pW << 1;
        int newH = pH << 1;
        int[] buf = new int[(newW + 1) * (newH + 1)];
        int st = (int) l.startSalt;
        int ss = (int) l.startSeed;

        for (int j = 0; j < pH; j++) {
            int idx = (j << 1) * newW;
            int v00 = out[(j) * pW];
            int v01 = (j + 1 < pH) ? out[(j + 1) * pW] : v00;
            for (int i = 0; i < pW; i++) {
                int v10 = (i + 1 < pW) ? out[i + 1 + j * pW] : v00;
                int v11 = (i + 1 < pW && j + 1 < pH) ? out[i + 1 + (j + 1) * pW] : v01;

                if (v00 == v01 && v00 == v10 && v00 == v11) {
                    buf[idx] = v00;
                    buf[idx + 1] = v00;
                    buf[idx + newW] = v00;
                    buf[idx + newW + 1] = v00;
                    idx += 2;
                } else {
                    int chunkX = (i + pX) << 1;
                    int chunkZ = (j + pZ) << 1;
                    int cs = ss;
                    cs += chunkX;
                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += chunkZ;
                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += chunkX;
                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += chunkZ;

                    buf[idx] = v00;
                    buf[idx + newW] = (((cs >> 24) & 1) != 0) ? v01 : v00;
                    idx++;

                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += st;
                    buf[idx] = (((cs >> 24) & 1) != 0) ? v10 : v00;

                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += st;
                    int r = (cs >> 24) & 3;
                    buf[idx + newW] = (r == 0 ? v00 : (r == 1 ? v10 : (r == 2 ? v01 : v11)));
                    idx++;
                }
                v00 = v10;
                v01 = v11;
            }
        }
        for (int j = 0; j < h; j++) {
            System.arraycopy(buf, (j + (z & 1)) * newW + (x & 1), out, j * w, w);
        }
        return 0;
    }

    public static int mapZoom(Layer l, int[] out, int x, int z, int w, int h) {
        int pX = x >> 1;
        int pZ = z >> 1;
        int pW = ((x + w) >> 1) - pX + 1;
        int pH = ((z + h) >> 1) - pZ + 1;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        int newW = pW << 1;
        int newH = pH << 1;
        int[] buf = new int[(newW + 1) * (newH + 1)];
        int st = (int) l.startSalt;
        int ss = (int) l.startSeed;

        for (int j = 0; j < pH; j++) {
            int idx = (j << 1) * newW;
            int v00 = out[(j) * pW];
            int v01 = (j + 1 < pH) ? out[(j + 1) * pW] : v00;
            for (int i = 0; i < pW; i++) {
                int v10 = (i + 1 < pW) ? out[i + 1 + j * pW] : v00;
                int v11 = (i + 1 < pW && j + 1 < pH) ? out[i + 1 + (j + 1) * pW] : v01;

                if (v00 == v01 && v00 == v10 && v00 == v11) {
                    buf[idx] = v00;
                    buf[idx + 1] = v00;
                    buf[idx + newW] = v00;
                    buf[idx + newW + 1] = v00;
                    idx += 2;
                } else {
                    int chunkX = (i + pX) << 1;
                    int chunkZ = (j + pZ) << 1;
                    int cs = ss;
                    cs += chunkX;
                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += chunkZ;
                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += chunkX;
                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += chunkZ;

                    buf[idx] = v00;
                    buf[idx + newW] = (((cs >> 24) & 1) != 0) ? v01 : v00;
                    idx++;

                    cs = (int) (cs * (cs * 1284865837L + 4150755663L));
                    cs += st;
                    buf[idx] = (((cs >> 24) & 1) != 0) ? v10 : v00;

                    buf[idx + newW] = select4(cs, st, v00, v01, v10, v11);
                    idx++;
                }
                v00 = v10;
                v01 = v11;
            }
        }
        for (int j = 0; j < h; j++) {
            System.arraycopy(buf, (j + (z & 1)) * newW + (x & 1), out, j * w, w);
        }
        return 0;
    }

    private static int select4(int cs, int st, int v00, int v01, int v10, int v11) {
        int cv00 = (v00 == v10 ? 1 : 0) + (v00 == v01 ? 1 : 0) + (v00 == v11 ? 1 : 0);
        int cv10 = (v10 == v01 ? 1 : 0) + (v10 == v11 ? 1 : 0);
        int cv01 = (v01 == v11 ? 1 : 0);
        if (cv00 > cv10 && cv00 > cv01) {
            return v00;
        } else if (cv10 > cv00 && cv10 > cv01) {
            return v10;
        } else if (cv01 > cv00) {
            return v01;
        } else {
            cs = (int) (cs * (cs * 1284865837L + 4150755663L));
            cs += st;
            int r = (cs >> 24) & 3;
            return (r == 0 ? v00 : r == 1 ? v10 : r == 2 ? v01 : v11);
        }
    }

    //======================================================================
    // 9b) We now fill in all the placeholders (mapAddIsland, mapRemoveTooMuchOcean, etc.)
    //     using direct translations from Layers.c
    //======================================================================

    // Additional small helpers from Layers.c:

    /**
     * Is it a shallow ocean variant? (ocean, frozen ocean, warm ocean, etc., but not deep_*)
     */
    static boolean isShallowOcean(int id) {
        return (id == OCEAN || id == FROZEN_OCEAN || id == WARM_OCEAN ||
                id == LUKEWARM_OCEAN || id == COLD_OCEAN);
    }

    /**
     * Is it a deep ocean variant?
     */
    static boolean isDeepOcean(int id) {
        return (id == DEEP_OCEAN || id == DEEP_WARM_OCEAN ||
                id == DEEP_LUKEWARM_OCEAN || id == DEEP_COLD_OCEAN ||
                id == DEEP_FROZEN_OCEAN);
    }

    /**
     * Is it any ocean or deep ocean?
     */
    static boolean isOceanic(int id) {
        switch (id) {
            case OCEAN:
            case FROZEN_OCEAN:
            case WARM_OCEAN:
            case LUKEWARM_OCEAN:
            case COLD_OCEAN:
            case DEEP_OCEAN:
            case DEEP_WARM_OCEAN:
            case DEEP_LUKEWARM_OCEAN:
            case DEEP_COLD_OCEAN:
            case DEEP_FROZEN_OCEAN:
                return true;
        }
        return false;
    }

    static boolean isBiomeSnowy(int id) {
        // The C code checks if temp < 0.1
        if (id < 0 || id >= 256 || !biomes[id].valid) return false;
        return (biomes[id].temp < 0.1);
    }

    /**
     * Checks if two IDs are "similar" in the sense used by the 'mapHills' etc.
     */
    static boolean areSimilar(int id1, int id2) {
        if (id1 == id2) return true;
        // special check for badlands plateau variants
        if ((id1 == WOODED_BADLANDS_PLATEAU || id1 == BADLANDS_PLATEAU) &&
                (id2 == WOODED_BADLANDS_PLATEAU || id2 == BADLANDS_PLATEAU)) {
            return true;
        }
        if (id1 < 0 || id1 >= 256 || id2 < 0 || id2 >= 256) return false;
        // The C code also checks mutated logic, but simplified here:
        return (biomes[id1].type == biomes[id2].type);
    }

    static boolean areSimilar113(int id1, int id2) {
        // for 1.13 hills logic
        if (id1 == id2) return true;
        if ((id1 == WOODED_BADLANDS_PLATEAU || id1 == BADLANDS_PLATEAU) &&
                (id2 == WOODED_BADLANDS_PLATEAU || id2 == BADLANDS_PLATEAU)) {
            return true;
        }
        if (id1 < 0 || id1 >= 256 || id2 < 0 || id2 >= 256) return false;
        return (biomes[id1].type == biomes[id2].type);
    }

    // Now the direct translations:

    public static final MapFunction MAP_ADD_ISLAND = LayersPort::mapAddIslandC;
    public static final MapFunction MAP_REMOVE_TOO_MUCH_OCEAN = LayersPort::mapRemoveTooMuchOceanC;
    public static final MapFunction MAP_ADD_SNOW = LayersPort::mapAddSnowC;
    public static final MapFunction MAP_COOL_WARM = LayersPort::mapCoolWarmC;
    public static final MapFunction MAP_HEAT_ICE = LayersPort::mapHeatIceC;
    public static final MapFunction MAP_SPECIAL = LayersPort::mapSpecialC;
    public static final MapFunction MAP_ADD_MUSHROOM_ISLAND = LayersPort::mapAddMushroomIslandC;
    public static final MapFunction MAP_DEEP_OCEAN = LayersPort::mapDeepOceanC;
    public static final MapFunction MAP_BIOME = (l, out, x, z, w, h) -> {
        return LayersPort.mapBiomeC(l, out, x, z, w, h, false);
    };
    public static final MapFunction MAP_BIOME_BE = (l, out, x, z, w, h) -> {
        return LayersPort.mapBiomeC(l, out, x, z, w, h, true);
    };
    public static final MapFunction MAP_ADD_BAMBOO = LayersPort::mapAddBambooC;
    public static final MapFunction MAP_RIVER_INIT = LayersPort::mapRiverInitC;
    public static final MapFunction MAP_BIOME_EDGE = LayersPort::mapBiomeEdgeC;
    public static final MapFunction MAP_HILLS = (l, out, x, z, w, h) -> {
        return LayersPort.mapHillsC(l, out, x, z, w, h, false);
    };
    public static final MapFunction MAP_HILLS_113 = (l, out, x, z, w, h) -> {
        return LayersPort.mapHillsC(l, out, x, z, w, h, true);
    };
    public static final MapFunction MAP_RIVER = LayersPort::mapRiverC;
    public static final MapFunction MAP_SMOOTH = LayersPort::mapSmoothC;
    public static final MapFunction MAP_RARE_BIOME = LayersPort::mapRareBiomeC;
    public static final MapFunction MAP_SHORE = LayersPort::mapShoreC;
    public static final MapFunction MAP_RIVER_MIX = LayersPort::mapRiverMixC;
    public static final MapFunction MAP_VORONOI_ZOOM = LayersPort::mapVoronoiZoom;
    public static final MapFunction MAP_OCEAN_TEMP = LayersPort::mapOceanTempC;
    public static final MapFunction MAP_OCEAN_MIX = LayersPort::mapOceanMix;

    //======================================================================
    // Main
    //======================================================================
    public static void main(String[] args) throws InterruptedException {
        // 1) Initialize biomes.
        initBiomes();
        if (args.length == 0) {
            SwingUtilities.invokeLater(RiverCracker::showGUI);
            return;
        }

        // 2) Decide MC version and largeBiomes.
        MCVersion mcver = MCVersion.MC_1_12;
        boolean largeBiomes = false;

        // 3) Process command-line arguments.
        // Separate numeric arguments from any file argument (ending with ".txt")
        List<String> numericArgs = new ArrayList<>();
        List<String> seedFileNames = new ArrayList<>();
        for (String arg : args) {
            if (arg.toLowerCase().endsWith(".txt")) {
                seedFileNames.add(arg);
            } else {
                numericArgs.add(arg);
            }
        }

        List<BoundingBox> boxes = new ArrayList<>();
        if (!numericArgs.isEmpty() && numericArgs.get(numericArgs.size() - 1).equalsIgnoreCase("scale")) {
            // Scale mode: number of numeric arguments (excluding "scale") must be a multiple of 3.
            int numNumeric = numericArgs.size() - 1;
            if (numNumeric % 3 != 0) {
                System.err.println("Error: In scale mode, the number of numeric arguments (excluding 'scale') must be a multiple of 3.");
                System.err.println("For example: java RiverCracker 100 200 512 scale");
                System.err.println("or: java RiverCracker 100 200 512 300 400 256 scale");
                System.exit(1);
            }
            for (int i = 0; i < numNumeric; i += 3) {
                try {
                    int centerX = Integer.parseInt(numericArgs.get(i));
                    int centerZ = Integer.parseInt(numericArgs.get(i + 1));
                    int scaleValue = Integer.parseInt(numericArgs.get(i + 2));
                    int half = scaleValue / 2;
                    int xmin = centerX - half;
                    int zmin = centerZ - half;
                    int xmax = centerX + half;
                    int zmax = centerZ + half;
                    boxes.add(new BoundingBox(xmin, zmin, xmax, zmax));
                    System.out.println("Added scale-mode box: Center (" + centerX + ", " + centerZ + "), scale " + scaleValue +
                            " -> Box: (" + xmin + ", " + zmin + ") to (" + xmax + ", " + zmax + ")");
                } catch (NumberFormatException e) {
                    System.err.println("Error: All numeric values in scale mode must be valid integers.");
                    System.exit(1);
                }
            }
        } else if (!numericArgs.isEmpty() && numericArgs.size() % 4 == 0) {
            try {
                for (int i = 0; i < numericArgs.size(); i += 4) {
                    int xmin = Integer.parseInt(numericArgs.get(i));
                    int zmin = Integer.parseInt(numericArgs.get(i + 1));
                    int xmax = Integer.parseInt(numericArgs.get(i + 2));
                    int zmax = Integer.parseInt(numericArgs.get(i + 3));
                    boxes.add(new BoundingBox(xmin, zmin, xmax, zmax));
                    System.out.println("Added box: (" + xmin + ", " + zmin + ") to (" + xmax + ", " + zmax + ")");
                }
            } catch (NumberFormatException e) {
                System.err.println("Error: Please ensure all bounding box coordinates are integers.");
                System.exit(1);
            }
        } else {
            // Use default boxes.
            boxes.add(new BoundingBox(320, -103, 323, -97));
            boxes.add(new BoundingBox(310, -108, 313, -100));
            boxes.add(new BoundingBox(303, -108, 306, -100));
            boxes.add(new BoundingBox(297, -108, 300, -100));
            boxes.add(new BoundingBox(287, -108, 290, -100));
            boxes.add(new BoundingBox(273, -115, 277, -110));
            boxes.add(new BoundingBox(273, -115, 277, -110));
            boxes.add(new BoundingBox(225, -121, 229, -117));
            boxes.add(new BoundingBox(112, -150, 120, -148));
            boxes.add(new BoundingBox(73, -294, 77, -294));
            boxes.add(new BoundingBox(109, -352, 112, -348));
            boxes.add(new BoundingBox(151, -352, 155, -348));
            System.out.println("Using default bounding boxes.");
        }

        // 4) If one or more seed files are provided, load their lower-20-bit masks and compute the intersection.
        Set<Integer> targetMasks = new HashSet<>();
        boolean useTextFile = false;
        if (!seedFileNames.isEmpty()) {
            useTextFile = true;
            for (String file : seedFileNames) {
                Set<Integer> fileMasks = loadSeedLower20Masks(file);
                if (targetMasks.isEmpty()) {
                    targetMasks.addAll(fileMasks);
                } else {
                    targetMasks.retainAll(fileMasks);
                }
                System.out.println("Loaded " + fileMasks.size() + " lower-20-bit seed masks from " + file);
            }
            System.out.println("Intersection of seed masks across files has " + targetMasks.size() + " masks.");
        }

        // 5) Define seed ranges.
        long range1Start, range1End, range2Start, range2End;
        if (useTextFile) {
            range1Start = 0L;
            range1End   = 33554431L;
            range2Start = 33554432L;
            range2End   = 67108863L;
        } else {
            range1Start = 0L;
            range1End   = 11118602L;
            range2Start = 22237206L;
            range2End   = 44673034L;
        }

        // 6) Prepare an ExecutorService for parallel search.
        final int CHUNK_SIZE = 10_000;
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

// Use atomic counters to track progress.
        AtomicInteger completedChunks = new AtomicInteger(0);
        AtomicLong seedsSearched = new AtomicLong(0);
        ConcurrentLinkedQueue<Long> validSeeds = new ConcurrentLinkedQueue<>();

// Compute the total number of chunks (tasks) to be submitted:
        int totalChunks1 = (int) (((range1End - range1Start + 1) + CHUNK_SIZE - 1) / CHUNK_SIZE);
        int totalChunks2 = (int) (((range2End - range2Start + 1) + CHUNK_SIZE - 1) / CHUNK_SIZE);
        int totalChunks = totalChunks1 + totalChunks2;

// Set up a scheduled executor to print progress every second:
        ScheduledExecutorService progressExecutor = Executors.newSingleThreadScheduledExecutor();
        final long[] prevSeedCount = {0};  // use an array so it can be mutated inside the lambda

        ScheduledFuture<?> progressFuture = progressExecutor.scheduleAtFixedRate(() -> {
            int doneChunks = completedChunks.get();
            long currentSeeds = seedsSearched.get();
            long seedsPerSec = currentSeeds - prevSeedCount[0];
            prevSeedCount[0] = currentSeeds;
            double percent = (doneChunks / (double) totalChunks) * 100;
            System.out.printf("Progress: %d/%d chunks (%.2f%%), seeds searched: %d, seeds/sec: %d%n",
                    doneChunks, totalChunks, percent, currentSeeds, seedsPerSec);
        }, 0, 1, TimeUnit.SECONDS);

// Submit tasks for the first seed range.
        for (long cs = range1Start; cs <= range1End; cs += CHUNK_SIZE) {
            final long chunkStart = cs;
            final long chunkEnd = Math.min(cs + CHUNK_SIZE - 1, range1End);
            executor.submit(() -> {
                for (long seed = chunkStart; seed <= chunkEnd; seed++) {
                    seedsSearched.incrementAndGet();  // count this seed as processed
                    if (!targetMasks.isEmpty() && !targetMasks.contains((int)(seed & 0xFFFFF))) {
                        continue;
                    }
                    boolean seedValid = true;
                    for (BoundingBox bb : boxes) {
                        if (!hasRiverInBoundingBox(seed, mcver, largeBiomes,
                                bb.xMin, bb.zMin, bb.xMax, bb.zMax)) {
                            seedValid = false;
                            break;
                        }
                    }
                    if (seedValid) {
                        validSeeds.add(seed);
                        System.out.println("Match found on Seed: " + seed);
                    }
                }
                completedChunks.incrementAndGet();
            });
        }

// Submit tasks for the second seed range.
        for (long cs = range2Start; cs <= range2End; cs += CHUNK_SIZE) {
            final long chunkStart = cs;
            final long chunkEnd = Math.min(cs + CHUNK_SIZE - 1, range2End);
            executor.submit(() -> {
                for (long seed = chunkStart; seed <= chunkEnd; seed++) {
                    seedsSearched.incrementAndGet();  // count each processed seed
                    if (!targetMasks.isEmpty() && !targetMasks.contains((int)(seed & 0xFFFFF))) {
                        continue;
                    }
                    boolean seedValid = true;
                    for (BoundingBox bb : boxes) {
                        if (!hasRiverInBoundingBox(seed, mcver, largeBiomes,
                                bb.xMin, bb.zMin, bb.xMax, bb.zMax)) {
                            seedValid = false;
                            break;
                        }
                    }
                    if (seedValid) {
                        validSeeds.add(seed);
                        System.out.println("Match found on Seed: " + seed);
                    }
                }
                completedChunks.incrementAndGet();
            });
        }

        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

// Cancel the progress printer and shut down its executor.
        progressFuture.cancel(true);
        progressExecutor.shutdownNow();

        // 7) Write valid seeds (and partner seeds when applicable) to output.txt.
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("output.txt")))) {
            for (Long seed : validSeeds) {
                writer.println(seed);
                if (!useTextFile) {
                    writer.println(partner(seed));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Done searching and exporting seeds!");
    }

    /**
     * Helper method: load a seed file and return a set of lower-20-bit masks.
     */
    private static Set<Integer> loadSeedLower20Masks(String filename) {
        Set<Integer> masks = new HashSet<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    long seed = Long.parseLong(line);
                    masks.add((int)(seed & 0xFFFFF));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid seed in file: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading seed file: " + e.getMessage());
        }
        return masks;
    }


    //======================================================================
    // LayersPort class with map functions (unchanged – use your existing translations)
    //======================================================================
    // (The LayersPort class is included below as in your original code.)
}


    /**
 * LayersPort is where we place the direct Java translations of the C methods.
 * Each static method matches (Layer, out[], x, z, w, h).
 */
class LayersPort
{
    // ============ MAP_ADD_ISLAND =============
    public static int mapAddIslandC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        long st = l.startSalt;
        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            int idxZ0 = (j+0)*pW;
            int idxZ1 = (j+1)*pW;
            int idxZ2 = (j+2)*pW;
            for (int i = 0; i < w; i++)
            {
                int v00 = out[idxZ0 + i+0];
                int v20 = out[idxZ0 + i+1];
                int v02 = out[idxZ2 + i+0];
                int v22 = out[idxZ2 + i+1];
                int v11 = out[idxZ1 + i+1];

                int v = v11;
                if (v11 == 0)
                {
                    // check neighbors
                    if (v00 != 0 || v20 != 0 || v02 != 0 || v22 != 0)
                    {
                        long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                        int inc = 0;
                        v = 1;

                        if (v00 != 0)
                        {
                            ++inc;
                            v = v00;
                            cs = RiverCracker.mcStepSeed(cs, st);
                        }
                        if (v20 != 0)
                        {
                            ++inc;
                            if (inc == 1 || RiverCracker.mcFirstIsZero(cs, 2)) v = v20;
                            cs = RiverCracker.mcStepSeed(cs, st);
                        }
                        if (v02 != 0)
                        {
                            ++inc;
                            if (inc == 1) {
                                v = v02;
                            } else if (inc == 2) {
                                if (RiverCracker.mcFirstIsZero(cs, 2)) v = v02;
                            } else {
                                if (RiverCracker.mcFirstIsZero(cs, 3)) v = v02;
                            }
                            cs = RiverCracker.mcStepSeed(cs, st);
                        }
                        if (v22 != 0)
                        {
                            ++inc;
                            if (inc == 1) {
                                v = v22;
                            } else if (inc == 2) {
                                if (RiverCracker.mcFirstIsZero(cs, 2)) v = v22;
                            } else if (inc == 3) {
                                if (RiverCracker.mcFirstIsZero(cs, 3)) v = v22;
                            } else {
                                if (RiverCracker.mcFirstIsZero(cs, 4)) v = v22;
                            }
                            cs = RiverCracker.mcStepSeed(cs, st);
                        }

                        // final check
                        if (v != 4 && !RiverCracker.mcFirstIsZero(cs, 3))
                            v = 0;
                    }
                }
                else if (v11 != 4)
                {
                    // check if neighbors are ocean
                    if (v00 == 0 || v20 == 0 || v02 == 0 || v22 == 0)
                    {
                        long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                        if (RiverCracker.mcFirstIsZero(cs, 5))
                            v = 0;
                    }
                }

                out[i + j*w] = v;
            }
        }

        return 0;
    }

    // ============ MAP_REMOVE_TOO_MUCH_OCEAN =============
    public static int mapRemoveTooMuchOceanC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1) + (j+1)*pW];
                out[i + j*w] = v11;

                if (out[(i+1) + (j+0)*pW] != 0) continue;
                if (out[(i+2) + (j+1)*pW] != 0) continue;
                if (out[(i+0) + (j+1)*pW] != 0) continue;
                if (out[(i+1) + (j+2)*pW] != 0) continue;

                if (v11 == 0)
                {
                    long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                    if (RiverCracker.mcFirstIsZero(cs, 2))
                    {
                        out[i + j*w] = 1;
                    }
                }
            }
        }

        return 0;
    }

    // ============ MAP_ADD_SNOW =============
    public static int mapAddSnowC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1) + (j+1)*pW];
                if (RiverCracker.isShallowOcean(v11)) {
                    out[i + j*w] = v11;
                }
                else {
                    long cs = RiverCracker.getChunkSeed(ss, i + x, j + z);
                    int r = RiverCracker.mcFirstInt(cs, 6);
                    int v;
                    if (r == 0) v = 4;
                    else if (r <= 1) v = 3;
                    else v = 1;
                    out[i + j*w] = v;
                }
            }
        }

        return 0;
    }

    // ============ MAP_COOL_WARM =============
    public static int mapCoolWarmC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int v11 = out[(i+1)+(j+1)*pW];
                if (v11 == 1) {
                    int v10 = out[(i+1)+(j+0)*pW];
                    int v21 = out[(i+2)+(j+1)*pW];
                    int v01 = out[(i+0)+(j+1)*pW];
                    int v12 = out[(i+1)+(j+2)*pW];
                    if (v10 == 3 || v10 == 4 || v21 == 3 || v21 == 4 ||
                            v01 == 3 || v01 == 4 || v12 == 3 || v12 == 4) {
                        v11 = 2;
                    }
                }
                out[i + j*w] = v11;
            }
        }
        return 0;
    }

    // ============ MAP_HEAT_ICE =============
    public static int mapHeatIceC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int v11 = out[(i+1)+(j+1)*pW];
                if (v11 == 4) {
                    int v10 = out[(i+1)+(j+0)*pW];
                    int v21 = out[(i+2)+(j+1)*pW];
                    int v01 = out[(i+0)+(j+1)*pW];
                    int v12 = out[(i+1)+(j+2)*pW];
                    if (v10 == 1 || v10 == 2 || v21 == 1 || v21 == 2 ||
                            v01 == 1 || v01 == 2 || v12 == 1 || v12 == 2)
                    {
                        v11 = 3;
                    }
                }
                out[i + j*w] = v11;
            }
        }
        return 0;
    }

    // ============ MAP_SPECIAL =============
    public static int mapSpecialC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int err = l.p.getMap.apply(l.p, out, x, z, w, h);
        if (err != 0) return err;

        long st = l.startSalt;
        long ss = l.startSeed;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int v = out[i + j*w];
                if (v != 0) {
                    long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                    if (RiverCracker.mcFirstIsZero(cs, 13)) {
                        cs = RiverCracker.mcStepSeed(cs, st);
                        // The C code sets bits in high nibble: "v |= (1 + mcFirstInt(cs,15))<<8"
                        // We'll store them but won't use them in color. Just store as we do in C:
                        int r = RiverCracker.mcFirstInt(cs, 15);
                        int bits = (1 + r) << 8;  // 0xf00 region
                        v = (v & 0xff) | (bits & 0xf00);
                        out[i + j*w] = v;
                    }
                }
            }
        }

        return 0;
    }

    // ============ MAP_ADD_MUSHROOM_ISLAND =============
    public static int mapAddMushroomIslandC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1)+(j+1)*pW];
                // surrounded by ocean?
                if (v11 == 0) {
                    if (out[(i+0)+(j+0)*pW] == 0 &&
                            out[(i+2)+(j+0)*pW] == 0 &&
                            out[(i+0)+(j+2)*pW] == 0 &&
                            out[(i+2)+(j+2)*pW] == 0)
                    {
                        long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                        if (RiverCracker.mcFirstIsZero(cs, 100)) {
                            v11 = RiverCracker.MUSHROOM_FIELDS;
                        }
                    }
                }
                out[i + j*w] = v11;
            }
        }

        return 0;
    }

    // ============ MAP_DEEP_OCEAN =============
    public static int mapDeepOceanC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1)+(j+1)*pW];
                if (RiverCracker.isShallowOcean(v11))
                {
                    int oceans = 0;
                    if (RiverCracker.isShallowOcean(out[(i+1)+(j+0)*pW])) oceans++;
                    if (RiverCracker.isShallowOcean(out[(i+2)+(j+1)*pW])) oceans++;
                    if (RiverCracker.isShallowOcean(out[(i+0)+(j+1)*pW])) oceans++;
                    if (RiverCracker.isShallowOcean(out[(i+1)+(j+2)*pW])) oceans++;
                    if (oceans >= 8)
                    {
                        switch (v11)
                        {
                            case RiverCracker.WARM_OCEAN:       v11 = RiverCracker.DEEP_WARM_OCEAN; break;
                            case RiverCracker.LUKEWARM_OCEAN:   v11 = RiverCracker.DEEP_LUKEWARM_OCEAN; break;
                            case RiverCracker.COLD_OCEAN:       v11 = RiverCracker.DEEP_COLD_OCEAN; break;
                            case RiverCracker.FROZEN_OCEAN:     v11 = RiverCracker.DEEP_FROZEN_OCEAN; break;
                            default:                                 v11 = RiverCracker.DEEP_OCEAN; break;
                        }
                    }
                }
                out[i + j*w] = v11;
            }
        }

        return 0;
    }

    // ============ MAP_BIOME & MAP_BIOME_BE =============
    // We'll unify them into one method with a boolean "bedrock".
    public static int mapBiomeC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h, boolean bedrock)
    {
        int err = l.p.getMap.apply(l.p, out, x, z, w, h);
        if (err != 0) return err;

        long ss = l.startSeed;

        // Weighted sets:
        int[] warmBiomes       = { RiverCracker.DESERT, RiverCracker.DESERT, RiverCracker.DESERT,
                RiverCracker.SAVANNA, RiverCracker.SAVANNA, RiverCracker.PLAINS };
        int[] lushBiomesJava   = { RiverCracker.FOREST, RiverCracker.DARK_FOREST, RiverCracker.MOUNTAINS,
                RiverCracker.PLAINS, RiverCracker.BIRCH_FOREST, RiverCracker.SWAMP };
        // bedrock has a slightly different "lushBiomes" array:
        int[] lushBiomesBE     = { RiverCracker.FOREST, RiverCracker.DARK_FOREST, RiverCracker.MOUNTAINS,
                RiverCracker.PLAINS, RiverCracker.PLAINS, RiverCracker.PLAINS,
                RiverCracker.BIRCH_FOREST, RiverCracker.SWAMP };
        int[] coldBiomes       = { RiverCracker.FOREST, RiverCracker.MOUNTAINS,
                RiverCracker.TAIGA, RiverCracker.PLAINS };
        int[] snowBiomes       = { RiverCracker.SNOWY_TUNDRA, RiverCracker.SNOWY_TUNDRA,
                RiverCracker.SNOWY_TUNDRA, RiverCracker.SNOWY_TAIGA };

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int idx = i + j*w;
                int id  = out[idx];
                int hasHighBit = (id & 0xf00);
                id &= ~0xf00;  // remove the high bits

                if (id == RiverCracker.MUSHROOM_FIELDS || RiverCracker.isOceanic(id)) {
                    // skip
                    out[idx] = id;
                    continue;
                }

                long cs = RiverCracker.getChunkSeed(ss, i + x, j + z);
                int outVal = 0;
                // The code checks the "temperature classification" (Warm, Lush, Cold, Freezing)
                // We'll do it by comparing the "tempCat" in the Biome array if it's one of the special placeholders:
                // 1=Warm, 2=Lush, 3=Cold, 4=Freezing. The code in Layers.c uses placeholders 1..4 in the parent result.
                switch (id)
                {
                    case 1: // Warm
                        if (hasHighBit != 0) { // mutated
                            outVal = (RiverCracker.mcFirstIsZero(cs, 3)
                                    ? RiverCracker.BADLANDS_PLATEAU
                                    : RiverCracker.WOODED_BADLANDS_PLATEAU);
                        } else {
                            outVal = warmBiomes[RiverCracker.mcFirstInt(cs, 6)];
                        }
                        break;
                    case 2: // Lush
                        if (hasHighBit != 0) {
                            // mutated
                            outVal = RiverCracker.JUNGLE;
                        } else {
                            if (!bedrock) {
                                outVal = lushBiomesJava[RiverCracker.mcFirstInt(cs, 6)];
                            } else {
                                outVal = lushBiomesBE[RiverCracker.mcFirstInt(cs, 6)];
                            }
                        }
                        break;
                    case 3: // Cold
                        if (hasHighBit != 0) {
                            outVal = RiverCracker.GIANT_TREE_TAIGA;
                        } else {
                            outVal = coldBiomes[RiverCracker.mcFirstInt(cs, 4)];
                        }
                        break;
                    case 4: // Freezing
                        outVal = snowBiomes[RiverCracker.mcFirstInt(cs, 4)];
                        break;
                    default:
                        // fallback
                        outVal = RiverCracker.MUSHROOM_FIELDS;
                }
                out[idx] = outVal;
            }
        }

        return 0;
    }

    // ============ MAP_ADD_BAMBOO =============
    public static int mapAddBambooC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int err = l.p.getMap.apply(l.p, out, x, z, w, h);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int idx = i + j*w;
                int v   = out[idx];
                if (v == RiverCracker.JUNGLE)
                {
                    long cs = RiverCracker.getChunkSeed(ss, i + x, j + z);
                    if (RiverCracker.mcFirstIsZero(cs, 10))
                    {
                        out[idx] = RiverCracker.BAMBOO_JUNGLE;
                    }
                }
            }
        }

        return 0;
    }

    // ============ MAP_RIVER_INIT =============
    public static int mapRiverInitC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int err = l.p.getMap.apply(l.p, out, x, z, w, h);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                if (out[i + j*w] > 0)
                {
                    long cs = RiverCracker.getChunkSeed(ss, i + x, j + z);
                    // in C: out[i + j*w] = mcFirstInt(cs,299999) + 2
                    // We'll just replicate that exactly:
                    int r = RiverCracker.mcFirstInt(cs, 299999);
                    out[i + j*w] = r;
                }
                else
                {
                    out[i + j*w] = 0;
                }
            }
        }

        return 0;
    }

    // ============ MAP_BIOME_EDGE =============
    public static int mapBiomeEdgeC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1)+(j+1)*pW];
                int v10 = out[(i+1)+(j+0)*pW];
                int v21 = out[(i+2)+(j+1)*pW];
                int v01 = out[(i+0)+(j+1)*pW];
                int v12 = out[(i+1)+(j+2)*pW];
                int outVal = v11;

                // replicate the "replaceEdge" logic
                if (!replaceEdge(out, i, j, w, v10, v21, v01, v12, v11,
                        RiverCracker.WOODED_BADLANDS_PLATEAU, RiverCracker.BADLANDS)) {
                    if (!replaceEdge(out, i, j, w, v10, v21, v01, v12, v11,
                            RiverCracker.BADLANDS_PLATEAU, RiverCracker.BADLANDS)) {
                        if (!replaceEdge(out, i, j, w, v10, v21, v01, v12, v11,
                                RiverCracker.GIANT_TREE_TAIGA, RiverCracker.TAIGA)) {
                            if (v11 == RiverCracker.DESERT) {
                                if (v10 != RiverCracker.SNOWY_TUNDRA && v21 != RiverCracker.SNOWY_TUNDRA &&
                                        v01 != RiverCracker.SNOWY_TUNDRA && v12 != RiverCracker.SNOWY_TUNDRA) {
                                    outVal = v11;
                                } else {
                                    outVal = RiverCracker.WOODED_MOUNTAINS;
                                }
                            }
                            else if (v11 == RiverCracker.SWAMP)
                            {
                                boolean isDesert = (v10 == RiverCracker.DESERT || v21 == RiverCracker.DESERT ||
                                        v01 == RiverCracker.DESERT || v12 == RiverCracker.DESERT);
                                boolean isSnowTaiga = (v10 == RiverCracker.SNOWY_TAIGA || v21 == RiverCracker.SNOWY_TAIGA ||
                                        v01 == RiverCracker.SNOWY_TAIGA || v12 == RiverCracker.SNOWY_TAIGA);
                                boolean isSnowTundra = (v10 == RiverCracker.SNOWY_TUNDRA || v21 == RiverCracker.SNOWY_TUNDRA ||
                                        v01 == RiverCracker.SNOWY_TUNDRA || v12 == RiverCracker.SNOWY_TUNDRA);

                                if ( !(isDesert || isSnowTaiga || isSnowTundra) )
                                {
                                    // check for (bamboo_)jungle
                                    boolean nearJungle = (v10 == RiverCracker.JUNGLE || v21 == RiverCracker.JUNGLE ||
                                            v01 == RiverCracker.JUNGLE || v12 == RiverCracker.JUNGLE ||
                                            v10 == RiverCracker.BAMBOO_JUNGLE || v21 == RiverCracker.BAMBOO_JUNGLE ||
                                            v01 == RiverCracker.BAMBOO_JUNGLE || v12 == RiverCracker.BAMBOO_JUNGLE);

                                    if (nearJungle) outVal = RiverCracker.JUNGLE_EDGE;
                                    else outVal = v11;
                                }
                                else {
                                    outVal = RiverCracker.PLAINS;
                                }
                            }
                            else {
                                outVal = v11;
                            }
                        }
                    }
                }
                out[i + j*w] = outVal;
            }
        }

        return 0;
    }

    private static boolean replaceEdge(int[] out, int i, int j, int w,
                                       int v10, int v21, int v01, int v12, int id,
                                       int baseID, int edgeID)
    {
        if (id != baseID) return false;
        // must match parent's "areSimilar113" for these pre-1.13 logic:
        boolean sim1 = RiverCracker.areSimilar113(v10, baseID);
        boolean sim2 = RiverCracker.areSimilar113(v21, baseID);
        boolean sim3 = RiverCracker.areSimilar113(v01, baseID);
        boolean sim4 = RiverCracker.areSimilar113(v12, baseID);

        if (sim1 && sim2 && sim3 && sim4) {
            out[i + j*w] = id; // keep
        } else {
            out[i + j*w] = edgeID;
        }
        return true;
    }

    // ============ MAP_HILLS & MAP_HILLS_113 =============
    public static int mapHillsC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h, boolean is113)
    {
        // We need data from both parents:
        if (l.p2 == null) {
            System.err.println("mapHills requires second parent!");
            return -1;
        }
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        // first parent = "biome chain"
        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;
        int[] buf = Arrays.copyOf(out, pW*pH);

        // second parent = "river chain"
        err = l.p2.getMap.apply(l.p2, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        long st = l.startSalt;
        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int a11 = buf[(i+1)+(j+1)*pW]; // biome
                int b11 = out[(i+1)+(j+1)*pW]; // river
                int idx = i + j*w;

                int bn = (b11 - 2) % 29;
                boolean isBone = (bn == 1);  // (b11 >= 2 && (b11-2)%29 == 1)
                boolean isZero = (bn == 0);

                if (!RiverCracker.isShallowOcean(a11) && b11 >= 2 && isBone && !is113 && a11 < 128)
                {
                    // old hills logic
                    int m = RiverCracker.biomes[a11].mutated;
                    if (m > 0) {
                        out[idx] = m;
                    } else {
                        out[idx] = a11;
                    }
                }
                else if (!RiverCracker.isDeepOcean(a11) && b11 >= 2 && isBone && is113)
                {
                    // 1.13 logic
                    int m = RiverCracker.biomes[a11].mutated;
                    if (m > 0) {
                        out[idx] = m;
                    } else {
                        out[idx] = a11;
                    }
                }
                else
                {
                    long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                    // in C code: if(bn != 1 || !mcFirstIsZero(cs,3)) ...
                    if (!(isBone) || !RiverCracker.mcFirstIsZero(cs,3)) {
                        out[idx] = a11;
                    }
                    else {
                        int hillID = a11;
                        // same logic as C
                        hillID = pickHillVariant(cs, st, a11, is113);

                        if (isZero && hillID != a11) {
                            int mut = RiverCracker.biomes[hillID].mutated;
                            if (mut >= 0) hillID = mut;
                        }

                        if (hillID != a11)
                        {
                            int a10 = buf[(i+1)+(j+0)*pW];
                            int a21 = buf[(i+2)+(j+1)*pW];
                            int a01 = buf[(i+0)+(j+1)*pW];
                            int a12 = buf[(i+1)+(j+2)*pW];
                            int equals = 0;
                            boolean eq1 = (is113 ? RiverCracker.areSimilar113(a10,a11)
                                    : RiverCracker.areSimilar(a10,a11));
                            boolean eq2 = (is113 ? RiverCracker.areSimilar113(a21,a11)
                                    : RiverCracker.areSimilar(a21,a11));
                            boolean eq3 = (is113 ? RiverCracker.areSimilar113(a01,a11)
                                    : RiverCracker.areSimilar(a01,a11));
                            boolean eq4 = (is113 ? RiverCracker.areSimilar113(a12,a11)
                                    : RiverCracker.areSimilar(a12,a11));
                            if (eq1) equals++;
                            if (eq2) equals++;
                            if (eq3) equals++;
                            if (eq4) equals++;
                            if (equals >= 3) {
                                out[idx] = hillID;
                            } else {
                                out[idx] = a11;
                            }
                        }
                        else {
                            out[idx] = a11;
                        }
                    }
                }
            }
        }

        return 0;
    }

    private static int pickHillVariant(long cs, long st, int a11, boolean is113)
    {
        // see the switch in C code
        int hillID = a11;
        switch(a11)
        {
            case RiverCracker.DESERT:
                hillID = RiverCracker.DESERT_HILLS; break;
            case RiverCracker.FOREST:
                hillID = RiverCracker.WOODED_HILLS; break;
            case RiverCracker.BIRCH_FOREST:
                hillID = RiverCracker.BIRCH_FOREST_HILLS; break;
            case RiverCracker.DARK_FOREST:
                hillID = RiverCracker.PLAINS; break;
            case RiverCracker.TAIGA:
                hillID = RiverCracker.TAIGA_HILLS; break;
            case RiverCracker.GIANT_TREE_TAIGA:
                hillID = RiverCracker.GIANT_TREE_TAIGA_HILLS; break;
            case RiverCracker.SNOWY_TAIGA:
                hillID = RiverCracker.SNOWY_TAIGA_HILLS; break;
            case RiverCracker.PLAINS:
                cs = RiverCracker.mcStepSeed(cs, st);
                if (RiverCracker.mcFirstIsZero(cs, 3)) hillID = RiverCracker.WOODED_HILLS;
                else hillID = RiverCracker.FOREST;
                break;
            case RiverCracker.SNOWY_TUNDRA:
                hillID = RiverCracker.SNOWY_MOUNTAINS; break;
            case RiverCracker.JUNGLE:
                hillID = RiverCracker.JUNGLE_HILLS; break;
            case RiverCracker.BAMBOO_JUNGLE:
                if (is113) {
                    hillID = RiverCracker.BAMBOO_JUNGLE_HILLS;
                } else {
                    hillID = RiverCracker.JUNGLE_HILLS; // pre-1.14 fallback
                }
                break;
            case RiverCracker.OCEAN:
                hillID = RiverCracker.DEEP_OCEAN; break;
            case RiverCracker.MOUNTAINS:
                hillID = RiverCracker.WOODED_MOUNTAINS; break;
            case RiverCracker.SAVANNA:
                hillID = RiverCracker.SAVANNA_PLATEAU; break;
            default:
                // special checks
                if (RiverCracker.areSimilar(a11, RiverCracker.WOODED_BADLANDS_PLATEAU)) {
                    hillID = RiverCracker.BADLANDS;
                }
                else if (RiverCracker.isDeepOcean(a11)) {
                    cs = RiverCracker.mcStepSeed(cs, st);
                    if (RiverCracker.mcFirstIsZero(cs, 3)) {
                        cs = RiverCracker.mcStepSeed(cs, st);
                        if (RiverCracker.mcFirstIsZero(cs, 2)) hillID = RiverCracker.PLAINS;
                        else hillID = RiverCracker.FOREST;
                    }
                }
                break;
        }
        return hillID;
    }

    // ============ MAP_RIVER =============
    public static int mapRiverC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v01 = (out[(i+0)+(j+1)*pW]);
                int v11 = (out[(i+1)+(j+1)*pW]);
                int v21 = (out[(i+2)+(j+1)*pW]);
                int v10 = (out[(i+1)+(j+0)*pW]);
                int v12 = (out[(i+1)+(j+2)*pW]);

                if (v11 == v01 && v11 == v10 && v11 == v12 && v11 == v21) {
                    out[i + j*w] = -1;
                } else {
                    out[i + j*w] = RiverCracker.RIVER;
                }
            }
        }

        return 0;
    }

    // ============ MAP_SMOOTH =============
    public static int mapSmoothC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1)+(j+1)*pW];
                int v01 = out[(i+0)+(j+1)*pW];
                int v10 = out[(i+1)+(j+0)*pW];

                if (v11 != v01 || v11 != v10)
                {
                    int v21 = out[(i+2)+(j+1)*pW];
                    int v12 = out[(i+1)+(j+2)*pW];
                    if (v01 == v21 && v10 == v12)
                    {
                        long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                        // check highest bit 1<<24
                        if ((cs & (1L << 24)) != 0)
                            v11 = v10;
                        else
                            v11 = v01;
                    }
                    else
                    {
                        if (v01 == v21) v11 = v01;
                        if (v10 == v12) v11 = v10;
                    }
                }
                out[i + j*w] = v11;
            }
        }
        return 0;
    }

    // ============ MAP_RARE_BIOME =============
    public static int mapRareBiomeC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int err = l.p.getMap.apply(l.p, out, x, z, w, h);
        if (err != 0) return err;

        long ss = l.startSeed;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v = out[i + j*w];
                if (v == RiverCracker.PLAINS)
                {
                    long cs = RiverCracker.getChunkSeed(ss, i+x, j+z);
                    if (RiverCracker.mcFirstIsZero(cs, 57))
                    {
                        // mutated plains = sunflower_plains = plains + 128
                        v = RiverCracker.PLAINS + 128;
                    }
                }
                out[i + j*w] = v;
            }
        }

        return 0;
    }

    // ============ MAP_SHORE =============
    public static int mapShoreC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        int pX = x - 1;
        int pZ = z - 1;
        int pW = w + 2;
        int pH = h + 2;

        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        for (int j = 0; j < h; j++)
        {
            for (int i = 0; i < w; i++)
            {
                int v11 = out[(i+1)+(j+1)*pW];
                int v10 = out[(i+1)+(j+0)*pW];
                int v21 = out[(i+2)+(j+1)*pW];
                int v01 = out[(i+0)+(j+1)*pW];
                int v12 = out[(i+1)+(j+2)*pW];

                int outVal = v11;
                if (v11 == RiverCracker.MUSHROOM_FIELDS)
                {
                    if (v10 != RiverCracker.OCEAN && v21 != RiverCracker.OCEAN &&
                            v01 != RiverCracker.OCEAN && v12 != RiverCracker.OCEAN)
                        outVal = v11;
                    else
                        outVal = RiverCracker.MUSHROOM_FIELD_SHORE;
                }
                else if (RiverCracker.biomes[v11].type == RiverCracker.JUNGLE_TYPE)
                {
                    // check if neighbors are JFTO
                    if (isBiomeJFTO(v10) && isBiomeJFTO(v21) &&
                            isBiomeJFTO(v01) && isBiomeJFTO(v12))
                    {
                        if (!RiverCracker.isOceanic(v10) && !RiverCracker.isOceanic(v21) &&
                                !RiverCracker.isOceanic(v01) && !RiverCracker.isOceanic(v12))
                        {
                            outVal = v11;
                        }
                        else
                        {
                            outVal = RiverCracker.BEACH;
                        }
                    }
                    else
                    {
                        outVal = RiverCracker.JUNGLE_EDGE;
                    }
                }
                else if (v11 != RiverCracker.MOUNTAINS &&
                        v11 != RiverCracker.WOODED_MOUNTAINS &&
                        v11 != RiverCracker.MOUNTAIN_EDGE)
                {
                    if (RiverCracker.isBiomeSnowy(v11))
                    {
                        outVal = replaceOcean2(v10,v21,v01,v12, v11, RiverCracker.SNOWY_BEACH);
                    }
                    else if (v11 != RiverCracker.BADLANDS && v11 != RiverCracker.WOODED_BADLANDS_PLATEAU)
                    {
                        if (v11 != RiverCracker.OCEAN && v11 != RiverCracker.DEEP_OCEAN &&
                                v11 != RiverCracker.RIVER && v11 != RiverCracker.SWAMP)
                        {
                            outVal = replaceOcean2(v10,v21,v01,v12, v11, RiverCracker.BEACH);
                        }
                    }
                    else
                    {
                        // check plateau
                        if (!RiverCracker.isOceanic(v10) && !RiverCracker.isOceanic(v21) &&
                                !RiverCracker.isOceanic(v01) && !RiverCracker.isOceanic(v12))
                        {
                            // if neighbors are all MESA, keep
                            if (RiverCracker.biomes[v10].type == RiverCracker.MESA_TYPE &&
                                    RiverCracker.biomes[v21].type == RiverCracker.MESA_TYPE &&
                                    RiverCracker.biomes[v01].type == RiverCracker.MESA_TYPE &&
                                    RiverCracker.biomes[v12].type == RiverCracker.MESA_TYPE)
                                outVal = v11;
                            else
                                outVal = RiverCracker.DESERT;
                        }
                    }
                }
                else
                {
                    outVal = replaceOcean2(v10,v21,v01,v12,v11,RiverCracker.STONE_SHORE);
                }

                out[i + j*w] = outVal;
            }
        }

        return 0;
    }

    private static boolean isBiomeJFTO(int id)
    {
        if (id < 0 || id >= 256 || !RiverCracker.biomes[id].valid) return false;
        int t = RiverCracker.biomes[id].type;
        return (t == RiverCracker.JUNGLE_TYPE || id == RiverCracker.FOREST ||
                id == RiverCracker.TAIGA || RiverCracker.isOceanic(id));
    }

    private static int replaceOcean2(int v10, int v21, int v01, int v12, int id, int replaceID)
    {
        if (!RiverCracker.isOceanic(id)) {
            if (!RiverCracker.isOceanic(v10) && !RiverCracker.isOceanic(v21) &&
                    !RiverCracker.isOceanic(v01) && !RiverCracker.isOceanic(v12))
            {
                return id;
            }
            else
            {
                return replaceID;
            }
        }
        return id;
    }

    // ============ MAP_RIVER_MIX =============
    public static int mapRiverMixC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        if (l.p2 == null) {
            System.err.println("mapRiverMix requires second parent!");
            return -1;
        }
        int err = l.p.getMap.apply(l.p, out, x, z, w, h); // biome chain
        if (err != 0) return err;

        int[] buf = Arrays.copyOf(out, w*h);

        err = l.p2.getMap.apply(l.p2, out, x, z, w, h); // rivers
        if (err != 0) return err;

        for (int i = 0; i < w*h; i++)
        {
            int landID = buf[i];
            int riverID = out[i];
            if (RiverCracker.isOceanic(landID)) {
                out[i] = landID;
            }
            else
            {
                if (riverID == RiverCracker.RIVER)
                {
                    if (landID == RiverCracker.SNOWY_TUNDRA)
                        out[i] = RiverCracker.FROZEN_RIVER;
                    else if (landID == RiverCracker.MUSHROOM_FIELDS ||
                            landID == RiverCracker.MUSHROOM_FIELD_SHORE)
                        out[i] = RiverCracker.MUSHROOM_FIELD_SHORE;
                    else
                        out[i] = RiverCracker.RIVER;
                }
                else
                {
                    out[i] = landID;
                }
            }
        }

        return 0;
    }

    // ============ MAP_OCEAN_TEMP =============
    public static int mapOceanTempC(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        // replicate getOceanTemp from the C code
        RiverCracker.OceanRandom rnd = l.oceanRnd;
        if (rnd == null) {
            // fallback
            Arrays.fill(out, RiverCracker.OCEAN);
            return 0;
        }

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                double tmp = getOceanTemp(rnd, (i + x)/8.0, (j + z)/8.0, 0);
                int outVal;
                if (tmp > 0.4)      outVal = RiverCracker.WARM_OCEAN;
                else if (tmp > 0.2) outVal = RiverCracker.LUKEWARM_OCEAN;
                else if (tmp < -0.4)outVal = RiverCracker.FROZEN_OCEAN;
                else if (tmp < -0.2)outVal = RiverCracker.COLD_OCEAN;
                else                outVal = RiverCracker.OCEAN;
                out[i + j*w] = outVal;
            }
        }
        return 0;
    }

    // Code that computes the perlin-like noise used for ocean temperature:
    private static double lerp(double part, double from, double to) {
        return from + part*(to - from);
    }
    private static double indexedLerp(int idx, double d1, double d2, double d3)
    {
        // The C code table:
        // cEdgeX, cEdgeY, cEdgeZ for 16 entries. We'll just do the same in Java:
        double[] cEdgeX = {1.0,-1.0,1.0,-1.0, 1.0,-1.0,1.0,-1.0, 0.0,0.0,0.0,0.0, 1.0,0.0,-1.0,0.0};
        double[] cEdgeY = {1.0,1.0,-1.0,-1.0, 0.0,0.0,0.0,0.0, 1.0,-1.0,1.0,-1.0, 1.0,-1.0,1.0,-1.0};
        double[] cEdgeZ = {0.0,0.0,0.0,0.0, 1.0,1.0,-1.0,-1.0, 1.0,1.0,-1.0,-1.0, 0.0,1.0,0.0,-1.0};

        idx &= 0xf;
        return cEdgeX[idx]*d1 + cEdgeY[idx]*d2 + cEdgeZ[idx]*d3;
    }
    private static double getOceanTemp(RiverCracker.OceanRandom rnd, double d1, double d2, double d3)
    {
        d1 += rnd.a;
        d2 += rnd.b;
        d3 += rnd.c;
        int i1 = (int)Math.floor(d1);
        int i2 = (int)Math.floor(d2);
        int i3 = (int)Math.floor(d3);
        d1 -= i1;
        d2 -= i2;
        d3 -= i3;
        double t1 = d1*d1*d1 * (d1*(d1*6.0 -15.0)+10.0);
        double t2 = d2*d2*d2 * (d2*(d2*6.0 -15.0)+10.0);
        double t3 = d3*d3*d3 * (d3*(d3*6.0 -15.0)+10.0);

        i1 &= 0xff;
        i2 &= 0xff;
        i3 &= 0xff;

        int a1 = rnd.d[i1]   + i2;
        int a2 = rnd.d[a1]   + i3;
        int a3 = rnd.d[a1+1] + i3;
        int b1 = rnd.d[i1+1] + i2;
        int b2 = rnd.d[b1]   + i3;
        int b3 = rnd.d[b1+1] + i3;

        double l1 = indexedLerp(rnd.d[a2],     d1,   d2,   d3);
        double l2 = indexedLerp(rnd.d[b2],     d1-1, d2,   d3);
        double l3 = indexedLerp(rnd.d[a3],     d1,   d2-1, d3);
        double l4 = indexedLerp(rnd.d[b3],     d1-1, d2-1, d3);
        double l5 = indexedLerp(rnd.d[a2+1],   d1,   d2,   d3-1);
        double l6 = indexedLerp(rnd.d[b2+1],   d1-1, d2,   d3-1);
        double l7 = indexedLerp(rnd.d[a3+1],   d1,   d2-1, d3-1);
        double l8 = indexedLerp(rnd.d[b3+1],   d1-1, d2-1, d3-1);

        l1 = lerp(t1, l1, l2);
        l3 = lerp(t1, l3, l4);
        l5 = lerp(t1, l5, l6);
        l7 = lerp(t1, l7, l8);

        l1 = lerp(t2, l1, l3);
        l5 = lerp(t2, l5, l7);

        return lerp(t3, l1, l5);
    }

    // ============ MAP_OCEAN_MIX =============
    public static int mapOceanMix(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        // We need both parents: p=land, p2=ocean
        if (l.p2 == null) {
            System.err.println("mapOceanMix requires second parent!");
            return -1;
        }
        int err = l.p2.getMap.apply(l.p2, out, x, z, w, h); // ocean
        if (err != 0) return err;
        int[] otyp = Arrays.copyOf(out, w*h);

        // The code in Layers.c tries to figure out a bounding region that might
        // contain warm/frozen ocean, calls the land parent in that bounding box,
        // then merges them. For simplicity, we'll do a direct approach.

        // 1) Get the land from l.p
        int[] land = new int[w*h];
        err = l.p.getMap.apply(l.p, land, x, z, w, h);
        if (err != 0) return err;

        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int landID = land[i + j*w];
                int oceanID = otyp[i + j*w];
                // If not oceanic, keep land:
                if (!RiverCracker.isOceanic(landID)) {
                    out[i + j*w] = landID;
                    continue;
                }
                // If ocean is warm or frozen, we attempt to transform it:
                int replaceID = -1;
                if (oceanID == RiverCracker.WARM_OCEAN) {
                    replaceID = RiverCracker.LUKEWARM_OCEAN;
                }
                else if (oceanID == RiverCracker.FROZEN_OCEAN) {
                    replaceID = RiverCracker.COLD_OCEAN;
                }
                if (replaceID > 0) {
                    // check neighbors up to 8 blocks away
                    boolean foundLandClose = false;
                    outer_loop:
                    for (int ii = -8; ii <= 8; ii += 4) {
                        for (int jj = -8; jj <= 8; jj += 4) {
                            int xx = i + ii;
                            int zz = j + jj;
                            if (xx < 0 || xx >= w || zz < 0 || zz >= h) continue;
                            if (!RiverCracker.isOceanic(land[xx + zz*w])) {
                                foundLandClose = true;
                                break outer_loop;
                            }
                        }
                    }
                    if (foundLandClose) {
                        oceanID = replaceID;
                    }
                }
                // If deep ocean, keep it matched:
                if (landID == RiverCracker.DEEP_OCEAN) {
                    // e.g. warm_ocean -> deep_warm_ocean, etc.
                    switch (oceanID) {
                        case RiverCracker.LUKEWARM_OCEAN: oceanID = RiverCracker.DEEP_LUKEWARM_OCEAN; break;
                        case RiverCracker.OCEAN:          oceanID = RiverCracker.DEEP_OCEAN;          break;
                        case RiverCracker.COLD_OCEAN:     oceanID = RiverCracker.DEEP_COLD_OCEAN;     break;
                        case RiverCracker.FROZEN_OCEAN:   oceanID = RiverCracker.DEEP_FROZEN_OCEAN;   break;
                    }
                }
                out[i + j*w] = oceanID;
            }
        }
        return 0;
    }

    // ============ MAP_VORONOI_ZOOM =============
    public static int mapVoronoiZoom(RiverCracker.Layer l, int[] out, int x, int z, int w, int h)
    {
        // direct translation from Layers.c
        x -= 2;
        z -= 2;
        int pX = x >> 2;
        int pZ = z >> 2;
        int pW = ((x + w) >> 2) - pX + 2;
        int pH = ((z + h) >> 2) - pZ + 2;

        // Generate the parent layer at scale >> 2
        int err = l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        if (err != 0) return err;

        // We will fill a big buffer of size (pW*4) x (pH*4)
        int newW = pW << 2;
        int newH = pH << 2;
        int[] buf = new int[(newW+1)*(newH+1)];

        long st = l.startSalt;
        long ss = l.startSeed;

        for (int j = 0; j < pH - 1; j++)
        {
            // These are the four parent-corner biomes
            int v00 = out[ j      * pW];
            int v01 = out[(j + 1) * pW];

            for (int i = 0; i < pW - 1; i++)
            {
                int v10 = out[i + 1 +  j      * pW];
                int v11 = out[i + 1 + (j + 1) * pW];

                // If all 4 corners are the same, fill 4x4 sub-square quickly:
                if (v00 == v10 && v00 == v01 && v00 == v11)
                {
                    // fill a 4x4 block
                    for (int jj = 0; jj < 4; jj++)
                    {
                        int idx = ( (j << 2) + jj ) * newW + (i << 2);
                        for (int ii = 0; ii < 4; ii++)
                        {
                            buf[idx + ii] = v00;
                        }
                    }
                }
                else
                {
                    // Otherwise, we do partial random offsets to decide which corner is nearest
                    // Get random offsets for the four corners:
                    // (matching the chunkSeed approach in the C code)
                    long cs;

                    cs = RiverCracker.getChunkSeed(ss, (i + pX) << 2, (j + pZ) << 2);
                    int da1 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36;
                    cs = RiverCracker.mcStepSeed(cs, st);
                    int da2 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36;

                    cs = RiverCracker.getChunkSeed(ss, (i + pX + 1) << 2, (j + pZ) << 2);
                    int db1 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36 + 40_000; // 40<<10
                    cs = RiverCracker.mcStepSeed(cs, st);
                    int db2 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36;

                    cs = RiverCracker.getChunkSeed(ss, (i + pX) << 2, (j + pZ + 1) << 2);
                    int dc1 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36;
                    cs = RiverCracker.mcStepSeed(cs, st);
                    int dc2 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36 + 40_000;

                    cs = RiverCracker.getChunkSeed(ss, (i + pX + 1) << 2, (j + pZ + 1) << 2);
                    int dd1 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36 + 40_000;
                    cs = RiverCracker.mcStepSeed(cs, st);
                    int dd2 = (RiverCracker.mcFirstInt(cs, 1024) - 512) * 36 + 40_000;

                    // Fill 4x4 sub-block
                    for (int jj = 0; jj < 4; jj++)
                    {
                        int base = ((j << 2) + jj) * newW + (i << 2);

                        // precompute part of distance for each corner
                        // multiply jj by 10240 to match scale
                        int mj = 10240 * jj;
                        long sja = (long)(mj - da2)*(mj - da2);
                        long sjb = (long)(mj - db2)*(mj - db2);
                        long sjc = (long)(mj - dc2)*(mj - dc2);
                        long sjd = (long)(mj - dd2)*(mj - dd2);

                        for (int ii = 0; ii < 4; ii++)
                        {
                            // likewise for 'ii'
                            int mi = 10240 * ii;

                            long distA = (long)(mi - da1)*(mi - da1) + sja;
                            long distB = (long)(mi - db1)*(mi - db1) + sjb;
                            long distC = (long)(mi - dc1)*(mi - dc1) + sjc;
                            long distD = (long)(mi - dd1)*(mi - dd1) + sjd;

                            // pick whichever corner is minimal
                            int c;
                            if (distA < distB && distA < distC && distA < distD) {
                                c = v00;
                            }
                            else if (distB < distA && distB < distC && distB < distD) {
                                c = v10;
                            }
                            else if (distC < distA && distC < distB && distC < distD) {
                                c = v01;
                            }
                            else {
                                c = v11;
                            }
                            buf[base + ii] = c;
                        }
                    }
                }

                // Move corners for next iteration
                v00 = v10;
                v01 = v11;
            }
        }

        // Now copy the relevant [w*h] subregion out of buf
        for (int j2 = 0; j2 < h; j2++)
        {
            int srcOff = ( (j2 + (z & 3)) * newW ) + (x & 3);
            System.arraycopy(buf, srcOff, out, j2*w, w);
        }

        return 0;
    }

}
