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

import javax.swing.ViewportLayout;

public class LigmaGame extends ApplicationAdapter {
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	private ShapeRenderer shapeRenderer;
	private Circle circulopassaro;
	private Rectangle retanguloCanoCima;
	private  Rectangle retanguloCanoBaixo;

	private float larguraDispositivo;
	private  float alturaDispositivo;
	private  float variacao = 0;
	private  float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private  float posicaoCanoHorizontal;
	private  float posicaocanoVertical;
	private  float espacoEntreCanos;
	private Random random;
	private  int pontos = 0;
	private  int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	@Override
	public void create () {
		inicializarTexturas();
		inicializarObjetos();
	}

	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );

		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}
	


	private  void inicializarTexturas(){
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
	}

	private  void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo/2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.WHITE);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.WHITE);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circulopassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("LigmaGame");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima, 0");

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}
	private void verificarEstadoJogo(){
		boolean toqueTela = Gdx.input.justTouched();
		if (estadoJogo == 0){
			if(toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
			else if( estadoJogo == 1){
				if(toqueTela){
					gravidade = -15;
					somVoando.play();
				}
				posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
				if (posicaoCanoHorizontal < -canoTopo.getWidth()){
					posicaoCanoHorizontal = larguraDispositivo;
					posicaocanoVertical = random.nextInt(400) - 200;
					passouCano = false;
				}
				if (posicaoInicialVerticalPassaro >0 || toqueTela){
					posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
					gravidade++;
				}
				else if (estadoJogo ==2){
					if(pontos > pontuacaoMaxima){
						pontuacaoMaxima = pontos;
						preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
						preferencias.flush();
					}
					posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

					if(toqueTela){
						estadoJogo = 0;
						pontos =0;
						gravidade=0;
						posicaoHorizontalPassaro = 0;
						posicaoInicialVerticalPassaro = alturaDispositivo/2;
						posicaoCanoHorizontal = larguraDispositivo;
					}
				}
			}
		}
	}

	private void detectarColisoes(){
		circulopassaro.set(
				50 + posicaoHorizontalPassaro + passaros[0].getWidth()/2,
				posicaoInicialVerticalPassaro + passaros[0].getHeight()/2,
				passaros[0].getWidth()/2
		);

		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaocanoVertical,
				canoBaixo.getWidth(),canoBaixo.getHeight()
		);
		retanguloCanoCima.set(
				posicaoCanoHorizontal,
				alturaDispositivo/2 + espacoEntreCanos/2 + posicaocanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circulopassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circulopassaro, retanguloCanoBaixo);
	if(colidiuCanoCima || colidiuCanoBaixo){
		if(estadoJogo ==1){
			somColisao.play();
			estadoJogo =2;
		}
	}
	}

	private void desenharTexturas(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[(int) variacao],
		50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo,posicaoCanoHorizontal,
				alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posicaocanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2,
				alturaDispositivo -110);

		if(estadoJogo ==2){
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDispositivo/2);
			textoReiniciar.draw(batch,
					"toque para reiniciar!", larguraDispositivo/2 -140,
					alturaDispositivo/2 - gameOver.getHeight()/2);
			textoMelhorPontuacao.draw(batch,
					"Seu record é: " + pontuacaoMaxima + " pontos.",
					larguraDispositivo/2 -140, alturaDispositivo/2 - gameOver.getHeight());
		}
		batch.end();
	}

	public  void validarPontos(){
		if(posicaoCanoHorizontal < 50-passaros[0].getWidth()){
			if(!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() *10;
		if (variacao > 3)
			variacao = 0;
	}

	@Override
	public void resize(int width, int height){
		viewport.update(width, height);
	}
	public void dispose () {
	}

}
