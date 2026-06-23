import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public final class MaskGenerator {

    public static void main(String[] args) {
        buildMasksArrays(1, (x, y) -> y << 2 | x & 3);
        System.out.println("####################");
        buildMasksArrays(2, (x, y) -> y << 2 | x & 3);
        System.out.println("####################");
        printXsAndYs(2);
        System.out.println("####################");
        printCode1();
    }

    private static void printXsAndYs(int number) {
        int length = (4 + 2 * number - 2) * (8 + 2 * number - 2);
        int[] xs = new int[length];
        int[] ys = new int[length];

        for (int index = 0; index < length; index++) {
            int x = index % (4 + 2 * number - 2) - 1;
            int y = index / (4 + 2 * number - 2) - 1;
            xs[index] = x;
            ys[index] = y;
        }
        System.out.printf("const int[%d] %s_%d = int[%d]%s;%n", length, "XS", number, length, Masks.getArrayStringSigned(xs));
        System.out.printf("const int[%d] %s_%d = int[%d]%s;%n", length, "YS", number, length, Masks.getArrayStringSigned(ys));
    }

    private static Masks buildMasksArrays(int number, IndexFunction bitIndex) {
        Masks masks = new Masks(4, 8, number);
        int maskIndex = 0;

        for (int y = -number + 1; y < 8 + number - 1; y++)
            for (int x = -number + 1; x < 4 + number - 1; x++) {
                int index = maskIndex++;
                setBit(masks, bitIndex, index, x - 1, y - 1);
                setBit(masks, bitIndex, index, x - 1, y);
                setBit(masks, bitIndex, index, x - 1, y + 1);

                setBit(masks, bitIndex, index, x, y + 1);
                setBit(masks, bitIndex, index, x, y - 1);

                setBit(masks, bitIndex, index, x + 1, y - 1);
                setBit(masks, bitIndex, index, x + 1, y);
                setBit(masks, bitIndex, index, x + 1, y + 1);
            }

        System.out.println(masks.toString(number));
        return masks;
    }

    private static void setBit(Masks masks, IndexFunction indexFunction, int maskIndex, int x, int y) {
        int bitIndex = indexFunction.index(x, y);
        if (x < 0) {
            if (y < 0) masks.bottomLeft[maskIndex] |= 1 << bitIndex;
            else if (y >= 8) masks.topLeft[maskIndex] |= 1 << bitIndex;
            else masks.left[maskIndex] |= 1 << bitIndex;
        } else if (x >= 4) {
            if (y < 0) masks.bottomRight[maskIndex] |= 1 << bitIndex;
            else if (y >= 8) masks.topRight[maskIndex] |= 1 << bitIndex;
            else masks.right[maskIndex] |= 1 << bitIndex;
        } else {
            if (y < 0) masks.bottom[maskIndex] |= 1 << bitIndex;
            else if (y >= 8) masks.top[maskIndex] |= 1 << bitIndex;
            else masks.center[maskIndex] |= 1 << bitIndex;
        }
    }

    private static void printCode1() {
        int index = 0;
        StringBuilder builder = new StringBuilder();
        Masks masks = buildMasksArrays(1, (x, y) -> y << 2 | x & 3);

        for (int y = 0; y < 8; y++)
            for (int x = 0; x < 4; x++) {
                int index1 = index;
                builder.append("""
                        if (x < 0) {
                            if (y < 0) counter = region.bottomLeft >> BITINDEX & 1;
                            else if (y >= 8) counter = region.topLeft >> BITINDEX & 1;
                            else counter = region.left >> BITINDEX & 1;
                        } else if (x >= 4) {
                            if (y < 0) counter = region.bottomRight >> BITINDEX & 1;
                            else if (y >= 8) counter = region.topRight >> BITINDEX & 1;
                            else counter = region.right >> BITINDEX & 1;
                        } else {
                            if (y < 0) counter = region.bottom >> BITINDEX & 1;
                            else if (y >= 8) counter = region.top >> BITINDEX & 1;
                            else counter = region.center >> BITINDEX & 1;
                        }
                        counter <<= 4;
                        counter += bitCount(
                            region.topLeft & TOP_LEFT |
                            region.top & TOP |
                            region.topRight & TOP_RIGHT |
                            region.left & LEFT |
                            region.center & CENTER |
                            region.right & RIGHT |
                            region.bottomLeft & BOTTOM_LEFT |
                            region.bottom & BOTTOM |
                            region.bottomRight & BOTTOM_RIGHT);
                        result |= (RULES >> counter & 1) << BITINDEX;
                        
                        """
                        .replace("BITINDEX", String.valueOf((y << 2 | x & 3) & 31))
                        .replace("x", String.valueOf(x))
                        .replace("y", String.valueOf(y))
                        .replace("RULES", "0xC0008")
                        .replace("INDEX", String.valueOf(index1))
                        .replace("TOP_LEFT", "0x" + Integer.toHexString(masks.topLeft[index1]))
                        .replace("TOP_RIGHT", "0x" + Integer.toHexString(masks.topRight[index1]))
                        .replace("BOTTOM_LEFT", "0x" + Integer.toHexString(masks.bottomLeft[index1]))
                        .replace("BOTTOM_RIGHT", "0x" + Integer.toHexString(masks.bottomRight[index1]))
                        .replace("TOP", "0x" + Integer.toHexString(masks.top[index1]))
                        .replace("LEFT", "0x" + Integer.toHexString(masks.left[index1]))
                        .replace("CENTER", "0x" + Integer.toHexString(masks.center[index1]))
                        .replace("RIGHT", "0x" + Integer.toHexString(masks.right[index1]))
                        .replace("BOTTOM", "0x" + Integer.toHexString(masks.bottom[index1]))
                );
                index1++;
                index = index1;
            }

        StringSelection selection = new StringSelection(builder.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        System.out.println(builder);
    }

    private static void printCode2() {
        int index = 0;
        StringBuilder builder = new StringBuilder();
        Masks masks = buildMasksArrays(2, (x, y) -> y << 2 | x & 3);

        for (int y = -1; y <= 8; y++)
            for (int x = -1; x <= 4; x++) {
                int index1 = index;
                builder.append("""
                        if (x < 0) {
                            if (y < 0) counter = region.bottomLeft >> BITINDEX & 1;
                            else if (y >= 8) counter = region.topLeft >> BITINDEX & 1;
                            else counter = region.left >> BITINDEX & 1;
                        } else if (x >= 4) {
                            if (y < 0) counter = region.bottomRight >> BITINDEX & 1;
                            else if (y >= 8) counter = region.topRight >> BITINDEX & 1;
                            else counter = region.right >> BITINDEX & 1;
                        } else {
                            if (y < 0) counter = region.bottom >> BITINDEX & 1;
                            else if (y >= 8) counter = region.top >> BITINDEX & 1;
                            else counter = region.center >> BITINDEX & 1;
                        }
                        counter <<= 4;
                        counter += bitCount(
                            region.topLeft & TOP_LEFT |
                            region.top & TOP |
                            region.topRight & TOP_RIGHT |
                            region.left & LEFT |
                            region.center & CENTER |
                            region.right & RIGHT |
                            region.bottomLeft & BOTTOM_LEFT |
                            region.bottom & BOTTOM |
                            region.bottomRight & BOTTOM_RIGHT);
                        value = (RULES >> counter & 1) << BITINDEX;
                        if (x < 0) {
                            if (y < 0) result.bottomLeft |= value;
                            else if (y >= 8) result.topLeft |= value;
                            else result.left |= value;
                        } else if (x >= 4) {
                            if (y < 0) result.bottomRight |= value;
                            else if (y >= 8) result.topRight |= value;
                            else result.right |= value;
                        } else {
                            if (y < 0) result.bottom |= value;
                            else if (y >= 8) result.top |= value;
                            else result.center |= value;
                        }
                        
                        """
                        .replace("BITINDEX", String.valueOf((y << 2 | x & 3) & 31))
                        .replace("x", String.valueOf(x))
                        .replace("y", String.valueOf(y))
                        .replace("RULES", "0xC0008")
                        .replace("INDEX", String.valueOf(index1))
                        .replace("TOP_LEFT", "0x" + Integer.toHexString(masks.topLeft[index1]))
                        .replace("TOP_RIGHT", "0x" + Integer.toHexString(masks.topRight[index1]))
                        .replace("BOTTOM_LEFT", "0x" + Integer.toHexString(masks.bottomLeft[index1]))
                        .replace("BOTTOM_RIGHT", "0x" + Integer.toHexString(masks.bottomRight[index1]))
                        .replace("TOP", "0x" + Integer.toHexString(masks.top[index1]))
                        .replace("LEFT", "0x" + Integer.toHexString(masks.left[index1]))
                        .replace("CENTER", "0x" + Integer.toHexString(masks.center[index1]))
                        .replace("RIGHT", "0x" + Integer.toHexString(masks.right[index1]))
                        .replace("BOTTOM", "0x" + Integer.toHexString(masks.bottom[index1]))
                );
                index1++;
                index = index1;
            }

        StringSelection selection = new StringSelection(builder.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        System.out.println(builder);
    }


    private interface IndexFunction {
        int index(int x, int y);
    }

    private record Masks(int[] topLeft, int[] top, int[] topRight,
                         int[] left, int[] center, int[] right,
                         int[] bottomLeft, int[] bottom, int[] bottomRight) {

        public Masks(int length) {
            this(new int[length], new int[length], new int[length],
                    new int[length], new int[length], new int[length],
                    new int[length], new int[length], new int[length]);
        }

        public Masks(int width, int height, int number) {
            this((width + 2 * number - 2) * (height + 2 * number - 2));
        }

        public String toString(int number) {
            String format = "const uint[%d] %s_%d = uint[%d]%s;%n".formatted(top.length, "%s", number, top.length, "%s");

            return format.formatted("TOP_LEFT", getArrayString(topLeft)) +
                    format.formatted("TOP", getArrayString(top)) +
                    format.formatted("TOP_RIGHT", getArrayString(topRight)) +
                    format.formatted("LEFT", getArrayString(left)) +
                    format.formatted("CENTER", getArrayString(center)) +
                    format.formatted("RIGHT", getArrayString(right)) +
                    format.formatted("BOTTOM_LEFT", getArrayString(bottomLeft)) +
                    format.formatted("BOTTOM", getArrayString(bottom)) +
                    format.formatted("BOTTOM_RIGHT", getArrayString(bottomRight));
        }

        private static String getArrayString(int[] array) {
            StringBuilder builder = new StringBuilder("(");
            for (int mask : array) builder.append("0x").append(Integer.toHexString(mask)).append(",");
            builder.setCharAt(builder.length() - 1, ')');
            return builder.toString();
        }

        private static String getArrayStringSigned(int[] array) {
            StringBuilder builder = new StringBuilder("(");
            for (int mask : array) builder.append(mask).append(",");
            builder.setCharAt(builder.length() - 1, ')');
            return builder.toString();
        }
    }
}
