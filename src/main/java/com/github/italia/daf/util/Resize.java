package com.github.italia.daf.util;

import net.coobird.thumbnailator.Thumbnails;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Resize {
    private byte[] buffer;

    public Resize(byte[] buffer) {
        this.buffer = buffer;
    }

    public byte[] to(Geometry g) throws IOException {
        return to(g.getW(), g.getH());
    }

    public byte[] to(int w, int h) throws IOException {
        final ByteArrayOutputStream thumb = new ByteArrayOutputStream();
        Thumbnails.of(
                new ByteArrayInputStream(this.buffer))
                .size(w, h)
                .keepAspectRatio(true)
                .toOutputStream(thumb);
        return thumb.toByteArray();
    }
}