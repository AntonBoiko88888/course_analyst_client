package com.idealist.stocks;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.SurfaceHolder;

import java.util.ArrayList;

class GameThread extends Thread {
    private SurfaceHolder mSurfaceHolder;
    private StockPriceView mPanel;
    private boolean mRun = false;
    private ArrayList<PointF> mPoints = new ArrayList<PointF>();
    private boolean mFirst = true;
    private StockMarketEmulator mMarket;
    Canvas c;

    public static double cwalue = 0;

    SServer server;

    public GameThread(SurfaceHolder surfaceHolder, StockPriceView panel, StockMarketEmulator market, SServer server) {
        mSurfaceHolder = surfaceHolder;
        mPanel = panel;
        mMarket = market;


        server.addListener("message", new SServer.ListenerCallBack() {
            @Override
            public void call(String res) {
                cwalue = Double.parseDouble(res);
            }
        });

    }

    public void setRunning(boolean run) { //Allow us to stop the thread
        mRun = run;
    }

//    @Override
//    public void run() {
//        while (mRun) {     //When setRunning(false) occurs, _run is
//            c = null;      //set to false and loop ends, stopping thread
//            try {
//                c = mSurfaceHolder.lockCanvas(null);
//                synchronized (mSurfaceHolder) {
//                    mPanel.mCurTime+=10;
//                    mPanel.setCurStockPrice(mMarket.next());
//                    mPoints.add(new PointF(mPanel.mCurTime, mPanel.getCurStockPrice()));
//                    if (mPoints.size() > 170) {
//                        for (int i = 0; i < 50; i++) mPoints.remove(0);
//                        generatePath();
//                    }
//                    if (mFirst) mPanel.mPriceLine.moveTo(mPanel.mCurTime, mPanel.getCurStockPrice());
//                    else mPanel.mPriceLine.lineTo(mPanel.mCurTime, mPanel.getCurStockPrice());
//                    mFirst = false;
//                    mPanel.postInvalidate();
//                    Thread.sleep(100);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } finally {
//                if (c != null) {
//                    try {
//                        mSurfaceHolder.unlockCanvasAndPost(c);
//                    } catch(Exception e) {}
//                }
//            }
//        }
//    }


    @Override
    public void run() {
            c = null;      //set to false and loop ends, stopping thread
        while (mRun) {
            try {
                c = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    mPanel.mCurTime += 10;
                    mPanel.setCurStockPrice((float) cwalue);
                    mPoints.add(new PointF(mPanel.mCurTime, mPanel.getCurStockPrice()));
                    if (mPoints.size() > 150) {
                        for (int i = 0; i < 50; i++) mPoints.remove(0);
                        generatePath();
                    }
                    if (mFirst)
                        mPanel.mPriceLine.moveTo(mPanel.mCurTime, mPanel.getCurStockPrice());
                    else mPanel.mPriceLine.lineTo(mPanel.mCurTime, mPanel.getCurStockPrice());
                    mFirst = false;
                    mPanel.postInvalidate();
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (c != null) {
                    try {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    } catch (Exception e) {
                    }
                }
            }
        }

    }


    private void generatePath() {
        mPanel.mPriceLine = new Path();
        boolean first = true;
        for (PointF p : mPoints) {
            if (first) {
                mPanel.mPriceLine.moveTo(p.x, p.y);
            } else {
                mPanel.mPriceLine.lineTo(p.x, p.y);
            }
            first = false;
        }
    }
}
