package com.idealist.stocks;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameActivity extends FragmentActivity {

    FrameLayout mSellYellow;
    FrameLayout mBuyYellow;
    Button mSellButton;
    Button mBuyButton;
    Button mPlayButton;
    StockPriceView mStockPriceView;
    TextView mSharesText;
    TextView mMoneyText;
    TextView mCurPriceText;
    private double mMoney = 100;
    private long mShares = 0;
    CardFrontFragment mFrontFragment;
    CardBackFragment mBackFragment;
    Typeface tf1;
    Typeface tf2;
    private boolean mShowingBack = false;
    ArrayList<PointF> mBuyPoints = new ArrayList<PointF>();
    ArrayList<PointF> mSellPoints = new ArrayList<PointF>();
    Animation mShakeAnimation;
    Animation mShakeAnimation2;
    Animation mShakeAnimation3;
    Animation mShakeAnimation4;
    Animation mShakeAnimation5;

    SServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        SharedPreferences sp = getSharedPreferences("vars", getApplicationContext().MODE_PRIVATE);
        mMoney = sp.getFloat("money", 100f);
        if (sp.contains("shares")) {
            SharedPreferences.Editor editor = sp.edit();
            mShares = sp.getInt("shares", 0);
            if (mShares < 0) {
                mShares = 1000000;
            }
            editor.remove("shares");
            editor.commit();
        } else {
            mShares = sp.getLong("shares-long", 0);
        }

        tf1 = Typeface.createFromAsset(this.getAssets(),"fonts/paraaminobenzoic.ttf");
        tf2 = Typeface.createFromAsset(this.getAssets(),"fonts/digital7.ttf");

        server = new SServer(this, "http://192.168.101.216:8080");

        server.connect(new SServer.ServerCallBack() {
            @Override
            public void onConnect() {

            }

            @Override
            public void onError(String text) {

            }

            @Override
            public void onDisconnect() {

            }
        }, "Anton");



        mSellYellow = (FrameLayout) findViewById(R.id.sell_green);
        mBuyYellow = (FrameLayout) findViewById(R.id.buy_red);
        mSellButton = (Button) findViewById(R.id.sell_button);
        mPlayButton = (Button) findViewById(R.id.play_button);
        mBuyButton = (Button) findViewById(R.id.buy_button);
        mSharesText = (TextView) findViewById(R.id.shares_text);
        mMoneyText = (TextView) findViewById(R.id.money_text);
        mCurPriceText = (TextView) findViewById(R.id.cur_price_text);
        mSellButton.setTypeface(tf1);
        mBuyButton.setTypeface(tf1);
        mPlayButton.setTypeface(tf1);
        ((Button)findViewById(R.id.play_button2)).setTypeface(tf1);
        mSharesText.setTypeface(tf2);
        mMoneyText.setTypeface(tf2);
        mCurPriceText.setTypeface(tf2);

        mShakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        mShakeAnimation2 = AnimationUtils.loadAnimation(this, R.anim.shake2);
        mShakeAnimation3 = AnimationUtils.loadAnimation(this, R.anim.shake3);
        mShakeAnimation4 = AnimationUtils.loadAnimation(this, R.anim.shake4);
        mShakeAnimation5 = AnimationUtils.loadAnimation(this, R.anim.shake5);

        mBuyButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                buyClicked(null);
                server.sendData("buy","");
                return true;
            }
        });

        mSellButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                sellClicked(null);
                server.sendData("sell","");
                return true;
            }
        });

        updateSharesText();
        updateMoneyText();

        mBackFragment = new CardBackFragment();
        mFrontFragment = new CardFrontFragment();

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.stock_container, mFrontFragment)
                    .commit();
        }
    }

    public void setStockPriceView(StockPriceView stockPriceView) {
        mStockPriceView = stockPriceView;
        mStockPriceView.setGameActivity(this);
    }


    private void flipCard() {
        if (mShowingBack) {
            mShowingBack = false;
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .replace(R.id.stock_container, mFrontFragment)
                    .commit();
        } else {
            mShowingBack = true;
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_right_in, R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in, R.animator.card_flip_left_out)
                    .replace(R.id.stock_container, mBackFragment)
                    .commit();
        }
    }

    /**
     * A fragment representing the front of the card.
     */
    public static class CardFrontFragment extends Fragment {

        TextView mBestScore;
        TextView mCurrentScore;
        TextView mBestScoreText;
        TextView mCurrentScoreText;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_front, container, false);
            return v;
        }

        public void hideAll() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBestScore.setVisibility(View.GONE);
                    mCurrentScore.setVisibility(View.GONE);
                    mBestScoreText.setVisibility(View.GONE);
                    mCurrentScoreText.setVisibility(View.GONE);
                }
            }, getResources().getInteger(R.integer.card_flip_time_half));

        }

        @Override
        public void onResume() {
            super.onResume();
            mBestScore = (TextView) getView().findViewById(R.id.text_bestscore);
            mCurrentScore = (TextView) getView().findViewById(R.id.text_currentscore);
            mBestScoreText = (TextView) getView().findViewById(R.id.text_bestscore_text);
            mCurrentScoreText = (TextView) getView().findViewById(R.id.text_currentscore_text);
            SharedPreferences sp = getActivity().getSharedPreferences("vars", MODE_PRIVATE);
            float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));
            float currentscore = sp.getFloat("money", 100f);
            String bestscoreStr = "$" + String.format("%.2f", bestscore);
            String currentscoreStr = "$" + String.format("%.2f", currentscore);
            mBestScore.setText(bestscoreStr);
            mCurrentScore.setText(currentscoreStr);

            mBestScore.setTypeface(((GameActivity)getActivity()).tf2);
            mCurrentScore.setTypeface(((GameActivity)getActivity()).tf2);
            mBestScoreText.setTypeface(((GameActivity)getActivity()).tf1);
            mCurrentScoreText.setTypeface(((GameActivity)getActivity()).tf1);

        }
    }

    /**
     * A fragment representing the back of the card.
     */
    public static class CardBackFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_card_back, container, false);
            GameActivity ga = (GameActivity) getActivity();
            StockPriceView spv = (StockPriceView) v.findViewById(R.id.stock_price);
            ga.setStockPriceView(spv);
            return v;
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sp = getSharedPreferences("vars", getApplicationContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        if (mShowingBack) {
            saveCurPrice();
//            playSound(R.raw.start);
            mStockPriceView.clearCanvas();
            mStockPriceView.setRunning(false);

            float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));
            editor.putFloat("money", (float)mMoney);
            editor.apply();

//            checkAchievements();
            if (mMoney > bestscore) {
                editor.putString("bestscore", mMoney+"");
                editor.commit();
            }

            mBuyPoints.clear();
            mSellPoints.clear();
            findViewById(R.id.play_frame).setVisibility(View.VISIBLE);
            findViewById(R.id.buysell_frame).setVisibility(View.GONE);

            TranslateAnimation aRight = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0);
            aRight.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
            aRight.setInterpolator(new DecelerateInterpolator());
            aRight.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    mPlayButton.setEnabled(true);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            Button playButton2 = (Button) findViewById(R.id.play_button2);
            playButton2.startAnimation(aRight);


            TranslateAnimation aLeft = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
            aLeft.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
            aLeft.setInterpolator(new DecelerateInterpolator());
            mPlayButton.startAnimation(aLeft);
            mCurPriceText.setVisibility(View.INVISIBLE);
            flipCard();
        } else {
            super.onBackPressed();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if(server.isConnected())
            try {
                server.disconnect();
            } catch (URISyntaxException e) {

            }
    }

    @Override
    public void onPause() {
        saveCurPrice();
        SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat("money", (float)mMoney);
        editor.putLong("shares-long", mShares);
        editor.commit();

        super.onPause();
    }

    public void saveCurPrice() {
        SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if (mStockPriceView != null) {
            editor.putFloat("curprice", mStockPriceView.getCurStockPrice());
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateMoneyText() {
        String moneyStr;
        if (mMoney > 1000000000) {
            moneyStr = "$" + String.format("%6.3e", mMoney);
        } else {
            moneyStr = "$" + String.format("%.2f", mMoney);
        }
        mMoneyText.setText(moneyStr);
    }

    private void updateSharesText() {
        String sharesStr;
        if (mShares > 1000000) {
            sharesStr = String.format("%6.3e", mShares) + " Shares";
        } else {
            sharesStr = mShares + " " + (mShares == 1 ? "Share" : "Shares");
        }
        mSharesText.setText(sharesStr);
    }

    public void buyClicked(View view) {
        ScaleAnimation aClick = new ScaleAnimation(
                1, 1.1f, 1, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aClick.setDuration(100);
        mBuyButton.startAnimation(aClick);
        float curPrice = mStockPriceView.getCurStockPriceActual();
        double moneySpent = curPrice;
        if (view == null) moneySpent = curPrice * ((long)(mMoney / curPrice));
        if (moneySpent > mMoney || moneySpent == 0) {
            mMoneyText.setTextColor(getResources().getColor(R.color.buydot));
//            playSound(R.raw.nobuy);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMoneyText.setTextColor(getResources().getColor(R.color.sellgreen));
                }
            }, 500);
            return;
        }
//        if (view == null) playSound(R.raw.buyall); else playSound(R.raw.coin);
        mBuyPoints.add(new PointF(mStockPriceView.mCurTime, mStockPriceView.getCurStockPrice()));
        if (mBuyPoints.size() > 100) {
            for (int i = 0; i < 50; i++) {
                mBuyPoints.remove(0);
            }
        }

        long newShares = 1;
        if (view == null) newShares = (long) (mMoney / mStockPriceView.getCurStockPriceActual());

        mMoney -= moneySpent;
        mShares += newShares;
        updateMoneyText();
        updateSharesText();

        setBuyFill();
        setSellFill();
    }

    public void sellClicked(View view) {
        ScaleAnimation aClick = new ScaleAnimation(
                1, 1.1f, 1, 1,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        aClick.setDuration(100);
        mSellButton.startAnimation(aClick);
        float moneyGained = (int) mStockPriceView.getCurStockPriceActual();
        if (view == null) moneyGained = (mShares * mStockPriceView.getCurStockPriceActual());
        if (mShares <= 0) {
            mSharesText.setTextColor(getResources().getColor(R.color.buydot));
//            playSound(R.raw.nobuy);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSharesText.setTextColor(getResources().getColor(R.color.buyob));
                }
            }, 500);
            return;
        }
//        if (view == null) playSound(R.raw.sellall); else playSound(R.raw.coin2);
        mSellPoints.add(new PointF(mStockPriceView.mCurTime, mStockPriceView.getCurStockPrice()));
        if (mSellPoints.size() > 100) {
            for (int i = 0; i < 50; i++) {
                mSellPoints.remove(0);
            }
        }

        mMoney += moneyGained;
        if (view == null) mShares = 0; else mShares--;
        updateMoneyText();
        updateSharesText();

        setBuyFill();
        setSellFill();

        SharedPreferences sp = getSharedPreferences("vars", MODE_PRIVATE);
        float bestscore = Float.parseFloat(sp.getString("bestscore", "0.0"));
        if (mMoney > bestscore) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("bestscore", mMoney+"");
            editor.commit();
        }

    }

    public void playClicked(final View v) {
//        playSound(R.raw.back);
        mFrontFragment.hideAll();
        v.setEnabled(false);
        TranslateAnimation aRight = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        aRight.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
        aRight.setInterpolator(new DecelerateInterpolator());
        aRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                findViewById(R.id.play_frame).setVisibility(View.GONE);
                findViewById(R.id.buysell_frame).setVisibility(View.VISIBLE);
                AnimationSet animationSet = new AnimationSet(true);
                TranslateAnimation aDown = new TranslateAnimation(
                        TranslateAnimation.RELATIVE_TO_SELF, 0, TranslateAnimation.RELATIVE_TO_SELF, 0,
                        TranslateAnimation.RELATIVE_TO_SELF, -1, TranslateAnimation.RELATIVE_TO_SELF, 0);
                aDown.setDuration(1000);
                AlphaAnimation fadeIn = new AlphaAnimation(0, 1);
                fadeIn.setDuration(500);

                animationSet.addAnimation(aDown);
                animationSet.addAnimation(fadeIn);

                mMoneyText.startAnimation(animationSet);
                mSharesText.startAnimation(animationSet);
                v.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        mPlayButton.startAnimation(aRight);
        Button playButton2 = (Button) findViewById(R.id.play_button2);
        TranslateAnimation aLeft = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1, Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 1);
        aLeft.setDuration(getResources().getInteger(R.integer.card_flip_time_full));
        aLeft.setInterpolator(new DecelerateInterpolator());
        playButton2.startAnimation(aLeft);
        mCurPriceText.setVisibility(View.VISIBLE);
        flipCard();


        final RecyclerView xatRecycle = findViewById(R.id.activity_chat_recyclerview);
        final RecAdapter recAdapter = new RecAdapter(this);

        xatRecycle.setAdapter(recAdapter);
        xatRecycle.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));


        server.getTopPlayers(new SServer.OnUsersReadyListener() {
            @Override
            public void onUsersReady(List<User> users) {
                recAdapter.addContent(users);
                xatRecycle.scrollToPosition(recAdapter.getItemCount() - 1);
            }
        });
    }

    boolean ranOnce = false;
    public void onCurStockPriceUpdated(final float curStockPrice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!ranOnce) {
                    setSellFill();
                    setBuyFill();
                    ranOnce = true;
                }
                String curStr = "$" + String.format("%.2f", mStockPriceView.getCurStockPriceActual());
                mCurPriceText.setText("Price: " + curStr);
            }
        });
    }

    public void setSellFill() {
        ViewGroup.LayoutParams sLayout = mSellYellow.getLayoutParams();
        float oldHeight = mSellYellow.getHeight();
        float newHeight = ((mShares+0.0f)/10.0f) * mSellButton.getHeight();
        sLayout.height = (int) newHeight;
        if (sLayout.height < 0) {
            sLayout.height = 0;
        }
        if (sLayout.height > mSellButton.getHeight()) {
            sLayout.height = mSellButton.getHeight();
        }
        mSellYellow.setLayoutParams(sLayout);

        if (!(oldHeight == mSellButton.getHeight() && newHeight >= oldHeight)) {
            ScaleAnimation a = new ScaleAnimation(1f, 1f, (oldHeight / newHeight), 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1f);
            a.setDuration(500);
            a.setInterpolator(new BounceInterpolator());
            mSellYellow.startAnimation(a);
        }

        if (mShares > 0) {
            mSellButton.setTextColor(getResources().getColor(R.color.white2));
            mSellButton.setTypeface(mSellButton.getTypeface(), Typeface.BOLD);
            if (mShares >= 20) {
                mSharesText.setTextColor(getResources().getColor(R.color.sellgreen2));
                mSharesText.setTypeface(mSharesText.getTypeface(), Typeface.BOLD);
            } else {
                mSharesText.setTextColor(getResources().getColor(R.color.sellgreen));
                mSharesText.setTypeface(mSharesText.getTypeface(), Typeface.NORMAL);
            }
        } else {
            mSellButton.setTextColor(getResources().getColor(R.color.buysell));
            mSellButton.setTypeface(mSellButton.getTypeface(), Typeface.NORMAL);
            mSharesText.setTextColor(getResources().getColor(R.color.sellgreen));
            mSharesText.setTypeface(mSharesText.getTypeface(), Typeface.NORMAL);
        }
    }

    public void setBuyFill() {
        ViewGroup.LayoutParams bLayout = mBuyYellow.getLayoutParams();
        float oldHeight = mBuyYellow.getHeight();
        float newHeight = (((float) mMoney + 0.0f) / 100.0f) * mBuyButton.getHeight();
        bLayout.height = (int) newHeight;
        if (bLayout.height > mBuyButton.getHeight()) {
            bLayout.height = mBuyButton.getHeight();
        } else if (bLayout.height < 0) {
            bLayout.height = 0;
        }
        mBuyYellow.setLayoutParams(bLayout);

        if (!(oldHeight == mBuyButton.getHeight() && newHeight >= oldHeight)) {
            ScaleAnimation a = new ScaleAnimation(1f, 1f, (oldHeight / newHeight), 1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 1f);
            a.setDuration(500);
            a.setInterpolator(new BounceInterpolator());
            mBuyYellow.startAnimation(a);
        }

        if (mMoney > 100) {
            mBuyButton.setTextColor(getResources().getColor(R.color.white2));
            mBuyButton.setTypeface(mBuyButton.getTypeface(), Typeface.BOLD);
            if (mMoney >= 200) {
                mMoneyText.setTextColor(getResources().getColor(R.color.buybet));
                mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.BOLD);

//                if (mMoney >= 500) {
//                    if (mMoney >= 1000) {
//                        if (mMoney >= 1000000) {
//                            if (mMoney >= 1000000000) {
//                                if (mMoney >= 9223372036854d) {
//                                    if (mStockPriceView.getAnimation() == null || !mStockPriceView.getAnimation().equals(mShakeAnimation5)) mStockPriceView.startAnimation(mShakeAnimation5);
//                                } else {
//                                    if (mStockPriceView.getAnimation() == null || !mStockPriceView.getAnimation().equals(mShakeAnimation4)) mStockPriceView.startAnimation(mShakeAnimation4);
//                                }
//                            } else {
//                                if (mStockPriceView.getAnimation() == null || !mStockPriceView.getAnimation().equals(mShakeAnimation3)) mStockPriceView.startAnimation(mShakeAnimation3);
//                            }
//                        } else {
//                            if (mStockPriceView.getAnimation() == null || !mStockPriceView.getAnimation().equals(mShakeAnimation2)) mStockPriceView.startAnimation(mShakeAnimation2);
//                        }
//                    } else {
//                        if (mStockPriceView.getAnimation() == null || !mStockPriceView.getAnimation().equals(mShakeAnimation)) mStockPriceView.startAnimation(mShakeAnimation);
//                    }
//                } else {
//                    mStockPriceView.clearAnimation();
//                }
            } else {
                mMoneyText.setTextColor(getResources().getColor(R.color.buyob));
                mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.NORMAL);
//                mStockPriceView.clearAnimation();
            }
        } else {
            mBuyButton.setTextColor(getResources().getColor(R.color.buysell));
            mBuyButton.setTypeface(mBuyButton.getTypeface(), Typeface.NORMAL);
            mMoneyText.setTextColor(getResources().getColor(R.color.buyob));
            mMoneyText.setTypeface(mMoneyText.getTypeface(), Typeface.NORMAL);
//            mStockPriceView.clearAnimation();
        }
    }

    HashMap<Integer, MediaPlayer> mediaMap = new HashMap<Integer, MediaPlayer>();
    public void playSound(int soundId){
        if (mediaMap.containsKey(soundId)) {
            mediaMap.get(soundId).start();
        } else {
            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), soundId);
            mp.start();
            mediaMap.put(soundId, mp);
        }
    }
}
