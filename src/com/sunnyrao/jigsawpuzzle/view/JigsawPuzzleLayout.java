package com.sunnyrao.jigsawpuzzle.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.sunnyrao.jigsawpuzzle.R;
import com.sunnyrao.jigsawpuzzle.utils.ImagePiece;
import com.sunnyrao.jigsawpuzzle.utils.ImageSplitterUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class JigsawPuzzleLayout extends RelativeLayout implements
		View.OnClickListener {

	private static int[] imgs = { R.drawable.img00, R.drawable.img01,
			R.drawable.img02, R.drawable.img03, R.drawable.img04 };

	private static int sum = imgs.length;

	private static final int MAX_COLUMN = 5;

	private int mColumn = 3;
	/**
	 * 容器的内边距
	 */
	private int mPadding;
	/**
	 * 每张小图之间的距离（横，纵） dp
	 */
	private int mMargin = 3;

	private ImageView[] mJigsawPuzzleItems;

	private int mItemWidth;
	/**
	 * 游戏的图片
	 */
	private Bitmap mBitmap;

	private List<ImagePiece> mItemBitmaps;

	private boolean once;
	/**
	 * 游戏面板的宽度
	 */
	private int mWidth;

	private boolean isGameSuccess;
	private boolean isGameOver;

	public interface GameListener {

		void nextLevel(int nextLevel);

		void timeChanged(int currentTime);

		void gameOver();
	}

	public GameListener mListener;

	/**
	 * 设置接口回调
	 */
	public void setOnGameListener(GameListener listener) {
		this.mListener = listener;
	}

	private int mLevel = 1;
	private static final int TIME_CHANGED = 0x001;
	private static final int NEXT_LEVEL = 0x010;

	/**
	 * 获得当前等级数
	 */
	public int getLevel() {
		return mLevel;
	}

	/**
	 * 判断游戏是否结束
	 */
	public boolean isGameSuccess() {
		return isGameSuccess;
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TIME_CHANGED:
				if (isGameSuccess || isGameOver || isPause)
					return;
				if (mListener != null) {
					mListener.timeChanged(mTime);
				}
				if (mTime == 0) {
					isGameOver = true;
					if (mListener != null)
						mListener.gameOver();
					return;
				}
				mTime--;
				mHandler.sendEmptyMessageDelayed(TIME_CHANGED, 1000);
				break;
			case NEXT_LEVEL:
				mLevel++;
				if (mListener != null) {
					mListener.nextLevel(mLevel);
				} else {
					nextLevel();
				}
				break;
			}
		}

	};

	/**
	 * 是否启动时间限制
	 */
	private boolean isTimeEnabled = false;
	private int mTime;

	/**
	 * 设置是否开启时间
	 */
	public void setTimeEnabled(boolean isTimeEnabled) {
		this.isTimeEnabled = isTimeEnabled;
	}

	public JigsawPuzzleLayout(Context context) {
		this(context, null);
	}

	public JigsawPuzzleLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public JigsawPuzzleLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				3, getResources().getDisplayMetrics());
		mPadding = min(getPaddingLeft(), getPaddingRight(), getPaddingTop(),
				getPaddingBottom());
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 取宽和高中的小值
		mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
		if (!once) {
			// 进行切图，以及排序
			initBitmap();
			// 设置ImageView(Item)的宽高等属性
			initItem();
			// 判断是否开启时间
			checkTimeEnable();

			once = true;
		}
		setMeasuredDimension(mWidth, mWidth);
	}

	private void checkTimeEnable() {
		if (isTimeEnabled) {
			// 根据当前等级设置时间
			countTimeByColumn();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	/**
	 * 根据当前等级设置时间
	 */
	private void countTimeByColumn() {
		mTime = (int) Math.pow(2, mColumn - 2) * 60;
	}

	/**
	 * 进行切图，以及排序
	 */
	private void initBitmap() {
		int rand = (int) (Math.random() * sum);
		// 随机取一张图片
		mBitmap = BitmapFactory.decodeResource(getResources(), imgs[rand]);
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		// 使用sort完成我们的乱序
		Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {

			@Override
			public int compare(ImagePiece lhs, ImagePiece rhs) {
				return Math.random() > 0.5 ? 1 : -1;
			}
		});
	}

	/**
	 * 设置ImageView(Item)的宽高等属性
	 */
	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1))
				/ mColumn;
		mJigsawPuzzleItems = new ImageView[mColumn * mColumn];
		// 生成我们的item，设置rule
		for (int i = 0; i < mJigsawPuzzleItems.length; i++) {
			ImageView item = new ImageView(getContext());
			item.setOnClickListener(this);
			item.setImageBitmap(mItemBitmaps.get(i).getBitmap());

			mJigsawPuzzleItems[i] = item;
			item.setId(i + 1);
			// 在Item的tag中存储了index
			item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					mItemWidth, mItemWidth);
			// 不是第一列
			if (i % mColumn != 0) {
				lp.leftMargin = mMargin;
				lp.addRule(RelativeLayout.RIGHT_OF,
						mJigsawPuzzleItems[i - 1].getId());
			}
			// 如果不是第一行 , 设置topMargin和rule
			if ((i + 1) > mColumn) {
				lp.topMargin = mMargin;
				lp.addRule(RelativeLayout.BELOW,
						mJigsawPuzzleItems[i - mColumn].getId());
			}
			addView(item, lp);
		}
	}

	public void restart() {
		isGameOver = false;
		if (mLevel % sum == 1 && mColumn < MAX_COLUMN)
			mColumn--;
		nextLevel();
	}

	private boolean isPause;

	public void pause() {
		isPause = true;
		mHandler.removeMessages(TIME_CHANGED);
	}

	public void resume() {
		if (isPause) {
			isPause = false;
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	// // 调试用
	// public void cheat() {
	// mLevel++;
	// }

	/**
	 * 进入下一关
	 */
	public void nextLevel() {
		this.removeAllViews();
		mFirst = mSecond = null;
		mHandler.removeMessages(TIME_CHANGED);
		mAimLayout = null;
		if (mLevel % sum == 1 && mColumn < MAX_COLUMN)
			mColumn++;
		isGameSuccess = false;
		checkTimeEnable();
		initBitmap();
		initItem();
	}

	/**
	 * 获取多个参数的最小值
	 */
	private int min(int... params) {
		int min = params[0];
		for (int param : params) {
			if (param < min)
				min = param;
		}
		return min;
	}

	private ImageView mFirst;
	private ImageView mSecond;

	@Override
	public void onClick(View v) {
		if (isAniming)
			return;
		// 两次点击同一个Item
		if (mFirst == v) {
			mFirst.setColorFilter(null);
			mFirst = null;
			return;
		}
		if (mFirst == null) {
			mFirst = (ImageView) v;
			mFirst.setColorFilter(Color.parseColor("#8898C7D1"));
		} else {
			mSecond = (ImageView) v;
			// 交换我们的Item
			exchangeView();
		}
	}

	/**
	 * 动画层
	 */
	private RelativeLayout mAimLayout;
	private boolean isAniming;

	/**
	 * 交换我们的Item
	 */
	private void exchangeView() {
		mFirst.setColorFilter(null);
		// 构造我们的动画层
		setUpAnimLayout();

		final ImageView first = new ImageView(getContext());
		final Bitmap firstBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mFirst.getTag())).getBitmap();
		first.setImageBitmap(firstBitmap);
		LayoutParams lp = new LayoutParams(mItemWidth, mItemWidth);
		lp.leftMargin = mFirst.getLeft() - mPadding;
		lp.topMargin = mFirst.getTop() - mPadding;
		first.setLayoutParams(lp);
		mAimLayout.addView(first);

		final ImageView second = new ImageView(getContext());
		final Bitmap secondBitmap = mItemBitmaps.get(
				getImageIdByTag((String) mSecond.getTag())).getBitmap();
		second.setImageBitmap(secondBitmap);
		LayoutParams lp2 = new LayoutParams(mItemWidth, mItemWidth);
		lp2.leftMargin = mSecond.getLeft() - mPadding;
		lp2.topMargin = mSecond.getTop() - mPadding;
		second.setLayoutParams(lp2);
		mAimLayout.addView(second);

		// 设置动画
		TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft()
				- mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
		anim.setDuration(300);
		anim.setFillAfter(true);
		first.startAnimation(anim);

		TranslateAnimation animSecond = new TranslateAnimation(0,
				-mSecond.getLeft() + mFirst.getLeft(), 0, -mSecond.getTop()
						+ mFirst.getTop());
		animSecond.setDuration(300);
		animSecond.setFillAfter(true);
		second.startAnimation(animSecond);

		// 监听动画
		anim.setAnimationListener(new Animation.AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				mFirst.setVisibility(View.INVISIBLE);
				mSecond.setVisibility(View.INVISIBLE);
				isAniming = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				String firstTag = (String) mFirst.getTag();
				String secondTag = (String) mSecond.getTag();

				mFirst.setImageBitmap(secondBitmap);
				mSecond.setImageBitmap(firstBitmap);

				mFirst.setTag(secondTag);
				mSecond.setTag(firstTag);

				first.setVisibility(View.GONE);
				second.setVisibility(View.GONE);
				mFirst.setVisibility(View.VISIBLE);
				mSecond.setVisibility(View.VISIBLE);

				mFirst = mSecond = null;
				// 判断用户游戏是否成功
				checkSuccess();
				isAniming = false;
			}
		});
	}

	/**
	 * 判断用户游戏是否成功
	 */
	private void checkSuccess() {
		boolean isSuccess = true;
		for (int i = 0; i < mJigsawPuzzleItems.length; i++) {
			ImageView imageView = mJigsawPuzzleItems[i];
			if (getImageIndexByTag((String) imageView.getTag()) != i) {
				isSuccess = false;
				break;
			}
		}

		if (isSuccess) {
			isGameSuccess = true;
			mHandler.removeMessages(TIME_CHANGED);
			// Toast.makeText(getContext(), "You succeeded!",
			// Toast.LENGTH_SHORT)
			// .show();
			mHandler.sendEmptyMessage(NEXT_LEVEL);
		}
	}

	/**
	 * 根据tag获取Id
	 */
	public int getImageIdByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[0]);
	}

	/**
	 * 根据tag获取Index
	 */
	public int getImageIndexByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[1]);
	}

	/**
	 * 构造我们的动画层
	 */
	private void setUpAnimLayout() {
		if (mAimLayout == null) {
			mAimLayout = new RelativeLayout(getContext());
			addView(mAimLayout);
		} else {
			mAimLayout.removeAllViews();
		}
	}
}
