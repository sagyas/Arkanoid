package game;

import java.awt.Color;

import game.animation.Animation;
import game.animation.AnimationRunner;
import game.animation.CountdownAnimation;
import game.animation.KeyPressStoppableAnimation;
import game.animation.PauseScreen;
import sprite.parts.Ball;
import sprite.parts.LivesIndicator;
import sprite.parts.ScoreIndicator;
import sprite.parts.ScoreTrackingListener;
import sprite.parts.Sprite;
import sprite.parts.SpriteCollection;
import collision.parts.Block;
import collision.parts.Collidable;
import collision.parts.Paddle;
import game.levels.LevelInformation;
import hit.BlockRemover;
import hit.Counter;
import hit.BallRemover;
import shapes.Point;
import shapes.Rectangle;
import biuoop.DrawSurface;
import biuoop.GUI;
import biuoop.KeyboardSensor;

/**
 * create game.
 */
public class GameLevel implements Animation {
    private SpriteCollection sprites = new SpriteCollection();
    private GameEnvironment environment = new GameEnvironment();
    private GUI gui;
    private KeyboardSensor keyboard;
    /**
     * The constant WIDTH.
     */
    public static final int WIDTH = 800;
    /**
     * The constant HEIGHT.
     */
    public static final int HEIGHT = 600;
    private BlockRemover blkRemove = new BlockRemover(this, new Counter());
    private BallRemover ballRemove = new BallRemover(this, new Counter());
    private ScoreIndicator score;
    private ScoreTrackingListener stl;
    private LivesIndicator lives;
    private AnimationRunner runner;
    private boolean running;
    private boolean pause = true;
    private Paddle paddle;
    private LevelInformation levelInfo;
    private boolean stop = false;

    /**
     * The constant SIZE.
     */
    public static final int SIZE = 25;

    /**
     * Instantiates a new Game level.
     *
     * @param info  the info
     * @param ks    the ks
     * @param ar    the ar
     * @param g     the g
     * @param score the score
     * @param life  the life
     */
    public GameLevel(LevelInformation info, KeyboardSensor ks, AnimationRunner ar, GUI g,
                     ScoreIndicator score, LivesIndicator life) {
        this.levelInfo = info;
        this.keyboard = ks;
        this.runner = ar;
        this.gui = g;
        this.score = score;
        this.stl = new ScoreTrackingListener(this.score.getCounter());
        this.lives = life;
    }

    /**
     * adds collidable to game.
     *
     * @param c the object
     */
    public void addCollidable(Collidable c) {
        this.environment.addCollidable((Collidable) c);
    }

    /**
     * adds sprite to game.
     *
     * @param s the object
     */
    public void addSprite(Sprite s) {
        this.sprites.addSprite((Sprite) s);
    }

    /**
     * gets the game environment.
     *
     * @return game environment
     */
    public GameEnvironment getEnvironment() {
        return this.environment;
    }

    /**
     * Initialize a new game: create the Blocks and Ball (and Paddle) and add them
     * to the game.
     */
    public void initialize() {
        //lives.getCounter().increase(2);
        this.addSprite(this.levelInfo.getBackground());
        /*
         * creates the bounds of the game
         */
        Rectangle rec1 = new Rectangle(new Point(0, 20), WIDTH, SIZE);
        Block upper = new Block(rec1);
        Rectangle rec2 = new Rectangle(new Point(0, 45), SIZE, HEIGHT - SIZE);
        Block left = new Block(rec2);
        Rectangle rec3 = new Rectangle(new Point(WIDTH - SIZE, 45), SIZE, HEIGHT - SIZE);
        Block right = new Block(rec3);
        Rectangle rec4 = new Rectangle(new Point(0, 620), 800, 200);
        Block lower = new Block(rec4);
        /*
         * adds the bounds to the game
         */
        upper.addToGame(this);
        left.addToGame(this);
        right.addToGame(this);
        lower.addToGame(this);

        for (int i = 0; i < this.levelInfo.numberOfBlocksToRemove(); i++) {
            Block b = this.levelInfo.blocks().get(i);
            b.addHitListener(this.blkRemove);
            b.addHitListener(this.stl);
            b.addToGame(this);
            this.blkRemove.getCounter().increase(1);
        }
        lives.addToGame(this);
        score.addToGame(this);
        Sprite name = new Sprite() {
            @Override
            public void drawOn(DrawSurface d) {
                d.drawText(550, 18, "Level Name: " + levelInfo.levelName(), 20);
            }

            @Override
            public void timePassed(double dt) {

            }
        };
        this.addSprite(name);
    }

    /**
     * gets sprites collection.
     *
     * @return sprites collection
     */
    public SpriteCollection getSprites() {
        return this.sprites;
    }

    /**
     * starts the game.
     *
     * @param args not important
     */
    public static void main(String[] args) {
    }

    /**
     * Run.
     */
    public void run() {
        if (this.lives.getCounter().getValue() > 0) {
            this.playOneTurn();
        }
    }

    /**
     * Remove collidable.
     *
     * @param c the c
     */
    public void removeCollidable(Collidable c) {
        sprites.getList().remove(c);
    }

    /**
     * Remove sprite.
     *
     * @param s the s
     */
    public void removeSprite(Sprite s) {
        sprites.getList().remove(s);
    }

    /**
     * Gets height.
     *
     * @return the height
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * Gets score.
     *
     * @return the score
     */
    public ScoreIndicator getScore() {
        return this.score;
    }

    @Override
    public void doOneFrame(DrawSurface d, double dt) {
        //draws all
        this.levelInfo.getBackground().drawOn(d);
        this.sprites.drawAllOn(d);
        this.sprites.notifyAllTimePassed(dt);
        //check for pause
        if (this.keyboard.isPressed("p")) {
            this.runner.run(new KeyPressStoppableAnimation(this.gui.getKeyboardSensor(), "space",
                    new PauseScreen(this.keyboard)));
        }
        //check for no blocks
        if (this.blkRemove.getCounter().getValue() == 0) {
            //give score 100
            this.score.getCounter().increase(100);
            this.sprites.drawAllOn(d);
            this.stop = true;
            this.running = false;
            //check for no more balls
        } else if (this.ballRemove.getCounter().getValue() == 0) {
            this.pause = true;
            //decrease lives
            lives.getCounter().decrease(1);
            if (lives.getCounter().getValue() > 0) {
                this.getSprites().getList().remove(this.paddle);
                this.getEnvironment().getList().remove(this.paddle);
                this.playOneTurn();
            } else {
                this.stop = true;
                this.running = false;
            }
        }
    }

    /**
     * Play one turn.
     */
    public void playOneTurn() {
        this.resetPaddle();
        this.createBallsOnTopOfPaddle(); // or a similar method
        //the countdown animation
        if (this.pause) {
            this.runner.run(new CountdownAnimation(2, 3, this.getSprites()));
            this.pause = false;
        }
        this.running = true;
        // use our runner to run the current animation -- which is one turn of
        // the game.
        this.runner.run(this);
    }

    @Override
    public boolean shouldStop() {
        return this.stop;
    }

    /**
     * Create balls on top of paddle.
     */
    public void createBallsOnTopOfPaddle() {

        try {
            for (int i = 0; i < this.levelInfo.numberOfBalls(); i++) {
                //create ball on the paddle
                Ball ball = new Ball(new Point(this.gui.getDrawSurface().getWidth() / 2,
                        this.paddle.getCollisionRectangle().getUpperLeft().getY()),
                        5, Color.WHITE);
                ball.setVelocity(this.levelInfo.initialBallVelocities().get(i));
                //add them to game and gives them listener
                ball.addToGame(this);
                ball.addHitListener(this.ballRemove);
                //count the balls number
                this.ballRemove.getCounter().increase(1);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Reset paddle.
     */
    public void resetPaddle() {
        this.paddle = new Paddle(this.gui, this.levelInfo.paddleSpeed(), this.levelInfo.paddleWidth());
        this.paddle.setGUI(this.gui);
        this.paddle.addToGame(this);
    }

    /**
     * Gets blocks.
     *
     * @return the blocks
     */
    public int getBlocks() {
        return this.blkRemove.getCounter().getValue();
    }

    /**
     * Gets lives.
     *
     * @return the lives
     */
    public int getLives() {
        return this.lives.getCounter().getValue();
    }
}