import game.GameFlow;
import game.HighScoresTable;
import game.LevelSets;
import game.Menu;
import game.ShowHiScoresTask;
import game.Task;
import game.animation.AnimationRunner;
import game.animation.HighScoresAnimation;
import game.animation.MenuAnimation;
import game.levels.DirectHit;
import game.levels.FinalFour;
import game.levels.Green3;
import game.levels.LevelInformation;
import game.levels.LevelSpecificationReader;
import game.levels.WideEasy;
import sprite.parts.LivesIndicator;
import sprite.parts.ScoreIndicator;
import biuoop.GUI;
import biuoop.KeyboardSensor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * creates the game.
 */
public class Ass6Game {
    /**
     * calls the game functions.
     *
     * @param args not relevant
     */
    public static void main(String[] args) {

        LevelSpecificationReader r = new LevelSpecificationReader();
        Reader reader = null;
        List from = new ArrayList();
        //list of levels
        List<LevelInformation> list = new ArrayList<LevelInformation>();
        HashMap<String, Task> levelsBySetKey = new HashMap<String, Task>();

        //creates the gui
        GUI gui = new GUI("Arkanoid", 800, 600);
        //creates the levels

        LevelInformation info1 = new DirectHit();
        LevelInformation info2 = new WideEasy();
        LevelInformation info3 = new Green3();
        LevelInformation info4 = new FinalFour();
        //creates all that needed for the game
        KeyboardSensor ks = gui.getKeyboardSensor();
        AnimationRunner ar = new AnimationRunner(gui);
        ScoreIndicator score = new ScoreIndicator();
        LivesIndicator lives = new LivesIndicator();
        HighScoresTable table = new HighScoresTable(5);
        //7 lives for the game
        lives.getCounter().increase(7);
        GameFlow gameLevel = new GameFlow(ks, ar, gui, score, lives, table);
        File file = new File("src/Scores.txt");
        try {
            table.load(file);
        } catch (IOException e) {
            try {
                FileOutputStream out = new FileOutputStream("src/Scores.txt");
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        String path = null;
        LevelSets set = new LevelSets();
        try {
            if (args.length == 0) {
                path = "level_sets.txt";
            }
            if (args.length != 0) {
                path = args[0];
            }
            InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(path);
            if (is != null) {
                reader = new InputStreamReader(is);
                try {
                    set = set.fromReader(reader);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException();
        }
//        try {
//            reader = new FileReader(new File(args[0]));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        try {
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


// ...
        while (true) {
            Menu<Task<Void>> menu = new MenuAnimation<Task<Void>>("Arkanoid", ks, gui);
            Menu<Task<Void>> subMenu = new MenuAnimation<Task<Void>>("Select Level", ks, gui);
            menu.addSubMenu("s", "Start Game", subMenu);
            AnimationRunner runner = new AnimationRunner(gui);


            for (int i = 0; i < set.getLevelSetList().size(); i++) {
                List<LevelInformation> levelList = new ArrayList<LevelInformation>();
                try {
                    InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(
                            set.getLevelSetList().get(i).getLevelDefinitionPath());
                    reader = new InputStreamReader(is);
                    //reader = new FileReader(new File(set.getLevelSetList().get(i).getLevelDefinitionPath()));
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                try {
                    from = r.fromReader(reader);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (int j = 0; j < from.size(); j++) {
                    LevelInformation info = (LevelInformation) from.get(j);
                    levelList.add(info);
                }
                Task levelSet = new Task() {
                    @Override
                    public Object run() {
                        gameLevel.runLevels(levelList);
                        return null;
                    }
                };
                levelsBySetKey.put(set.getLevelSetList().get(i).getKey(), levelSet);
                subMenu.addSelection(set.getLevelSetList().get(i).getKey(),
                        set.getLevelSetList().get(i).getMessage(), levelSet);
            }


            Task quit = new Task() {
                @Override
                public Object run() {
                    System.exit(0);
                    return null;
                }
            };

            Task newGame = new Task() {
                @Override
                public Object run() {
                    ((MenuAnimation<Task<Void>>) menu).setFalse();
                    runner.run(subMenu);
                    return null;
                }
            };


            HighScoresAnimation scores = new HighScoresAnimation(table, ks, "space");
            menu.addSelection("h", "Hi scores", new ShowHiScoresTask(runner, scores));
            menu.addSelection("q", "quit", quit);
            runner.run(menu);
            // wait for user selection
            Task<Void> task = menu.getStatus();
            task.run();
        }

        //gameLevel.runLevels(list);
    }
}