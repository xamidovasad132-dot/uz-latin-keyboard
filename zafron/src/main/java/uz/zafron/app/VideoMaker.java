package uz.zafron.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Qisqa vertikal video: qorong'i fon, tez kesish, pastda qalin yozuv. Ovozsiz. */
final class VideoMaker {
    static final int W = 720, H = 1280, FPS = 30, PER = 40; // har bir yozuv ~1.3 soniya
    private static final int[] PAL = {0xFFD62828, 0xFF1E6EBE, 0xFFE67814, 0xFF14967F};

    private MediaCodec enc;
    private MediaMuxer mux;
    private int track = -1;
    private boolean started = false;
    private long outCount = 0;
    private final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();

    interface Progress {
        void on(int percent);
    }

    static File make(Context ctx, List<String> caps, Progress pg) throws Exception {
        return new VideoMaker().run(ctx, caps, pg);
    }

    private File run(Context ctx, List<String> caps, Progress pg) throws Exception {
        File out = new File(ctx.getCacheDir(), "zafaron_video.mp4");
        if (out.exists()) out.delete();
        MediaFormat fmt = MediaFormat.createVideoFormat("video/avc", W, H);
        fmt.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        fmt.setInteger(MediaFormat.KEY_BIT_RATE, 3500000);
        fmt.setInteger(MediaFormat.KEY_FRAME_RATE, FPS);
        fmt.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        enc = MediaCodec.createEncoderByType("video/avc");
        enc.configure(fmt, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        Surface surf = enc.createInputSurface();
        enc.start();
        mux = new MediaMuxer(out.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

        Bitmap logo = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.zlogo);
        List<String> scenes = new ArrayList<>(caps);
        int total = (scenes.size() + 1) * PER;
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        text.setTextAlign(Paint.Align.CENTER);
        Random rnd = new Random(7);
        try {
            for (int f = 0; f < total; f++) {
                int idx = f / PER;
                float lt = (f % PER) / (float) FPS;
                Canvas c = surf.lockCanvas(null);
                try {
                    draw(c, fill, text, rnd, scenes, logo, idx, lt, f, total);
                } finally {
                    surf.unlockCanvasAndPost(c);
                }
                drain(false);
                if (pg != null && f % 10 == 0) pg.on(f * 100 / total);
            }
            enc.signalEndOfInputStream();
            drain(true);
        } finally {
            try {
                enc.stop();
            } catch (Exception ignored) {
            }
            enc.release();
            try {
                if (started) mux.stop();
            } catch (Exception ignored) {
            }
            mux.release();
            surf.release();
        }
        return out;
    }

    private void drain(boolean end) {
        while (true) {
            int i = enc.dequeueOutputBuffer(info, 10000);
            if (i == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!end) return;
            } else if (i == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                track = mux.addTrack(enc.getOutputFormat());
                mux.start();
                started = true;
            } else if (i >= 0) {
                java.nio.ByteBuffer b = enc.getOutputBuffer(i);
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) info.size = 0;
                if (info.size > 0 && started && b != null) {
                    b.position(info.offset);
                    b.limit(info.offset + info.size);
                    info.presentationTimeUs = outCount * 1000000L / FPS;
                    outCount++;
                    mux.writeSampleData(track, b, info);
                }
                enc.releaseOutputBuffer(i, false);
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) return;
            }
        }
    }

    private void draw(Canvas c, Paint fill, Paint text, Random rnd, List<String> scenes, Bitmap logo,
                      int idx, float lt, int f, int total) {
        boolean outro = idx >= scenes.size();
        int col = outro ? 0xFFE67814 : PAL[idx % PAL.length];
        float kick = Math.max(0f, 1f - lt / 0.25f);
        c.drawColor(0xFF080606);
        fill.setShader(new RadialGradient(W / 2f, H * 0.42f, 900,
                new int[]{(col & 0x00FFFFFF) | 0x99000000, 0x00000000}, null, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, W, H, fill);
        fill.setShader(null);

        c.save();
        float sc = 1f + 0.10f * kick + 0.03f * (f % PER) / PER;
        c.scale(sc, sc, W / 2f, H / 2f);
        c.translate((rnd.nextFloat() - 0.5f) * 24 * kick, (rnd.nextFloat() - 0.5f) * 24 * kick);

        if (outro) {
            float p = Math.min(1f, lt / 0.5f);
            int s = (int) (520 * (0.6f + 0.4f * p));
            Paint bp = new Paint(Paint.FILTER_BITMAP_FLAG);
            bp.setAlpha((int) (255 * p));
            c.drawBitmap(logo, null, new android.graphics.Rect(W / 2 - s / 2, 520 - s / 2, W / 2 + s / 2, 520 + s / 2), bp);
            text.setStyle(Paint.Style.FILL);
            text.setColor(0xFFF0DCC8);
            text.setTextSize(44);
            c.drawText("Zafaron — sun’iy intellekt", W / 2f, 860, text);
        } else {
            // markazdagi urg'u halqalari
            fill.setStyle(Paint.Style.STROKE);
            fill.setStrokeWidth(10);
            for (int r = 0; r < 3; r++) {
                float rad = 120 + r * 90 + ((lt * 260) % 90);
                fill.setColor((col & 0x00FFFFFF) | ((int) (200 - r * 60) << 24));
                c.drawCircle(W / 2f, 470, rad, fill);
            }
            fill.setStyle(Paint.Style.FILL);
            fill.setColor(0xFFFFFFFF);
            c.drawCircle(W / 2f, 470, 22, fill);
            String cap = scenes.get(idx).toUpperCase(java.util.Locale.ROOT);
            drawCaption(c, text, cap, kick);
        }
        c.restore();

        // chetlarni qoraytirish (vinyetka)
        fill.setShader(new RadialGradient(W / 2f, H / 2f, 900,
                new int[]{0x00000000, 0x00000000, 0xCC000000}, new float[]{0f, 0.55f, 1f}, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, W, H, fill);
        fill.setShader(null);
        if (f > total - 8) {
            fill.setColor(Color.argb((int) (255 * (f - (total - 8)) / 8f), 0, 0, 0));
            c.drawRect(0, 0, W, H, fill);
        }
    }

    private void drawCaption(Canvas c, Paint text, String cap, float kick) {
        float size = 84 * (1f + 0.2f * kick);
        text.setTextSize(size);
        List<String> lines = wrap(text, cap, W - 80);
        while (lines.size() > 3 && size > 40) {
            size -= 6;
            text.setTextSize(size);
            lines = wrap(text, cap, W - 80);
        }
        float y = H - 260 - (lines.size() - 1) * size * 0.55f;
        for (String ln : lines) {
            text.setStyle(Paint.Style.STROKE);
            text.setStrokeWidth(12);
            text.setColor(Color.BLACK);
            c.drawText(ln, W / 2f, y, text);
            text.setStyle(Paint.Style.FILL);
            text.setColor(Color.WHITE);
            c.drawText(ln, W / 2f, y, text);
            y += size * 1.1f;
        }
    }

    private List<String> wrap(Paint p, String s, float max) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : s.split(" ")) {
            String t = cur.length() == 0 ? w : cur + " " + w;
            if (p.measureText(t) > max && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(w);
            } else {
                cur = new StringBuilder(t);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
}
