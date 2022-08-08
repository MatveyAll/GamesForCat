
package com.example.gamesforcat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;


import androidx.appcompat.app.AppCompatActivity;

public class MouseGameActivity extends AppCompatActivity {

    DrawView drawView;
    int viewWidth,viewHeight;

    public class DrawView extends SurfaceView implements SurfaceHolder.Callback {


        public DrawView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            drawThread.setTowardPoint((int)event.getX(),(int)event.getY());
            return false;
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawThread = new DrawThread(getContext(),getHolder());
            drawThread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            drawThread.requestStop();
            boolean retry = true;
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    //
                }
            }
        }
        private DrawThread drawThread;

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh); // нахождение размеров экрана
            viewHeight = h;
            viewWidth = w;
        }
    }

    Timer timer = new Timer();
    Priority pr = new Priority();

    public class TimerThread extends Thread{
        @Override
        public void run() {
            while (true) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timer.milliSeconds += 1;
                        if (timer.milliSeconds == 1000) {
                            timer.milliSeconds = 0;
                            timer.seconds += 1;
                        }
                    }
                });
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class DrawThread extends Thread {

        private final SurfaceHolder surfaceHolder;

        private volatile boolean running = true;

        private final Paint backgroundPaint = new Paint();
        private final Paint textPaint = new Paint();
        private final Bitmap bitmap;
        private int touchPointX;
        private int touchPointY;

        public void setTowardPoint(int x, int y) {
            touchPointX = x;
            touchPointY = y;
        }

        {
            textPaint.setColor(Color.parseColor("#ff5c6e"));
            textPaint.setTextSize(50);
            backgroundPaint.setColor(Color.parseColor("#ff9aa5"));
            backgroundPaint.setStyle(Paint.Style.FILL);
        }

        public DrawThread(Context context, SurfaceHolder surfaceHolder) {
            bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.mouse_foreground);
            this.surfaceHolder = surfaceHolder;
        }

        public void requestStop() {
            running = false;
        }


        @Override
        public void run() {

            boolean test = true;
            int size = viewWidth; // размер мыши на экране
            int verticalBias = viewWidth / 2; // вертикальное смещение изображения
            TimerThread timerThread2 = new TimerThread();
            timerThread2.start();
            while (running) {

                Canvas canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {

                    try {
                        int randomNumber = (int) (Math.random() * 2000); // рандомное число для паузы таймера

                        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
                        canvas.drawText("Текущее время/рекорд: "+ timer.seconds + ":" + (timer.milliSeconds / 10) + "/"+ timer.bestSec + ":" + (timer.bestMilli / 10),0,40,textPaint);

                        if (pr.nextTest % 2 == 0) test = true; // условия для нужно едля появлении картинки после 2 касаний

                        if (test){ //проверка на то что касание было произведено
                            if (pr.justStarted >= 1 && pr.whileOnScreen != 1) {
                                try {
                                    Thread.sleep(randomNumber);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                            canvas.drawBitmap(bitmap, null, new Rect(viewWidth / 2 - size, viewHeight / 2 - size + verticalBias,
                                    viewWidth / 2 + size, viewHeight / 2 + size + verticalBias), backgroundPaint);
                            pr.whileOnScreen = 1;
                        }
                        if (!test)  pr.whileOnScreen = 0;
                        if (test && touchPointX == 1 && touchPointY == 1) {
                            touchPointX = 0; // условия для того чтобы не производились постоянные автоклики
                            touchPointY = 0;
                        }
                        if (touchPointX != 0 && touchPointY != 0) { //проверка на то что касание было произведено
                            timer.ifClicked += 1;
                            test = false;
                            pr.nextTest += 1;
                            pr.justStarted += 1;
                            touchPointX = 0;
                            touchPointY = 0;

                            if (timer.seconds < timer.bestSec) {
                                timer.bestSec = timer.seconds;
                                timer.bestMilli = timer.milliSeconds;
                            }

                            if (timer.milliSeconds <= timer.bestMilli) timer.bestMilli = timer.milliSeconds;

                            if (!timer.set) {
                                timer.bestSec = timer.seconds;
                                timer.bestMilli = timer.milliSeconds;
                                timer.set = true;
                            }

                            if (timer.ifClicked % 2 == 0){
                                timer.milliSeconds = 0;
                                timer.seconds = 0;
                            }
                        }

                        if (touchPointX == 0 && touchPointY == 0 && !test && pr.whileOnScreen != 1) {
                            touchPointX = 1;
                            touchPointY = 1;
                        }

                    } finally {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        drawView = new DrawView(this);
        setContentView(drawView);
        TimerThread timerThread = new TimerThread();
        timerThread.start();
    }

    // лучшее время нигде не сохраняется, потому что код Матвея не приспособлен для его сохранения
    // я пробовал, но это вызывает баги
    // поэтому пусть будет так, будто так и задумано было =)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        drawView.drawThread.running = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        drawView.drawThread.running = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        drawView.drawThread.running = false;
    }

    static class Priority {
        int nextTest = 0;
        int justStarted = 0;
        int whileOnScreen = 0;
    }

    static class Timer {
        boolean set = false;
        int bestMilli = 0;
        int bestSec = 0;
        int ifClicked = 0;
        int milliSeconds = 0;
        int seconds = 0;
    }
}
