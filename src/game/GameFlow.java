package game;

import biuoop.DialogManager;
import game.animation.AnimationRunner;
import game.animation.End;
import game.animation.HighScoresAnimation;
import game.animation.KeyPressStoppableAnimation;
import game.levels.LevelInformation;
import sprite.parts.LivesIndicator;
import sprite.parts.ScoreIndicator;
import biuoop.GUI;
import biuoop.KeyboardSensor;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The type Game flow.
 */
public class GameFlow {
    private GUI gui;
    private KeyboardSensor keyboard;
    private ScoreIndicator score;
    private LivesIndicator lives;
    private AnimationRunner runner;
    private HighScoresTable table;

    /**
     * Instantiates a new Game flow.
     *
     * @param ks     the keyboard
     * @param ar     the animation runner
     * @param g      the gui
     * @param score  the score
     * @param life   the life
     * @param scores the scores
     */
    public GameFlow(KeyboardSensor ks, AnimationRunner ar, GUI g,
                    ScoreIndicator score, LivesIndicator life, HighScoresTable scores) {
        this.keyboard = ks;
        this.runner = ar;
        this.gui = g;
        this.score = score;
        this.lives = life;
        this.table = scores;
    }

    /**
     * Run levels.
     *
     * @param levels the levels
     */
    public void runLevels(List<LevelInformation> levels) {
        File file = new File("src/Scores.txt");
        try {
            this.table.load(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //to know what to print
        boolean win = true;
        for (LevelInformation levelInfo : levels) {

            GameLevel level = new GameLevel(levelInfo,
                    this.keyboard,
                    this.runner, this.gui, this.score, this.lives);
//initialize rhe game
            level.initialize();
//plays turn when needed
            while (level.getBlocks() > 0 && level.getLives() > 0) {
                level.playOneTurn();
            }
//if no lives you lose
            if (level.getLives() == 0) {
                win = false;
                break;
            }

        }

        //run end screen
        runner.run(new KeyPressStoppableAnimation(this.gui.getKeyboardSensor(), "space", new End(keyboard,
                score.getCounter().getValue(), win)));

        if (this.table.getRank(this.score.getCounter().getValue()) == 1
                || this.table.getRank(this.score.getCounter().getValue()) == this.table.size()
                || this.table.getRank(this.score.getCounter().getValue()) == 0) {
            DialogManager dialog = this.gui.getDialogManager();
            String name = dialog.showQuestionDialog("Name", "What is your name?", "");
            System.out.println(name);
            this.table.add(new ScoreInfo(name, this.score.getCounter().getValue()));

            try {
                table.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        runner.run(new KeyPressStoppableAnimation(this.gui.getKeyboardSensor(), "space",
                new HighScoresAnimation(this.table, this.gui.getKeyboardSensor(), "space")));
        this.score.getCounter().decrease(this.score.getCounter().getValue());
        //close screen
        //this.gui.close();
    }
}