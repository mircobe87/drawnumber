package it.homepc.mibe.graphics;

public class NumberDrawerFactory {

    public static final int DEFAULT_SIZE_PX = 512;

    public static NumberDrawer getDefaultNumberDrawer(String numberString) {
        return new DefaultNumberDrawer(DEFAULT_SIZE_PX, inputCleaner(numberString));
    }

    private static String inputCleaner(String numberString) {
        String out = "";
        if (numberString != null) {
            out = numberString.replaceAll("[^0-9]", "");
        }
        return out;
    }

}
