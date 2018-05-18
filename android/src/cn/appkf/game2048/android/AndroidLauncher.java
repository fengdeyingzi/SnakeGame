package cn.appkf.game2048.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.libgdx.snakegame.SnakeGame;
import com.xl.game.tool.Log;

public class AndroidLauncher extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new SnakeGame(), config);
	}
	
	
	public void error(java.lang.String tag, java.lang.String message) 
	{
	Log.e(tag,message);
	}
}
