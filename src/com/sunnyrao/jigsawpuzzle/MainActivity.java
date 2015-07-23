package com.sunnyrao.jigsawpuzzle;

import com.sunnyrao.jigsawpuzzle.view.JigsawPuzzleLayout;
import com.sunnyrao.jigsawpuzzle.view.JigsawPuzzleLayout.GameListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private JigsawPuzzleLayout mGameLayout;
	private TextView mLevel;
	private TextView mTime;
	private Button mBtnNext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mTime = (TextView) findViewById(R.id.id_time);
		mLevel = (TextView) findViewById(R.id.id_level);
		mBtnNext = (Button) findViewById(R.id.id_btn_next);

		mGameLayout = (JigsawPuzzleLayout) findViewById(R.id.id_gameView);
		mGameLayout.setTimeEnabled(true);

		mGameLayout.setOnGameListener(new GameListener() {

			@Override
			public void timeChanged(int currentTime) {
				mTime.setText("" + currentTime);
			}

			@Override
			public void nextLevel(final int nextLevel) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Game Info")
						.setMessage("Level Up!!!")
						.setPositiveButton("NEXT",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mGameLayout.nextLevel();
										mLevel.setText("" + nextLevel);
									}
								}).show();
			}

			@Override
			public void gameOver() {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Game Info")
						.setMessage("Game Over!!!")
						.setPositiveButton("RESTART",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mGameLayout.restart();
									}
								})
						.setNegativeButton("QUIT",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).show();
			}
		});

		mBtnNext.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mGameLayout.isGameSuccess()) {
					mGameLayout.nextLevel();
					mLevel.setText("" + mGameLayout.getLevel());
				} else {
					mGameLayout.restart();
				}
				
				// µ˜ ‘”√
				// mGameLayout.cheat();
				// mGameLayout.nextLevel();
				// mLevel.setText("" + mGameLayout.getLevel());
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGameLayout.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGameLayout.resume();
	}
}
