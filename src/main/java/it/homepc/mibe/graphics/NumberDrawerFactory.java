package it.homepc.mibe.graphics;

public class NumberDrawerFactory {

    public static NumberDrawer getDefaultNumberDrawer(String numberString) {
        return new DefaultNumberDrawer(inputCleaner(numberString));
    }

    private static String inputCleaner(String numberString) {
        String out = "";
        if (numberString != null) {
            out = numberString.replaceAll("([^0-9])", "").replace("\n", "");
        }
        return out;
    }

}
