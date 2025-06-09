package com.example.quanbadulichmietvuon.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.example.quanbadulichmietvuon.R;

public class MarqueeBannerView extends View {
    private Paint paintText, paintImage;
    private Bitmap backgroundBitmap;
    private String text ;
    private float textWidth;
    private float xOffset = 0;
    private float speed = 3;
    private float cornerRadius = 50f; // Độ bo góc

    public MarqueeBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        // lấy text theo ngôn ngữ đã chọn
        text = context.getString(R.string.what_to_eat_where_to_stay);
        // Load ảnh nền từ drawable
        backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.banner);

        // Vẽ chữ
        paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintText.setTextSize(40);
        paintText.setColor(0xFFFFFFFF); // Màu trắng
        textWidth = paintText.measureText(text);

        // Paint cho ảnh
        paintImage = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // tạo path bo góc
        Path path = new Path();
        RectF rect = new RectF(0, 0, getWidth(), getHeight());
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW);
        canvas.clipPath(path);

        // vẽ background là ảnh bo góc
        if (backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, null, rect, paintImage);
        }

        // tạo paint viền chữ lấp lánh
        Paint paintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintStroke.setTextSize(40);
        paintStroke.setColor(0xFFFFD700); // vàng ánh kim
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(8);

        // vẽ viền chữ trước
        canvas.drawText(text, xOffset, getHeight() / 2 + 20, paintStroke);

        // vẽ chữ chính
        canvas.drawText(text, xOffset, getHeight() / 2 + 20, paintText);

        // xử lý hiệu ứng chạy chữ
        xOffset -= speed;
        if (xOffset < -textWidth) {
            xOffset = getWidth();
        }
        invalidate();
    }
}
