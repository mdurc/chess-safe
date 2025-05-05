package chess.model.util;

import chess.model.Move;
import chess.model.Move.MoveType;
import javax.sound.sampled.*;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {
    public enum SoundType {
        MOVE_SELF("resources/sounds/move-self.wav"),
        MOVE_CHECK("resources/sounds/move-check.wav"),
        CAPTURE("resources/sounds/capture.wav"),
        CASTLE("resources/sounds/castle.wav"),
        ILLEGAL("resources/sounds/illegal.wav"),
        PROMOTION("resources/sounds/promote.wav"),
        APP_LOAD("resources/sounds/app-load.wav");

        private final String resourcePath;

        SoundType(String path) {
            this.resourcePath = path;
        }
    }

    private static final Map<SoundType, Clip> CLIPS = new EnumMap<>(SoundType.class);
    private static final ExecutorService SOUND_POOL = Executors.newFixedThreadPool(2);

    static {
        initializeSounds();
    }

    private static void initializeSounds() {
        Arrays.stream(SoundType.values()).forEach(soundType -> {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(new File(soundType.resourcePath))) {
                AudioFormat format = ais.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                Clip clip = (Clip) AudioSystem.getLine(info);
                clip.open(ais);
                CLIPS.put(soundType, clip);
            } catch (Exception e) {
                System.err.println("Error loading sound: " + soundType + " - " + e.getMessage());
            }
        });
    }

    public static void playSound(SoundType type) {
        SOUND_POOL.execute(() -> {
            Clip clip = CLIPS.get(type);
            if (clip != null) {
                try {
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.setFramePosition(0);
                    clip.start();
                } catch (Exception e) {
                    System.err.println("Error playing sound: " + e.getMessage());
                }
            }
        });
    }

    public static void playSoundForMove(Move move) {
        if (move == null) {
            playSound(SoundType.ILLEGAL);
            return;
        }

        List<MoveType> types = move.getTypes();
        if (types.isEmpty()) return;
        if (types.contains(MoveType.CHECKMATE) || types.contains(MoveType.CHECK)) {
            playSound(SoundType.MOVE_CHECK);
        } else if (types.contains(MoveType.CAPTURE)) {
            playSound(SoundType.CAPTURE);
        } else if (types.contains(MoveType.CASTLE_LONG) || types.contains(MoveType.CASTLE_SHORT)) {
            playSound(SoundType.CASTLE);
        } else if (types.contains(MoveType.PROMOTION)) {
            playSound(SoundType.PROMOTION);
        } else {
            playSound(SoundType.MOVE_SELF);
        }
    }

    public static void shutdown() {
        SOUND_POOL.shutdown();
        CLIPS.values().forEach(Clip::close);
    }
}
