package core.settings;

public enum FloatSetting {

    GUI_SIZE(0.25F, 1.0F, 1.0F, 0.01F),
    TEXT_SIZE(0.5F, 3.0F, 1.0F, 0.01F),
    RIM_THICKNESS(0.0F, 0.1F, 0.015625F);

    public static void setIfPresent(String name, String value) {
        try {
            valueOf(name).value = Float.parseFloat(value);
        } catch (IllegalArgumentException ignore) {

        }
    }

    FloatSetting(float min, float max, float defaultValue, float accuracy) {
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.accuracy = accuracy;
    }

    FloatSetting(float min, float max, float defaultValue) {
        this.min = min;
        this.max = max;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.accuracy = 0.00001F;
    }

    void setValue(float value) {
        this.value = value;
    }

    public float value() {
        return value;
    }

    public float valueFronFraction(float fraction) {
        float unroundedValue = min + fraction * (max - min);
        float roundingOffset = absMin(-(unroundedValue % accuracy), accuracy - unroundedValue % accuracy);
        return unroundedValue + roundingOffset;
    }

    public float fractionFromValue(float value) {
        return (value - min) / (max - min);
    }

    public float defaultValue() {
        return defaultValue;
    }

    private static float absMin(float a, float b) {
        return Math.abs(a) < Math.abs(b) ? a : b;
    }

    private final float min, max, defaultValue, accuracy;
    private float value;
}
