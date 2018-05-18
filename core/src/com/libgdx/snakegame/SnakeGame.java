package com.libgdx.snakegame;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
//import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Vector2;

public class SnakeGame extends ApplicationAdapter {
  private String TAG ="SnakeGame";
	private ShapeRenderer shapeRenderer;
	private SpriteBatch batch;
	private Texture snakeHead;
	private Texture apple;
	private Texture snakeBody;
	private static final float MOVE_TIME = 0.5f; // 贪吃蛇每秒钟自己移动一个距离
	private float timer = MOVE_TIME;
	private static final int SNAKE_MOVEMENT = 32; // 贪吃蛇本身大小为32*32，每次移动一格
	private int snakeX = 0, snakeY = 0;
	private boolean appleAvailable = false;
	private boolean directionSet = false;
	private int appleX, appleY;
	private static final int GRID_CELL = 32;

	private Array<BodyPart> bodyParts = new Array<BodyPart>();
	private STATE state = STATE.PLAYING;
	private static final String GAME_OVER_TEXT = "Game Over... Tap space to restart!";
	private Label layout;
	
	private BitmapFont bitmapFont;
	private FitViewport viewport;
	
	private static final int WORLD_WIDTH = 640;
	private static final int WORLD_HEIGHT = 480;
  private int map_w,map_h;
	
	
	
	public class MyGestureListener implements GestureDetector. GestureListener{
		@Override
		public boolean touchDown(float x, float y, int pointer, int button) {
		return false;
		}
		@Override
		public boolean tap(float x, float y, int count, int button) {
		return false;
		}
		@Override
		public boolean longPress(float x, float y) {
		return false;
		}
		@Override
		public boolean fling(float velocityX, float velocityY, int button) {
		if(velocityX>0 && velocityX >velocityY)
			updateDirection(RIGHT);
		if(velocityX<0 && velocityX<velocityY)
			updateDirection(LEFT);
		if(velocityY<0 && velocityY<velocityX)
			updateDirection(UP);
		if(velocityY>0 && velocityY>velocityX)
			updateDirection(DOWN);
			return false;
		}
		@Override
		public boolean pan(float x, float y, float deltaX, float deltaY) {
		return false;
		}
		@Override
		public boolean panStop(float x, float y, int pointer, int button) {
		return false;
		}
		@Override
		public boolean zoom (float originalDistance, float currentDistance){
		return false;
		}
		@Override
		public boolean pinch (Vector2 initialFirstPointer, Vector2 initialSecondPointer, Vector2 firstPointer, Vector2 secondPointer){
		return false;
		}
	}
	
	@Override
	public void create() {
		Gdx.app.setLogLevel(Gdx.app.LOG_DEBUG);
		map_w = WORLD_WIDTH/SNAKE_MOVEMENT;
		map_h = WORLD_HEIGHT/SNAKE_MOVEMENT;
		batch = new SpriteBatch();
		Gdx.app.error(TAG,"1111");
		snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
		apple = new Texture(Gdx.files.internal("apple.png"));
		snakeBody = new Texture(Gdx.files.internal("snakebody.png"));
	Gdx.app.error(TAG,"2222");
		shapeRenderer = new ShapeRenderer();
		bitmapFont = new BitmapFont();
		Label.LabelStyle style = new Label.LabelStyle();
		style.font = bitmapFont;
		style.fontColor=Color.BLUE;
		
		
	Gdx.app.error(TAG,"3333");
		layout = new Label("aa",style);
	;
	Gdx.input.setInputProcessor(new GestureDetector(new MyGestureListener()));

	
	
	Gdx.app.error(TAG,"4556");
		viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
		viewport.apply(true);
	Gdx.app.error(TAG,"6666666");
	
	}

	private void checkForRestart() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) doRestart();
		if(Gdx.input.isKeyJustPressed(Input.Keys.MENU)) doRestart();
		if(Gdx.input.isTouched())
		{
			
			doRestart();
		}
	}
	
	private void doRestart() {
		state = STATE.PLAYING;
		bodyParts.clear();
		snakeDirection = RIGHT;
		directionSet = false;
		timer = MOVE_TIME;
		snakeX = 0;
		snakeY = 0;
		snakeXBeforeUpdate = 0;
		snakeYBeforeUpdate = 0;
		appleAvailable = false;
	}
	
	@Override
	public void render() {

		clearScreen();
		
		switch(state) {
		case PLAYING: {
			queryInput();
			updateSnake(Gdx.graphics.getDeltaTime());
			checkAppleCollision();
			checkAndPlaceApple();
			drawGrid();
		}
		break;
		case GAME_OVER: {
			checkForRestart();
		}
		break;
		}

		

		draw();
	}
	
	private void updateSnake(float delta) {
		timer -= delta;
		if (timer <= 0) {
			timer = MOVE_TIME;
			moveSnake();
			checkForOutOfBounds();
			updateBodyPartsPosition();
			checkSnakeBodyCollision();
			checkWallCollision();
			directionSet = false;
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose() {
		batch.dispose();
		snakeHead.dispose();
		apple.dispose();
		snakeBody.dispose();
		shapeRenderer.dispose();
		bitmapFont.dispose();
	}

	/**
	 * 检测苹果是否与贪吃蛇发生碰撞，位置重合，即发生碰撞 因为每次移动都是32个像素
	 */
	private void checkAppleCollision() {

		if (appleAvailable && appleX == snakeX && appleY == snakeY) {
			// 当贪吃蛇吃了苹果之后，添加贪吃蛇的身体，虽然是insert到Body数组的第一个
			// 但是通过updateBodyPartsPosition方法后成为最后一个
			BodyPart bodyPart = new BodyPart(snakeBody);
			bodyPart.updateBodyPosition(snakeX, snakeY);
			bodyParts.insert(0, bodyPart);
			appleAvailable = false;
		}
	}

	private void draw() {
		batch.setProjectionMatrix(viewport.getCamera().combined);
		batch.begin();
		batch.draw(snakeHead, snakeX, snakeY);
		for (BodyPart bodyPart : bodyParts) {
			bodyPart.draw(batch);
		}
		if (appleAvailable) {
			batch.draw(apple, appleX, appleY);
		}
		if (state == STATE.GAME_OVER) {
			layout.setText( GAME_OVER_TEXT);
			layout.setSize(layout.getPrefWidth(),layout.getPrefHeight());
			bitmapFont.draw(batch, GAME_OVER_TEXT, (viewport.getWorldWidth() - layout.getWidth()) / 2, 
					(viewport.getWorldHeight() - layout.getHeight()) / 2);
		}
		batch.end();
	}

	private void drawGrid() {
		// ShaperRender设置映射矩阵
		shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		for (int x = 0; x < viewport.getWorldWidth(); x += GRID_CELL) {
			for (int y = 0; y < viewport.getWorldHeight(); y += GRID_CELL) {
				// 画出一个正方形
				shapeRenderer.rect(x, y, GRID_CELL, GRID_CELL);
			}
		}
		shapeRenderer.end();
	}
	
	private void clearScreen() {
		Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, Color.BLACK.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	/**
	 * 判断贪吃蛇的投是否和身体接触，接触则游戏结束
	 */
	private void checkSnakeBodyCollision() {
		for (BodyPart bodyPart : bodyParts) {
			if (bodyPart.x == snakeX && bodyPart.y == snakeY)
				state = STATE.GAME_OVER;
		}
	}
	
	//判断贪吃蛇是否撞墙
	private void checkWallCollision()
	{
		if(snakeX<0 || snakeX>=map_w*SNAKE_MOVEMENT || snakeY<0 || snakeY>=map_h*SNAKE_MOVEMENT)
		{
			state = STATE.GAME_OVER;
		}
	}

	private void checkAndPlaceApple() {
		// 其实苹果只有一个，通过appleAvailable控制苹果的位置，并改变位置
		if (!appleAvailable) {
			do {
				appleX = MathUtils.random((int)viewport.getWorldWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
				appleY = MathUtils.random((int)viewport.getWorldHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
				appleAvailable = true;
			} while (appleX == snakeX && appleY == snakeY); // 如果苹果的位置和贪吃蛇的位置重复，那么需要重新生成一个苹果
		}
	}

	/**
	 * 检查贪吃蛇所在位置的边界
	 */
	private void checkForOutOfBounds() {
		if (snakeX >= Gdx.graphics.getWidth()) { // 如果贪吃蛇在最右边，那么移动到最左边
			snakeX = 0;
		}
		if (snakeX < 0) {
			snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
		}
		if (snakeY >= Gdx.graphics.getHeight()) {
			snakeY = 0;
		}
		if (snakeY < 0) {
			snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
		}
	}

	private static final int RIGHT = 0;
	private static final int LEFT = 1;
	private static final int UP = 2;
	private static final int DOWN = 3;
	private int snakeDirection = RIGHT;

	private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

	private void moveSnake() {
		// 身体始终移动到贪吃蛇当前的位置
		snakeXBeforeUpdate = snakeX;
		snakeYBeforeUpdate = snakeY;

		switch (snakeDirection) {
		case RIGHT: {
			snakeX += SNAKE_MOVEMENT;
			return;
		}
		case LEFT: {
			snakeX -= SNAKE_MOVEMENT;
			return;
		}
		case UP: {
			snakeY += SNAKE_MOVEMENT;
			return;
		}
		case DOWN: {
			snakeY -= SNAKE_MOVEMENT;
			return;
		}
		}
	}

	private void updateBodyPartsPosition() {
		if (bodyParts.size > 0) {
			// 每次只需移动一块身体
			BodyPart bodyPart = bodyParts.removeIndex(0);
			bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
			bodyParts.add(bodyPart);
		}
	}

	private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection) {
		// 当贪吃蛇没有身体可以回头，其余时候只有方向不同才能转向
		if (snakeDirection != oppositeDirection || bodyParts.size == 0)
			snakeDirection = newSnakeDirection;
	}

	private void updateDirection(int newSnakeDirection) {
		if (!directionSet && snakeDirection != newSnakeDirection) {
			directionSet = true;
			switch (newSnakeDirection) {
			case LEFT: {
				updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
			}
				break;
			case RIGHT: {
				updateIfNotOppositeDirection(newSnakeDirection, LEFT);
			}
				break;
			case UP: {
				updateIfNotOppositeDirection(newSnakeDirection, DOWN);
			}
				break;
			case DOWN: {
				updateIfNotOppositeDirection(newSnakeDirection, UP);
			}
				break;
			}
		}
	}

	private void queryInput() {

		boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
		boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
		boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
		boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);

		if (lPressed) updateDirection(LEFT);
		if (rPressed) updateDirection(RIGHT);
		if (uPressed) updateDirection(UP);
		if (dPressed) updateDirection(DOWN);

		int x=0,y=0;
		int keywidth=Gdx.graphics.getWidth()/3;
		int keyheight = Gdx.graphics.getHeight()/3;
		/*
		if(Gdx.input.isTouched(0))
		{
			x = Gdx.input.getX(0);
			y = Gdx.input.getY(0);
			//上
			if(x>keywidth && x<keywidth*2 && y>=0 && y<keyheight)
			{
				updateDirection(UP);
			}
			//下
			if(x>keywidth && x<keywidth*2 && y>keyheight*2 && y<keyheight*3)
			{
				updateDirection(DOWN);
			}
		//左
		if(x>=0 && x<keywidth && y>keyheight && y<keyheight*2)
		{
updateDirection(LEFT);
		}
			//右
			if(x>keywidth*2 && x<keywidth*3 && y>keyheight && y<keyheight*2){
				updateDirection(RIGHT);
			}
		}
		*/
		
	}

	private enum STATE {
		PLAYING, GAME_OVER
	}
	
	public class BodyPart {
		private int x, y;
		private Texture texture;

		public BodyPart(Texture texture) {
			this.texture = texture;
		}

		public void updateBodyPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public void draw(Batch batch) {
			// 当贪吃蛇移动开头之后，才绘制身体
			if (!(x == snakeX && y == snakeY)) {
				batch.draw(texture, x, y);
			}
		}
	}
}
