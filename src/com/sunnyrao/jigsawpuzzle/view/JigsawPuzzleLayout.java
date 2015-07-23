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
	 * �������ڱ߾�
	 */
	private int mPadding;
	/**
	 * ÿ��Сͼ֮��ľ��루�ᣬ�ݣ� dp
	 */
	private int mMargin = 3;

	private ImageView[] mJigsawPuzzleItems;

	private int mItemWidth;
	/**
	 * ��Ϸ��ͼƬ
	 */
	private Bitmap mBitmap;

	private List<ImagePiece> mItemBitmaps;

	private boolean once;
	/**
	 * ��Ϸ���Ŀ��
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
	 * ���ýӿڻص�
	 */
	public void setOnGameListener(GameListener listener) {
		this.mListener = listener;
	}

	private int mLevel = 1;
	private static final int TIME_CHANGED = 0x001;
	private static final int NEXT_LEVEL = 0x010;

	/**
	 * ��õ�ǰ�ȼ���
	 */
	public int getLevel() {
		return mLevel;
	}

	/**
	 * �ж���Ϸ�Ƿ����
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
	 * �Ƿ�����ʱ������
	 */
	private boolean isTimeEnabled = false;
	private int mTime;

	/**
	 * �����Ƿ���ʱ��
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
		// ȡ��͸��е�Сֵ
		mWidth = Math.min(getMeasuredHeight(), getMeasuredWidth());
		if (!once) {
			// ������ͼ���Լ�����
			initBitmap();
			// ����ImageView(Item)�Ŀ�ߵ�����
			initItem();
			// �ж��Ƿ���ʱ��
			checkTimeEnable();

			once = true;
		}
		setMeasuredDimension(mWidth, mWidth);
	}

	private void checkTimeEnable() {
		if (isTimeEnabled) {
			// ���ݵ�ǰ�ȼ�����ʱ��
			countTimeByColumn();
			mHandler.sendEmptyMessage(TIME_CHANGED);
		}
	}

	/**
	 * ���ݵ�ǰ�ȼ�����ʱ��
	 */
	private void countTimeByColumn() {
		mTime = (int) Math.pow(2, mColumn - 2) * 60;
	}

	/**
	 * ������ͼ���Լ�����
	 */
	private void initBitmap() {
		int rand = (int) (Math.random() * sum);
		// ���ȡһ��ͼƬ
		mBitmap = BitmapFactory.decodeResource(getResources(), imgs[rand]);
		mItemBitmaps = ImageSplitterUtil.splitImage(mBitmap, mColumn);
		// ʹ��sort������ǵ�����
		Collections.sort(mItemBitmaps, new Comparator<ImagePiece>() {

			@Override
			public int compare(ImagePiece lhs, ImagePiece rhs) {
				return Math.random() > 0.5 ? 1 : -1;
			}
		});
	}

	/**
	 * ����ImageView(Item)�Ŀ�ߵ�����
	 */
	private void initItem() {
		mItemWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1))
				/ mColumn;
		mJigsawPuzzleItems = new ImageView[mColumn * mColumn];
		// �������ǵ�item������rule
		for (int i = 0; i < mJigsawPuzzleItems.length; i++) {
			ImageView item = new ImageView(getContext());
			item.setOnClickListener(this);
			item.setImageBitmap(mItemBitmaps.get(i).getBitmap());

			mJigsawPuzzleItems[i] = item;
			item.setId(i + 1);
			// ��Item��tag�д洢��index
			item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					mItemWidth, mItemWidth);
			// ���ǵ�һ��
			if (i % mColumn != 0) {
				lp.leftMargin = mMargin;
				lp.addRule(RelativeLayout.RIGHT_OF,
						mJigsawPuzzleItems[i - 1].getId());
			}
			// ������ǵ�һ�� , ����topMargin��rule
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

	// // ������
	// public void cheat() {
	// mLevel++;
	// }

	/**
	 * ������һ��
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
	 * ��ȡ�����������Сֵ
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
		// ���ε��ͬһ��Item
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
			// �������ǵ�Item
			exchangeView();
		}
	}

	/**
	 * ������
	 */
	private RelativeLayout mAimLayout;
	private boolean isAniming;

	/**
	 * �������ǵ�Item
	 */
	private void exchangeView() {
		mFirst.setColorFilter(null);
		// �������ǵĶ�����
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

		// ���ö���
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

		// ��������
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
				// �ж��û���Ϸ�Ƿ�ɹ�
				checkSuccess();
				isAniming = false;
			}
		});
	}

	/**
	 * �ж��û���Ϸ�Ƿ�ɹ�
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
	 * ����tag��ȡId
	 */
	public int getImageIdByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[0]);
	}

	/**
	 * ����tag��ȡIndex
	 */
	public int getImageIndexByTag(String tag) {
		String[] split = tag.split("_");
		return Integer.parseInt(split[1]);
	}

	/**
	 * �������ǵĶ�����
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
