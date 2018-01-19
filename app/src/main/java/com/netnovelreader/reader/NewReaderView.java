package com.netnovelreader.reader;

import android.content.Context;
import android.databinding.ObservableArrayList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by yangbo on 2018/1/19.
 */

public class NewReaderView extends View {

    ObservableArrayList<String> text;
    private Paint paint = new Paint();
    Float txtFontSize = 50F;
    Float indacitorFontSize = 30F;
    private boolean isFirstDraw = true;
    private FirstDrawListener listener;

    public NewReaderView(Context context) {
        super(context);
    }

    public NewReaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NewReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NewReaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
        paint.setColor(Color.BLACK);
        paint.setTextSize(40F);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isFirstDraw){
            isFirstDraw = false;
            if(listener != null){
                listener.doDrawPrepare();
            }
            listener = null;
        }
        super.onDraw(canvas);
        paint.setTextSize(indacitorFontSize);
        canvas.drawText(text.get(0), getWidth() - indacitorFontSize * text.get(0).length(), getHeight() - indacitorFontSize, paint);
        paint.setTextSize(txtFontSize);
        for(int i = 1; i < text.size(); i++){
            canvas.drawText(text.get(i).replace(" ", "    "), getWidth() * 0.04F, getHeight() * 0.04F + ( i - 1 ) * txtFontSize, paint);
        }

    }

    public ObservableArrayList<String> getText() {
        return text;
    }

    public void setText(ObservableArrayList<String> text) {
        this.text = text;
        invalidate();
    }

    public void setFirstDrawListener(FirstDrawListener listener){
        if(isFirstDraw = true){
            this.listener = listener;
        }
    }

    interface FirstDrawListener{
        void doDrawPrepare();
    }

}
