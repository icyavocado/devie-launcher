package com.evie.search.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;

/* JADX INFO: loaded from: classes.dex */
public class TextDrawable extends ShapeDrawable {
    private final Paint borderPaint;
    private final int borderThickness;
    private final int color;
    private final int fontSize;
    private final int height;
    private final float radius;
    private final RectShape shape;
    private final String text;
    private final Paint textPaint;
    private final int width;

    public static class Builder implements IBuilder, IConfigBuilder, IShapeBuilder {
        private int borderThickness;
        private int color;
        private Typeface font;
        private int fontSize;
        private int height;
        private boolean isBold;
        public float radius;
        private RectShape shape;
        private String text;
        public int textColor;
        private boolean toUpperCase;
        private int width;

        private Builder() {
            this.text = "";
            this.color = -7829368;
            this.textColor = -1;
            this.borderThickness = 0;
            this.width = -1;
            this.height = -1;
            this.shape = new RectShape();
            this.font = Typeface.create("sans-serif-light", 0);
            this.fontSize = -1;
            this.isBold = false;
            this.toUpperCase = false;
        }

        @Override // com.evie.search.drawable.TextDrawable.IShapeBuilder
        public IConfigBuilder beginConfig() {
            return this;
        }

        @Override // com.evie.search.drawable.TextDrawable.IConfigBuilder
        public IConfigBuilder bold() {
            this.isBold = true;
            return this;
        }

        public TextDrawable build(String str, int i) {
            this.color = i;
            this.text = str;
            return new TextDrawable(this);
        }

        @Override // com.evie.search.drawable.TextDrawable.IShapeBuilder
        public TextDrawable buildRound(String str, int i) {
            round();
            return build(str, i);
        }

        @Override // com.evie.search.drawable.TextDrawable.IConfigBuilder
        public IShapeBuilder endConfig() {
            return this;
        }

        @Override // com.evie.search.drawable.TextDrawable.IConfigBuilder
        public IConfigBuilder fontSize(int i) {
            this.fontSize = i;
            return this;
        }

        public IBuilder round() {
            this.shape = new OvalShape();
            return this;
        }

        @Override // com.evie.search.drawable.TextDrawable.IConfigBuilder
        public IConfigBuilder withBorder(int i) {
            this.borderThickness = i;
            return this;
        }
    }

    public interface IBuilder {
    }

    public interface IConfigBuilder {
        IConfigBuilder bold();

        IShapeBuilder endConfig();

        IConfigBuilder fontSize(int i);

        IConfigBuilder withBorder(int i);
    }

    public interface IShapeBuilder {
        IConfigBuilder beginConfig();

        TextDrawable buildRound(String str, int i);
    }

    private TextDrawable(Builder builder) {
        super(builder.shape);
        this.shape = builder.shape;
        this.height = builder.height;
        this.width = builder.width;
        this.radius = builder.radius;
        this.text = builder.toUpperCase ? builder.text.toUpperCase() : builder.text;
        this.color = builder.color;
        this.fontSize = builder.fontSize;
        this.textPaint = new Paint();
        this.textPaint.setColor(builder.textColor);
        this.textPaint.setAntiAlias(true);
        this.textPaint.setFakeBoldText(builder.isBold);
        this.textPaint.setTypeface(builder.font);
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setStrokeWidth(builder.borderThickness);
        this.borderThickness = builder.borderThickness;
        this.borderPaint = new Paint();
        this.borderPaint.setColor(builder.textColor);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth(this.borderThickness);
        this.borderPaint.setAntiAlias(true);
        getPaint().setColor(this.color);
    }

    public static IShapeBuilder builder() {
        return new Builder();
    }

    private void drawBorder(Canvas canvas) {
        RectF rectF = new RectF(getBounds());
        int i = this.borderThickness;
        rectF.inset(i / 2, i / 2);
        RectShape rectShape = this.shape;
        if (rectShape instanceof OvalShape) {
            canvas.drawOval(rectF, this.borderPaint);
        } else if (!(rectShape instanceof RoundRectShape)) {
            canvas.drawRect(rectF, this.borderPaint);
        } else {
            float f = this.radius;
            canvas.drawRoundRect(rectF, f, f, this.borderPaint);
        }
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Rect bounds = getBounds();
        if (this.borderThickness > 0) {
            drawBorder(canvas);
        }
        int iSave = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        int iWidth = this.width;
        if (iWidth < 0) {
            iWidth = bounds.width();
        }
        int iHeight = this.height;
        if (iHeight < 0) {
            iHeight = bounds.height();
        }
        int iMin = this.fontSize;
        if (iMin < 0) {
            iMin = Math.min(iWidth, iHeight) / 2;
        }
        this.textPaint.setTextSize(iMin);
        canvas.drawText(this.text, iWidth / 2, (iHeight / 2) - ((this.textPaint.descent() + this.textPaint.ascent()) / 2.0f), this.textPaint);
        canvas.restoreToCount(iSave);
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.height;
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.width;
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public int getOpacity() {
        return -3;
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public void setAlpha(int i) {
        this.textPaint.setAlpha(i);
    }

    @Override // android.graphics.drawable.ShapeDrawable, android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter colorFilter) {
        this.textPaint.setColorFilter(colorFilter);
    }
}
