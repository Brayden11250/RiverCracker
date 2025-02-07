import java.util.*;
import java.util.Arrays;

public class RiverGenerator {

    public static final int RIVER = 7;      
    public static final int NO_RIVER = -1;  

    public static long partner(long seed) {
        if (seed < 22237206L) {
            return 22237205L - seed;
        } else {
            return 89346069L - seed;
        }
    }

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
    public static int mcFirstInt(long s, int mod) {
        int ret = (int)((s >>> 24) % mod);
        return ret < 0 ? ret + mod : ret;
    }
    public static boolean mcFirstIsZero(long s, int mod) {
        return ((s >>> 24) % mod) == 0;
    }

    public static class BoundingBox {
        public int xMin, zMin, xMax, zMax;
        public BoundingBox(int xMin, int zMin, int xMax, int zMax) {
            this.xMin = xMin;
            this.zMin = zMin;
            this.xMax = xMax;
            this.zMax = zMax;
        }
    }

    @FunctionalInterface
    public interface MapFunction {
        int apply(Layer l, int[] out, int x, int z, int w, int h);
    }
    public static class Layer {
        public long layerSeed, startSalt, startSeed;
        public MapFunction getMap;
        public Layer p, p2;
    }
    public static class LayerStack {
        public Layer[] layers;
        public Layer entry;
        public LayerStack(int size) {
            layers = new Layer[size];
            for (int i = 0; i < size; i++) {
                layers[i] = new Layer();
            }
        }
    }

    public static final MapFunction MAP_CONSTANT = (l, out, x, z, w, h) -> {
        Arrays.fill(out, 1);
        return 0;
    };

    public static int mapRiverInit(Layer l, int[] out, int x, int z, int w, int h) {
        l.p.getMap.apply(l.p, out, x, z, w, h);
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                if (out[i + j * w] > 0) {
                    long cs = getChunkSeed(l.startSeed, i + x, j + z);
                    out[i + j * w] = 2 + mcFirstInt(cs, 299999);
                } else {
                    out[i + j * w] = 0;
                }
            }
        }
        return 0;
    }

    public static int mapZoom(Layer l, int[] out, int x, int z, int w, int h) {
        int pX = x >> 1, pZ = z >> 1;
        int pW = (((x + w) >> 1) - pX) + 1, pH = (((z + h) >> 1) - pZ) + 1;
        l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        int newW = pW << 1, newH = pH << 1;
        int[] buf = new int[(newW + 1) * (newH + 1)];
        long st = l.startSalt, ss = l.startSeed;
        for (int j = 0; j < pH; j++) {
            int idx = (j << 1) * newW;
            int v00 = out[j * pW];
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
                    int chunkX = (i + pX) << 1, chunkZ = (j + pZ) << 1;
                    long cs = ss;
                    cs += chunkX; cs = cs * (cs * 1284865837L + 4150755663L);
                    cs += chunkZ; cs = cs * (cs * 1284865837L + 4150755663L);
                    cs += chunkX; cs = cs * (cs * 1284865837L + 4150755663L);
                    cs += chunkZ;
                    buf[idx] = v00;
                    buf[idx + newW] = (((cs >>> 24) & 1) != 0) ? v01 : v00;
                    idx++;
                    cs = cs * (cs * 1284865837L + 4150755663L); cs += st;
                    buf[idx] = (((cs >>> 24) & 1) != 0) ? v10 : v00;
                    buf[idx + newW] = select4(cs, st, v00, v01, v10, v11);
                    idx++;
                }
                v00 = v10; v01 = v11;
            }
        }
        for (int j = 0; j < h; j++)
            System.arraycopy(buf, (j + (z & 1)) * newW + (x & 1), out, j * w, w);
        return 0;
    }

    private static int select4(long cs, long st, int v00, int v01, int v10, int v11) {
        int cv00 = ((v00 == v10 ? 1 : 0) + (v00 == v01 ? 1 : 0) + (v00 == v11 ? 1 : 0));
        int cv10 = ((v10 == v01 ? 1 : 0) + (v10 == v11 ? 1 : 0));
        int cv01 = (v01 == v11 ? 1 : 0);
        if (cv00 > cv10 && cv00 > cv01) return v00;
        else if (cv10 > cv00 && cv10 > cv01) return v10;
        else if (cv01 > cv00) return v01;
        else {
            cs = cs * (cs * 1284865837L + 4150755663L);
            cs += st;
            int r = (int)((cs >>> 24) & 3);
            return (r == 0 ? v00 : r == 1 ? v10 : r == 2 ? v01 : v11);
        }
    }

    public static int mapRiver(Layer l, int[] out, int x, int z, int w, int h) {
        int pX = x - 1, pZ = z - 1, pW = w + 2, pH = h + 2;
        l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int v01 = out[(i + 0) + (j + 1) * pW],
                    v11 = out[(i + 1) + (j + 1) * pW],
                    v21 = out[(i + 2) + (j + 1) * pW],
                    v10 = out[(i + 1) + (j + 0) * pW],
                    v12 = out[(i + 1) + (j + 2) * pW];
                out[i + j * w] = (v11 == v01 && v11 == v10 && v11 == v12 && v11 == v21)
                        ? NO_RIVER : RIVER;
            }
        }
        return 0;
    }

    public static int mapSmooth(Layer l, int[] out, int x, int z, int w, int h) {
        int pX = x - 1, pZ = z - 1, pW = w + 2, pH = h + 2;
        l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        long ss = l.startSeed;
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                int v11 = out[(i + 1) + (j + 1) * pW],
                    v01 = out[(i + 0) + (j + 1) * pW],
                    v10 = out[(i + 1) + (j + 0) * pW];
                if (v11 != v01 || v11 != v10) {
                    int v21 = out[(i + 2) + (j + 1) * pW],
                        v12 = out[(i + 1) + (j + 2) * pW];
                    if (v01 == v21 && v10 == v12) {
                        long cs = getChunkSeed(ss, i + x, j + z);
                        v11 = ((cs & (1L << 24)) != 0) ? v10 : v01;
                    } else {
                        if (v01 == v21) v11 = v01;
                        if (v10 == v12) v11 = v10;
                    }
                }
                out[i + j * w] = v11;
            }
        }
        return 0;
    }

    public static int mapVoronoiZoom(Layer l, int[] out, int x, int z, int w, int h) {
        x -= 2; z -= 2;
        int pX = x >> 2, pZ = z >> 2;
        int pW = (((x + w) >> 2) - pX) + 2, pH = (((z + h) >> 2) - pZ) + 2;
        l.p.getMap.apply(l.p, out, pX, pZ, pW, pH);
        int newW = pW << 2, newH = pH << 2;
        int[] buf = new int[(newW + 1) * (newH + 1)];
        long st = l.startSalt, ss = l.startSeed;
        for (int j = 0; j < pH - 1; j++) {
            int v00 = out[j * pW], v01 = out[(j + 1) * pW];
            for (int i = 0; i < pW - 1; i++) {
                int v10 = out[i + 1 + j * pW], v11 = out[i + 1 + (j + 1) * pW];
                if (v00 == v10 && v00 == v01 && v00 == v11) {
                    for (int jj = 0; jj < 4; jj++) {
                        int idx = ((j << 2) + jj) * newW + (i << 2);
                        for (int ii = 0; ii < 4; ii++) {
                            buf[idx + ii] = v00;
                        }
                    }
                } else {
                    long cs;
                    cs = getChunkSeed(ss, (i + pX) << 2, (j + pZ) << 2);
                    int da1 = (mcFirstInt(cs, 1024) - 512) * 36;
                    cs = mcStepSeed(cs, st);
                    int da2 = (mcFirstInt(cs, 1024) - 512) * 36;
                    cs = getChunkSeed(ss, (i + pX + 1) << 2, (j + pZ) << 2);
                    int db1 = (mcFirstInt(cs, 1024) - 512) * 36 + 40000;
                    cs = mcStepSeed(cs, st);
                    int db2 = (mcFirstInt(cs, 1024) - 512) * 36;
                    cs = getChunkSeed(ss, (i + pX) << 2, (j + pZ + 1) << 2);
                    int dc1 = (mcFirstInt(cs, 1024) - 512) * 36;
                    cs = mcStepSeed(cs, st);
                    int dc2 = (mcFirstInt(cs, 1024) - 512) * 36 + 40000;
                    cs = getChunkSeed(ss, (i + pX + 1) << 2, (j + pZ + 1) << 2);
                    int dd1 = (mcFirstInt(cs, 1024) - 512) * 36 + 40000;
                    cs = mcStepSeed(cs, st);
                    int dd2 = (mcFirstInt(cs, 1024) - 512) * 36 + 40000;
                    for (int jj = 0; jj < 4; jj++) {
                        int base = ((j << 2) + jj) * newW + (i << 2);
                        int mj = 10240 * jj;
                        long sja = (long)(mj - da2) * (mj - da2);
                        long sjb = (long)(mj - db2) * (mj - db2);
                        long sjc = (long)(mj - dc2) * (mj - dc2);
                        long sjd = (long)(mj - dd2) * (mj - dd2);
                        for (int ii = 0; ii < 4; ii++) {
                            int mi = 10240 * ii;
                            long distA = (long)(mi - da1) * (mi - da1) + sja;
                            long distB = (long)(mi - db1) * (mi - db1) + sjb;
                            long distC = (long)(mi - dc1) * (mi - dc1) + sjc;
                            long distD = (long)(mi - dd1) * (mi - dd1) + sjd;
                            int c = (distA < distB && distA < distC && distA < distD) ? v00 :
                                    (distB < distA && distB < distC && distB < distD) ? v10 :
                                            (distC < distA && distC < distB && distC < distD) ? v01 : v11;
                            buf[base + ii] = c;
                        }
                    }
                }
                v00 = v10; v01 = v11;
            }
        }
        for (int j2 = 0; j2 < h; j2++) {
            int srcOff = ((j2 + (z & 3)) * newW) + (x & 3);
            System.arraycopy(buf, srcOff, out, j2 * w, w);
        }
        return 0;
    }

    public static void setupLayer(Layer l, Layer p, int s, MapFunction func) {
        l.layerSeed = getLayerSeed(s);
        l.p = p;
        l.p2 = null;
        l.getMap = func;
    }
    public static void setWorldSeed(Layer layer, long worldSeed) {
        if (layer == null) return;
        if (layer.p != null) setWorldSeed(layer.p, worldSeed);
        if (layer.p2 != null) setWorldSeed(layer.p2, worldSeed);
        long st = worldSeed;
        st = mcStepSeed(st, layer.layerSeed);
        st = mcStepSeed(st, layer.layerSeed);
        st = mcStepSeed(st, layer.layerSeed);
        layer.startSalt = st;
        layer.startSeed = mcStepSeed(st, 0);
    }
    public static int genArea(Layer layer, int[] out, int areaX, int areaZ, int w, int h) {
        Arrays.fill(out, 0);
        return layer.getMap.apply(layer, out, areaX, areaZ, w, h);
    }
    public static void setupGenerator(LayerStack stack, long seed) {
        Layer[] l = stack.layers;
        setupLayer(l[0], null, 1, MAP_CONSTANT);
        setupLayer(l[1], l[0], 100, RiverGenerator::mapRiverInit);
        setupLayer(l[2], l[1], 1000, RiverGenerator::mapZoom);
        setupLayer(l[3], l[2], 1001, RiverGenerator::mapZoom);
        setupLayer(l[4], l[3], 1000, RiverGenerator::mapZoom);
        setupLayer(l[5], l[4], 1001, RiverGenerator::mapZoom);
        setupLayer(l[6], l[5], 1002, RiverGenerator::mapZoom);
        setupLayer(l[7], l[6], 1003, RiverGenerator::mapZoom);
        setupLayer(l[8], l[7], 1, RiverGenerator::mapRiver);
        setupLayer(l[9], l[8], 1000, RiverGenerator::mapSmooth);
        setupLayer(l[10], l[9], 10, RiverGenerator::mapVoronoiZoom);
        setWorldSeed(l[10], seed);
        stack.entry = l[10];
    }

    public static boolean hasRiverInBoundingBox(long seed, Object mcver, boolean largeBiomes,
                                                  int xMin, int zMin, int xMax, int zMax) {
        LayerStack stack = new LayerStack(11);
        setupGenerator(stack, seed);

        int width = xMax - xMin + 1;
        int height = zMax - zMin + 1;

        int[] out = new int[width * height];
        genArea(stack.entry, out, xMin, zMin, width, height);

        for (int cell : out) {
            if (cell == RIVER) {
                return true;
            }
        }
        return false;
    }
}
