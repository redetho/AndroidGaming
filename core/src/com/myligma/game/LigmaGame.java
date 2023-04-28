package com.myligma.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Random;

public class LigmaGame extends ApplicationAdapter {

	// declaracao de variaveis
	SpriteBatch batch;

	Texture[] birdArray;


	Texture backTex;
	Texture pipeDownTex;
	Texture startGameTex;
	Texture coinGold;
	Texture coinSilver;


	Texture pipeUpTex;
	Texture gameOverTex;

	ShapeRenderer shapeRenderer;


	Circle birdCol;
	Circle coinGoldCollider;
	Circle coinSilverCollider;
	Rectangle rectanglePipeUpCol;
	Rectangle rectanglePipeDownCol;

	float devWidth;
	float devHeight;
	float variation = 0;
	float gravity = 2;
	float birdStartingVerticalPosition;
	float posPipeHorizontal;
	float posPipeVertical;
	float spaceBetweenPipes;

	float posCoinHorizontal;
	float posCoinVertical;

	float posCoinHorizontalSilver;
	float posCoinVerticalSilver;

	Random random;
	int points = 0;
	int maxScore = 0;
	boolean pipePassed = false;
	int gameState = 0;
	float posBirdHorizontal;
	BitmapFont scoreTex;
	BitmapFont restartTex;
	BitmapFont bestScoreTex;
	Sound flyingSound;
	Sound collisionSound;
	Sound scoreSound;

	Preferences preferences;

	OrthographicCamera camera;
	Viewport viewport;
	final float VIRTUAL_WIDTH = 720;
	final float VIRTUAL_HEIGHT = 1280;

	@Override
	// inicializa as texturas e objetos
	public void create () {
		startTextures();
		startObjects();
	}
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verifyGameState();
		validatePoints();
		drawTextures();
		detectCollisions();
	}
	//inicializa as texturas
	private void startTextures(){
		birdArray = new Texture[3];
		birdArray[0] = new Texture("passaro1.png");
		birdArray[1] = new Texture("passaro2.png");
		birdArray[2] = new Texture("passaro3.png");
		startGameTex = new Texture("bird.png");

		backTex = new Texture("fundo.png");
		pipeDownTex = new Texture("cano_baixo_maior.png");
		pipeUpTex = new Texture("cano_topo_maior.png");
		gameOverTex = new Texture("game_over.png");
		coinGold = new Texture("goldCoin.png");
		coinSilver = new Texture("silverCoin.png");
	}

	private void startObjects(){
		//inicializa os objetos
		batch = new SpriteBatch();
		random = new Random();

		devWidth = VIRTUAL_WIDTH;
		devHeight = VIRTUAL_HEIGHT;
		birdStartingVerticalPosition = devHeight/2;
		posPipeHorizontal = devWidth;
		spaceBetweenPipes = 350;

		scoreTex = new BitmapFont();
		scoreTex.setColor(Color.WHITE);
		scoreTex.getData().setScale(10);

		restartTex = new BitmapFont();
		restartTex.setColor(Color.GREEN);
		restartTex.getData().setScale(2);

		bestScoreTex = new BitmapFont();
		bestScoreTex.setColor(Color.RED);
		bestScoreTex.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		birdCol = new Circle();
		rectanglePipeDownCol = new Rectangle();
		rectanglePipeUpCol = new Rectangle();

		posCoinVertical = devHeight/2;
		posCoinHorizontal = devWidth;

		posCoinVerticalSilver = devHeight/2;
		posCoinHorizontalSilver = devWidth;

		coinSilverCollider = new Circle();
		coinGoldCollider = new Circle();

		flyingSound = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		collisionSound = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		scoreSound = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferences = Gdx.app.getPreferences("flappyBird");
		maxScore = preferences.getInteger("maxScore",0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}
	// checa estados do jogo
	private void verifyGameState(){
		boolean touchScreen = Gdx.input.justTouched();
		if( gameState == 0){
			if( touchScreen){
				gravity = -15;
				gameState = 1;
				flyingSound.play();
			}
		}else if (gameState == 1){
			if(touchScreen){
				gravity = -15;
				flyingSound.play();
			}
			//faz os canos se moverem
			posPipeHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			posCoinHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			posCoinHorizontalSilver -= Gdx.graphics.getDeltaTime() * 200;
			if( posPipeHorizontal < -pipeUpTex.getWidth()){
				posPipeHorizontal = devWidth;
				posPipeVertical = random.nextInt(400) - 200;
				pipePassed = false;
			}
			if( posCoinHorizontal < -coinGold.getWidth()){
				posCoinHorizontal = devWidth + random.nextInt(Math.round((devWidth * .5f)));
				posCoinVertical = random.nextInt(Math.round(devHeight)) - 200;}
			if(posCoinHorizontalSilver <-coinSilver.getWidth()){
				posCoinHorizontalSilver = devWidth + random.nextInt(Math.round((devWidth * .5f)));
				posCoinVerticalSilver = random.nextInt(Math.round(devHeight));
			}
			if( birdStartingVerticalPosition > 0 || touchScreen)
				birdStartingVerticalPosition = birdStartingVerticalPosition - gravity;
			gravity++;
		}else if( gameState == 2){
			if (points> maxScore){
				maxScore = points;
				preferences.putInteger("maxScore", maxScore);
				preferences.flush();
			}
			posBirdHorizontal -= Gdx.graphics.getDeltaTime()*500;;
			// reinicia o jogo
			if(touchScreen){
				gameState = 0;
				points = 0;
				gravity = 0;
				posBirdHorizontal = 0;
				birdStartingVerticalPosition = devHeight/2;
				posPipeHorizontal = devWidth;
			}
		}
	}

	private void detectCollisions(){
		//detecta colisoes, do passaro e canos
		birdCol.set(
				50 + posBirdHorizontal + birdArray[0].getWidth()/2,
				birdStartingVerticalPosition + birdArray[0].getHeight()/2,
				birdArray[0].getWidth()/2
		);
		coinGoldCollider.set(
				50 + posCoinHorizontal + coinGold.getWidth()/2,
				posCoinVertical + coinGold.getHeight()/2,
				coinGold.getWidth()/2
		);
		coinSilverCollider.set(
				50 + posCoinHorizontalSilver + coinSilver.getWidth()/2,
				posCoinVerticalSilver + coinSilver.getHeight()/2,
				coinSilver.getWidth()/2

		);
		rectanglePipeDownCol.set(
				posPipeHorizontal,
				devHeight/2 - pipeDownTex.getHeight() - spaceBetweenPipes / 2 + posPipeVertical,
				pipeDownTex.getWidth(), pipeDownTex.getHeight()
		);
		rectanglePipeUpCol.set(
				posPipeHorizontal, devHeight / 2 + spaceBetweenPipes / 2 + posPipeVertical,
				pipeUpTex.getWidth(), pipeUpTex.getHeight()
		);

		boolean collidedPipeTop = Intersector.overlaps(birdCol, rectanglePipeUpCol);
		boolean collidedPipeDown = Intersector.overlaps(birdCol, rectanglePipeDownCol);

		if (collidedPipeTop || collidedPipeDown){
			if (gameState == 1){
				collisionSound.play();
				gameState = 2;
			}
		}
		boolean collideCoinGold = Intersector.overlaps(birdCol, coinGoldCollider);
		boolean collideCoin = Intersector.overlaps(birdCol, coinSilverCollider);
		if(collideCoinGold){
			posCoinHorizontal = -devHeight;
			points = points + 10;
			scoreSound.play();
		}else  if (collideCoin){
			posCoinHorizontalSilver = -devHeight;
			points = points + 5;
			scoreSound.play();
		}

	}
	//cria as texturas no jogo, na tela inteira
	private void drawTextures(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(backTex,0,0,devWidth, devHeight);
		batch.draw(birdArray[(int) variation],
				50 + posBirdHorizontal, birdStartingVerticalPosition);
		batch.draw(pipeDownTex, posPipeHorizontal,
				devHeight/2 - pipeDownTex.getHeight() - spaceBetweenPipes/2 + posPipeVertical);
		batch.draw(pipeUpTex, posPipeHorizontal,
				devHeight/2 + spaceBetweenPipes/2 + posPipeVertical);
		scoreTex.draw(batch, String.valueOf(points), devWidth/2,
				devHeight - 110);
		batch.draw(coinGold, 50 + posCoinHorizontal, posCoinVertical);
		batch.draw(coinSilver, 50 + posCoinHorizontalSilver, posCoinVerticalSilver);

		if(gameState == 0) {
			batch.draw(startGameTex, devWidth/2 - startGameTex.getWidth()/2,
					devHeight/3);
		}

		if(gameState == 2){
			batch.draw(gameOverTex, devWidth/2 - gameOverTex.getWidth()/2,
					devHeight/2);
			restartTex.draw(batch,
					"Toque para reiniciar!", devWidth/2 - 140,
					devHeight/2 - gameOverTex.getHeight()/2);
			bestScoreTex.draw(batch,
					"Seu recorde é: "+ maxScore + " pontos",
					devWidth/2-140, devHeight/2 - gameOverTex.getHeight());
		}
		batch.end();
	}
	//Confere se o passaro passou entre os canos
	public void validatePoints(){
		if( posPipeHorizontal < 50-birdArray[0].getWidth()){
			if (!pipePassed){
				points++;
				pipePassed = true;
				scoreSound.play();
			}
		}

		variation += Gdx.graphics.getDeltaTime() * 10;

		if (variation > 3)
			variation = 0;
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void dispose () {
	}
}