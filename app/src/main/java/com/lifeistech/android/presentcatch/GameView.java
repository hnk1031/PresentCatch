package com.lifeistech.android.presentcatch;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by hnk_1031 on 16/08/05.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    //Frame per secondという一秒間に何回画面を更新するかという値
    static final long FPS = 30;
    static final long FRAME_TIME = 1000 / FPS;

    SurfaceHolder surfaceHolder;
    Thread thread;

    Present present;
    Bitmap presentImage;

    //画面の横幅と高さを保存しておく変数
    int screenWidth, screenHeight;

    //プレイヤークラスと画像の変数を追加
    Player player;
    Bitmap playerImage;

    //スコア用の変数
    int score = 0;
    //ライフ用の変数
    int life = 10;

    public GameView(Context context) {
        super(context);
        //surfaceHolderの状態を取得するための処理
        getHolder().addCallback(this);

        //画像を読み込む
        Resources resources = context.getResources();
        presentImage = BitmapFactory.decodeResource(resources, R.drawable.img_present0);

        //プレイヤー画像の読み込みを追加
        playerImage = BitmapFactory.decodeResource(resources,R.drawable.img_player);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        thread = new Thread(this);
        thread.start();
    }

    class Present {
        private static final int WIDTH = 100;
        private static final int HEIGHT = 100;

        float x,y;

        public Present() {
            Random random =new Random();
            x = random.nextInt(screenWidth - WIDTH);
            y = 0;
        }

        public void update() {
            y +=15.0f;
        }

        public void reset() {
            Random random = new Random();
            x = random.nextInt(screenWidth - WIDTH);
            y = 0;
        }
    }

    class Player {
        final int WIDTH = 200;
        final int HEIGHT = 200;

        float x,y;

        public Player() {
            x = 0;
            y = screenHeight - HEIGHT;
        }

        //移動用のメソッド
        public void move(float diffX) {
            this.x += diffX;
            this.x = Math.max(0,x);
            this.x = Math.min(screenWidth - WIDTH, x);
        }

        //プレゼントとの当たり判定を行うメソッド
        public boolean isEnter(Present present) {
            if (present.x + Present.WIDTH > x && present.x < x + WIDTH &&
                    present.y + Present.HEIGHT > y && present.y < y +HEIGHT) {
                return true;
            }
            return false;
        }
        
    }

    @Override
    public void run() {
        //プレイヤーを作る
        player = new Player();

        //プレゼントクラスを一つ作る
        present = new Present();

        //スコアとライフ表示用のPaintクラス
        Paint textPaint = new Paint();
        //文字の色
        textPaint.setColor(Color.BLACK);
        //太文字かどうか
        textPaint.setFakeBoldText(true);
        //文字の大きさ
        textPaint.setTextSize(100);

        while (thread != null) {
            Canvas canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            //プレイヤーの表示処理を追加
            canvas.drawBitmap(playerImage,player.x,player.y,null);
            //プレゼントの画像を描く
            canvas.drawBitmap(presentImage,present.x,present.y, null);

            //当たり判定
            if (player.isEnter(present)) {
                //プレゼントをキャッチした時
                present.reset();
                //スコアを10アップ
                score += 10;
            } else if (present.y > screenHeight){
                //プレゼントをキャッチできなかった時
                present.reset();
                //ライフを1減らす
                life --;
            } else {
                present.update();
            }

            canvas.drawText("SCORE : " + score,50,150,textPaint);
            canvas.drawText("LIFE : " + life,50,300,textPaint);

            if (life <= 0) {
                canvas.drawText("Game Over",screenWidth /3 ,screenHeight /2 ,textPaint);
                surfaceHolder.unlockCanvasAndPost(canvas);
                break;
            }

            if (present.y > screenHeight) {
                //Y座標が画面の高さを超えたのでresetする
                present.reset();
            } else {
                //プレゼントの位置を更新する
                present.update();
            }

            //キャンバスの内容を反映させる
            surfaceHolder.unlockCanvasAndPost(canvas);

            try {
                Thread.sleep(FRAME_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread = null;
    }
}
