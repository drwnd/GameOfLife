public final class MaskGenerator {

    public static void main(String[] args) {
        buildMasksArrays(4, 8, (x, y) -> (y << 2 | x & 3) & 31);
    }

    private static void buildMasksArrays(int width, int height, IndexFunction indexFunction) {
        Masks masks = new Masks(width, height);

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                int maskIndex = indexFunction.index(x, y);
                setBit(masks, indexFunction, maskIndex, x - 1, y - 1);
                setBit(masks, indexFunction, maskIndex, x - 1, y);
                setBit(masks, indexFunction, maskIndex, x - 1, y + 1);

                setBit(masks, indexFunction, maskIndex, x, y + 1);
                setBit(masks, indexFunction, maskIndex, x, y - 1);

                setBit(masks, indexFunction, maskIndex, x + 1, y - 1);
                setBit(masks, indexFunction, maskIndex, x + 1, y);
                setBit(masks, indexFunction, maskIndex, x + 1, y + 1);
            }

        System.out.println(masks);
    }

    private static void setBit(Masks masks, IndexFunction indexFunction, int maskIndex, int x, int y) {
        int index = indexFunction.index(x, y);
        if (x < 0) {
            if (y < 0) masks.bottomLeft[maskIndex] |= 1 << index;
            else if (y >= 8) masks.topLeft[maskIndex] |= 1 << index;
            else masks.left[maskIndex] |= 1 << index;
        } else if (x >= 4) {
            if (y < 0) masks.bottomRight[maskIndex] |= 1 << index;
            else if (y >= 8) masks.topRight[maskIndex] |= 1 << index;
            else masks.right[maskIndex] |= 1 << index;
        } else {
            if (y < 0) masks.bottom[maskIndex] |= 1 << index;
            else if (y >= 8) masks.top[maskIndex] |= 1 << index;
            else masks.center[maskIndex] |= 1 << index;
        }
    }

    private interface IndexFunction {
        int index(int x, int y);
    }

    private record Masks(int[] topLeft, int[] top, int[] topRight,
                         int[] left, int[] center, int[] right,
                         int[] bottomLeft, int[] bottom, int[] bottomRight) {

        public Masks(int width, int height) {
            this(new int[width * height], new int[width * height], new int[width * height],
                    new int[width * height], new int[width * height], new int[width * height],
                    new int[width * height], new int[width * height], new int[width * height]);
        }

        @Override
        public String toString() {
            String format = "const uint[%d] %s = uint[%d]%s;%n".formatted(top.length, "%s", top.length, "%s");

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
    }
}
