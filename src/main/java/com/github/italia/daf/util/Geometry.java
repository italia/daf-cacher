package com.github.italia.daf.util;

public class Geometry {
    private int h;
    private int w;

    public Geometry(int w, int h) {
        this.w = w;
        this.h = h;
    }

    public Geometry() {
    }

    public int getH() {
        return h;
    }

    public int getW() {
        return w;
    }

    @Override
    public String toString() {
        return w + "x" + h;
    }

    public static Geometry fromString(String g) {
        final String[] gg = g.split("x");
        return new Geometry(Integer.parseInt(gg[0]), Integer.parseInt(gg[1]));
    }
}