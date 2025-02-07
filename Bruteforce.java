import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Bruteforce {
    private static Set<Integer> loadSeedLower20Masks(String filename) {
        Set<Integer> masks = new HashSet<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(filename));
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                try {
                    long seed = Long.parseLong(line);
                    masks.add((int) (seed & 0xFFFFF));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping invalid seed in file: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading seed file: " + e.getMessage());
        }
        return masks;
    }

    public static void runBruteforce(String[] args) {
        Object mcver = "MC_1_12";
        boolean largeBiomes = false;
        List<String> numericArgs = new ArrayList<>();
        List<String> seedFileNames = new ArrayList<>();
        for (String arg : args) {
            if (arg.toLowerCase().endsWith(".txt")) {
                seedFileNames.add(arg);
            } else {
                numericArgs.add(arg);
            }
        }
        List<RiverGenerator.BoundingBox> boxes = new ArrayList<>();
        if (!numericArgs.isEmpty() && numericArgs.get(numericArgs.size()-1).equalsIgnoreCase("range")) {
            int numNumeric = numericArgs.size() - 1;
            if (numNumeric % 4 != 0) {
                System.err.println("Error: In range mode, the number of numeric arguments (excluding 'range') must be a multiple of 4.");
                System.exit(1);
            }
            for (int i = 0; i < numNumeric; i += 4) {
                int xmin = Integer.parseInt(numericArgs.get(i));
                int zmin = Integer.parseInt(numericArgs.get(i + 1));
                int xmax = Integer.parseInt(numericArgs.get(i + 2));
                int zmax = Integer.parseInt(numericArgs.get(i + 3));
                boxes.add(new RiverGenerator.BoundingBox(xmin, zmin, xmax, zmax));
                System.out.println("Added range-mode box: (" + xmin + ", " + zmin + ") to (" + xmax + ", " + zmax + ")");
            }
        } else if (!numericArgs.isEmpty()) {
            if (numericArgs.size() % 3 != 0) {
                System.err.println("Error: In scale mode, the number of numeric arguments must be a multiple of 3.");
                System.exit(1);
            }
            for (int i = 0; i < numericArgs.size(); i += 3) {
                int centerX = Integer.parseInt(numericArgs.get(i));
                int centerZ = Integer.parseInt(numericArgs.get(i + 1));
                int scaleValue = Integer.parseInt(numericArgs.get(i + 2));
                int half = scaleValue / 2;
                int xmin = centerX - half;
                int zmin = centerZ - half;
                int xmax = centerX + half;
                int zmax = centerZ + half;
                boxes.add(new RiverGenerator.BoundingBox(xmin, zmin, xmax, zmax));
                System.out.println("Added scale-mode box: Center (" + centerX + ", " + centerZ + "), scale " + scaleValue +
                        " -> Box: (" + xmin + ", " + zmin + ") to (" + xmax + ", " + zmax + ")");
            }
        } else {
            boxes.add(new RiverGenerator.BoundingBox(320, -103, 323, -97));
            System.out.println("Using default bounding box: (320, -103) to (323, -97)");
        }
        Set<Integer> targetMasks = new HashSet<>();
        boolean useTextFile;
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
            System.out.println("Intersection of seed masks has " + targetMasks.size() + " masks.");
        } else {
            useTextFile = false;
        }
        long range1Start, range1End, range2Start, range2End;
        if (useTextFile) {
            range1Start = 0L;
            range1End   = 33_554_431L;
            range2Start = 33_554_432L;
            range2End   = 67_108_863L;
        } else {
            range1Start = 0L;
            range1End   = 11_118_602L;
            range2Start = 22_237_206L;
            range2End   = 44_673_034L;
        }
        final int CHUNK_SIZE = 10_000;
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicInteger completedChunks = new AtomicInteger(0);
        AtomicLong seedsSearched = new AtomicLong(0);
        ConcurrentLinkedQueue<Long> validSeeds = new ConcurrentLinkedQueue<>();
        int totalChunks1 = (int) (((range1End - range1Start + 1) + CHUNK_SIZE - 1) / CHUNK_SIZE);
        int totalChunks2 = (int) (((range2End - range2Start + 1) + CHUNK_SIZE - 1) / CHUNK_SIZE);
        int totalChunks = totalChunks1 + totalChunks2;
        ScheduledExecutorService progressExecutor = Executors.newSingleThreadScheduledExecutor();
        final long[] prevSeedCount = {0};
        ScheduledFuture<?> progressFuture = progressExecutor.scheduleAtFixedRate(() -> {
            int doneChunks = completedChunks.get();
            long currentSeeds = seedsSearched.get();
            long seedsPerSec = currentSeeds - prevSeedCount[0];
            prevSeedCount[0] = currentSeeds;
            double percent = (doneChunks / (double) totalChunks) * 100;
            System.out.printf("Progress: %d/%d chunks (%.2f%%), seeds searched: %d, seeds/sec: %d%n",
                    doneChunks, totalChunks, percent, currentSeeds, seedsPerSec);
        }, 0, 1, TimeUnit.SECONDS);
        for (long cs = range1Start; cs <= range1End; cs += CHUNK_SIZE) {
            final long chunkStart = cs;
            final long chunkEnd = Math.min(cs + CHUNK_SIZE - 1, range1End);
            executor.submit(() -> {
                for (long seed = chunkStart; seed <= chunkEnd; seed++) {
                    seedsSearched.incrementAndGet();
                    if (!targetMasks.isEmpty() && !targetMasks.contains((int) (seed & 0xFFFFF))) {
                        continue;
                    }
                    boolean seedValid = true;
                    for (RiverGenerator.BoundingBox bb : boxes) {
                        if (!RiverGenerator.hasRiverInBoundingBox(seed, mcver, largeBiomes,
                                bb.xMin, bb.zMin, bb.xMax, bb.zMax)) {
                            seedValid = false;
                            break;
                        }
                    }
                    if (seedValid) {
                        validSeeds.add(seed);
                        if (useTextFile) {
                            System.out.println("Match found on Seed: " + seed);
                        } else {
                            System.out.println("Match found on Seed: " + seed +
                                    " (Partner: " + RiverGenerator.partner(seed) + ")");
                        }
                    }
                }
                completedChunks.incrementAndGet();
            });
        }
        for (long cs = range2Start; cs <= range2End; cs += CHUNK_SIZE) {
            final long chunkStart = cs;
            final long chunkEnd = Math.min(cs + CHUNK_SIZE - 1, range2End);
            executor.submit(() -> {
                for (long seed = chunkStart; seed <= chunkEnd; seed++) {
                    seedsSearched.incrementAndGet();
                    if (!targetMasks.isEmpty() && !targetMasks.contains((int) (seed & 0xFFFFF))) {
                        continue;
                    }
                    boolean seedValid = true;
                    for (RiverGenerator.BoundingBox bb : boxes) {
                        if (!RiverGenerator.hasRiverInBoundingBox(seed, mcver, largeBiomes,
                                bb.xMin, bb.zMin, bb.xMax, bb.zMax)) {
                            seedValid = false;
                            break;
                        }
                    }
                    if (seedValid) {
                        validSeeds.add(seed);
                        if (useTextFile) {
                            System.out.println("Match found on Seed: " + seed);
                        } else {
                            System.out.println("Match found on Seed: " + seed +
                                    " (Partner: " + RiverGenerator.partner(seed) + ")");
                        }
                    }
                }
                completedChunks.incrementAndGet();
            });
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        progressFuture.cancel(true);
        progressExecutor.shutdownNow();
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("output.txt")))) {
            for (Long seed : validSeeds) {
                if (useTextFile) {
                    writer.println(seed);
                } else {
                    writer.println(seed);
                    writer.println(RiverGenerator.partner(seed));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done searching and exporting seeds!");
    }
}
