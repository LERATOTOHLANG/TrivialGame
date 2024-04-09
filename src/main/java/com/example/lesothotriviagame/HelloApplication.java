package com.example.lesothotriviagame;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;

public class HelloApplication extends Application {

    private int score = 0;
    private int questionIndex = 0;
    private TriviaQuestion[] questions = {
            new TriviaQuestion("Who is the founder of Basotho?",
                    new String[]{"Moshoeshoe", "Theisi", "Mohato", "Seeiso"},
                    "Moshoeshoe", "/Lesotho.jpeg"),
            new TriviaQuestion("Where was the gun war started?",
                    new String[]{"Qalabane", "Masite", "Mpharane", "Berea"},
                    "Qalabane", "/Qalabane.jpg"),
            new TriviaQuestion("Where is Qiloane mountain Located?",
                    new String[]{"Maseru", "Berea", "Peka", "Qacha"},
                    "Maseru", "/Qiloane.jpg"),
            new TriviaQuestion("what is the name given to the galaxy of stars in Lesotho?",
                    new String[]{"Tosa", "Mochochonono", "Molala", "Mphatlalatsane"},
                    "Molala", "/Lesotho.jpeg", "/y2mate.com - Lesotho horse training_360p (1).mp4"),
            new TriviaQuestion("Who is the Majesty of Lesotho?",
                    new String[]{"Letsie III", "Bereng", "Seeiso", "Masopha"},
                    "Letsie III", "/Mohato.jpeg"),
    };

    private Label scoreLabel = new Label("Score: 0");
    private Label questionLabel = new Label();
    private VBox optionsBox = new VBox(10);

    private Clip audioClip;

    @Override
    public void start(Stage primaryStage) {
        StackPane root = new StackPane();
        root.getStyleClass().add("root"); // Apply "root" style class
        Scene scene = new Scene(root, 800, 600);

        // Load CSS file
        URL cssUrl = getClass().getResource("/style.css");
        if (cssUrl != null) {
            System.out.println("CSS file URL: " + cssUrl.toString());
            scene.getStylesheets().add(cssUrl.toExternalForm()); // Add CSS to scene
        } else {
            System.err.println("Failed to load CSS file");
        }

        // Apply style class to question label
        questionLabel.getStyleClass().add("question-label");

        // Load the initial question
        loadQuestion(root, scene);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Lesotho Trivia");
        primaryStage.show();

        // Play audio continuously throughout the game
        playAudio("/molisa_molisa.wav");
    }

    private void loadQuestion(StackPane root, Scene scene) {
        if (questionIndex < questions.length) {
            TriviaQuestion currentQuestion = questions[questionIndex];
            questionLabel.setText(currentQuestion.getQuestion());

            // Load the image
            ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(currentQuestion.getImagePath())));
            imageView.setFitWidth(scene.getWidth()); // Adjust image width to fit scene width
            imageView.setFitHeight(scene.getHeight()); // Adjust image height to fit scene height

            optionsBox.getChildren().clear();
            for (String option : currentQuestion.getOptions()) {
                Button optionButton = new Button(option);
                optionButton.getStyleClass().add("option-button");
                optionButton.setOnAction(e -> checkAnswer(optionButton.getText()));
                optionsBox.getChildren().add(optionButton);
            }

            // Set position of optionsBox to top right corner
            StackPane.setAlignment(optionsBox, Pos.TOP_RIGHT);

            // Center question label
            StackPane.setAlignment(questionLabel, Pos.CENTER);

            // Add image and other components to the root
            root.getChildren().clear();
            root.getChildren().addAll(imageView, scoreLabel, questionLabel, optionsBox);

            questionIndex++; // Increment questionIndex here

            // Check if the current question has a video path
            if (currentQuestion.getVideoPath() != null && !currentQuestion.getVideoPath().isEmpty()) {
                // Play video
                playVideo(currentQuestion.getVideoPath(), root);
            }
        } else {
            // Display final score
            Label finalScoreLabel = new Label("Final Score: " + score + "/" + questions.length);
            finalScoreLabel.getStyleClass().add("final-score-label");
            StackPane.setAlignment(finalScoreLabel, Pos.CENTER);
            root.getChildren().clear();
            root.getChildren().add(finalScoreLabel);

            // Stop the audio only if it's playing
            stopAudio();
        }
    }

    private void checkAnswer(String selectedAnswer) {
        TriviaQuestion currentQuestion = questions[questionIndex - 1];
        if (currentQuestion.isCorrect(selectedAnswer)) {
            score++;
            scoreLabel.setText("Score: " + score);
        }
        loadQuestion((StackPane) scoreLabel.getParent(), scoreLabel.getScene());
    }

    private void playAudio(String audioPath) {
        try {
            URL soundUrl = getClass().getResource(audioPath);
            if (soundUrl != null) {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
                audioClip = AudioSystem.getClip();
                audioClip.open(audioInputStream);
                audioClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the audio continuously
                audioClip.start();
            } else {
                System.err.println("Audio file not found: " + audioPath);
            }
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Unsupported audio file format: " + audioPath);
            e.printStackTrace();
        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private void stopAudio() {
        if (audioClip != null && audioClip.isRunning()) {
            audioClip.stop();
            audioClip.close();
        }
    }

    private void playVideo(String videoPath, StackPane root) {
        // Create a Media object
        Media media = new Media(getClass().getResource(videoPath).toExternalForm());

        // Create a MediaPlayer
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        // Create a MediaView
        MediaView mediaView = new MediaView(mediaPlayer);

        // Add MediaView to the root
        root.getChildren().add(mediaView);

        // Set MediaView size to match the scene
        mediaView.fitWidthProperty().bind(root.widthProperty());
        mediaView.fitHeightProperty().bind(root.heightProperty());

        // Play the video
        mediaPlayer.play();

        // Schedule an event to remove the MediaView after 8 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
            root.getChildren().remove(mediaView);
        }));
        timeline.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class TriviaQuestion {
    private String question;
    private String[] options;
    private String correctAnswer;
    private final String imagePath;
    private String videoPath;

    public TriviaQuestion(String question, String[] options, String correctAnswer, String imagePath) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.imagePath = imagePath;
    }

    public TriviaQuestion(String question, String[] options, String correctAnswer, String imagePath, String videoPath) {
        this.question = question;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.imagePath = imagePath;
        this.videoPath = videoPath;
    }

    public String getQuestion() {
        return question;
    }

    public String[] getOptions() {
        return options;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getVideoPath() {
        return videoPath;
    }

    public boolean isCorrect(String selectedAnswer) {
        return selectedAnswer.equals(correctAnswer);
    }
}
