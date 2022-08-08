
package com.example.gamesforcat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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


public class FlyGameActivity extends AppCompatActivity {

    static final String BEST_SCORE_KEY = "bestScore";

    Field field; // игровое поле

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        field = new Field(this);
        setContentView(field);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        field.isRunning = false;
        saveData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        field.isRunning = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        field.isRunning = false;
    }

    public void saveData() {
        MainActivity.editor.putInt(BEST_SCORE_KEY, field.bestScore);

        MainActivity.editor.commit();
    }

    class Field extends SurfaceView implements SurfaceHolder.Callback {

        Game game;
        Paint paint = new Paint();
        Canvas canvas;
        Fly fly;

        int screenWidth, screenHeight;
        float xTap = -100, yTap = -100; // координаты нажатия; -100 отому что муха может заползать за края
        boolean isRunning;
        int score, bestScore; // пойманные мухи

        public Field(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @Override
        public void surfaceCreated(@NonNull SurfaceHolder holder) {
            // всё это относится к тексту
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#ff5c6e"));
            paint.setTextSize(50);

            int size = 350;
            fly = new Fly((float) (Math.random() * (screenWidth - size)), (float) (Math.random() * (screenHeight - size)), size);
            bestScore = MainActivity.preferences.getInt(BEST_SCORE_KEY, 0);

            isRunning = true;
            game = new Game();
            game.start();
        }

        @Override
        public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            isRunning = false;
            saveData();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            screenWidth = w;
            screenHeight = h;
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            xTap = event.getX();
            yTap = event.getY();
            return super.onTouchEvent(event);
        }

        class Game extends Thread {

            @Override
            public void run() {
                while (isRunning) {
                    canvas = getHolder().lockCanvas();
                    try {
                        drawObjects();

                        fly.tryToChangeDirectionAndVelocities(screenWidth, screenHeight); // я знаю, что лучше было бы разбить этот метод на несколько, вроде таких:
                        // changeDirection(), needToChangeDirection(), changeVelocities(), needToChangeVelocities()
                        // я сначала так и сделал, но потом столкнулся с проблемами
                        // исправить их у меня получилось, когда я объединил все эти методы
                        fly.move();

                        if ((xTap > fly.x && xTap < fly.x + fly.size) // счиатем пойманных мух
                                && (yTap > fly.y && yTap < fly.y + fly.size)) {
                            score++;
                            if (score > bestScore) bestScore = score;

                            fly.velocityX += 30; // типо муха испаугалась и побежала быстрее
                            fly.velocityY += 30;

                            xTap = -100;
                            yTap = -100;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        canvas.drawText(e.toString(), 0, (float) screenHeight / 2, paint);
                        isRunning = false;
                    } finally {
                        getHolder().unlockCanvasAndPost(canvas);
                    }
                }
            }

            private void drawObjects() {
                canvas.drawColor(Color.parseColor("#ff9aa5")); // использую цвет, а не картинку, потому что с картинкой сильно подвисает

                canvas.drawText("Поймано мух/рекорд: " + score + "/" + bestScore, 0, 40, paint);

                canvas.drawBitmap(fly.image, fly.frames[fly.direction][fly.currentFrame], fly.body, paint);
            }
        }

        class Fly {

            Rect body = new Rect();
            Bitmap image;
            Rect[][] frames = new Rect[8][2];

            float x, y; // координаты верхнего левого угла мухи
            int size;
            float velocityX, velocityY;
            int direction; // направление движения мухи
            int timeCount; // нужен для смены кадров анимации мухи
            int currentFrame; // текущий кадр анимации

            public Fly(float x, float y, int size) {
                this.x = x;
                this.y = y;
                this.size = size;

                velocityX = (float) (Math.random() * 10) + 7;
                velocityY = (float) (Math.random() * 10) + 7;
                image = BitmapFactory.decodeResource(getResources(), R.drawable.flies);
                body.set((int) x, (int) y, (int) x + size, (int) y + size);
                for (int i = 0; i < frames.length; i++) { // разделение изображения на кадры
                    for (int j = 0; j < frames[i].length; j++) {
                        frames[i][j] = new Rect((image.getWidth() / 2) * j, (image.getHeight() / 8) * i,
                                (image.getWidth() / 2) * (j + 1), (image.getHeight() / 8) * (i + 1));
                    }
                }
                direction = (int) (Math.random() * 7);
            }

            public void move() {
                timeCount++;
                if (timeCount % 3 == 0) currentFrame = timeCount % 2; // смена кадра анимации
                switch (direction) {
                    case 0:
                        y -= velocityY;
                        break;
                    case 1:
                        x += velocityX;
                        y -= velocityY;
                        break;
                    case 2:
                        x += velocityX;
                        break;
                    case 3:
                        x += velocityX;
                        y += velocityY;
                        break;
                    case 4:
                        y += velocityY;
                        break;
                    case 5:
                        x -= velocityX;
                        y += velocityY;
                        break;
                    case 6:
                        x -= velocityX;
                        break;
                    case 7:
                        x -= velocityX;
                        y -= velocityY;
                        break;
                }
                body.set((int) x, (int) y, (int) x + size, (int) y + size);
            }

            public void tryToChangeDirectionAndVelocities(int w, int h) {
                if (!((x < 0 || x + size > w) || (y < 0 || y + size > h))) return;

                // не пугайтесь! это всего лишь условия для случайной смены направления движения мухи
                if (x < 0 && y < 0) direction = (int) (Math.random() * 3) + 2;
                else if (x < 0 && y + size > h) direction = (int) (Math.random() * 3);
                else if (x + size > w && y < 0) direction = (int) (Math.random() * 3) + 4;
                else if (x + size > w && y + size > h) direction = (int) (Math.random() * 3) + 6;
                else if (x < 0) {
                    direction = (int) (Math.random() * 5);
                    x++; // муха без этого не может ползать вдоль границ экрана
                }
                else if (x + size > w) {
                    direction = (int) (Math.random() * 5) + 4;
                    x--;
                }
                else if (y < 0) {
                    direction = (int) (Math.random() * 5) + 2;
                    y++;
                }
                else if (y + size > h) {
                    direction = (int) (Math.random() * 5) + 6;
                    y--;
                }
                direction %= 8; // если значение направления будет больше или равно 8, оно уменьшится до диапазона 0-7
                velocityX = (float) (Math.random() * 10) + 7;
                velocityY = (float) (Math.random() * 10) + 7;
            }
        }
    }
}
